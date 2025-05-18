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
import com.example.TODO.databinding.AlertdialogAddRenameHabitBinding
import com.example.TODO.databinding.FragmentHabitsBinding
import com.example.TODO.databinding.PartLoadingBinding
import com.example.TODO.model.Repositories
import com.example.TODO.model.habits.Habit
import com.example.TODO.view.adapters.HabitActionListener
import com.example.TODO.view.adapters.HabitsAdapter
import com.example.TODO.viewmodel.HabitViewModel
import com.example.TODO.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {

    private lateinit var binding: FragmentHabitsBinding
    private lateinit var adapter: HabitsAdapter
    private lateinit var viewModel: HabitViewModel
    private lateinit var loadingBinding: PartLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = HabitsAdapter(object : HabitActionListener {
            override fun deleteHabit(habit: Habit) = showDeleteDialog(habit)
            override fun renameHabit(habit: Habit) = showNameDialog(habit)
            override fun pickHabit(habit: Habit) = launchCaseFragment(habit)
        })

        // ViewModel setup using ViewModelFactory
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(Repositories, this)
        )[HabitViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHabitsBinding.inflate(inflater, container, false)
        loadingBinding = PartLoadingBinding.bind(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Collecting ViewModel state using lifecycle-aware collection
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiHabitsState.collect { habitsUiState ->
                    hideAll()
                    when {
                        habitsUiState.isError -> showError()
                        habitsUiState.isLoading -> showLoading()
                        habitsUiState.habits.isEmpty() -> {
                            showEmpty()
                            adapter.data = emptyList()
                        }
                        else -> {
                            showSuccess()
                            adapter.data = habitsUiState.habits
                        }
                    }
                }
            }
        }

        binding.createHabitButton.setOnClickListener {
            showNameDialog(null)
        }

        loadingBinding.againButton.setOnClickListener {
            viewModel.updateHabits()
        }

        return binding.root
    }

    private fun hideAll() {
        binding.root.children.forEach { it.visibility = View.GONE }
    }

    private fun showEmpty() {
        loadingBinding.emptyContainer.visibility = View.VISIBLE
        binding.createHabitButton.visibility = View.VISIBLE
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

    private fun showNameDialog(habit: Habit?) {
        val dialogBinding = AlertdialogAddRenameHabitBinding.inflate(layoutInflater)

        if (habit != null) dialogBinding.nameHabitEditText.setText(habit.name)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.enter_name))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialogBinding.nameHabitEditText.requestFocus()

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val enteredText = dialogBinding.nameHabitEditText.text.toString()
                if (enteredText.isBlank()) {
                    dialogBinding.nameHabitEditText.error = getString(R.string.empty)
                    return@setOnClickListener
                }

                if (habit != null) viewModel.renameHabit(habit.id, enteredText)
                else viewModel.addHabit(enteredText)

                dialog.dismiss()
            }

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDeleteDialog(habit: Habit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sure_delete_habit))
            .setPositiveButton(getString(R.string.delete), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                viewModel.deleteHabit(habit)
                dialog.dismiss()
            }

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun launchCaseFragment(habit: Habit) {
        val fragment = CasesFragment.newInstance(habit.id)
        parentFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
}
