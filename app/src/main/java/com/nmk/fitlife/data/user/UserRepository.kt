package com.nmk.fitlife.data.user

class UserRepository(private val userDao: UserDao) {
    suspend fun register(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun getByEmail(email: String): User? = userDao.getUserByEmail(email)
}