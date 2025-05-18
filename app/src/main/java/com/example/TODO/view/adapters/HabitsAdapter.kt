package com.example.TODO.view.adapters

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.TODO.R
import com.example.TODO.databinding.ItemHabitBinding
import com.example.TODO.model.habits.Habit


class HabitsDiffCallback(
    private val oldList: List<Habit>,
    private val newList: List<Habit>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

}

interface HabitActionListener {

    fun deleteHabit(habit: Habit)

    fun renameHabit(habit: Habit)

    fun pickHabit(habit: Habit)

}

class HabitsAdapter(
    private val actionListener: HabitActionListener
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>(), View.OnClickListener {

    var data: List<Habit> = emptyList()
        set(newValue) {
            val diffCallback = HabitsDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }


    class HabitViewHolder(val binding: ItemHabitBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHabitBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.moreButton.setOnClickListener(this)

        return HabitViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = data[position]
        holder.itemView.tag = habit
        holder.binding.moreButton.tag = habit
        holder.binding.nameTextView.text = habit.name
    }

    override fun onClick(v: View) {
        val habit = v.tag as Habit

        when (v.id) {
            R.id.moreButton -> {
                showPopupMenu(v)
            }

            else -> {
                actionListener.pickHabit(habit)
            }
        }
    }

    private fun showPopupMenu(v: View) {
        val context = v.context
        val popupMenu = PopupMenu(context, v)
        val habit = v.tag as Habit

        popupMenu.menu.add(0, ID_DELETE, Menu.NONE, context.getString(R.string.delete))
        popupMenu.menu.add(0, ID_RENAME, Menu.NONE, context.getString(R.string.rename))

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_DELETE -> {
                    actionListener.deleteHabit(habit)
                }
                ID_RENAME -> {
                    actionListener.renameHabit(habit)
                }
            }
            return@setOnMenuItemClickListener true
        }

        popupMenu.show()
    }


    companion object {
        private const val ID_RENAME = 0
        private const val ID_DELETE = 1
    }
}