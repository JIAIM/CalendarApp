package com.example.calendrapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast


import androidx.recyclerview.widget.RecyclerView


class EventAdapter(private val events: MutableList<Event>, private val context: CalendarActivity) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_title)
        val time: TextView = itemView.findViewById(R.id.event_time)
        val context: TextView = itemView.findViewById(R.id.event_context)
        val editButton: ImageView = itemView.findViewById(R.id.edit_event)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val dbHelper = DBHelper(holder.itemView.context, null)
        val currentEvent = events[position]
        holder.title.text = currentEvent.title
        holder.time.text = currentEvent.time
        holder.context.text = currentEvent.context

        holder.editButton.setOnClickListener {
            val event = events[position]
            context.showEditEventDialog(event)
        }

        holder.deleteButton.setOnClickListener {
            val sharedPreferences = holder.itemView.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val loginText = sharedPreferences.getString("currentUser", "Unknown User")
            val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)
            val emailFromDb = if (!isGoogleUser) {
                loginText?.let { dbHelper.getEmailByLogin(it) }
            } else {
                loginText
            }
            val userId = emailFromDb?.let { dbHelper.getUserIdByEmail(it) }

            if (userId != null) {
                val isDeleted = dbHelper.deleteEvent(currentEvent.title, currentEvent.day, currentEvent.month, currentEvent.year, currentEvent.time, userId)

                if (isDeleted) {
                    events.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, events.size)
                    context.updateCalendar()
                    Toast.makeText(holder.itemView.context, "Подія видалена", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(holder.itemView.context, "Помилка при видаленні події", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(holder.itemView.context, "Помилка: користувач не знайдено!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = events.size
}

