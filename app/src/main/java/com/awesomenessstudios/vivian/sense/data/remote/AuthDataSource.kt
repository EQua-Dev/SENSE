package com.awesomenessstudios.vivian.sense.data.remote

import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    suspend fun signUp(email: String, password: String, displayName: String): Result<SenseUser>
    suspend fun signIn(email: String, password: String): Result<SenseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<SenseUser?>
    fun getCurrentUserFlow(): Flow<SenseUser?>
    suspend fun updateProfile(user: SenseUser): Result<SenseUser>
}