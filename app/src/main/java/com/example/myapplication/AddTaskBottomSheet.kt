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
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddTaskBottomSheet(private val onTaskAdded: (Task) -> Unit) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val newTaskTitle = view.findViewById<EditText>(R.id.bsdTaskTitle)
        val newTaskDescription = view.findViewById<EditText>(R.id.bsdTaskDescription)
        val newTaskDueDate = view.findViewById<TextView>(R.id.bsdTaskDueDate)

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
        view.findViewById<Button>(R.id.AddButton).setOnClickListener {
            val title = newTaskTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                onTaskAdded(Task(
                    title = title, description = newTaskDescription.text.toString().trim(), dueDate = newTaskDueDate.text.toString().trim()))
                dismiss()
            } else {
                newTaskTitle.error = "Title is required to add a new task. Please enter a title and try again."
            }
        }
    }
}