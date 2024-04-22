package com.example.runner.OTHER

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.runner.R
import com.example.runner.databinding.MarkerViewBinding
import com.example.runner.db.run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Locale

class CustomMarkerView(
    val runs:List<run>,
    c:Context,
    layoutId:Int
):MarkerView(c,layoutId) {

    private val tvDate: TextView
    private val tvAvgSpeed: TextView
    private val tvDistance: TextView
    private val tvTime: TextView
    private val tvCalories: TextView




    //to access an xml layout in a class we do like this
    init {

        // Find the TextViews by their respective IDs within the inflated layout
        tvDate = findViewById(R.id.tvDate)
        tvAvgSpeed = findViewById(R.id.tvAvgSpeed)
        tvDistance = findViewById(R.id.tvDistance)
        tvTime = findViewById(R.id.tvDuration) // Assuming tvDuration corresponds to tvTime
        tvCalories = findViewById(R.id.tvCaloriesBurned)


    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if (e==null){
            return
        }
        val curr=e.x.toInt()
        val run=runs[curr]

        val calendar= Calendar.getInstance().apply {
            timeInMillis=run.timestamp
        }

        val date= SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text=date.format(calendar.time)

        val avgSpeed="${run.avgSpeed}Km/h"
        tvAvgSpeed.text=avgSpeed

        val dist="${run.distanceMeters/1000f}km"
        tvDistance.text=dist

        tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val cal="${run.caloriesBurned}kcal"
        tvCalories.text=cal
    }
}