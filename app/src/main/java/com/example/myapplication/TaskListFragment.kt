package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.AddTaskBottomSheet
import com.example.myapplication.TaskAdapter
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.databinding.FragmentTaskListBinding
import kotlinx.coroutines.launch

class TaskListFragment : Fragment(R.layout.fragment_task_list) {
    private var _binding: FragmentTaskListBinding? = null
    private val binding
        get() = _binding!!

    private val taskDao by lazy {
        TaskDatabase.getDatabase(requireContext()).taskDao()
    }
    val taskAdapter = TaskAdapter { }

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

        binding.taskRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        binding.fabAddTask.setOnClickListener {
            addTask()
        }
        setUpObservers()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            taskDao.getAllTasks().collect { tasks ->
                taskAdapter.refreshData(tasks)
            }
        }
    }
    private fun addTask() {
        AddTaskBottomSheet { task ->
            lifecycleScope.launch {
                taskDao.insertTask(task)
            }
        }.show(parentFragmentManager, "AddTaskBottomSheet")
    }
}