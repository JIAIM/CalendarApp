package com.example.calendrapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CalendarAdapter(
    private val days: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private val today: String = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position == selectedPosition)
    }

    override fun getItemCount(): Int = days.size

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.day_text)
        private val cell: LinearLayout = itemView.findViewById(R.id.cell_day)

        fun bind(day: String, isSelected: Boolean) {
            dayText.text = day

            if (day.isEmpty()) {
                cell.setBackgroundColor(itemView.context.getColor(R.color.no_day))
            } else {
                val backgroundColor = when {
                    isSelected -> itemView.context.getColor(R.color.green_day)
                    day == today -> itemView.context.getColor(R.color.orange_day)
                    else -> itemView.context.getColor(R.color.basic_day)
                }
                cell.setBackgroundColor(backgroundColor)
            }

            itemView.setOnClickListener {
                if (day.isNotEmpty()) {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition

                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)

                    onItemClick(day)
                }
            }
        }
    }
}


