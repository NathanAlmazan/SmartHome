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
import com.example.smarthome.dto.UserSettings
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
    private var _timestamp by mutableStateOf(Date(System.currentTimeMillis() + (8 * (60 * 60 * 1000))))
    private var _history = mutableStateListOf<Summary>()
    private var _settings by mutableStateOf(UserSettings())
    private var _imageUrl by mutableStateOf("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")

    val error: String? get() = _error
    val selected: DeviceModel? get() = _selected
    val devices: List<DeviceModel> get() = _devices
    val report: Report? get() = _report
    val schedule: Schedules? get() = _schedule
    val timestamp: Date get() = _timestamp
    val history: List<Summary> get() = _history
    val imageUrl: String get() = _imageUrl
    val settings: UserSettings get() = _settings

    init {
        setSession() // Initialize Session
        setDeviceList() // Fetch device lists
        setEnergyReport(_timestamp) // Fetch Energy Report
        setUserSettings() // Fetch user settings
        setEnergyHistory() // Fetch Energy History
    }

    fun setSession() {
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
                                    when (it.action) {
                                        "REPORT" -> setEnergyReport(_timestamp)
                                        "VIDEO" -> _imageUrl = it.recipient
                                        else -> setDeviceList()
                                    }
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
        Log.d("Energy Date", _timestamp.time.toString())

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

    fun updateUserSettings(settings: UserSettings) {
        viewModelScope.launch {
            when(val response = reportRepository.updateCostPerWatt(settings)) {
                is Result.Success<UserSettings> -> {
                    _settings = response.data
                    setEnergyReport(_timestamp)
                    Log.d("Energy Report", response.data.toString())
                }
                is Result.Error -> {
                    _error = response.exception
                    Log.d("HTTP Exception", response.exception)
                }
            }
        }
    }

    private fun setUserSettings() {
        viewModelScope.launch {
            when(val response = reportRepository.getCostPerWatt()) {
                is Result.Success<UserSettings> -> {
                    _settings = response.data
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