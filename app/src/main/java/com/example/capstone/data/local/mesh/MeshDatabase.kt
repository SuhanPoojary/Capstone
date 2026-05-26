package com.example.capstone.data.local.mesh

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MeshMessageEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(MeshRoomConverters::class)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun meshMessageDao(): MeshMessageDao

    companion object {
        @Volatile
        private var INSTANCE: MeshDatabase? = null

        fun getInstance(context: Context): MeshDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    MeshRoomMigrationPlan.DATABASE_NAME,
                ).build().also { INSTANCE = it }
            }
        }
    }
}

