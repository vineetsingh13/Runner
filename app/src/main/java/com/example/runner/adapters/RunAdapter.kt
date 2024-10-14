package com.example.runner.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
            Glide.with(this).load(run.img).into(holder.binding.tvRunImage)

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

        holder.itemView.setOnClickListener {
            val date=SimpleDateFormat("dd.MMM.yy", Locale.getDefault())
            val avgSpeed="${run.avgSpeed}\nKm/h"
            val dist="${run.distanceMeters/1000f}\nkm"
            val cal="${run.caloriesBurned}\nkcal"
            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }

            showDialog(holder.itemView.context,
                date.format(calendar.time),
                TrackingUtility.getFormattedStopWatchTime(run.timeInMillis),
                dist,
                cal,
                avgSpeed,
                run.img!!)
        }
    }

    private fun showDialog(context: Context, date: String, timer:String, dist:String, cal: String, speed:String, img:Bitmap) {

        val builder=AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_itemview_dialog, null)

        val dialogCancel = dialogView.findViewById<TextView>(R.id.cancelButton)
        val dialogDelete = dialogView.findViewById<Button>(R.id.deleteButton)
        val dialogShare = dialogView.findViewById<Button>(R.id.shareButton)
        val dialogDate = dialogView.findViewById<TextView>(R.id.dateField)
        val dialogTimer = dialogView.findViewById<TextView>(R.id.timerField)
        val dialogDistance = dialogView.findViewById<TextView>(R.id.distance_value)
        val dialogCal = dialogView.findViewById<TextView>(R.id.calories_value)
        val dialogSpeed = dialogView.findViewById<TextView>(R.id.speed_value)
        val dialogImg = dialogView.findViewById<ImageView>(R.id.mapImage)

        dialogImg.setImageBitmap(img)
        dialogDate.text=date
        dialogTimer.text=timer
        dialogDistance.text=dist
        dialogCal.text=cal
        dialogSpeed.text=speed

        val dialog=builder
            .setView(dialogView)
            .create()


        dialog.show()
        dialogCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}