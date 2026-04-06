package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.data.Task
class TaskAdapter(context: Context, tasks: MutableList<Task>): ArrayAdapter<Task>(context, R.layout.item_task, tasks){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val task = getItem(position)!!
        view.findViewById<TextView>(R.id.TaskTitle).text = task.title
        view.findViewById<TextView>(R.id.TaskDescription).text = task.description
        view.findViewById<TextView>(R.id.TaskDueDate).text = "Due: ${task.dueDate}"
        return view
    }
}