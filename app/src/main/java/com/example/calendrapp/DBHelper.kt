package com.example.calendrapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Calendar

class DBHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "app", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            login TEXT,
            email TEXT,
            password TEXT,
            is_google_auth INTEGER DEFAULT 0
        )
    """
        db!!.execSQL(createUsersTable)

        val createEventsTable = """
        CREATE TABLE events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_user INTEGER NOT NULL,          
            name TEXT NOT NULL,                
            context TEXT,                      
            time TEXT NOT NULL,                
            date INTEGER NOT NULL,             
            month INTEGER NOT NULL,            
            year INTEGER NOT NULL,            
            FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE
        )
    """
        db.execSQL(createEventsTable)
    }


    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL(/* sql = */ "DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun addUser(user: User): Boolean {
        val db = this.writableDatabase

        val query = "SELECT * FROM users WHERE email = ?"
        val cursor = db.rawQuery(query, arrayOf(user.email))
        val isEmailExists = cursor.moveToFirst()
        cursor.close()

        if (isEmailExists) {
            db.close()
            return false
        }

        val values = ContentValues().apply {
            put("login", user.login)
            put("email", user.email)
            put("password", user.password)
            put("is_google_auth", 0)
        }

        return try {
            db.insertOrThrow("users", null, values)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun addGoogleUser(email: String): Boolean {
        val db = this.writableDatabase

        val query = "SELECT * FROM users WHERE email = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val isEmailExists = cursor.moveToFirst()
        cursor.close()

        if (isEmailExists) {
            db.close()
            return false
        }

        val values = ContentValues().apply {
            put("email", email)
            put("is_google_auth", 1)
        }

        return try {
            db.insertOrThrow("users", null, values)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun addEvent(event: Event): Boolean {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("id_user", event.userId)
            put("name", event.title)
            put("context", event.context)
            put("time", event.time)
            put("date", event.day)
            put("month", event.month)
            put("year", event.year)
        }

        return try {
            db.insertOrThrow("events", null, values)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun updateEvent(eventId: Int, event: Event): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", event.title)
            put("context", event.context)
            put("time", event.time)
        }
        return try {
            db.update("events", values, "id = ?", arrayOf(eventId.toString())) > 0
        } catch (e: Exception) {
            Log.e("DBHelper", "Помилка під час оновлення події", e)
            false
        } finally {
            db.close()
        }
    }

    fun deleteEvent(title: String, day: Int, month: Int, year: Int, time: String, userId: Int): Boolean {
        val db = this.writableDatabase
        return try {
            db.delete(
                "events",
                "name = ? AND date = ? AND month = ? AND year = ? AND time = ? AND id_user = ?",
                arrayOf(title, day.toString(), month.toString(), year.toString(), time, userId.toString())
            ) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun findEventId(event: Event): Int? {
        val db = this.readableDatabase
        val query = """
        SELECT id FROM events 
        WHERE name = ? AND date = ? AND month = ? AND year = ? AND time = ? AND id_user = ?
    """
        val cursor = db.rawQuery(query, arrayOf(event.title, event.day.toString(), event.month.toString(), event.year.toString(), event.time, event.userId.toString()))

        return if (cursor.moveToFirst()) {
            val eventId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            cursor.close()
            eventId
        } else {
            cursor.close()
            null
        }
    }

    fun getUser(login: String, password: String): Boolean {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM users WHERE login = ? AND password = ?", arrayOf(login, password))
        val isAuthenticated = result.moveToFirst()
        result.close()
        return isAuthenticated
    }

    @SuppressLint("Range")
    fun getEmailByLogin(login: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT email FROM users WHERE login = ?", arrayOf(login))

        if (cursor.moveToFirst()) {
            val email = cursor.getString(cursor.getColumnIndex("email"))
            cursor.close()
            return email
        }

        cursor.close()
        return null
    }

    @SuppressLint("Range")
    fun getUserIdByEmail(email: String): Int? {
        val db = this.readableDatabase
        val query = "SELECT id FROM users WHERE email = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndex("id"))
            cursor.close()
            userId
        } else {
            cursor.close()
            null
        }
    }

    fun getEventsForDate(day: Int, month: Int, year: Int, userId: Int): List<Event> {
        val db = this.readableDatabase
        val events = mutableListOf<Event>()

        val query = """
        SELECT * FROM events 
        WHERE date = ? AND month = ? AND year = ? AND id_user = ?
        ORDER BY time ASC  
    """
        val cursor = db.rawQuery(query, arrayOf(day.toString(), month.toString(), year.toString(), userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("id_user")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    context = cursor.getString(cursor.getColumnIndexOrThrow("context")),
                    time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                    day = cursor.getInt(cursor.getColumnIndexOrThrow("date")),
                    month = cursor.getInt(cursor.getColumnIndexOrThrow("month")),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
                )
                events.add(event)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return events
    }




    fun isUserExists(login: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE login = ?", arrayOf(login))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun isPasswordIncorrect(login: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE login = ? AND password = ?", arrayOf(login, password))
        val isIncorrect = !cursor.moveToFirst()
        cursor.close()
        return isIncorrect
    }

    fun isGoogleUser(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND is_google_auth = 1", arrayOf(email))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getDaysWithEvents(month: Int, year: Int, userId: Int): List<Pair<Int, Boolean>> {
        val daysInMonth = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.YEAR, year)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysWithEvents = mutableListOf<Pair<Int, Boolean>>()

        for (day in 1..daysInMonth) {
            val hasEvents = hasEventsForDate(day, month, year, userId)
            daysWithEvents.add(Pair(day, hasEvents))
        }

        return daysWithEvents
    }

    fun hasEventsForDate(day: Int, month: Int, year: Int, userId: Int): Boolean {
        val db = this.readableDatabase
        val query = """
        SELECT COUNT(*) FROM events 
        WHERE date = ? AND month = ? AND year = ? AND id_user = ?
    """
        val cursor = db.rawQuery(query, arrayOf(day.toString(), month.toString(), year.toString(), userId.toString()))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }
}


