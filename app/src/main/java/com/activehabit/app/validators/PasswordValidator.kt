package com.activehabit.app.validators

fun getPasswordValidationError(password: String): String? {
    return when {
        password.isBlank() ->
            "Введите пароль"
        password.length < 6 ->
            "Минимальная длина пароля — 6 символов"
        password.length > 15 ->
            "Максимальная длина пароля — 15 символов"
        !password.all { character -> character.code in 33..126 } ->
            "Используйте только латинские буквы, цифры и спецсимволы без пробелов"
        else -> null
    }
}