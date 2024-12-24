@file:Suppress("DEPRECATION")

package com.example.calendrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val userLogin: EditText = findViewById(R.id.login_username)
        val userPassword: EditText = findViewById(R.id.login_password)
        val button: Button = findViewById(R.id.login_button)
        val googleAuth: LinearLayout = findViewById(R.id.login_google)
        val linkToReg: TextView = findViewById(R.id.login_register)

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (!isFieldValid(login, "Input login") || !isFieldValid(password, "Input password")) {
                return@setOnClickListener
            }

            val db = DBHelper(this, null)
            val isAuth = db.getUser(login, password)

            if (isAuth) {
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("currentUser", login)
                editor.putBoolean("isGoogleUser", false)
                editor.apply()

                Toast.makeText(this, "Користувач аутентифікований", Toast.LENGTH_LONG).show()
                userLogin.text.clear()
                userPassword.text.clear()
                val intent = Intent(this, CalendarActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Користувач НЕ автентифікований", Toast.LENGTH_LONG).show()

                if (!db.isUserExists(login)) {
                    userLogin.error = "Логін не існує"
                    userLogin.requestFocus()
                } else {
                    userLogin.error = null
                    if (db.isPasswordIncorrect(login, password)) {
                        userPassword.error = "Невірний пароль"
                        userPassword.requestFocus()
                    } else {
                        userPassword.error = null
                    }
                }
            }
        }

        googleAuth.setOnClickListener {
            signInWithGoogle()
        }

        linkToReg.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email

            if (email != null) {
                val db = DBHelper(this, null)
                if (db.isGoogleUser(email)) {
                    val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.putString("currentUser", email)
                    editor.putBoolean("isGoogleUser", true)
                    editor.apply()

                    Toast.makeText(this, "Користувач з електронною поштою $email автентифіковано за допомогою Google", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                } else {
                    val isAdded = db.addGoogleUser(email)
                    if (isAdded) {
                        Toast.makeText(this, "Ласкаво просимо, $email! Користувач зареєстрований за допомогою Google", Toast.LENGTH_LONG).show()
                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("currentUser", email)
                        editor.putBoolean("isGoogleUser", true)
                        editor.apply()
                        val intent = Intent(this, CalendarActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Помилка реєстрації через Google", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Не вдається знайти електронну адресу користувача", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Помилка входу: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun isFieldValid(field: String, errorMessage: String): Boolean {
        if (field.isEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}
