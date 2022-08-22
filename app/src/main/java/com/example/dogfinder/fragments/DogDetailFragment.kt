package com.example.dogfinder.fragments

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dogfinder.R
import com.example.dogfinder.dataClasses.Dog
import com.example.dogfinder.databinding.FragmentDogDetailBinding
import com.example.dogfinder.mvvmDatabase.DogViewModal
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_dog_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class DogDetailFragment : Fragment() {

    private lateinit var viewModel : DogViewModal
    private lateinit var  storageReference: StorageReference
    private lateinit var binding: FragmentDogDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DogViewModal::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = DogDetailFragmentArgs.fromBundle(requireArguments()).date
        val location = DogDetailFragmentArgs.fromBundle(requireArguments()).location
        Log.d("Detail", "Dog date ${date} and location ${location}")

//        lifecycleScope.launch(Dispatchers.IO){
//            val dog : Dog = viewModel.getDog(date)
////            Log.d("Detail", "Dog loc and date ${dog.location}")
            binding.tvLocation.text  = location
            binding.tvTimedate.text = date

        val imgid = date
        getImage(imgid)
//        }
    }

    private fun getImage(imgid : String) {
        storageReference = FirebaseStorage.getInstance().getReference("images/"+imgid+".jpg")
//        try{
//            val localFile : File = File.createTempFile("tempFile", ".jpg")
//            storageReference.getFile(localFile) //storing fetched img into local file
//                .addOnSuccessListener {
//                    val bitmapImg = BitmapFactory.decodeFile(localFile.absolutePath)
//                    binding.dogpicDetail.setImageBitmap(bitmapImg)
//                }
//                .addOnFailureListener{
//                    Toast.makeText(requireContext(), "Failed to retrieve file", Toast.LENGTH_SHORT).show()
//                }
//        }catch (e : IOException){
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
//        }

//        val storageRef = Firebase.storage.reference
        Glide.with(this).load(storageReference).into(binding.dogpicDetail)
    }
}