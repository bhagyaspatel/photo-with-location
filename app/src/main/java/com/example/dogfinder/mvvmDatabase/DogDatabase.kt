package com.example.dogfinder.mvvmDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dogfinder.dataClasses.Dog

@Database (entities = [Dog::class], version = 1)
abstract class DogDatabase : RoomDatabase(){

    abstract fun getDogDetailDao() : DogDetailDao

    companion object{
        @Volatile
        private var instance : DogDatabase? = null

        fun getDataBase(context: Context) = instance ?: synchronized(this){
            Room.databaseBuilder(
                context.applicationContext,
                DogDatabase::class.java,
                "dog_database"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }

}