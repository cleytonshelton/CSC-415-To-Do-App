package com.example.myapplication

import android.icu.util.Calendar
import com.example.myapplication.data.Task
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.app.DatePickerDialog
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTaskBottomSheet(
    private val existingTask: Task? = null,
    private val onTaskAdded: (Task) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val newTaskTitle = view.findViewById<EditText>(R.id.bsdTaskTitle)
        val newTaskDescription = view.findViewById<EditText>(R.id.bsdTaskDescription)
        val newTaskDueDate = view.findViewById<TextView>(R.id.bsdTaskDueDate)
        val newPriority = view.findViewById<TextView>(R.id.bsdPrioritySelection)
        val addTaskButton = view.findViewById<Button>(R.id.AddButton)
        var defaultPriority = "Medium"
        val newTags = view.findViewById<EditText>(R.id.bsdTagInput)

        existingTask?.let {
            newTaskTitle.setText(it.title)
            newTaskDescription.setText(it.description)
            newTaskDueDate.text = it.dueDate
            newPriority.text = it.priority
            defaultPriority = it.priority
            newTags.setText(it.tags)
            addTaskButton.text = "Save New Changes"
        }

        newPriority.setOnClickListener {
            val popup = PopupMenu(requireContext(), newPriority)
            popup.menu.add("Low")
            popup.menu.add("Medium")
            popup.menu.add("High")
            popup.setOnMenuItemClickListener { item ->
                defaultPriority = item.title.toString()
                newPriority.text = "Priority: $defaultPriority"
                true
            }
            popup.show()
        }

        newTaskDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    newTaskDueDate.text = "${month + 1}/$dayOfMonth/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        addTaskButton.setOnClickListener {
            val title = newTaskTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                onTaskAdded(Task(
                    id = existingTask?.id ?: 0,title = title, description = newTaskDescription.text.toString().trim(), dueDate = newTaskDueDate.text.toString().trim(), priority = defaultPriority, tags = newTags.text.toString().trim()))
                dismiss()
            } else {
                newTaskTitle.error = "Title is required to add a new task. Please enter a title and try again."
            }
        }
    }
}