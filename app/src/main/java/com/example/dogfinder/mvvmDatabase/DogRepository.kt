package com.example.dogfinder.mvvmDatabase

import android.app.Application
import com.example.dogfinder.dataClasses.Dog

class DogRepository(context: Application) {

    private val dogDetailDao : DogDetailDao = DogDatabase.getDataBase(context).getDogDetailDao()

    fun getDog(date : String) : Dog {
        return dogDetailDao.getDog(date)
    }

    suspend fun insertDog(dog : Dog){
        return dogDetailDao.insertDog(dog)
    }
}