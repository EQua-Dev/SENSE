package com.awesomenessstudios.vivian.sense.data.remote.impl

import android.util.Log
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.remote.AuthDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthDataSource {

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<SenseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")

            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }


            val user = SenseUser(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                createdAt = Date()
            )

            Log.d("SignUp", "Attempting to write user to Firestore")

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            firebaseUser.updateProfile(profileUpdates).await()
            Result.success(user)
        } catch (e: Exception) {
            Log.e("SignUp", "Error during signup", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<SenseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Sign in failed")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user =
                userDoc.toObject(SenseUser::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<SenseUser?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                Result.success(null)
            } else {
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                val user = userDoc.toObject(SenseUser::class.java)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserFlow(): Flow<SenseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            trySend(firebaseUser?.let {
                SenseUser(
                    id = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: ""
                )
            })
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override suspend fun updateProfile(user: SenseUser): Result<SenseUser> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

