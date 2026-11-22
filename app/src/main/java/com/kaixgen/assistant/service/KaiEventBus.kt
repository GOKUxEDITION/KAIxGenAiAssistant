package com.kaixgen.assistant.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface KaiHardwareEvent {
    data class VoiceActivityDetected(val rmsDb: Float, val timestampMs: Long) : KaiHardwareEvent
    data class DoubleClapDetected(val peakRatio: Float, val timestampMs: Long) : KaiHardwareEvent
    data class LockedAppIntercepted(val packageName: String, val timestampMs: Long) : KaiHardwareEvent
    data class IntruderCaptured(val imagePath: String, val reason: String, val timestampMs: Long) : KaiHardwareEvent
}

object KaiEventBus {
    private val _events = MutableSharedFlow<KaiHardwareEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<KaiHardwareEvent> = _events.asSharedFlow()

    private val _isListeningServiceRunning = MutableStateFlow(false)
    val isListeningServiceRunning: StateFlow<Boolean> = _isListeningServiceRunning.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(-60f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    fun emitEvent(event: KaiHardwareEvent) {
        _events.tryEmit(event)
    }

    fun setServiceRunning(running: Boolean) {
        _isListeningServiceRunning.value = running
    }

    fun updateRmsDb(rmsDb: Float) {
        _liveRmsDb.value = rmsDb
    }
}
