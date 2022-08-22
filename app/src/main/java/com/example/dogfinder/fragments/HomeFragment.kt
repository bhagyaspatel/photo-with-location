package com.example.dogfinder.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.dogfinder.R
import com.example.dogfinder.dataClasses.AddressResult
import com.example.dogfinder.dataClasses.Dog
import com.example.dogfinder.dataClasses.LatLong
import com.example.dogfinder.databinding.FragmentHomeBinding
import com.example.dogfinder.mvvmDatabase.DogViewModal
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 2
        private const val PERMISSION_ALL = 1
        private const val REQUEST_CHECK_SETTING = 1001
        const val TAG = "Dog HomeFagment"

        private val permissions: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var binding: FragmentHomeBinding

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var storage : FirebaseStorage
    private lateinit var storageReferance : StorageReference

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var viewModel : DogViewModal

    private lateinit var currentDateandTime: String

    private var id : Long? = null
    private var dog = Dog()

    private lateinit var myIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(2000)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        storage = FirebaseStorage.getInstance()
        storageReferance = storage.getReference()

        viewModel = ViewModelProvider(this).get(DogViewModal::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.takePhotobtn.setOnClickListener {
            val sdf = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss")
            currentDateandTime = sdf.format(Date())
            dog.date = currentDateandTime
            Log.d(TAG, "date is ${dog.date}")

            checkForPermissions()
        }

        binding.showDataBtn.setOnClickListener {

            if (dog.date != null && dog.location != null){
//                val id = viewModel.getDog(dog.date!!).id
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToDogDetailFragment(dog.date!!, dog.location!!)
                )
            }else{
                Log.d(TAG, "dog.date or location is null")
                Toast.makeText(requireContext(), "First take a picture", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun checkForPermissions() {
        if (!hasPermissions(requireContext(), permissions)) {
            Log.d(TAG, "does not have all permissions")
            requestPermissions(permissions, PERMISSION_ALL)
        }
        else{
            Log.d(TAG, "Have all permissions of camera and coarse location")
            if (!isGpsEnabled()) {  //checking if gps is on or not
                turnOnGpsAndDoWork()
            }else{
                doWork()
            }
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "OnrequestPermissionResult run")

        if (requestCode == PERMISSION_ALL) {
            if (grantResults.isNotEmpty()) {
                if ((checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)
                    && (checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED)) {
                    Log.d(TAG, "OnrequestPermissionResult -> granted permission")

                    if (!isGpsEnabled()) {
                        turnOnGpsAndDoWork()
                    }else{
                        doWork()
                    }
                }else{
                    Log.d(TAG, "OnrequestPermissionResult -> non granted permission")
                    Log.d(TAG, "else part in onRqstPermsnResult")
                    if (shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA) ||
                        shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                        val snackbar = Snackbar.make(
                            binding.container,
                            getString(R.string.permissionSnakbar),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        snackbar.setAction("OK") {
                            requestPermissions(
                                permissions,
                                PERMISSION_ALL
                            )
                        }
                        snackbar.show()
                    }else{
                        Toast.makeText(requireContext(), getString(R.string.permissionAllowGuidance), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Log.d(TAG, "else part in onRqstPermsnResult with toast")
                Toast.makeText(requireContext(), "Request to Permission was Denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "location and camera permission not granted")
        }
    }

    private fun turnOnGpsAndDoWork() {

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireContext().applicationContext)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener {

            try {
                val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
                Toast.makeText(requireContext(), "Gps is on", Toast.LENGTH_SHORT).show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvableApiException = e as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                requireActivity(),
                                REQUEST_CHECK_SETTING
                            )
                        } catch (e: IntentSender.SendIntentException) {

                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
        doWork()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "camera opening func.")

//        val sdf = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss")
//        currentDateandTime = sdf.format(Date())
//        dog.date = currentDateandTime
//        Log.d(TAG, "date is ${dog.date}")

        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            val pic: Bitmap = data!!.extras!!.get("data") as Bitmap
            Log.d(TAG, "pic taken successfully $pic")
            convertAndStore(pic)
        }
    }

    private fun convertAndStore(pic: Bitmap) {
        val imgUri : Uri? = bitmapToUri(pic)
//        val sdf = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss")
//        currentDateandTime = sdf.format(Date())
//        dog.date = currentDateandTime
        Log.d(TAG, "pic converted successfully $imgUri")

        val dogImageRef = storageReferance.child("images/$currentDateandTime.jpg")
        Log.d(TAG, "convert And Store ()")

        if (imgUri != null) {
            dogImageRef.putFile(imgUri)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "stored successful")

                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "store unsuccessful")
                }
        }
    }

    private fun bitmapToUri(pic: Bitmap): Uri? {
        val outputStream = ByteArrayOutputStream()
        pic.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, pic, "Title", null)
        Log.d(TAG, "path is $path") //coming null
        return Uri.parse(path)
    }

    private fun isGpsEnabled(): Boolean {
        Log.d(TAG, "checking gps")
        var locationManager: LocationManager? = null
        var isEnabled = false

        if (locationManager == null) {
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) //returns true is gps turned on
        //else false
        return isEnabled
    }

    fun doWork() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
        Log.d(TAG, "location and camera permission granted")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                LocationServices.getFusedLocationProviderClient(requireActivity())
                    .removeLocationUpdates(this)

                if (!p0.equals(null) && p0.locations.size > 0) {
                    val idx = p0.locations.size - 1
                    val latitude = p0.locations.get(idx).latitude
                    val longitude = p0.locations.get(idx).longitude

                    val pos = LatLong(latitude, longitude)
                    Log.d(TAG, "Logitude : ${longitude}, latitude : ${latitude}")
                    GlobalScope.launch {
                        fetchAddressFromLocation(pos)
//                        dog.location = address
//                        viewModel.insertDog(dog)
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun fetchAddressFromLocation(pos : LatLong){
        //since feching address takes some time, try send it to background service
        //we have not sent it to background yet

        Log.d(TAG, "fetchAddressFromLoc() called")
        Log.d(TAG, "Passed latLong is $pos")

        val addressResult = AddressResult()
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        //geocoder is the one which converts lati&longi into readable address and it returns list of addresses
        var locationAddress : List<Address> = emptyList()
        //Address is also a class (inside system)

        try{
            locationAddress = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
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
            dog.location = addressResult.data.toString()
            Log.d(TAG, "address u are standing is : ${dog.location} & ${dog.date}")
//            viewModel.insertDog(dog)
        }
        else{
            Log.d (TAG, "locationAdress is empty")
        }
    }
}