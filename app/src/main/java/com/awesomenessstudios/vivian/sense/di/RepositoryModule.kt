package com.awesomenessstudios.vivian.sense.di


import com.awesomenessstudios.vivian.sense.data.remote.AuthDataSource
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.awesomenessstudios.vivian.sense.data.remote.impl.FirebaseAuthDataSource
import com.awesomenessstudios.vivian.sense.data.remote.impl.FirebasePostDataSource
import com.awesomenessstudios.vivian.sense.data.repository.AuthRepositoryImpl
import com.awesomenessstudios.vivian.sense.data.repository.PostRepositoryImpl
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import com.awesomenessstudios.vivian.sense.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthDataSource(
        firebaseAuthDataSource: FirebaseAuthDataSource
    ): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostDataSource(
        firebasePostDataSource: FirebasePostDataSource
    ): PostDataSource

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository
}
