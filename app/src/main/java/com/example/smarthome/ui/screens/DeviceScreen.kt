package com.example.smarthome.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.smarthome.R
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.dto.Schedules
import com.example.smarthome.ui.viewmodels.MainViewModel


@Composable
fun DeviceScreen(
    mainViewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val device = mainViewModel.selected
    val schedule = mainViewModel.schedule

    val handleToggleSwitch: (String, Boolean) -> Unit = { id, status ->
        mainViewModel.sendCommand(id, status)
    }

    val handleSaveChanges: (String, String) -> Unit = { name, category ->
        device?.deviceId?.let { mainViewModel.updateDeviceDetails(it, name, category) }
    }

    val handleStartTimer: (Int) -> Unit = { seconds ->
        device?.let {
            it.deviceId?.let { id -> mainViewModel.sendTimer(id, seconds) }
        }
    }

    val handleStopTimer: () -> Unit = {
        device?.let {
            it.deviceId?.let { id -> mainViewModel.sendTimer(id, 0, "TIMER_STOP") }
        }
    }

    val handleStartScheduler: (Int, Int, Int, Int) -> Unit = { startHour, startMinute, endHour, endMinute ->
        device?.let {
            it.deviceId?.let { id ->
                mainViewModel.createDeviceSchedule(id, startHour, startMinute, endHour, endMinute)
            }
        }
    }

    val handleStopScheduler: () -> Unit = {
        device?.let {
            it.deviceId?.let { id ->
                mainViewModel.removeDeviceSchedule(id)
            }
        }
    }

    val handleNavigateBack: () -> Unit = {
        onNavigate(Screen.Home)
        mainViewModel.setSelectedDevice(null)
    }

    val handleRefresh: () -> Unit = {
        mainViewModel.setDeviceList()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {
        Header(device?.deviceCategory, handleNavigateBack, handleRefresh)
        DeviceSwitch(device, handleToggleSwitch, handleSaveChanges)

        if (device != null && device.deviceCategory?.equals("Security") == false) {
            CountdownTimer(device, handleStartTimer, handleStopTimer)
        }

        ScheduledDevice(device, schedule, handleStartScheduler, handleStopScheduler)
    }
}

@Composable
fun Header(
    category: String?,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "arrow back",
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                IconButton(onClick = { onRefresh() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "arrow back",
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                }
            }
        }
        Row(modifier = Modifier
            .width(100.dp)
            .height(100.dp)) {
            Box(modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(getDeviceIcon(category)?.icon ?: R.drawable.ic_electric_bolt),
                    contentDescription = "electric bolt",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp))
            }
        }
    }
}

@Composable
fun DeviceSwitch(
    device: DeviceModel?,
    onToggleSwitch: (String, Boolean) -> Unit,
    onSaveChanges: (String, String) -> Unit
) {
    var editMode by rememberSaveable { mutableStateOf(false) }
    val id by rememberSaveable { mutableStateOf(device?.deviceId) }
    var name by rememberSaveable { mutableStateOf(device?.deviceName) }
    var category by rememberSaveable { mutableStateOf(device?.deviceCategory) }
    var status by rememberSaveable { mutableStateOf(device?.deviceStatus) }

    LaunchedEffect(device) {
        device?.let {
            name = it.deviceName
            category = it.deviceCategory
            status = it.deviceStatus
        }
    }

    if (editMode) {
        Row {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Edit",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    if (name != null && category != null) {
                                        onSaveChanges(name!!, category!!)
                                        editMode = false
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_save),
                                    contentDescription = "save edit"
                                )
                                Text("Save")
                            }
                            IconButton(
                                onClick = { editMode = false },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "cancel edit"
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()) {
                    TextField(
                        label = { Text(text = "Device Name", style = MaterialTheme.typography.labelLarge) },
                        value = name ?: "",
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()) {
                    DropDownTextField(
                        label = "Category",
                        selectedText = category ?: "Outlet",
                        options = listOf("Outlet", "Office", "Kitchen", "Living Room", "Dining", "Lighting", "Air Condition", "Security"),
                        onSelect = { category = it }
                    )
                }
            }
        }
    } else {
        Row {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name ?: "",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(
                        onClick = { editMode = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "cancel edit"
                        )
                    }
                }
                Row {
                    Text(
                        text = category ?: "Outlet",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (status == true) "ON" else "OFF",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                    Switch(
                        checked = status ?: true,
                        onCheckedChange = {
                            status = it
                            onToggleSwitch(id!!, it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownTimer(
    device: DeviceModel?,
    onTimerStart: (Int) -> Unit,
    onTimerStop: () -> Unit
) {
    var hour by rememberSaveable { mutableIntStateOf(0) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var second by rememberSaveable { mutableIntStateOf(0) }
    var active by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(device) {
        device?.let {
           active = it.deviceTimer
        }
    }

    Column {
        Row(
            modifier = Modifier.padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Countdown",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (active) {
                        ElevatedButton(
                            onClick = {
                                active = false
                                onTimerStop()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_stop),
                                contentDescription = "start timer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Stop", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        ElevatedButton(
                            onClick = {
                                active = true
                                onTimerStart((hour * 3600000) + (minute * 60000) + (second * 1000))
                            },
                            colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.primary),
                            enabled = if (device != null) !device.deviceSchedule else true
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "start timer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Start", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        Row {
            Column(modifier = Modifier
                .weight(1f)
                .padding(4.dp)) {
                OutlinedTextField(
                    label = { Text(text = "Hour", style = MaterialTheme.typography.labelSmall) },
                    value = hour.toString(),
                    onValueChange = { hour = if (it.isEmpty()) 0 else it.toInt() },
                    colors = TextFieldDefaults.colors(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !active
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(4.dp)) {
                OutlinedTextField(
                    label = { Text(text = "Minute", style = MaterialTheme.typography.labelSmall) },
                    value = minute.toString(),
                    onValueChange = { minute = if (it.isEmpty()) 0 else it.toInt() },
                    colors = TextFieldDefaults.colors(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !active
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(4.dp)) {
                OutlinedTextField(
                    label = { Text(text = "Second", style = MaterialTheme.typography.labelSmall) },
                    value = second.toString(),
                    onValueChange = { second = if (it.isEmpty()) 0 else it.toInt() },
                    colors = TextFieldDefaults.colors(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !active
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledDevice(
    device: DeviceModel?,
    schedule: Schedules?,
    onStartSchedule: (Int, Int, Int, Int) -> Unit,
    onStopSchedule: () -> Unit
) {
    val startState = rememberTimePickerState()
    val endState = rememberTimePickerState()
    var active by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(device) {
        device?.let {
            active = it.deviceSchedule
        }
    }

    Column {
        Row(
            modifier = Modifier.padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Schedule",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (active) {
                        ElevatedButton(
                            onClick = {
                                active = false
                                onStopSchedule()
                            },
                            colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_stop),
                                contentDescription = "start timer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Stop", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        ElevatedButton(
                            onClick = {
                                active = true
                                onStartSchedule(startState.hour, startState.minute, endState.hour, endState.minute)
                            },
                            colors = ButtonDefaults.elevatedButtonColors(MaterialTheme.colorScheme.primary),
                            enabled = if (device != null) !device.deviceTimer else true
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "start timer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Start", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Start",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(48.dp)
                    )
                     if (active && schedule != null) {
                         Text(
                             text = "${schedule.startHour} : ${schedule.startMinute}",
                             textAlign = TextAlign.Start,
                             style = MaterialTheme.typography.displayMedium
                         )
                     } else {
                         TimeInput(
                             state = startState,
                             modifier = Modifier.scale(0.8f)
                         )
                     }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "End",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(48.dp)
                    )
                    if (active && schedule != null) {
                        Text(
                            text = "${schedule.endHour} : ${schedule.endMinute}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.displayMedium
                        )
                    } else {
                        TimeInput(
                            state = endState,
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DropDownTextField(
    label: String,
    selectedText: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        TextField(
            value = selectedText,
            onValueChange = { onSelect(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                },
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            },
            readOnly = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current){textFieldSize.width.toDp()})
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    onClick = {
                        onSelect(label)
                        expanded = false
                    }
                )
            }
        }
    }
}