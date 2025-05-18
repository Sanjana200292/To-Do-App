package com.example.TODO.view.screens

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.TODO.R
import com.example.TODO.databinding.AlertdialogRedactCommentCaseBinding
import com.example.TODO.databinding.FragmentCasesBinding
import com.example.TODO.databinding.PartLoadingBinding
import com.example.TODO.model.Repositories
import com.example.TODO.model.cases.Case
import com.example.TODO.model.graph.Graph
import com.example.TODO.model.timer.TimerCase
import com.example.TODO.view.Consts.DAY_UNIX_MILLIS
import com.example.TODO.view.Consts.KEY_HABIT_ID
import com.example.TODO.view.adapters.CasesActionListener
import com.example.TODO.view.adapters.CasesAdapter
import com.example.TODO.view.adapters.ItemList
import com.example.TODO.view.adapters.delegates.CaseDelegate
import com.example.TODO.view.adapters.delegates.GraphDelegate
import com.example.TODO.view.adapters.delegates.TimerDelegate
import com.example.TODO.viewmodel.CasesViewModel
import com.example.TODO.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date
import java.util.LinkedHashMap

class CasesFragment : Fragment() {

    private lateinit var binding: FragmentCasesBinding
    private lateinit var viewModel: CasesViewModel
    private lateinit var adapter: CasesAdapter
    private lateinit var loadingBinding: PartLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CasesAdapter(
            listOf(
                GraphDelegate(requireContext()),
                TimerDelegate(requireContext()),
                CaseDelegate(requireContext())
            ), object : CasesActionListener {

                override fun deleteCase(case: Case) {
                    showDialogDeleteCase(case)
                }

                override fun redactComment(case: Case) {
                    showDialogRedactComment(case)
                }

            })
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(Repositories, this)
        )[CasesViewModel::class.java]
        viewModel.init(getCurrentHabitId())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCasesBinding.inflate(inflater, container, false)
        loadingBinding = PartLoadingBinding.bind(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiCasesState.collect { casesUiState ->

                    hideAll()

                    if (casesUiState.isError) {
                        showError()
                    } else if (casesUiState.isLoading) {
                        showLoading()
                    } else if (casesUiState.cases.isEmpty()) {
                        showEmpty()
                        adapter.data = emptyList()
                    } else {
                        showSuccess()

                        val mapCases: MutableMap<Date, Int> = LinkedHashMap()
                        for (case in casesUiState.cases.reversed()) {

                            val date = Date(case.date - case.date % DAY_UNIX_MILLIS)

                            var count = mapCases[date]

                            if (count == null) count = 0

                            mapCases[date] = count + 1
                        }

                        adapter.data = listOf<ItemList>(
                            Graph(mapCases),
                            TimerCase(casesUiState.cases.first().date)
                        ) + casesUiState.cases
                    }
                }
            }
        }

        binding.createCaseButton.setOnClickListener {
            launchAddCaseFragment()
        }

        return binding.root
    }

    private fun showEmpty() {
        loadingBinding.emptyContainer.visibility = View.VISIBLE
        binding.createCaseButton.visibility = View.VISIBLE
    }

    private fun hideAll() {
        binding.root.children.forEach { it.visibility = View.GONE }
    }

    private fun showSuccess() {
        binding.root.children
            .filter { it.id != R.id.errorContainer && it.id != R.id.progressBar && it.id != R.id.emptyContainer }
            .forEach { it.visibility = View.VISIBLE }
    }

    private fun showError() {
        loadingBinding.errorContainer.visibility = View.VISIBLE
    }

    private fun showLoading() {
        loadingBinding.progressBar.visibility = View.VISIBLE
    }

    private fun showDialogRedactComment(case: Case) {
        val dialogBinding = AlertdialogRedactCommentCaseBinding.inflate(layoutInflater)

        if (case.comment.isNotBlank()) dialogBinding.commentCaseEditText.setText(case.comment)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.redact_comment))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialogBinding.commentCaseEditText.requestFocus()

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                viewModel.updateComment(case, dialogBinding.commentCaseEditText.text.toString())
                dialog.dismiss()
            }

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDialogDeleteCase(case: Case) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sure_delete_case))
            .setPositiveButton(getString(R.string.delete), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                viewModel.deleteCase(case)
                dialog.dismiss()
            }

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun launchAddCaseFragment() {
        val fragment = AddCaseFragment.newInstance(getCurrentHabitId())

        parentFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    private fun getCurrentHabitId(): Long = requireArguments().getLong(KEY_HABIT_ID)

    companion object {
        fun newInstance(habitId: Long): CasesFragment {
            val args: Bundle = Bundle().apply {
                putLong(KEY_HABIT_ID, habitId)
            }
            val fragment = CasesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
