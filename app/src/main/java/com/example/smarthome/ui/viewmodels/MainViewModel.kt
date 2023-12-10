package com.example.smarthome.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.dto.AuthModel
import com.example.smarthome.dto.CommandModel
import com.example.smarthome.dto.DeviceModel
import com.example.smarthome.dto.Report
import com.example.smarthome.dto.Schedules
import com.example.smarthome.dto.Summary
import com.example.smarthome.network.RestApiBuilder
import com.example.smarthome.network.Result
import com.example.smarthome.network.WebSocketBuilder
import com.example.smarthome.repository.auth.AuthRepositoryImpl
import com.example.smarthome.repository.device.DeviceRepositoryImpl
import com.example.smarthome.repository.report.ReportRepositoryImpl
import com.example.smarthome.repository.schedules.ScheduleRepositoryImpl
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(
    private val authRepository: AuthRepositoryImpl,
    private val deviceRepository: DeviceRepositoryImpl,
    private val schedRepository: ScheduleRepositoryImpl,
    private val reportRepository: ReportRepositoryImpl
): ViewModel() {
    private var _token by mutableStateOf<String?>(null)
    private var _error by mutableStateOf<String?>(null)
    private var _devices = mutableStateListOf<DeviceModel>()
    private var _selected by mutableStateOf<DeviceModel?>(null)
    private var _report by mutableStateOf<Report?>(null)
    private var _schedule by mutableStateOf<Schedules?>(null)
    private var _timestamp by mutableStateOf(Date())
    private var _history = mutableStateListOf<Summary>()

    val error: String? get() = _error
    val selected: DeviceModel? get() = _selected
    val devices: List<DeviceModel> get() = _devices
    val report: Report? get() = _report
    val schedule: Schedules? get() = _schedule
    val timestamp: Date get() = _timestamp
    val history: List<Summary> get() = _history

    init {
        setSession() // Initialize Session
        setDeviceList() // Fetch device lists
        setEnergyReport(_timestamp) // Fetch Energy Report
        setEnergyHistory() // Fetch Energy History
    }

    private fun setSession() {
        viewModelScope.launch {
            when(val response = authRepository.login("3b083cba-7891-4017-9c33-33b06b73a6d6")) {
                is Result.Success<AuthModel> -> {
                    _token = response.data.token
                    Log.d("Auth Token", response.data.token)

                    when (val initSession = deviceRepository.initializeSession("Bearer ${response.data.token}")) {
                        is Result.Success -> {
                            if (initSession.data) {
                                Log.d("WSS Status", "Connected")
                                deviceRepository.observeIncomingCommands().onEach {
                                    if (it.action == "REPORT") setEnergyReport(_timestamp)
                                    else setDeviceList()
                                }.launchIn(viewModelScope)
                            }
                        }
                        is Result.Error -> {
                            _error = initSession.exception
                            Log.d("WSS Exception", initSession.exception)
                        }
                    }
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    fun setDeviceList() {
        viewModelScope.launch {
            when(val response = deviceRepository.getAllDevices()) {
                is Result.Success<List<DeviceModel>> -> {
                    _devices.clear()
                    _devices.addAll(response.data)

                    if (_selected != null) {
                        Log.d("Device Updated", "Finding Device")
                        val device: DeviceModel? = response.data.find { it.deviceId.equals(_selected!!.deviceId) }

                        if (device != null) {
                            Log.d("Device Updated", device.toString())
                            _selected = device
                        }
                    }
                    Log.d("Device Count", response.data.size.toString())
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    fun sendCommand(deviceId: String, status: Boolean, action: String = "STATUS") {
        viewModelScope.launch {
            val value: Int = if (status) 0 else 1
            val command = CommandModel(deviceId, value, action)

            when(val response = deviceRepository.sendCommand(command)) {
                is Result.Success<CommandModel> -> setDeviceList()
                is Result.Error -> {
                    _error = response.exception
                    Log.d("WSS Exception", response.exception)

                    setSession() // Initialize Session Again
                }
            }
        }
    }

    fun sendTimer(deviceId: String, value: Int, action: String = "TIMER") {
        viewModelScope.launch {
            val command = CommandModel(deviceId, value, action)

            when(val response = deviceRepository.sendCommand(command)) {
                is Result.Success<CommandModel> -> setDeviceList()
                is Result.Error -> {
                    _error = response.exception
                    Log.d("WSS Exception", response.exception)

                    setSession() // Initialize Session Again
                }
            }
        }
    }

    fun setSelectedDevice(device: DeviceModel?) {
        _selected = device

        device?.let {
            it.deviceId?.let { id -> setDeviceSchedule(id) }
        }
    }

    fun updateDeviceDetails(id: String, name: String, category: String) {
        val device = DeviceModel(name, deviceId = id, deviceCategory = category)

        viewModelScope.launch {
            when(val response = deviceRepository.updateDevice(device)) {
                is Result.Success<DeviceModel> -> setDeviceList()
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    fun createDeviceSchedule(deviceId: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val schedule = Schedules(deviceId, startHour, startMinute, endHour, endMinute)
        Log.d("HTTP Request", schedule.toString())

        _schedule = schedule

        viewModelScope.launch {
            when(val response = schedRepository.addSchedule(schedule)) {
                is Result.Success<Schedules> -> setDeviceList()
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    fun removeDeviceSchedule(deviceId: String) {
        viewModelScope.launch {
            when(val response = schedRepository.removeSchedule(deviceId)) {
                is Result.Success<Schedules> -> setDeviceList()
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    fun setEnergyReport(datetime: Date) {
        _timestamp = datetime

        viewModelScope.launch {
            when(val response = reportRepository.getEnergyReport(datetime)) {
                is Result.Success<Report> -> {
                    _report = response.data
                    Log.d("Energy Report", response.data.toString())
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    private fun setDeviceSchedule(deviceId: String) {
        viewModelScope.launch {
            when(val response = schedRepository.getDeviceSchedule(deviceId)) {
                is Result.Success<Schedules> -> {
                    _schedule = response.data
                    Log.d("Energy Report", response.data.toString())
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    private fun setEnergyHistory() {
        viewModelScope.launch {
            when(val response = reportRepository.getEnergySummary()) {
                is Result.Success<List<Summary>> -> {
                    _history.clear()
                    _history.addAll(response.data)

                    Log.d("Energy Summary", response.data.size.toString())
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    authRepository = AuthRepositoryImpl(RestApiBuilder()),
                    deviceRepository = DeviceRepositoryImpl(WebSocketBuilder(), RestApiBuilder()),
                    schedRepository = ScheduleRepositoryImpl(RestApiBuilder()),
                    reportRepository = ReportRepositoryImpl(RestApiBuilder())
                )
            }
        }
    }
}