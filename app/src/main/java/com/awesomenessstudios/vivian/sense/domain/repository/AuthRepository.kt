package com.awesomenessstudios.vivian.sense.domain.repository

import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String, displayName: String): Result<SenseUser>
    suspend fun signIn(email: String, password: String): Result<SenseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<SenseUser?>
    fun getCurrentUserFlow(): Flow<SenseUser?>
    suspend fun updateProfile(user: SenseUser): Result<SenseUser>
}