package com.example.TODO.view.adapters.delegates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.TODO.R
import com.example.TODO.databinding.ItemGraphBinding
import com.example.TODO.model.graph.Graph
import com.example.TODO.view.adapters.CasesAdapter
import com.example.TODO.view.adapters.Delegate
import com.example.TODO.view.adapters.ItemList
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat


class GraphDelegate(private val context: Context) : Delegate {

    override fun forItem(itemList: ItemList) = itemList is Graph

    override fun getViewHolder(parent: ViewGroup, clickListener: View.OnClickListener): RecyclerView.ViewHolder = CasesAdapter.GraphViewHolder(
        ItemGraphBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun bindViewHolder(viewHolder: RecyclerView.ViewHolder, item: ItemList) {
        (viewHolder as CasesAdapter.GraphViewHolder).let { graphVH ->
            val graph = item as Graph
            graphVH.itemView.tag = graph

            val monthsMap: MutableMap<String, Int> = LinkedHashMap()
            val labelsMonth = mutableListOf<String>()
            val dataBarMonths = mutableListOf<BarEntry>()

            val dataBarDays = mutableListOf<BarEntry>()
            val labelsDays = mutableListOf<String>()
            var counter = 0f
            val sdfDays = SimpleDateFormat("dd.MM.yy")
            val sdfMonths = SimpleDateFormat("MMM yy")

            graph.data.forEach { (date, y) ->
                dataBarDays.add(BarEntry(counter, y.toFloat()))
                labelsDays.add(sdfDays.format(date))

                val month = sdfMonths.format(date)
                var value = monthsMap[month]

                if (value == null) value = 0

                monthsMap[month] = value + y

                counter++
            }

            counter = 0f

            monthsMap.forEach { (date, y) ->
                dataBarMonths.add(BarEntry(counter, y.toFloat()))
                labelsMonth.add(date)
                counter++
            }


            val barDaysDataSet = BarDataSet(dataBarDays, context.getString(R.string.relapse))
            barDaysDataSet.color = context.getColor(R.color.white)

            val barMonthsDataSet = BarDataSet(dataBarMonths, context.getString(R.string.relapse))
            barMonthsDataSet.color = context.getColor(R.color.white)


            with(graphVH.binding) {

                with(this.graph) {
                    data = BarData(barDaysDataSet)
                    data.isHighlightEnabled = false
                    setVisibleXRangeMaximum(7f)
                    setVisibleXRangeMinimum(5f)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setMaxVisibleValueCount(0)
                    setPinchZoom(false)
                    isDoubleTapToZoomEnabled = false
                    setScaleEnabled(false)
                    setDrawBarShadow(false)
                    setDrawValueAboveBar(false)
                    setDrawGridBackground(false)
                    moveViewToX(dataBarDays.size - 7f)

                    xAxis.valueFormatter = IndexAxisValueFormatter(labelsDays)
                    xAxis.setLabelCount(dataBarDays.size, true)
                    xAxis.labelCount = dataBarDays.size
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    xAxis.textColor = context.getColor(R.color.white)

                    axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    axisLeft.spaceTop = 0f
                    axisLeft.axisMinimum = 0f
                    axisLeft.textColor = context.getColor(R.color.white)
                    axisLeft.zeroLineColor = context.getColor(R.color.white)
                    axisLeft.axisLineColor = context.getColor(R.color.white)
                    axisLeft.gridColor = context.getColor(R.color.white)
                    axisLeft.granularity = 1f

                    axisRight.isEnabled = false

                    invalidate()
                }

               toggleButton.setOnCheckedChangeListener { _, isMonths ->
                    if (isMonths) {
                        this.graph.data = BarData(barMonthsDataSet)
                        this.graph.data.isHighlightEnabled = false
                        this.graph.xAxis.valueFormatter = IndexAxisValueFormatter(labelsMonth)
                        this.graph.xAxis.labelCount = dataBarMonths.size
                        this.graph.setVisibleXRangeMaximum(7f)
                        this.graph.setVisibleXRangeMinimum(7f)
                        this.graph.moveViewToX(dataBarMonths.size - 7f)
                        this.graph.xAxis.textColor = context.getColor(R.color.white)
                        this.graph.axisLeft.textColor = context.getColor(R.color.white)
                        this.graph.axisLeft.zeroLineColor = context.getColor(R.color.white)
                        this.graph.axisLeft.axisLineColor = context.getColor(R.color.white)
                        this.graph.axisLeft.gridColor = context.getColor(R.color.white)
                        this.graph.invalidate()
                    }
                    else {
                        this.graph.data = BarData(barDaysDataSet)
                        this.graph.data.isHighlightEnabled = false
                        this.graph.xAxis.valueFormatter = IndexAxisValueFormatter(labelsDays)
                        this.graph.xAxis.labelCount = dataBarDays.size
                        this.graph.setVisibleXRangeMinimum(5f)
                        this.graph.setVisibleXRangeMaximum(7f)
                        this.graph.moveViewToX(dataBarDays.size - 7f)
                        this.graph.xAxis.textColor = context.getColor(R.color.white)
                        this.graph.axisLeft.textColor = context.getColor(R.color.white)
                        this.graph.axisLeft.zeroLineColor = context.getColor(R.color.white)
                        this.graph.axisLeft.axisLineColor = context.getColor(R.color.white)
                        this.graph.axisLeft.gridColor = context.getColor(R.color.white)
                        this.graph.invalidate()
                    }
               }
            }
        }
    }

}