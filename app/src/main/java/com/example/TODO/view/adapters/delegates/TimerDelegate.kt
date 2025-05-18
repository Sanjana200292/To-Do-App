package com.example.TODO.view.adapters.delegates

import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.TODO.databinding.ItemTimerBinding
import com.example.TODO.model.timer.TimerCase
import com.example.TODO.view.Consts.DAY_UNIX_MILLIS
import com.example.TODO.view.adapters.CasesAdapter
import com.example.TODO.view.adapters.Delegate
import com.example.TODO.view.adapters.ItemList

class TimerDelegate(context: Context) : Delegate {

    override fun forItem(itemList: ItemList) = itemList is TimerCase

    override fun getViewHolder(parent: ViewGroup, clickListener: View.OnClickListener) =
        CasesAdapter.TimerViewHolder(
            ItemTimerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun bindViewHolder(viewHolder: RecyclerView.ViewHolder, item: ItemList) {
        (viewHolder as CasesAdapter.TimerViewHolder).let { timerViewHolder ->

            val timerCase = item as TimerCase
            timerViewHolder.itemView.tag = item

            with(timerViewHolder.binding) {

                days.text = ""
                timer.base = timerCase.dateStart - System.currentTimeMillis() + SystemClock.elapsedRealtime()
                timer.start()

                timer.setOnChronometerTickListener {

                    val valueChronometer: Long = SystemClock.elapsedRealtime() - timer.base

                    if (valueChronometer > DAY_UNIX_MILLIS) {

                        if (days.text.isNotBlank()) {
                            val daysLong = days.text.toString().replace(":", "").toLong()
                            days.text = "${(daysLong + 1).toString()}:"
                        } else days.text = "1:"

                        this.timer.base += DAY_UNIX_MILLIS
                    }
                }
            }
        }
    }
}