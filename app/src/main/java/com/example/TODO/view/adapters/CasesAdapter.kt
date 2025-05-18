package com.example.TODO.view.adapters

import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.TODO.R
import com.example.TODO.databinding.ItemCaseBinding
import com.example.TODO.databinding.ItemGraphBinding
import com.example.TODO.databinding.ItemTimerBinding
import com.example.TODO.model.cases.Case

class CasesDiffCallback(
    private val oldList: List<ItemList>,
    private val newList: List<ItemList>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldList[oldItemPosition] is Case && newList[newItemPosition] is Case) {
            val oldCase = oldList[oldItemPosition] as Case
            val newCase = newList[newItemPosition] as Case
            oldCase.id == newCase.id
        } else true
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newList[newItemPosition] == oldList[oldItemPosition]

}

interface CasesActionListener {

    fun deleteCase(case: Case)

    fun redactComment(case: Case)

}

interface ItemList

interface Delegate {

    fun forItem(itemList: ItemList): Boolean

    fun getViewHolder(parent: ViewGroup, clickListener: OnClickListener): ViewHolder

    fun bindViewHolder(viewHolder: ViewHolder, item: ItemList)

}

class CasesAdapter(
    private val delegates: List<Delegate>,
    private val actionListener: CasesActionListener
) : RecyclerView.Adapter<ViewHolder>(), View.OnClickListener {

    var data: List<ItemList> = emptyList()
        set(newValue) {
            val diffCallback = CasesDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }


    class CaseViewHolder(val binding: ItemCaseBinding) : ViewHolder(binding.root)
    class TimerViewHolder(val binding: ItemTimerBinding) : ViewHolder(binding.root)
    class GraphViewHolder(val binding: ItemGraphBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        delegates[viewType].getViewHolder(parent, this)

    override fun getItemViewType(position: Int) =
        delegates.indexOfFirst { delegate -> delegate.forItem(data[position]) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        delegates[getItemViewType(position)].bindViewHolder(holder, data[position])
    }

    override fun getItemCount() = data.size

    override fun onClick(v: View) {

        if (v.id == R.id.moreCaseButton) {
            showPopupMenu(v)
        }

    }

    private fun showPopupMenu(v: View) {
        val context = v.context
        val popupMenu = PopupMenu(context, v)
        val case = v.tag as Case

        popupMenu.menu.add(0, ID_REDACT_COMMENT, Menu.NONE, context.getString(R.string.redact_comment))
        popupMenu.menu.add(0, ID_DELETE, Menu.NONE, context.getString(R.string.delete))

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_REDACT_COMMENT -> {
                    actionListener.redactComment(case)
                }

                ID_DELETE -> {
                    actionListener.deleteCase(case)
                }
            }
            return@setOnMenuItemClickListener true
        }

        popupMenu.show()
    }

    companion object {
        private const val ID_REDACT_COMMENT = 0
        private const val ID_DELETE = 2
    }

}