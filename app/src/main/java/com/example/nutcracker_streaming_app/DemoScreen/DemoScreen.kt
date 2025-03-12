package com.example.nutcracker_streaming_app.DemoScreen

import android.util.Log
import android.util.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nutcracker_streaming_app.utils.Protocol
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import io.github.thibaultbee.streampack.ext.srt.streamers.CameraSrtLiveStreamer
import io.github.thibaultbee.streampack.views.PreviewView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@Composable
fun DemoScreen(
    state: DemoContact.State.Main,
    effectFlow: Flow<DemoContact.Effect>?,
    setEvent: (event: DemoContact.Event) -> Unit,
) {
    val context = LocalContext.current
    Log.d("ASJHDAKSDJ", "DemoScreen: ${state.protocol}")
    var srtChecked by remember {
        mutableStateOf(state.protocol == Protocol.Srt)
    }
    var startClicked by remember {
        mutableStateOf(false)
    }
    var link by remember {
        mutableStateOf(state.link)
    }
    val streamer =
        if (state.protocol == Protocol.Srt) CameraSrtLiveStreamer(context) else CameraRtmpLiveStreamer(
            context
        )
    val coroutineScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(16.dp),
            singleLine = true,
//            placeholder = Text(text = "srt://"),
            value = link,
            onValueChange = {
                link = it
                setEvent(DemoContact.Event.InputLink(it))
            })
        Row(
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "SRT"
            )
            Switch(
                checked = srtChecked,
                onCheckedChange = {
                    srtChecked = it
                    setEvent(DemoContact.Event.SelectProtocol(if (it) Protocol.Srt else Protocol.Rtmp))
                })
        }
        Row(
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "RTMP"
            )
            Switch(
                checked = !srtChecked,
                onCheckedChange = {
                    srtChecked = !it
                    setEvent(DemoContact.Event.SelectProtocol(if (it) Protocol.Rtmp else Protocol.Srt))
                })
        }

        Button(onClick = { startClicked = true }) {
            Text(text = "Start stream")
        }
        Button(onClick = { /*TODO*/ }) {
            Text(text = "End Stream")
        }

        AndroidView(
            modifier = Modifier.size(400.dp),
            factory = { context ->
                PreviewView(context).apply {
                }
            },
            update = { view ->
                if (startClicked) {
                    val videoConfig = VideoConfig(
                        startBitrate = 1000000, // 1.5 Mb/s
                        resolution = Size(1280, 720),
                        fps = 30
                    )

                    streamer.configure(audioConfig = AudioConfig(), videoConfig)
                    view.streamer = streamer
                    coroutineScope.launch {
                        streamer.startStream(link)
                    }
                }
            })
    }
}