package com.example.calendrapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "app", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                login TEXT,
                email TEXT,
                password TEXT,
                is_google_auth INTEGER DEFAULT 0
            )
        """
        db!!.execSQL(query)
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

    fun getUser(login: String, password: String): Boolean {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM users WHERE login = ? AND password = ?", arrayOf(login, password))
        val isAuthenticated = result.moveToFirst()
        result.close()
        return isAuthenticated
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
}
