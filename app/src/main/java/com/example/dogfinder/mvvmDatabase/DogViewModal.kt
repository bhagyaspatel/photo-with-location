package com.example.dogfinder.mvvmDatabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogfinder.dataClasses.Dog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DogViewModal (application: Application) : AndroidViewModel(application) {

    private val repo : DogRepository = DogRepository(application)

    fun insertDog (dog : Dog){
        viewModelScope.launch {
            repo.insertDog(dog)
        }
    }

    fun getDog (date : String) : Dog{
        return repo.getDog(date)
    }
}