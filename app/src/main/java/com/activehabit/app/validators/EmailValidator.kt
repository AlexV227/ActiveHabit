package com.activehabit.app.validators

fun getEmailValidationError(email: String): String? {
    val emailPattern = Regex(
        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    )

    return when {
        email.isBlank() ->
            "Введите email"

        !emailPattern.matches(email) ->
            "Введите корректный email"

        else -> null
    }
}