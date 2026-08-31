package com.callmate.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.callmate.ai.data.local.dao.*
import com.callmate.ai.data.local.entity.*

@Database(
    entities = [
        CallEntity::class,
        TranscriptEntity::class,
        AddressEntity::class,
        UserProfileEntity::class,
        AssistantSettingsEntity::class,
        AssistantInstructionsEntity::class,
        VoiceSettingsEntity::class,
        SilentModeSettingsEntity::class,
        NotificationSettingsEntity::class,
        CallSummaryEntity::class,
        AppPreferencesEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CallMateDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun addressDao(): AddressDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun localSettingsDao(): LocalSettingsDao
    abstract fun callSummaryDao(): CallSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: CallMateDatabase? = null

        fun getDatabase(context: Context): CallMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallMateDatabase::class.java,
                    "callmate_ai.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
