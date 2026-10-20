

package com.activehabit.app.network

data class ProfileResponse(
    val user: ProfileUser
)

data class ProfileUser(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String
)