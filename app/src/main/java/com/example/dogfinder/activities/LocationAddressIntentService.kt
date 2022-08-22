package com.example.dogfinder.activities

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.dogfinder.dataClasses.AddressResult
import com.example.dogfinder.dataClasses.LatLong
import com.example.dogfinder.fragments.HomeFragment.Companion.TAG
import java.io.IOException
import java.lang.Exception
import java.util.*

const val  LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA"
const val BROADCAST_ACTION_LOCATION_ADDRESS = "BROADCAST_ACTION_LOCATION_ADDRESS"

//we cant use jobintent service for long tasks
class LocationAddressIntentService : IntentService("LocationAddressIntentService") {
    override fun onHandleIntent(intent : Intent?) {
        intent ?: return

        val latLong : LatLong? = intent.getParcelableExtra(LOCATION_DATA_EXTRA)
        if (latLong == null){
            Log.d (TAG, "latLong is null")
            return
        }

        Log.d(TAG, "Passed latLong is $latLong")

        val addressResult = AddressResult()
        val geocoder = Geocoder(this, Locale.getDefault()) //geocoder is the one which converts
        //lati&longi into readable address and it returns list of addresses
        var locationAddress : List<Address> = emptyList()
        //Address is also a class (inside system)

        try{
            locationAddress = geocoder.getFromLocation(latLong.latitude, latLong.longitude, 1)
        }catch (e : IOException){
            addressResult.data = "Error occurred while fetching the addresses. Please try again."
        }catch (e : Exception){
            addressResult.data = "Unknown Error : $e"
        }

        if (locationAddress.isNotEmpty()){
            Log.d (TAG, "locationAdress is not  empty")

            val address = locationAddress[0]

            val addressToken = with(address){
                (0..maxAddressLineIndex).map {
                    getAddressLine(it)}
                //we are traversing address (each line)
            }
            addressResult.data = addressToken.joinToString (separator = "\n")
            addressResult.success = true
        }
        else{
            Log.d (TAG, "locationAdress is empty")
        }
        broadcastStatus(addressResult)
    }

    private fun broadcastStatus(addressResult: AddressResult) {
        Intent().also {intent ->
            intent.action = BROADCAST_ACTION_LOCATION_ADDRESS
            intent.putExtra(LOCATION_DATA_EXTRA, addressResult)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }


}