package com.example.myapplication

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.myapplication.data.Task
import com.example.myapplication.databinding.ItemTaskBinding

class TaskAdapter(
    private val onEditTaskClicked: (task: Task) -> Unit,
    private val onDeleteTaskClicked: (task: Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) = holder.bind(tasks[position])

    override fun getItemCount() = tasks.size

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.TaskTitle.text = task.title
            binding.TaskDescription.text = task.description
            binding.TaskDueDate.text = "Due ${task.dueDate}"
            binding.TaskPriority.text = "Priority: ${task.priority}"

            binding.EditTaskButton.setOnClickListener { onEditTaskClicked(task) }
            binding.DeleteTaskButton.setOnClickListener { onDeleteTaskClicked(task) }
        }
    }
}