package com.jarvis.features.inspector.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.jarvis.features.inspector.data.local.dao.NetworkTransactionDao
import com.jarvis.features.inspector.data.local.entity.NetworkTransactionEntity

@Database(
    entities = [NetworkTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InspectorDatabase : RoomDatabase() {
    
    abstract fun networkTransactionDao(): NetworkTransactionDao
    
    companion object {
        const val DATABASE_NAME = "inspector_database"
        
        fun create(context: Context): InspectorDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                InspectorDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            // ✅ PERFORMANCE: Enable WAL mode for better concurrent access
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            // ✅ PERFORMANCE: Use background thread for database operations
            .setQueryExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
            .build()
        }
    }
}