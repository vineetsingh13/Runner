package com.example.runner.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runner.OTHER.CustomMarkerView
import com.example.runner.OTHER.TrackingUtility
import com.example.runner.R
import com.example.runner.databinding.FragmentStatisticsBinding
import com.example.runner.ui.viewModels.MainViewModel
import com.example.runner.ui.viewModels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: FragmentStatisticsBinding

    private fun subscribeToObservers(){

        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {

            it?.let {
                val totalTime=TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text=totalTime
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km=it/1000f
                val total=round(km*10f)/10f
                val totalDist="${total} km"
                binding.tvTotalDistance.text=totalDist
            }
        })

        viewModel.totalCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                val cal="${it} kcal"
                binding.tvTotalCalories.text=cal
            }
        })

        viewModel.totalavgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avg= round(it*10f)/10f
                val st="${avg} km/h"
                binding.tvAverageSpeed.text=st
            }
        })

        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed=it.indices.map { i->BarEntry(i.toFloat(),it[i].avgSpeed) }

                val bardataset=BarDataSet(avgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor=Color.WHITE
                    color=ContextCompat.getColor(requireContext(),R.color.colorAccent)

                }

                binding.barChart.data= BarData(bardataset)
                binding.barChart.marker=CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                binding.barChart.invalidate()

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentStatisticsBinding.inflate((layoutInflater))


        return binding.root
    }

    private fun setupBarChart(){

        binding.barChart.xAxis.apply {
            position=XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor= Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisLeft.apply {
            axisLineColor=Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisRight.apply {
            axisLineColor=Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.apply {
            description.text="Avg Speed Over Time"
            legend.isEnabled=false
        }
    }
}