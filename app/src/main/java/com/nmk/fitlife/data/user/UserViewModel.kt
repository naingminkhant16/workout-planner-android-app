package com.nmk.fitlife.data.user

import androidx.lifecycle.ViewModel
import at.favre.lib.crypto.bcrypt.BCrypt

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun registerUser(user: User): Long {
        // Hash user provided password
        val hashedPassword = BCrypt.withDefaults().hashToString(
            12,
            user.password.toCharArray()
        )
        val newUser = user.copy(password = hashedPassword)
        return userRepository.register(newUser)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userRepository.getByEmail(email)
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer()
            .verify(
                password.toCharArray(),
                hashedPassword.toCharArray()
            ).verified
    }
}