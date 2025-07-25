package com.awesomenessstudios.vivian.sense.data.repository

import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.remote.AuthDataSource
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun signUp(email: String, password: String, displayName: String): Result<SenseUser> {
        return authDataSource.signUp(email, password, displayName)
    }

    override suspend fun signIn(email: String, password: String): Result<SenseUser> {
        return authDataSource.signIn(email, password)
    }

    override suspend fun signOut(): Result<Unit> {
        return authDataSource.signOut()
    }

    override suspend fun getCurrentUser(): Result<SenseUser?> {
        return authDataSource.getCurrentUser()
    }

    override fun getCurrentUserFlow(): Flow<SenseUser?> {
        return authDataSource.getCurrentUserFlow()
    }

    override suspend fun updateProfile(user: SenseUser): Result<SenseUser> {
        return authDataSource.updateProfile(user)
    }
}