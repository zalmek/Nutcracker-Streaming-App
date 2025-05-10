package com.example.nutcracker_streaming_app.utils

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.dp


@Composable
inline fun <reified StreamingService : Service, reified StreamingServiceBinder : Binder> rememberStreamingService(
    crossinline getService: @DisallowComposableCalls StreamingServiceBinder.() -> StreamingService,
): StreamingService? {
    val context: Context = LocalContext.current
    var boundService: StreamingService? by remember(context) { mutableStateOf(null) }
    val serviceConnection: ServiceConnection = remember(context) {
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                boundService = (service as StreamingServiceBinder).getService()
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                boundService = null
            }
        }
    }
    DisposableEffect(context, serviceConnection) {
        context.bindService(Intent(context, StreamingService::class.java), serviceConnection, Context.BIND_IMPORTANT)
        onDispose { context.unbindService(serviceConnection) }
    }
    return boundService
}

public val videoCamVector: ImageVector
    get() {
        if (_undefined != null) {
            return _undefined!!
        }
        _undefined = ImageVector.Builder(
            name = "VideoCamera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF0F172A)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15.75f, 10.5f)
                lineTo(20.4697f, 5.78033f)
                curveTo(20.9421f, 5.3079f, 21.75f, 5.6425f, 21.75f, 6.3107f)
                verticalLineTo(17.6893f)
                curveTo(21.75f, 18.3575f, 20.9421f, 18.6921f, 20.4697f, 18.2197f)
                lineTo(15.75f, 13.5f)
                moveTo(4.5f, 18.75f)
                horizontalLineTo(13.5f)
                curveTo(14.7426f, 18.75f, 15.75f, 17.7426f, 15.75f, 16.5f)
                verticalLineTo(7.5f)
                curveTo(15.75f, 6.2574f, 14.7426f, 5.25f, 13.5f, 5.25f)
                horizontalLineTo(4.5f)
                curveTo(3.2574f, 5.25f, 2.25f, 6.2574f, 2.25f, 7.5f)
                verticalLineTo(16.5f)
                curveTo(2.25f, 17.7426f, 3.2574f, 18.75f, 4.5f, 18.75f)
                close()
            }
        }.build()
        return _undefined!!
    }

private var _undefined: ImageVector? = null


sealed class AspectRatio {
    data object AspectRatio16to9: AspectRatio()
}

@Composable
internal fun calculateHeightOfUnusedArea(aspectRatio: AspectRatio): Dp {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density

    val screenHeight = containerSize.height.dp / density
    val screenWidth = containerSize.width.dp / density

    when (aspectRatio) {
        AspectRatio.AspectRatio16to9 -> {
            return (max(screenWidth,screenHeight) - min(screenWidth,screenHeight)*16/9)
        }
    }
}