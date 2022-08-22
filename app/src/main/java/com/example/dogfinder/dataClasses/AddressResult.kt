package com.example.dogfinder.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressResult (var data : String? = null , var success : Boolean? = false) : Parcelable