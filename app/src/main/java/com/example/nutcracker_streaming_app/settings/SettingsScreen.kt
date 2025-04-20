package com.example.nutcracker_streaming_app.settings

import android.annotation.SuppressLint
import android.util.Range
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutcracker_streaming_app.ui.theme.Colors
import com.example.nutcracker_streaming_app.ui.theme.Fonts
import com.example.nutcracker_streaming_app.utils.NsaPreferences
import com.example.nutcracker_streaming_app.utils.Option
import com.example.nutcracker_streaming_app.utils.StreamerHelper
import com.example.nutcracker_streaming_app.utils.toResolution
import com.example.nutcrackerstreamingapp.R
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val protocols = listOf(Option.Protocol.Srt, Option.Protocol.Rtmp)
    val scrollState = rememberScrollState()
    LaunchedEffect(NsaPreferences.audioEncoder) {
        StreamerHelper.refreshSettings(StreamerHelper.getSrtStreamer(context), context)
    }
    LaunchedEffect(NsaPreferences.videoEncoder) {
        StreamerHelper.refreshSettings(StreamerHelper.getSrtStreamer(context), context)
    }
    LaunchedEffect(Unit) {
        viewModel.setEvent(SettingsContract.Event.Refresh)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Background.main)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.padding(
                top = 36.dp,
                start = 12.dp,
                end = 12.dp,
                bottom = 12.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                color = Colors.Text.primary,
                text = stringResource(R.string.settings),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(30.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { navController.popBackStack() }),
                painter = painterResource(R.drawable.back_return_svgrepo_com),
                tint = Colors.Icons.primary,
                contentDescription = null,
            )
        }
        StreamLink(
            state = viewModel.viewState.value.rtmpLink,
            setEvent = viewModel::setEvent,
        )
        StreamLink(
            state = viewModel.viewState.value.srtLink,
            setEvent = viewModel::setEvent,
        )
        SettingItem(
            setEvent = viewModel::setEvent,
            currentOption = viewModel.viewState.value.resolution,
            options = viewModel.viewState.value.supportedStates.supportedResolutions.map { it.toResolution() }
                .toPersistentList()
        )
        SettingItem(
            setEvent = viewModel::setEvent,
            currentOption = viewModel.viewState.value.framerate,
            options = viewModel.viewState.value.supportedStates.supportedFramerates.map {
                Option.Framerate(
                    it
                )
            }.toPersistentList()
        )
        BitrateItem(
            setEvent = viewModel::setEvent,
            currentOption = viewModel.viewState.value.bitrateRange,
            bitrateAvailableRange = viewModel.viewState.value.supportedStates.supportedBitrates
        )
//        SettingItem(
//            setEvent = viewModel::setEvent,
//            currentOption = viewModel.viewState.value.audioEncoder,
//            options = viewModel.viewState.value.supportedStates.supportedAudioEncoder.map {
//                Option.AudioEncoder(
//                    it
//                )
//            }.toPersistentList()
//        )
//        SettingItem(
//            setEvent = viewModel::setEvent,
//            currentOption = viewModel.viewState.value.videoEncoder,
//            options = viewModel.viewState.value.supportedStates.supportedVideoEncoder.map {
//                Option.VideoEncoder(
//                    it
//                )
//            }.toPersistentList()
//        )
        SettingItem(
            setEvent = viewModel::setEvent,
            currentOption = viewModel.viewState.value.protocol,
            options = protocols.toPersistentList()
        )
    }
}

@Composable
private fun event(option: Option): SettingsContract.Event {
    return when (option) {
        is Option.Link.RtmpLink -> SettingsContract.Event.InputRtmpLink(option)
        is Option.Link.SrtLink -> SettingsContract.Event.InputSrtLink(option)
        is Option.Framerate -> SettingsContract.Event.SelectFramerate(option)
        is Option.Resolution -> SettingsContract.Event.SelectResolution(option)
        is Option.AudioEncoder -> SettingsContract.Event.SelectAudioEncoder(option)
        is Option.VideoEncoder -> SettingsContract.Event.SelectVideoEncoder(option)
        is Option.Protocol -> SettingsContract.Event.SelectProtocol(option)
        is Option.Bitrate -> SettingsContract.Event.InputBitrate(option)
    }
}

@Composable
private fun title(option: Option): String {
    return when (option) {
        is Option.Link.SrtLink -> stringResource(R.string.srt_url)
        is Option.Link.RtmpLink -> stringResource(R.string.rtmp_url)
        is Option.Framerate -> stringResource(R.string.framerate)
        is Option.Resolution -> stringResource(R.string.stream_resolution)
        is Option.AudioEncoder -> stringResource(R.string.audio_encoder)
        is Option.VideoEncoder -> stringResource(R.string.video_encoder)
        is Option.Protocol -> stringResource(R.string.stream_protocol)
        is Option.Bitrate -> stringResource(R.string.stream_bitrate)
    }
}

@Composable
fun StreamLink(
    state: Option.Link,
    setEvent: (SettingsContract.Event) -> Unit,
) {
    val openAlertDialog = remember { mutableStateOf(false) }
    SettingRow(currentOption = state, openAlertDialog)
    if (openAlertDialog.value) {
        InputDialog(openAlertDialog, state, setEvent)
    }
}

@Composable
fun BitrateItem(
    currentOption: Option.Bitrate,
    bitrateAvailableRange: Range<Int>,
    setEvent: (SettingsContract.Event) -> Unit,
) {
    val openAlertDialog = remember { mutableStateOf(false) }
    SettingRow(currentOption = currentOption, openAlertDialog)
    if (openAlertDialog.value) {
        BitrateDialog(openAlertDialog, currentOption, bitrateAvailableRange, setEvent)
    }
}

@Composable
private fun SettingItem(
    setEvent: (SettingsContract.Event) -> Unit,
    options: PersistentList<Option>,
    currentOption: Option,
) {
    val openAlertDialog = remember { mutableStateOf(false) }
    if (openAlertDialog.value) {
        SettingDialog(
            openAlertDialog = openAlertDialog,
            currentOption = currentOption,
            options = options,
            onOptionSelected = setEvent,
        )
    }
    SettingRow(currentOption, openAlertDialog)
}

@Composable
private fun SettingRow(
    currentOption: Option,
    openAlertDialog: MutableState<Boolean>
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .background(Colors.Background.row)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Colors.Utility.ripple),
                onClick = { openAlertDialog.value = true })
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 25.dp,
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    modifier = Modifier,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Fonts.robotoFamily,
                    lineHeight = 16.sp,
                    fontSize = 16.sp,
                    color = Colors.Text.primary,
                    text = title(currentOption)
                )
                Text(
                    modifier = Modifier,
                    fontWeight = FontWeight.Light,
                    fontFamily = Fonts.robotoFamily,
                    lineHeight = 16.sp,
                    fontSize = 14.sp,
                    color = Colors.Text.secondary,
                    text = currentOption.toPresentationString()
                )
            }
            HorizontalDivider(color = Colors.Background.main)
        }
    }
}

@Preview
@Composable
private fun PreviewSettingItem() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Background.main)
    ) {

    }
    SettingItem(
        setEvent = {},
        options = persistentListOf(),
        currentOption = Option.Resolution(1280, 720)
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun PreviewSettingDialog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Background.main)
    ) {
        SettingDialog(
            mutableStateOf(false),
            Option.Resolution(1280, 720),
            persistentListOf(
                Option.Resolution(1280, 720),
                Option.Resolution(1920, 1080),
                Option.Resolution(2560, 1440),
            ),
        ) { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputDialog(
    openAlertDialog: MutableState<Boolean>,
    currentOption: Option,
    onOptionEntered: (SettingsContract.Event) -> Unit
) {
    var text by remember { mutableStateOf(currentOption.toString()) }
    val event = when (currentOption) {
        is Option.Link.RtmpLink -> event(Option.Link.RtmpLink(text))
        is Option.Link.SrtLink -> event(Option.Link.SrtLink(text))
        else -> SettingsContract.Event.Refresh
    }
    BasicAlertDialog(
        onDismissRequest = { openAlertDialog.value = false },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Colors.Background.dialog)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 24.dp, 24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title(currentOption),
                    color = Colors.Text.primary,
                    fontFamily = Fonts.robotoFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                HorizontalDivider()
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = Colors.Text.primary,
                        fontFamily = Fonts.robotoFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Colors.Text.action,
                        focusedBorderColor = Colors.Text.action
                    )
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            openAlertDialog.value = false
                            onOptionEntered(event)
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontFamily = Fonts.robotoFamily,
                            fontSize = 16.sp,
                            color = Colors.Text.action
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingDialog(
    openAlertDialog: MutableState<Boolean>,
    currentOption: Option,
    options: PersistentList<Option>,
    onOptionSelected: (SettingsContract.Event) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { openAlertDialog.value = false },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Colors.Background.dialog)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 24.dp, 24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title(currentOption),
                    color = Colors.Text.primary,
                    fontFamily = Fonts.robotoFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier
                        .selectableGroup()
                        .requiredHeightIn(max = 500.dp)
                ) {
                    items(options, key = { it.hashCode() }) { option ->
                        val event = event(option)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == currentOption),
                                    onClick = {
                                        onOptionSelected(event)
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                colors = RadioButtonDefaults.colors(
                                    Colors.Button.radioButtonChecked,
                                    Colors.Button.radioButtonChecked,
                                    Colors.Button.radioButtonChecked,
                                    Colors.Button.radioButtonChecked
                                ),
                                selected = (option == currentOption),
                                onClick = null // null recommended for accessibility with screen readers
                            )
                            Text(
                                text = option.toPresentationString(),
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Colors.Text.primary,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { openAlertDialog.value = false },
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontFamily = Fonts.robotoFamily,
                            fontSize = 16.sp,
                            color = Colors.Text.action
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BitrateDialog(
    openAlertDialog: MutableState<Boolean>,
    currentOption: Option.Bitrate,
    bitrateAvailableRange: Range<Int>,
    onOptionEntered: (SettingsContract.Event) -> Unit
) {
    var text by remember { mutableStateOf((currentOption.range.lower / 1000).toString()) }
    val event = event(
        Option.Bitrate(
            Range(
                maxOf(
                    bitrateAvailableRange.lower,
                    minOf(
                        (text.toIntOrNull()?.times(1000)) ?: bitrateAvailableRange.lower,
                        bitrateAvailableRange.upper
                    )
                ),
                bitrateAvailableRange.upper
//                minOf(
//                    bitrateAvailableRange.upper,
//                    maxOf(text.toIntOrNull() ?: bitrateAvailableRange.upper, bitrateAvailableRange.lower)
//                )
            )
        )
    )
    BasicAlertDialog(
        onDismissRequest = { openAlertDialog.value = false },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Colors.Background.dialog)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 24.dp, 24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title(currentOption),
                    color = Colors.Text.primary,
                    fontFamily = Fonts.robotoFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                HorizontalDivider()
                Row {
                    OutlinedTextField(
                        value = text,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text(
                                text = stringResource(
                                    R.string.bitrate_supported_placeholder,
                                    bitrateAvailableRange.lower,
                                    bitrateAvailableRange.upper/1000
                                ),
                                color = Colors.Text.secondary
                            )
                        },
                        onValueChange = { text = it },
                        textStyle = TextStyle(
                            color = Colors.Text.primary,
                            fontFamily = Fonts.robotoFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Colors.Text.action,
                            focusedBorderColor = Colors.Text.action
                        )
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            openAlertDialog.value = false
                            onOptionEntered(event)
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontFamily = Fonts.robotoFamily,
                            fontSize = 16.sp,
                            color = Colors.Text.action
                        )
                    }
                }
            }
        }
    }
}