package com.example.smarthome.ui.screens

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.dto.Report
import com.example.smarthome.ui.viewmodels.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


sealed class Category(val name: String, @DrawableRes val icon: Int) {
    data object Outlet : Category("Outlet", R.drawable.ic_electric_plug)
    data object Office : Category("Office", R.drawable.ic_devices)
    data object Kitchen : Category("Kitchen", R.drawable.ic_microwave)
    data object LivingRoom : Category("Living Room", R.drawable.ic_television)
    data object Dining : Category("Dining", R.drawable.ic_kettle)
    data object Lighting : Category("Lighting", R.drawable.ic_bulb)
    data object AirCondition : Category("Air Condition", R.drawable.ic_fan)
    data object Security : Category("Security", R.drawable.ic_lock)
    data object Camera : Category("Camera", R.drawable.baseline_camera_24)
}

fun getDeviceIcon(category: String?): Category? {
    val deviceIcons = mapOf(
        "Outlet" to Category.Outlet,
        "Office" to Category.Office,
        "Kitchen" to Category.Kitchen,
        "Living Room" to Category.LivingRoom,
        "Dining" to Category.Dining,
        "Lighting" to Category.Lighting,
        "Air Condition" to Category.AirCondition,
        "Security" to Category.Security,
        "Camera" to Category.Camera
    )

    return deviceIcons[category]
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val devices = mainViewModel.devices
    val report = mainViewModel.report

    val handleToggleSwitch: (String, Boolean) -> Unit = { id, status ->
        mainViewModel.sendCommand(id, status)
    }

    val handleDeviceSelected: (DeviceModel) -> Unit = { device ->
        mainViewModel.setSelectedDevice(device)
        onNavigate(Screen.Device)
    }

    val handleRefresh: () -> Unit = {
        mainViewModel.setSession()
        mainViewModel.setDeviceList()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        GreetingCard()
        EnergyCard(report) { onNavigate(Screen.Energy) }
        DeviceGrid(devices, handleToggleSwitch, handleDeviceSelected, handleRefresh)
    }
}

@Composable
fun GreetingCard() {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp, 12.dp)) {
        Text(
            text = "Good Day!",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnergyCard(
    report: Report?,
    onEnergyNavigate: () -> Unit
) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
    val dayName = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 14.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                shape = RoundedCornerShape(120.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier
                        .weight(2f)
                        .padding(22.dp, 16.dp)) {
                        Text(
                            text = "Energy Consumption",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "$dayName, $currentDate",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(8.dp, 4.dp)) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onEnergyNavigate() }
                        ) {
                            Text(
                                text = "Details",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "energy details",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_electric_bolt),
                                contentDescription = "electric bolt")
                        }
                    }
                    Column(modifier = Modifier
                        .weight(2f)
                        .padding(4.dp)) {
                        if (report != null) {
                            Text(
                                text = "${String.format("%.2f", report.consumption).toDouble()}kWh",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Energy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_payment),
                                contentDescription = "electric bolt")
                        }
                    }
                    Column(modifier = Modifier
                        .weight(2f)
                        .padding(4.dp)) {
                        if (report != null) {
                            Text(
                                text = "₱${String.format("%.2f", report.cost).toDouble()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Cost",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceGrid(
    devices: List<DeviceModel>,
    onToggleSwitch: (String, Boolean) -> Unit,
    onDeviceSelected: (DeviceModel) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(2f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Devices",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 16.dp)
                )
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(0.dp, 16.dp)
        ) {
            itemsIndexed(devices) { index, device ->
                DeviceListItem(index, device, onToggleSwitch) { onDeviceSelected(device) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListItem(
    label: Int,
    device: DeviceModel,
    onToggleSwitch: (String, Boolean) -> Unit,
    onDeviceSelected: () -> Unit
) {
    var status by rememberSaveable { mutableStateOf(device.deviceStatus) }

    LaunchedEffect(device) {
        status = device.deviceStatus
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.deviceCategory.equals("Camera") || device.deviceCategory.equals("Security")) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.background
        ),
        onClick = { onDeviceSelected() }
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 2.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(0.dp, 16.dp, 0.dp, 0.dp)) {
                Box(modifier = Modifier
                    .height(60.dp)
                    .width(60.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(getDeviceIcon(device.deviceCategory)?.icon ?: R.drawable.ic_electric_plug),
                        contentDescription = "electric bolt")
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                if (!device.deviceCategory.equals("Camera")) {
                    Switch(
                        modifier = Modifier.scale(0.8f),
                        checked = status,
                        onCheckedChange = {
                            status = it
                            onToggleSwitch(device.deviceId!!, it)
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 2.dp, 8.dp, 8.dp)) {
            Column {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Device ${label + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


