package com.ljchengx.eudic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ljchengx.eudic.data.dao.RequestRecordDao
import com.ljchengx.eudic.data.dao.WordDao
import com.ljchengx.eudic.data.dao.WordbookDao
import com.ljchengx.eudic.data.entity.RequestRecord
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.entity.WordbookEntity

@Database(
    entities = [RequestRecord::class, WordEntity::class, WordbookEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun requestRecordDao(): RequestRecordDao
    abstract fun wordDao(): WordDao
    abstract fun wordbookDao(): WordbookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加token字段，默认值为空字符串
                database.execSQL("ALTER TABLE request_records ADD COLUMN token TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建单词本表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS wordbooks (
                        id TEXT NOT NULL PRIMARY KEY,
                        language TEXT NOT NULL,
                        name TEXT NOT NULL,
                        addTime TEXT NOT NULL,
                        isSelected INTEGER NOT NULL DEFAULT 0,
                        updateTime INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eudic_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 