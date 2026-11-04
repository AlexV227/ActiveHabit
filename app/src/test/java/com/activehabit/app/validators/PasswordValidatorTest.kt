package com.activehabit.app.validators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun emptyPasswordReturnsError() {
        val result = getPasswordValidationError("")

        assertEquals("Введите пароль", result)
    }

    @Test
    fun shortPasswordReturnsError() {
        val result = getPasswordValidationError("Ab1!")

        assertEquals(
            "Минимальная длина пароля — 6 символов",
            result
        )
    }

    @Test
    fun longPasswordReturnsError() {
        val result = getPasswordValidationError("Password1234567!")

        assertEquals(
            "Максимальная длина пароля — 15 символов",
            result
        )
    }

    @Test
    fun russianLettersReturnError() {
        val result = getPasswordValidationError("Пароль123!")

        assertEquals(
            "Используйте только латинские буквы, цифры и спецсимволы без пробелов",
            result
        )
    }

    @Test
    fun correctPasswordHasNoError() {
        val result = getPasswordValidationError("Test123!")

        assertNull(result)
    }
}