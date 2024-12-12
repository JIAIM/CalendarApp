package com.example.calendrapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var monthText: TextView
    private lateinit var monthNext: ImageView
    private lateinit var monthPrevious: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var calendarCellText: TextView

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        monthText = findViewById(R.id.calendar_month_text)
        monthNext = findViewById(R.id.calendar_month_next)
        monthPrevious = findViewById(R.id.calendar_month_previous)
        recyclerView = findViewById(R.id.recycler_view)

        recyclerView.layoutManager = GridLayoutManager(this, 7) // 7 столбцов (дни недели)

        monthNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        monthPrevious.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        updateCalendar()
    }

    private fun updateCalendar() {

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthText.text = monthFormat.format(calendar.time)

        val days = generateDaysForMonth(calendar)

        val calendarAdapter = CalendarAdapter(days) { day ->
            Toast.makeText(this, "Вы выбрали: $day", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = calendarAdapter
    }

    private fun generateDaysForMonth(calendar: Calendar): List<String> {
        val days = mutableListOf<String>()

        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)

        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        repeat(offset) { days.add("") }

        val maxDays = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDays) {
            days.add(day.toString())
        }

        while (days.size % 7 != 0) {
            days.add("")
        }

        return days
    }
}
