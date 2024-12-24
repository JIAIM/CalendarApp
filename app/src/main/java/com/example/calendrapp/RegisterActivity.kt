package com.example.calendrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidPassword(pwd: String): String? {
            return when {
                pwd.length < 8 -> Error.ERR_LEN
                pwd.any { it.isWhitespace() } -> Error.ERR_WHITESPACE
                !pwd.any { it.isDigit() } -> Error.ERR_DIGIT
                !pwd.any { it.isUpperCase() } -> Error.ERR_UPPER
                !pwd.any { !it.isLetterOrDigit() } -> Error.ERR_SPECIAL
                else -> null
            }
        }

        val userLogin: EditText = findViewById(R.id.register_username)
        val userEmail: EditText = findViewById(R.id.register_email)
        val userPassword: EditText = findViewById(R.id.register_password)
        val button: Button = findViewById(R.id.register_button)
        val linkToReg: TextView = findViewById(R.id.register_login)

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val email = userEmail.text.toString().trim()
            val password = userPassword.text.toString().trim()


            if (login.length < 3) {
                userLogin.error = "Логін повинен бути не менше 3 символів"
                userLogin.requestFocus()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                userEmail.error = "Введіть правильний Email"
                userEmail.requestFocus()
                return@setOnClickListener
            }

            val passwordError = isValidPassword(password)
            if (passwordError != null) {
                userPassword.error = passwordError
                userPassword.requestFocus()
                return@setOnClickListener
            }

            val db = DBHelper(this, null)
            val user = User(login, email, password)

            if (db.addUser(user)) {
                Toast.makeText(this, "Користувач успішно зареєстрований!", Toast.LENGTH_SHORT).show()
                userLogin.text.clear()
                userEmail.text.clear()
                userPassword.text.clear()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                userEmail.error = "Електронна адреса вже зареєстрована"
                userEmail.requestFocus()
            }
        }


        linkToReg.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}