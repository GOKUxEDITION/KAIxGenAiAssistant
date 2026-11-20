package com.kaixgen.assistant.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import androidx.lifecycle.ViewModel
import com.kaixgen.assistant.domain.voice.ActionExecutionResult
import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.KaiActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuickTaskItem(
    val id: Long,
    val title: String,
    val isDone: Boolean = false
)

data class MoreHubUiState(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val isTorchOn: Boolean = false,
    val isDrivingModeSheetOpen: Boolean = false,
    val tasks: List<QuickTaskItem> = listOf(
        QuickTaskItem(1L, "Test Voice Lock phrase & Backup PIN", isDone = true),
        QuickTaskItem(2L, "Configure custom Hinglish contact aliases", isDone = false),
        QuickTaskItem(3L, "Set Safe-Walk timer when commuting", isDone = false)
    ),
    val statusBanner: String? = null
)

class MoreHubViewModel(
    private val appContext: Context,
    private val parser: HinglishVoiceCommandParser,
    private val actionExecutor: KaiActionExecutor
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreHubUiState())
    val uiState: StateFlow<MoreHubUiState> = _uiState.asStateFlow()

    init {
        refreshDeviceStatus()
    }

    fun refreshDeviceStatus() {
        val batteryIntent = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        _uiState.update {
            it.copy(
                batteryPercent = percent,
                isCharging = charging
            )
        }
    }

    fun toggleFlashlight() {
        val next = !_uiState.value.isTorchOn
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, next)
                    _uiState.update {
                        it.copy(
                            isTorchOn = next,
                            statusBanner = if (next) "Flashlight turned ON" else "Flashlight turned OFF"
                        )
                    }
                    return
                }
            }
        } catch (_: Exception) {
            // Gracefully degrade on devices/emulators without rear flash hardware
        }
        _uiState.update {
            it.copy(
                isTorchOn = next,
                statusBanner = "Torch state updated (${if (next) "ON" else "OFF"})"
            )
        }
    }

    fun openDrivingModeSheet() {
        _uiState.update { it.copy(isDrivingModeSheetOpen = true) }
    }

    fun closeDrivingModeSheet() {
        _uiState.update { it.copy(isDrivingModeSheetOpen = false) }
    }

    fun addQuickTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val newItem = QuickTaskItem(id = System.currentTimeMillis(), title = trimmed)
        _uiState.update {
            it.copy(
                tasks = listOf(newItem) + it.tasks,
                statusBanner = "Task added: $trimmed"
            )
        }
    }

    fun toggleTask(id: Long) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == id) task.copy(isDone = !task.isDone) else task
                }
            )
        }
    }

    fun deleteTask(id: Long) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.filterNot { it.id == id })
        }
    }

    fun launchQuickTimer(minutes: Int) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                putExtra(AlarmClock.EXTRA_MESSAGE, "KAI Quick Timer")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            _uiState.update { it.copy(statusBanner = "Opened $minutes-min timer in Clock") }
        } catch (_: Exception) {
            _uiState.update { it.copy(statusBanner = "No Clock timer handler available on this device") }
        }
    }

    fun launchMapsNavigation() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        } catch (_: Exception) {
            runVoiceAction("open maps")
        }
    }

    fun runVoiceAction(transcript: String) {
        val parsed = parser.parse(transcript)
        val banner = when (val result = actionExecutor.execute(parsed, confirmed = true)) {
            is ActionExecutionResult.Executed -> result.displayMessage
            is ActionExecutionResult.NeedsConfirmation -> result.promptMessage
            is ActionExecutionResult.Failure -> result.errorMessage
        }
        _uiState.update { it.copy(statusBanner = banner) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }
}
