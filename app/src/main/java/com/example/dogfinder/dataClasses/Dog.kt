package com.example.dogfinder.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "dogTable")
data class Dog (
    var location : String? = null,
    var date : String?= null,

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
)