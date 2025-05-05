package com.example.nutcracker_streaming_app.utils

import android.view.Surface
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.camera.viewfinder.compose.Viewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.camera.viewfinder.core.TransformationInfo
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import androidx.camera.core.SurfaceRequest.TransformationInfo as CXTransformationInfo

@Composable
fun OverridedCameraXViewFinder(
    surfaceRequest: SurfaceRequest,
    updateSurface: (Surface) -> Unit,
    modifier: Modifier = Modifier,
    implementationMode: ImplementationMode = ImplementationMode.EXTERNAL,
    coordinateTransformer: MutableCoordinateTransformer? = null
) {
    val currentImplementationMode by rememberUpdatedState(implementationMode)

    val viewfinderArgs by
    produceState<ViewfinderArgs?>(initialValue = null, surfaceRequest) {
        // Convert the CameraX SurfaceRequest to ViewfinderSurfaceRequest. There should
        // always be a 1:1 mapping of CameraX SurfaceRequest to ViewfinderSurfaceRequest.
        val viewfinderSurfaceRequest =
            ViewfinderSurfaceRequest.Builder(surfaceRequest.resolution).build()

        // Launch undispatched so we always reach the try/finally in this coroutine
        launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                // Forward request cancellation to the ViewfinderSurfaceRequest by marking it
                // safe to release and cancelling this produceScope in case we haven't yet
                // produced a complete ViewfinderArgs.
                surfaceRequest.addRequestCancellationListener(Runnable::run) {
                    // This SurfaceRequest doesn't need to be completed, so let the
                    // Viewfinder know in case it has already generated a Surface.
                    viewfinderSurfaceRequest.markSurfaceSafeToRelease()
                    // Also complete the ViewfinderSurfaceRequest from the producer side
                    // in case we never sent it to the Viewfinder.
                    viewfinderSurfaceRequest.willNotProvideSurface()
                    this@produceState.cancel()
                }

                // Suspend until we retrieve the Surface
                val surface = viewfinderSurfaceRequest.getSurface()
                updateSurface(surface)
                // Provide the surface and mark safe to release once the
                // frame producer is finished.
                surfaceRequest.provideSurface(surface, Runnable::run) {
                    viewfinderSurfaceRequest.markSurfaceSafeToRelease()
                }
            } finally {
                // If we haven't provided the surface, such as if we're cancelled
                // while suspending on getSurface(), this call will succeed. Otherwise
                // it will be a no-op.
                surfaceRequest.willNotProvideSurface()
            }
        }

        // Convert the CameraX TransformationInfo callback into a StateFlow
        val transformationInfoFlow: StateFlow<CXTransformationInfo?> =
            MutableStateFlow<CXTransformationInfo?>(null)
                .also { stateFlow ->
                    // Set a callback to update this state flow
                    surfaceRequest.setTransformationInfoListener(Runnable::run) { transformInfo
                        ->
                        // Set the next value of the flow
                        stateFlow.value = transformInfo
                    }
                }
                .asStateFlow()

        // The ImplementationMode that will be used for all TransformationInfo updates.
        // This is locked in once we have updated ViewfinderArgs and won't change until
        // this produceState block is cancelled and restarted.
        var snapshotImplementationMode: ImplementationMode? = null
        snapshotFlow { currentImplementationMode }
            .combine(transformationInfoFlow.filterNotNull()) { implMode, transformInfo ->
                Pair(implMode, transformInfo)
            }
            .takeWhile { (implMode, _) ->
                val shouldAbort =
                    snapshotImplementationMode != null && implMode != snapshotImplementationMode
                if (shouldAbort) {
                    // Abort flow and invalidate SurfaceRequest so a new SurfaceRequest will
                    // be sent.
                    surfaceRequest.invalidate()
                } else {
                    // Got the first ImplementationMode. This will be used until this
                    // produceState is cancelled.
                    snapshotImplementationMode = implMode
                }
                !shouldAbort
            }
            .collect { (implMode, transformInfo) ->
                value =
                    ViewfinderArgs(
                        viewfinderSurfaceRequest,
                        implMode,
                        TransformationInfo(
                            sourceRotation = transformInfo.rotationDegrees,
                            isSourceMirroredHorizontally = transformInfo.isMirroring,
                            isSourceMirroredVertically = false,
                            cropRectLeft = transformInfo.cropRect.left,
                            cropRectTop = transformInfo.cropRect.top,
                            cropRectRight = transformInfo.cropRect.right,
                            cropRectBottom = transformInfo.cropRect.bottom
                        )
                    )
            }
    }

    viewfinderArgs?.let { args ->
        Viewfinder(
            surfaceRequest = args.viewfinderSurfaceRequest,
            implementationMode = args.implementationMode,
            transformationInfo = args.transformationInfo,
            modifier = modifier.fillMaxSize(),
            coordinateTransformer = coordinateTransformer
        )
    }
}

@Immutable
private data class ViewfinderArgs(
    val viewfinderSurfaceRequest: ViewfinderSurfaceRequest,
    val implementationMode: ImplementationMode,
    val transformationInfo: TransformationInfo
)