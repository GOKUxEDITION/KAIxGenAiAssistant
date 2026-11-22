package com.kaixgen.assistant.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VoicePhraseEntity::class,
        PinRecordEntity::class,
        LockedAppEntity::class,
        CommandHistoryEntity::class,
        VoiceAliasEntity::class,
        RoutineEntity::class,
        SettingEntity::class,
        VoiceprintProfileEntity::class,
        IntruderLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KaiDatabase : RoomDatabase() {

    abstract fun voicePhraseDao(): VoicePhraseDao
    abstract fun pinRecordDao(): PinRecordDao
    abstract fun lockedAppDao(): LockedAppDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun voiceAliasDao(): VoiceAliasDao
    abstract fun routineDao(): RoutineDao
    abstract fun settingDao(): SettingDao

    companion object {
        private const val DATABASE_NAME = "kaixgen_assistant_local.db"

        @Volatile
        private var INSTANCE: KaiDatabase? = null

        fun getInstance(context: Context): KaiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KaiDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
