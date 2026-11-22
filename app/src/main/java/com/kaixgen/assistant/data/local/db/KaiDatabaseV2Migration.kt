package com.kaixgen.assistant.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "voiceprint_profiles")
data class VoiceprintProfileEntity(
    @PrimaryKey val id: Int = 1,
    val embeddingCsv: String,
    val sampleCount: Int,
    val threshold: Float = 0.75f,
    val updatedAtMs: Long
)

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val imagePath: String,
    val reason: String,
    val targetPackage: String?,
    val capturedAtMs: Long
)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS voiceprint_profiles (
                id INTEGER NOT NULL PRIMARY KEY,
                embeddingCsv TEXT NOT NULL,
                sampleCount INTEGER NOT NULL,
                threshold REAL NOT NULL,
                updatedAtMs INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS intruder_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                imagePath TEXT NOT NULL,
                reason TEXT NOT NULL,
                targetPackage TEXT,
                capturedAtMs INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
