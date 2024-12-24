package com.example.calendrapp

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var monthText: TextView
    private lateinit var monthNext: ImageView
    private lateinit var monthPrevious: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var dayText: TextView
    private lateinit var createEvent: Button
    private lateinit var accountImage: ImageView
    private lateinit var eventsRecyclerView: RecyclerView
    val dbHelper = DBHelper(this, null)

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
        dayText = findViewById(R.id.calendar_items_text)
        createEvent = findViewById(R.id.create_event)
        accountImage = findViewById(R.id.account_image)
        eventsRecyclerView = findViewById(R.id.events_for_date)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.layoutManager = GridLayoutManager(this, 7)

        createEvent.setOnClickListener{
            val date = dayText.text.toString()
            val arrDate = parseDate(date)
            showCreateEventDialog(arrDate?.get(0) ?:0, arrDate?.get(1) ?:0, arrDate?.get(2) ?:0)
        }

        accountImage.setOnClickListener{
            showCreateAccountDialog()
        }

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

    @SuppressLint("SetTextI18n")
    fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        monthText.text = monthFormat.format(calendar.time)
        val month = SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.time)
        val monthYear = monthText.text
        val days = generateDaysForMonth(calendar)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loginText = sharedPreferences.getString("currentUser", "Unknown User")
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)
        val emailFromDb = if (!isGoogleUser) {
            loginText?.let { dbHelper.getEmailByLogin(it) }
        } else {
            loginText
        }
        val userId = emailFromDb?.let { dbHelper.getUserIdByEmail(it) }
        if (userId != null) {
            val daysWithEvents = dbHelper.getDaysWithEvents(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR),
                userId
            )


            val calendarAdapter = CalendarAdapter(month, days, daysWithEvents) { day ->
                dayText.text = "$day $monthYear Події:"
                createEvent.visibility = View.VISIBLE
                val arrDate = parseDate(dayText.text.toString())
                arrDate?.let { showEventsForDate(it[0], it[1], it[2]) }
            }
            recyclerView.adapter = calendarAdapter
        }
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

    @SuppressLint("SetTextI18n")
    private fun showCreateEventDialog(day: Int, month: Int, year: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_event, null)
        val eventName: EditText = dialogView.findViewById(R.id.event_name)
        val eventContext: EditText = dialogView.findViewById(R.id.event_context)
        val saveButton: Button = dialogView.findViewById(R.id.save_event)
        val timePickerButton: Button = dialogView.findViewById(R.id.time_picker_button)
        val eventDate: TextView = dialogView.findViewById(R.id.event_date)
        var selectedTime: String? = null

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loginText = sharedPreferences.getString("currentUser", "Unknown User")
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)

        eventDate.text = "$day.$month.$year"

        timePickerButton.setOnClickListener {
            showTimePicker { time ->
                selectedTime = time
                timePickerButton.text = "Час: $time"
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val name = eventName.text.toString()
            val context = eventContext.text.toString()
            val emailFromDb = if (!isGoogleUser) {
                loginText?.let { dbHelper.getEmailByLogin(it) }
            } else {
                loginText
            }

            if (name.isNotBlank() && selectedTime != null && emailFromDb != null) {
                val userId = dbHelper.getUserIdByEmail(emailFromDb)
                if (userId != null) {
                    val event = Event(userId, name, context, selectedTime!!, day, month, year)
                    val isAdded = dbHelper.addEvent(event)

                    if (isAdded) {
                        Toast.makeText(
                            this,
                            "Подія '$name' збережена на $selectedTime!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        val space: LinearLayout = findViewById(R.id.linear_events)
                        val events = dbHelper.getEventsForDate(day, month, year, userId).toMutableList()
                        val adapter = EventAdapter(events, this)
                        if (events.isNotEmpty()) {
                            space.visibility = View.VISIBLE
                        } else {
                            space.visibility = View.GONE
                        }
                        eventsRecyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        updateCalendar()
                    } else {
                        Toast.makeText(this, "Помилка при збереженні події!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Помилка: користувачана не знайдено!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Заповніть усі поля!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }



    @SuppressLint("SetTextI18n")
    private fun showCreateAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account, null)
        val login: TextView = dialogView.findViewById(R.id.account_login_text)
        val email: TextView = dialogView.findViewById(R.id.account_email_text)
        val google: TextView = dialogView.findViewById(R.id.account_google_text)
        val exitButton: Button = dialogView.findViewById(R.id.exit_account)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loginText = sharedPreferences.getString("currentUser", "Unknown User")
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)


        if (!isGoogleUser){
            login.visibility = View.VISIBLE
            login.text = "Логін: $loginText"
            val emailFromDb = loginText?.let { dbHelper.getEmailByLogin(it) }
            email.text = "Почта: $emailFromDb"
        } else {
            email.text = "Почта: $loginText"
        }
        google.text = "Google: ${if (isGoogleUser) "Підключено" else "Не підключено"}"


        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        exitButton.setOnClickListener {
            dialog.dismiss()
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    fun showEditEventDialog(event: Event) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_event, null)
        val eventName: EditText = dialogView.findViewById(R.id.event_name)
        val eventContext: EditText = dialogView.findViewById(R.id.event_context)
        val saveButton: Button = dialogView.findViewById(R.id.save_event)
        val timePickerButton: Button = dialogView.findViewById(R.id.time_picker_button)
        val eventDate: TextView = dialogView.findViewById(R.id.event_date)
        var selectedTime = event.time

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loginText = sharedPreferences.getString("currentUser", "Unknown User")
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)

        eventName.setText(event.title)
        eventContext.setText(event.context)
        eventDate.text = "${event.day}.${event.month}.${event.year}"
        timePickerButton.text = "Час: ${event.time}"

        timePickerButton.setOnClickListener {
            showTimePicker { time ->
                selectedTime = time
                timePickerButton.text = "Час: $time"
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val name = eventName.text.toString()
            val context = eventContext.text.toString()
            val emailFromDb = if (!isGoogleUser) {
                loginText?.let { dbHelper.getEmailByLogin(it) }
            } else {
                loginText
            }

            if (name.isNotBlank()  && emailFromDb != null) {
                val userId = dbHelper.getUserIdByEmail(emailFromDb)
                if (userId != null) {
                    val eventId = dbHelper.findEventId(event)
                    val isUpdated:Boolean
                    val updatedEvent = Event(userId, name, context, selectedTime, event.day, event.month, event.year)
                    if (eventId != null) {
                        isUpdated = dbHelper.updateEvent(
                            eventId,
                            updatedEvent)
                    } else {
                        isUpdated = false
                    }

                    if (isUpdated) {
                        Toast.makeText(
                            this,
                            "Подія '$name' оновлена!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        val space: LinearLayout = findViewById(R.id.linear_events)
                        val events = dbHelper.getEventsForDate(event.day, event.month, event.year, userId).toMutableList()
                        if (events.isNotEmpty()) {
                            space.visibility = View.VISIBLE
                        } else {
                            space.visibility = View.GONE
                        }
                        val adapter = EventAdapter(events, this)
                        eventsRecyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        updateCalendar()
                    } else {
                        Toast.makeText(this, "Помилка при оновленні події!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Помилка: користувачана не знайдено!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Заповніть усі поля!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }



    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    fun parseDate(dateString: String): IntArray? {
        val parts = dateString.replace("Події:", "").split(" ")

        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val day = parts[0].toIntOrNull() ?: return null
        val month = monthNames.indexOf(parts[1]) + 1
        val year = parts[2].toIntOrNull() ?: return null

        return intArrayOf(day, month, year)
    }

    private fun showEventsForDate(day: Int, month: Int, year: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val loginText = sharedPreferences.getString("currentUser", "Unknown User")
        val isGoogleUser = sharedPreferences.getBoolean("isGoogleUser", false)

        val emailFromDb = if (!isGoogleUser) {
            loginText?.let { dbHelper.getEmailByLogin(it) }
        } else {
            loginText
        }

        val userId = emailFromDb?.let { dbHelper.getUserIdByEmail(it) }

        if (userId != null) {
            val events = dbHelper.getEventsForDate(day, month, year, userId).toMutableList()
            val space: LinearLayout = findViewById(R.id.linear_events)
            if (events.isNotEmpty()) {
                space.visibility = View.VISIBLE
            } else {
                space.visibility = View.GONE
            }
            val adapter = EventAdapter(events, this)
            eventsRecyclerView.adapter = adapter
        } else {
            Toast.makeText(this, "Помилка: користувача не знайдено!", Toast.LENGTH_SHORT).show()
        }
    }
}
