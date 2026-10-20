
package com.activehabit.app.network

data class RegisterResponse(
    val message: String,
    val user: RegisteredUser
)

data class RegisteredUser(
    val id: Int,
    val name: String,
    val email: String,
    val created_at: String
)
