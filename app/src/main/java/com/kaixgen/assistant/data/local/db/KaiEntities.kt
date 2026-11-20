package com.kaixgen.assistant.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class VoicePhraseEntity(
    @PrimaryKey val id: String, // "LOCK" or "UNLOCK"
    val type: String,
    val textNormalized: String,
    val phoneticKey: String,
    val languageName: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pin_records")
data class PinRecordEntity(
    @PrimaryKey val id: Int = 1,
    val hashHex: String,
    val saltHex: String,
    val iterations: Int,
    val pinLength: Int,
    val failedCount: Int = 0,
    val cooldownUntilMs: Long = 0L,
    val changedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String,
    val isLocked: Boolean,
    val graceMode: String = "ONE_MINUTE"
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val transcript: String,
    val intentName: String,
    val slotsJson: String,
    val responseText: String,
    val status: String, // "EXECUTED", "CONFIRMED", "CANCELLED", "FAILED"
    val confidencePercent: Int,
    val latencyMs: Long
)

@Entity(tableName = "aliases")
data class VoiceAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String, // "APP" or "CONTACT"
    val spoken: String,
    val target: String
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val triggerPhrase: String,
    val actionsSummary: String,
    val enabled: Boolean = true
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
