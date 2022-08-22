package com.example.dogfinder.mvvmDatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.dogfinder.dataClasses.Dog

@Dao
interface DogDetailDao {

    @Query("SELECT * FROM dogTable WHERE 'date' =:date")
    fun getDog (date : String) : Dog

    @Insert
    suspend fun insertDog (dog : Dog)
}