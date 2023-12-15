package com.example.smarthome

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smarthome.ui.screens.AppScaffold
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val channelId = "CAHA_IOT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize channel
        createNotificationChannel()

        setContent {
            SmartHomeTheme {
                LaunchedEffect(mainViewModel.devices, mainViewModel.report, mainViewModel.settings, mainViewModel.history) {
                    mainViewModel.report?.let { report ->
                        if (mainViewModel.settings.frequency == "Daily" && report.cost > mainViewModel.settings.maxWattPerDay) {
                            showNotification(
                                "Daily Consumption Warning!",
                                "Your daily energy consumption exceeds ₱${String.format("%.2f", mainViewModel.settings.maxWattPerDay)}"
                            )
                        }
                        else if (mainViewModel.settings.frequency == "Monthly" && (mainViewModel.history[0].consumption * mainViewModel.settings.costPerWatt) > mainViewModel.settings.maxWattPerDay) {
                            showNotification(
                                "Monthly Consumption Warning!",
                                "Your monthly energy consumption exceeds ₱${String.format("%.2f", mainViewModel.settings.maxWattPerDay)}"
                            )
                        }
                    }
                }

                AppScaffold(mainViewModel)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CAHA"
            val descriptionText = "Energy Monitoring"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, desc: String) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(this)) {
                notify(2, builder.build())
            }
        }
    }
}