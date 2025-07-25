package com.awesomenessstudios.vivian.sense.domain.usecases.auth

import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<SenseUser> {
        return authRepository.signUp(email, password, displayName)
    }
}

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<SenseUser> {
        return authRepository.signIn(email, password)
    }
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<SenseUser?> {
        return authRepository.getCurrentUser()
    }
}