package com.example.dogfinder.dataClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LatLong (val latitude : Double, val longitude : Double) : Parcelable