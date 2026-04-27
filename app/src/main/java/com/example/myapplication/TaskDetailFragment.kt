package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Task
import com.example.myapplication.data.TaskDatabase
import com.example.myapplication.databinding.FragmentTaskDetailBinding
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val taskDao by lazy { TaskDatabase.getDatabase(requireContext()).taskDao() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        setUpObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt(BUNDLE_ID) ?: return

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            lifecycleScope.launch {
                taskDao.getTaskById(id)?.let { task ->
                    AddTaskBottomSheet(existingTask = task) { updatedTask ->
                        lifecycleScope.launch {
                            taskDao.updateTask(updatedTask)
                        }
                    }.show(parentFragmentManager, "EditTaskBottomSheet")
                }
            }
        }
    }

    private fun setUpObservers() {
        val id = arguments?.getInt(BUNDLE_ID) ?: return
        lifecycleScope.launch {
            taskDao.getTaskById(id)?.let { task -> bindTask(task) }
        }
    }

    private fun bindTask(task: Task) {
        binding.tvDetailTitle.text = task.title
        binding.tvDetailPriority.text = "Priority: ${task.priority}"
        binding.tvDetailDueDate.text = "Due Date: ${task.dueDate}"
        binding.tvDetailTags.text = "Tags: ${task.tags.ifBlank { "None" }}"
        binding.tvDetailDescription.text = "Description: ${task.description.ifBlank { "" }}"
    }

    companion object {
        private const val BUNDLE_ID = "task_id"

        fun newInstance(id: Int) = TaskDetailFragment().apply {
            arguments = bundleOf(BUNDLE_ID to id)
        }
    }
}