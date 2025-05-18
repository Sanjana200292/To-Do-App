package com.example.TODO.view.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.TODO.databinding.FragmentAddCaseBinding
import com.example.TODO.model.Repositories
import com.example.TODO.model.cases.Case
import com.example.TODO.view.Consts.KEY_HABIT_ID
import com.example.TODO.viewmodel.AddCaseViewModel
import com.example.TODO.viewmodel.MainViewModelFactory
import com.example.TODO.AlarmReceiver
import java.util.Calendar
import java.util.GregorianCalendar

class AddCaseFragment: Fragment() {

    private lateinit var binding: FragmentAddCaseBinding
    private lateinit var viewModel: AddCaseViewModel
    private var pickedDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, MainViewModelFactory(Repositories, this))[AddCaseViewModel::class.java]

        // Initialize ViewModel with habitId passed via fragment arguments
        val habitId = requireArguments().getLong(KEY_HABIT_ID)
        viewModel.init(habitId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCaseBinding.inflate(inflater, container, false)

        val calendar = Calendar.getInstance()
        val currentHours = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(Calendar.MINUTE)

        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        pickedDate = calendar.timeInMillis

        if (currentMin in 0..9)
            binding.currentTime.text = "$currentHours:0$currentMin"
        else
            binding.currentTime.text = "$currentHours:$currentMin"

        with(binding) {
            cancelButton.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }
            calendarViem.setOnDateChangeListener { _, year, month, dayOfMonth ->
                pickedDate = GregorianCalendar(year, month, dayOfMonth).timeInMillis
            }
            changeTimeButton.setOnClickListener {
                openDialog(currentMin, currentHours)
            }
            saveButton.setOnClickListener {
                val comment = commentET.text.toString()
                if (comment.isEmpty()) {
                    Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val time = currentTime.text.toString().split(":")
                val date = pickedDate + time[0].toLong() * 60 * 60 * 1000 + time[1].toLong() * 60000

                viewModel.createCase(Case(
                    0,
                    comment,
                    date,
                    requireArguments().getLong(KEY_HABIT_ID)
                ))

                setAlarm(comment, date)
                activity?.supportFragmentManager?.popBackStack()
            }
        }

        return binding.root
    }

    private fun openDialog(min: Int, hours: Int) {
        val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            if (minute in 0..9)
                binding.currentTime.text = "$hourOfDay:0$minute"
            else
                binding.currentTime.text = "$hourOfDay:$minute"
        }, hours, min, true)
        timePickerDialog.show()
    }

    private fun setAlarm(message: String, timeInMillis: Long) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("message", message)
            putExtra("notificationId", 1)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check API level and handle canScheduleExactAlarms() only if API level is 31 or higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                // Notify the user or log that exact alarms are not allowed
                Toast.makeText(context, "Exact alarms are not allowed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // For API levels lower than 31, just schedule the alarm
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }


    companion object {
        fun newInstance(habitId: Long): AddCaseFragment {
            val args: Bundle = Bundle().apply {
                putLong(KEY_HABIT_ID, habitId)
            }
            val fragment = AddCaseFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
