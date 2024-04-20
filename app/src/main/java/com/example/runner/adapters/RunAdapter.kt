package com.example.runner.adapters

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runner.OTHER.TrackingUtility
import com.example.runner.R
import com.example.runner.databinding.ItemRunBinding
import com.example.runner.db.run
import java.text.SimpleDateFormat
import java.util.Locale

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(val binding: ItemRunBinding): RecyclerView.ViewHolder(binding.root)

    val diffCallback=object: DiffUtil.ItemCallback<run>() {
        override fun areItemsTheSame(oldItem: run, newItem: run): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: run, newItem: run): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }

    }

    val differ=AsyncListDiffer(this,diffCallback)

    fun submitList(list: List<run>)=differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding=ItemRunBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return RunViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run=differ.currentList[position]


        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.binding.ivRunImage)

            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }

            val date=SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            holder.binding.tvDate.text=date.format(calendar.time)

            val avgSpeed="${run.avgSpeed}Km/h"
            holder.binding.tvAvgSpeed.text=avgSpeed

            val dist="${run.distanceMeters/1000f}km"
            holder.binding.tvDistance.text=dist

            holder.binding.tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val cal="${run.caloriesBurned}kcal"
            holder.binding.tvCalories.text=cal
        }
    }
}