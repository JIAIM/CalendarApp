package com.example.calendrapp

class Error {
    companion object {
        const val ERR_LEN = "Пароль повинен містити не менше восьми символів!"
        const val ERR_WHITESPACE = "Пароль не повинен містити пробілів!"
        const val ERR_DIGIT = "Пароль повинен містити хоча б одну цифру!"
        const val ERR_UPPER = "Пароль повинен містити хоча б одну велику літеру!"
        const val ERR_SPECIAL = "Пароль повинен містити принаймні один спеціальний символ, наприклад: _%-=+#@"
    }
}