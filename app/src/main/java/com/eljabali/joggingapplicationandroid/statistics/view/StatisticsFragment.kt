package com.eljabali.joggingapplicationandroid.statistics.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eljabali.joggingapplicationandroid.databinding.FragmentStatisticsBinding
import com.eljabali.joggingapplicationandroid.home.HomeActivity
import com.eljabali.joggingapplicationandroid.services.ForegroundService
import com.eljabali.joggingapplicationandroid.statistics.viewmodel.StatisticsViewModel
import com.eljabali.joggingapplicationandroid.util.TAG
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel


class StatisticsFragment : Fragment() {

    companion object {
        private const val SHOULD_STOP_SERVICE_KEY = "SHOULD_STOP_SERVICE_KEY"

        fun newInstance(shouldStopService: Boolean) = StatisticsFragment().apply {
            arguments = Bundle().apply {
                putBoolean(SHOULD_STOP_SERVICE_KEY, shouldStopService)
            }
        }

        private const val MAX_X_VALUE = 7
        private const val MAX_Y_VALUE = 30
        private const val MIN_Y_VALUE = 5
        private const val SET_LABEL = "Distance (Miles)"
        private val DAYS = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    }

    private val statisticsViewModel: StatisticsViewModel by viewModel()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentStatisticsBinding
    private var shouldStopService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldStopService = arguments?.getBoolean(SHOULD_STOP_SERVICE_KEY) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
        statisticsViewModel.onFragmentLaunch()
        configureBarChartAppearance()
        setDataForBarChart()
        monitorStatisticsViewState()
        setClickListeners()
        if (shouldStopService) {
            val homeActivity = activity as HomeActivity
            homeActivity.stopService(Intent(requireContext(), ForegroundService::class.java))
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    private fun monitorStatisticsViewState() {
        statisticsViewModel.observableStatisticsViewState
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { statisticsViewState -> setNewViewState(statisticsViewState) },
                { error -> Log.e(TAG, error.localizedMessage, error) }
            )
            .addTo(compositeDisposable)
    }

    private fun configureBarChartAppearance() {
        with(binding) {
            weeklyStatsBarChart.setDrawValueAboveBar(false)
            weeklyStatsBarChart.description.isEnabled = false
            weeklyStatsBarChart.legend.apply {
                textSize = 12f
                textColor = Color.WHITE
            }
            weeklyStatsBarChart.xAxis.apply {
                setAvoidFirstLastClipping(true)
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return DAYS[value.toInt()]
                    }
                }
            }
            weeklyStatsBarChart.axisLeft.textColor = Color.WHITE
            weeklyStatsBarChart.axisRight.textColor = Color.WHITE
        }
    }


    private fun setClickListeners() {
        with(binding) {
            startStopFloatingActionButton.setOnClickListener {
                activity?.startService(Intent(requireContext(), ForegroundService::class.java))
            }
        }
    }

    private fun setNewViewState(statisticsViewState: StatisticsViewState) {
        with(binding) {
            todayRunTextView.text = statisticsViewState.todayLastJogDistance
            todayTextView.text = statisticsViewState.dateToday
            thisWeeksRunsTextView.text =
                statisticsViewState.weeklyStats.weeklyTotalStats.totalRuns
            thisWeeksMilesTextView.text =
                statisticsViewState.weeklyStats.weeklyTotalStats.totalDistance
            thisWeeksTimeTextView.text =
                statisticsViewState.weeklyStats.weeklyTotalStats.totalTime
            averageWeeklyRunTextView.text =
                statisticsViewState.weeklyStats.weeklyAverageStats.averageRuns
            averageWeeklyMilesTextView.text =
                statisticsViewState.weeklyStats.weeklyAverageStats.averageDistance
            averageWeeklyTimeTextView.text =
                statisticsViewState.weeklyStats.weeklyAverageStats.averageTime
        }
        setDataForBarChart()
    }

    private fun setDataForBarChart() {
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, 30f))
        entries.add(BarEntry(1f, 80f))
        entries.add(BarEntry(2f, 60f))
        entries.add(BarEntry(3f, 50f))
        entries.add(BarEntry(4f, 70f))
        entries.add(BarEntry(5f, 60f))
        entries.add(BarEntry(6f, 60f))
        val barDataSet = BarDataSet(entries, "Distance(Miles)").apply {
            color = Color.WHITE
        }
        val barData = BarData(barDataSet).apply {
            setValueTextColor(Color.BLUE)
            setValueTextSize(12f)
            barWidth = 0.8f
        }
        binding.weeklyStatsBarChart.data = barData
        binding.weeklyStatsBarChart.setFitBars(true)
        binding.weeklyStatsBarChart.invalidate()
    }
}