package com.tory.demo.rv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RvAdapter()
        recyclerView.adapter = adapter

        val data = mutableListOf<Any>()

        val list = (0 until 100).map { TextModel(it, "text $it") }
        val listModel = ListModel("title recyclerView", list)
        data.add(TextModel(0, "ttt 1"))
        data.add(TextModel(1, "ttt 2"))
        data.add(TextModel(2, "ttt 3"))
        data.add(TextModel(3, "ttt 4"))
        data.add(listModel)

        adapter.setData(data)
    }
}
