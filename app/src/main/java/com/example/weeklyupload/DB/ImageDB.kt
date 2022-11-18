package com.example.weeklyupload.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weeklyupload.DAO.ImageDAO
import com.example.weeklyupload.Object.Image

@Database(entities = [Image::class], version = 1, exportSchema = false)
abstract class ImageDB : RoomDatabase() {

    abstract fun imageDAO(): ImageDAO


    companion object {
        @Volatile
        private var INSTANCE: ImageDB? = null

        fun getDB(context: Context): ImageDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance

            }
            synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext, ImageDB::class.java, "trip_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }

        }
    }
}