package com.example.smarthome.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.smarthome.R
import com.example.smarthome.dto.History
import com.example.smarthome.dto.Report
import com.example.smarthome.dto.UserSettings
import com.example.smarthome.ui.viewmodels.MainViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun EnergyScreen(
    mainViewModel: MainViewModel,
    onNavigate: (Screen) -> Unit
) {
    val frequency = rememberSaveable { mutableStateOf("Daily") }
    val report = mainViewModel.report
    val timestamp = mainViewModel.timestamp
    val history = mainViewModel.graph
    val costPerWatt = mainViewModel.settings.costPerWatt

    val handleSelectFrequency: (String) -> Unit = { selected ->
        frequency.value = selected

        when (selected) {
            "Hourly" -> mainViewModel.setGraphData("hour")
            "Daily" -> mainViewModel.setGraphData("day")
            "Weekly" -> mainViewModel.setGraphData("week")
            "Monthly" -> mainViewModel.setGraphData("month")
        }
    }

    val handleUpdateSettings: (UserSettings) -> Unit = { settings ->
        mainViewModel.updateUserSettings(settings)
    }

    val handleChangeReportDate: (Date) -> Unit = { reportDate ->
        mainViewModel.setEnergyReport(reportDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { onNavigate(Screen.Home) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "arrow back",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                )
            }
        }
        report?.let {
            EnergyConsumption(
                it,
                mainViewModel.settings,
                timestamp,
                handleUpdateSettings,
                handleChangeReportDate
            )

            EnergyDetails(it)
        }

        EnergyHistory(frequency.value, history, costPerWatt, handleSelectFrequency)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyConsumption(
    report: Report,
    userSettings: UserSettings,
    reportDate: Date,
    onUpdateSettings: (UserSettings) -> Unit,
    onDateChange: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var dialog by rememberSaveable { mutableStateOf(false) }
    var settings by rememberSaveable { mutableStateOf(false) }
    val current = reportDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val currentDate = current.format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
    val dayName = current.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        val (indicator, details) = createRefs()

        CircularProgressIndicator(
            progress = { 100f },
            modifier = Modifier
                .height(320.dp)
                .width(320.dp)
                .constrainAs(indicator) {
                    top.linkTo(parent.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                },
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Box(modifier = Modifier
            .constrainAs(details) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
            }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = { dialog = true }) {
                    Text(
                        text = "$dayName, $currentDate",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                if (dialog) {
                    DatePickerDialog(
                        onDismissRequest = { dialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialog = false
                                    datePickerState.selectedDateMillis?.let {
                                        onDateChange(Date(it))
                                    }
                                },
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }) {
                        DatePicker(state = datePickerState)
                    }
                }

                Text(
                    text = "${String.format(Locale.US, "%.2f", report.consumption).toDouble()}kWh",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )

                TextButton(onClick = { settings = true }) {
                    Text(
                        text = "₱${String.format(Locale.US, "%.2f", report.cost).toDouble()}",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }

                if (settings) {
                    SettingsDialog(
                        report = report,
                        settings = userSettings,
                        onSubmit = { settings -> onUpdateSettings(settings) },
                        onDismiss = { settings = false }
                    )
                }
            }
        }
    }
}

@Composable
fun EnergyDetails(report: Report) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // The section that holds the other power data
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_power),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.power).toDouble()}W",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Power",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_speed),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.current).toDouble()}A",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_electric_bolt),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.voltage).toDouble()}V",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Voltage",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // Card for humidity
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_power_meter),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.energy).toDouble()}kWh",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Energy",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_frequency),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.frequency).toDouble()}Hz",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_electric_plug),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${String.format(Locale.US, "%.2f", report.consumption).toDouble()}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "PFactor",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun formatIsoDate(isoDateString: String, selected: String): String {
    // Parse the ISO 8601 date string to a ZonedDateTime object
    val zonedDateTime = ZonedDateTime.parse(isoDateString, DateTimeFormatter.ISO_DATE_TIME)

    // Define the desired output format
    var formatter = DateTimeFormatter.ofPattern("MMM d, h a", Locale.ENGLISH)

    if (selected == "Daily" || selected == "Weekly") {
        formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
    } else if (selected == "Monthly") {
        formatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    }

    // Format the ZonedDateTime object to the desired format
    return zonedDateTime.format(formatter)
}

@Composable
fun EnergyHistory(
    selected: String,
    history: List<History>,
    costPerWatt: Double,
    onSelect: (String) -> Unit
) {
    val labels = history.map { data -> formatIsoDate(data.stamp, selected) }
    val values: Float = history.maxOfOrNull { data -> data.energy / 100f } ?: 50.0f
    val constant: Int = (values / 5.0f).toInt()
    val dataPoints = history.mapIndexed { index, data -> Point(index.toFloat(), data.energy / 100f) }

    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .backgroundColor(Color.Transparent)
        .steps(history.size - 1)
        .labelData { i -> labels[i] }
        .labelAndAxisLinePadding(15.dp)
        .axisLineColor(MaterialTheme.colorScheme.secondary)
        .axisLabelColor(MaterialTheme.colorScheme.secondary)
        .axisLabelFontSize(12.sp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i -> "${(i * constant)}kWh" }
        .axisLineColor(MaterialTheme.colorScheme.secondary)
        .axisLabelColor(MaterialTheme.colorScheme.secondary)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = dataPoints,
                    LineStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        lineType = LineType.SmoothCurve(isDotted = false)
                    ),
                    IntersectionPoint(
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    SelectionHighlightPoint(
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    ShadowUnderLine(
                        alpha = 0.5f,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.inversePrimary,
                                Color.Transparent
                            )
                        )
                    ),
                    SelectionHighlightPopUp()
                )
            )
        ),
        backgroundColor = MaterialTheme.colorScheme.surface,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Row {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start
            )
        }

        DropDownTextField(
            label = "Frequency",
            selectedText = selected,
            options = listOf("Hourly", "Daily", "Weekly", "Monthly"),
            onSelect = { onSelect(it) }
        )

        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            lineChartData = lineChartData
        )
    }
}

@Composable
fun SettingsDialog(
    report: Report,
    settings: UserSettings,
    onSubmit: (UserSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var cost by rememberSaveable { mutableDoubleStateOf(settings.costPerWatt) }
    var threshold by rememberSaveable { mutableDoubleStateOf(settings.maxWattPerDay) }
    var schedule by rememberSaveable { mutableStateOf(settings.frequency) }

    Dialog(onDismissRequest = { onDismiss() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Row {
                    Text(
                        text = "Energy Monitoring Settings",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${report.consumption}kWh x ",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(2f)) {
                        TextField(
                            label = {
                                Text(
                                    text = "Cost per kWh",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            value = cost.toString(),
                            onValueChange = { cost = it.toDouble() },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text(text = "₱") }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TextField(
                        label = {
                            Text(
                                text = "Threshold",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        value = threshold.toString(),
                        onValueChange = { threshold = it.toDouble() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text(text = "₱") }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    DropDownTextField(
                        label = "Threshold Schedule",
                        selectedText = schedule,
                        options = listOf("Daily", "Monthly"),
                        onSelect = { schedule = it }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onSubmit(UserSettings(cost, threshold, schedule))
                            onDismiss()
                        },
                    ) {
                        Text("Save")
                    }
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}