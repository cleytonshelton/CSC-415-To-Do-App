package com.example.myapplication

import com.example.myapplication.data.Task
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        adapter = TaskAdapter(this, tasks)
        findViewById<ListView>(R.id.taskRecyclerView).adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddTask).setOnClickListener {
            AddTaskBottomSheet { task ->
                tasks.add(task)
                adapter.notifyDataSetChanged()
            }.show(supportFragmentManager, "AddTaskBottomSheet")
        }
    }
}
