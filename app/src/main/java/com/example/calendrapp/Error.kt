package com.example.calendrapp

class Error {
    companion object {
        const val ERR_LEN = "Password must have at least eight characters!"
        const val ERR_WHITESPACE = "Password must not contain whitespace!"
        const val ERR_DIGIT = "Password must contain at least one digit!"
        const val ERR_UPPER = "Password must have at least one uppercase letter!"
        const val ERR_SPECIAL = "Password must have at least one special character, such as: _%-=+#@"
    }
}