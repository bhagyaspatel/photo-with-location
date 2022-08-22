package com.example.dogfinder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogfinder.R
import com.example.dogfinder.dataClasses.Dog

class DogAdapter(private val list : MutableList<Dog>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    inner class DogViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val locaion = itemView.findViewById<TextView>(R.id.tv_location)
        val timeDate = itemView.findViewById<TextView>(R.id.tv_timedate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val itemLayout = LayoutInflater.from (parent.context)
            .inflate(R.layout.dog_item, parent, false)
        return DogViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val currentItem = list[position]
        holder.locaion.text = currentItem.location
        holder.timeDate.text = currentItem.date
    }

    override fun getItemCount(): Int {
        return list.size
    }
}