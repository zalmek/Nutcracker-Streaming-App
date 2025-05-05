package com.example.nutcracker_streaming_app.permissions

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.ui.theme.Fonts
import com.example.nutcrackerstreamingapp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.collections.immutable.PersistentList

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionScreen(
    permissionsList: PersistentList<String>,
    permissionsState: MultiplePermissionsState,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(Colors.Background.main)
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            modifier = Modifier.padding(top = 60.dp, bottom = 50.dp),
            text = stringResource(R.string.permission_title),
            fontWeight = FontWeight.Bold,
            fontFamily = Fonts.robotoFamily,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Colors.Text.primary
        )
        Text(
            text = stringResource(R.string.permission_desc),
            modifier = Modifier.padding(bottom = 50.dp),
            color = Colors.Text.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = Fonts.robotoFamily,
            textAlign = TextAlign.Start,
        )
        for (permission in permissionsList) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val permissionGranted =
                    permissionsState.permissions.find { it.permission == permission }?.status == PermissionStatus.Granted
                Text(
                    text = permissionText(permission),
                    color = Colors.Text.primary
                )
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(
                        if (permissionGranted)
                            R.drawable.ic_accept_24
                        else R.drawable.ic_deny_24
                    ),
                    contentDescription = null,
                    tint = Colors.Icons.primary
                )
                if (!permissionGranted)
                    Button(
                        onClick = {
                            permissionsState.permissions.find { it.permission == permission }
                                ?.launchPermissionRequest()
                            if (permissionsState.permissions.find { it.permission == permission }?.status?.shouldShowRationale == false) {
                                Toast.makeText(
                                    context,
                                    "Выдайте разрешение через настройки приложения",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Colors.Background.button)
                    ) {
                        Text(
                            text = stringResource(R.string.ask_permission),
                            fontWeight = FontWeight.Normal,
                            fontFamily = Fonts.robotoFamily,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Colors.Text.primary
                        )
                    }
            }
        }
    }
}

@Composable
private fun permissionText(permission: String): String {
    return when (permission) {
        "android.permission.CAMERA" -> stringResource(R.string.camera)
        "android.permission.RECORD_AUDIO" -> stringResource(R.string.microphone)
        "android.permission.POST_NOTIFICATIONS" -> stringResource(R.string.permision_notifications)
        else -> permission
    }
}