package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [BazarExpenseEntity::class],
  version = 1,
  exportSchema = false
)
abstract class MessRoomDatabase : RoomDatabase() {

  abstract fun bazarExpenseDao(): BazarExpenseDao

  companion object {
    @Volatile
    private var INSTANCE: MessRoomDatabase? = null

    fun getDatabase(context: Context): MessRoomDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MessRoomDatabase::class.java,
          "smart_mess_room.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
