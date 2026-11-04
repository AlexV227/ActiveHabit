package com.activehabit.app.validators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmailValidatorTest {

    @Test
    fun emptyEmailReturnsError() {
        val result = getEmailValidationError("")

        assertEquals("Введите email", result)
    }

    @Test
    fun emailWithoutAtSignReturnsError() {
        val result = getEmailValidationError("testmail.ru")

        assertEquals("Введите корректный email", result)
    }

    @Test
    fun emailWithoutDomainReturnsError() {
        val result = getEmailValidationError("test@mail")

        assertEquals("Введите корректный email", result)
    }

    @Test
    fun correctEmailHasNoError() {
        val result = getEmailValidationError("test@mail.ru")

        assertNull(result)
    }
}