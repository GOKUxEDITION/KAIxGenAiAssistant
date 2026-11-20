package com.kaixgen.assistant.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoicePhraseDao {
    @Query("SELECT * FROM phrases")
    fun observeAllPhrases(): Flow<List<VoicePhraseEntity>>

    @Query("SELECT * FROM phrases WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VoicePhraseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(phrase: VoicePhraseEntity)

    @Query("DELETE FROM phrases")
    suspend fun clearAll()
}

@Dao
interface PinRecordDao {
    @Query("SELECT * FROM pin_records WHERE id = 1 LIMIT 1")
    fun observePinRecord(): Flow<PinRecordEntity?>

    @Query("SELECT * FROM pin_records WHERE id = 1 LIMIT 1")
    suspend fun getPinRecordOnce(): PinRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PinRecordEntity)

    @Query("UPDATE pin_records SET failedCount = :failedCount, cooldownUntilMs = :cooldownUntilMs WHERE id = 1")
    suspend fun updateAttemptState(failedCount: Int, cooldownUntilMs: Long)

    @Query("DELETE FROM pin_records")
    suspend fun clearAll()
}

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY isLocked DESC, appName ASC")
    fun observeAllApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT COUNT(*) FROM locked_apps")
    suspend fun countApps(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<LockedAppEntity>)

    @Query("UPDATE locked_apps SET isLocked = NOT isLocked WHERE packageName = :packageName")
    suspend fun toggleAppLock(packageName: String)

    @Query("UPDATE locked_apps SET isLocked = 0")
    suspend fun unlockAllApps()

    @Query("UPDATE locked_apps SET graceMode = :graceMode")
    suspend fun updateGraceModeForAll(graceMode: String)
}

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestampMs DESC LIMIT :limit")
    fun observeRecentCommands(limit: Int = 25): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(command: CommandHistoryEntity): Long

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM command_history")
    suspend fun clearAll()
}

@Dao
interface VoiceAliasDao {
    @Query("SELECT * FROM aliases ORDER BY id DESC")
    fun observeAllAliases(): Flow<List<VoiceAliasEntity>>

    @Query("SELECT * FROM aliases")
    suspend fun getAllOnce(): List<VoiceAliasEntity>

    @Query("SELECT COUNT(*) FROM aliases")
    suspend fun countAliases(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aliases: List<VoiceAliasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alias: VoiceAliasEntity)

    @Query("DELETE FROM aliases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM aliases")
    suspend fun clearAll()
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY name ASC")
    fun observeAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE enabled = 1")
    suspend fun getEnabledOnce(): List<RoutineEntity>

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun countRoutines(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(routines: List<RoutineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: RoutineEntity)

    @Query("UPDATE routines SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun observeAllSettings(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: SettingEntity)

    @Query("DELETE FROM settings")
    suspend fun clearAll()
}
