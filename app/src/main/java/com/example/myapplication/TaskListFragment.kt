package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.AddTaskBottomSheet
import com.example.myapplication.TaskAdapter
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.databinding.FragmentTaskListBinding
import kotlinx.coroutines.launch
import android.graphics.Color
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.Calendar
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class TaskListFragment : Fragment(R.layout.fragment_task_list) {
    private var _binding: FragmentTaskListBinding? = null
    private val binding
        get() = _binding!!

    private var allTasks: List<Task> = emptyList()

    private var selectedDate: CalendarDay? = null

    private val taskDao by lazy {
        TaskDatabase.getDatabase(requireContext()).taskDao()
    }
    val taskAdapter = TaskAdapter(
        onTaskClicked = { task ->
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, TaskDetailFragment.newInstance(task.id))
                addToBackStack(null)
            }
        },
        onEditTaskClicked = { task -> editTask(task) },
        onDeleteTaskClicked = { task -> deleteTask(task) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("DEBUG", "savedInstanceState = $savedInstanceState")
        android.util.Log.d("DEBUG", "selectedDate field = $selectedDate")

        binding.taskRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        binding.calendarView.clearSelection()

        if (savedInstanceState != null &&
            savedInstanceState.containsKey("saved_year")) {

            val year = savedInstanceState.getInt("saved_year")
            val month = savedInstanceState.getInt("saved_month")
            val day = savedInstanceState.getInt("saved_day")

            val restoredDate = CalendarDay.from(year, month, day)

            selectedDate = restoredDate
            binding.calendarView.setSelectedDate(restoredDate)
            filterTasksByDate(restoredDate)

        } else {
            val today = CalendarDay.today()
            selectedDate = today
            binding.calendarView.setSelectedDate(today)
            filterTasksByDate(today)
        }

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = date
            filterTasksByDate(date)
        }

        binding.fabAddTask.setOnClickListener {
            addTask()
        }

        setUpObservers()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        selectedDate?.let {
            outState.putInt("saved_year", it.year)
            outState.putInt("saved_month", it.month)
            outState.putInt("saved_day", it.day)
        }
    }

    private fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                taskDao.getAllTasks().collect { tasks ->
                    android.util.Log.d("DEBUG", "observer fired, selectedDate = $selectedDate")
                    android.util.Log.d("DEBUG", "observer fired, calendar.selectedDate = ${binding.calendarView.selectedDate}")
                    _binding?.let { currentBinding ->
                        allTasks = tasks

                        updateCalendarDecorators(tasks)

                        val currentSelectedDate = selectedDate ?: CalendarDay.today()
                        filterTasksByDate(currentSelectedDate)
                    }
                }
            }
        }
    }

    private fun updateCalendarDecorators(tasks: List<Task>) {
        val highPriorityDates = hashSetOf<CalendarDay>()
        val mediumPriorityDates = hashSetOf<CalendarDay>()
        val lowPriorityDates = hashSetOf<CalendarDay>()
        val completedDay = hashSetOf<CalendarDay>()
        val groupedByDate = tasks.groupBy { it.dueDate }

        groupedByDate.forEach { (dateStr, tasksOnDate) ->
            val calendarDay = parseDate(dateStr) ?: return@forEach

            if (tasksOnDate.isNotEmpty() && tasksOnDate.all { it.isCompleted }) {
                completedDay.add(calendarDay)
            }

            val topPriority = tasksOnDate.maxByOrNull {
                when(it.priority) {
                    "High" -> 3
                    "Medium" -> 2
                    else -> 1
                }
            }?.priority

            when(topPriority) {
                "High" -> highPriorityDates.add(calendarDay)
                "Medium" -> mediumPriorityDates.add(calendarDay)
                "Low" -> lowPriorityDates.add(calendarDay)
            }
        }

        binding.calendarView.removeDecorators()
        binding.calendarView.addDecorators(
            PriorityDecorator(Color.RED, highPriorityDates),  // High = Red
            PriorityDecorator(Color.YELLOW, mediumPriorityDates),  // Medium = Yellow
            PriorityDecorator(Color.BLUE, lowPriorityDates),  // Low = Blue
            PriorityDecorator(Color.GREEN, completedDay)  // All tasks completed for the day = Green
        )
        binding.calendarView.invalidateDecorators()
    }

    private fun filterTasksByDate(date: CalendarDay) {
        val selectedDateStr = "${date.month}/${date.day}/${date.year}"

        //android.util.Log.d("CALENDAR_DEBUG", "now searching for: '$selectedDateStr'")

        val filteredAndSorted = allTasks
            .filter { it.dueDate == selectedDateStr }
            .sortedBy { task ->
                when (task.priority) {
                    "High" -> 1
                    "Medium" -> 2
                    "Low" -> 3
                    else -> 4
                }
            }

        taskAdapter.refreshData(filteredAndSorted)
    }

    private fun parseDate(dateString: String): CalendarDay? {
        return try {
            val parts = dateString.split("/")
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()

            CalendarDay.from(year, month, day)
        } catch (e: Exception) {
            //android.util.Log.e("DECORATOR_DEBUG", "Failed to parse date: $dateString")
            null
        }
    }
    private fun addTask() {
        AddTaskBottomSheet { task ->
            lifecycleScope.launch {
                taskDao.insertTask(task)
            }
        }.show(parentFragmentManager, "AddTaskBottomSheet")
    }

    private fun editTask(task: Task) {
        AddTaskBottomSheet(existingTask = task) { updatedTask ->
            lifecycleScope.launch {
                taskDao.updateTask(updatedTask)
            }
        }.show(parentFragmentManager, "EditTaskBottomSheet")
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            taskDao.deleteTask(task)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PriorityDecorator(private val color: Int, private val dates: HashSet<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(15f, color))
    }
}