package com.telematics.features.account.use_case

import android.app.Activity
import android.util.Log
import com.telematics.domain.model.SessionData
import com.telematics.domain.model.authentication.PhoneAuthCallback
import com.telematics.domain.model.authentication.PhoneAuthCred
import com.telematics.domain.model.authentication.User
import com.telematics.domain.repository.AuthenticationRepo
import com.telematics.domain.repository.UserRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authenticationRepo: AuthenticationRepo,
    private val userRepo: UserRepo
) {

    private val TAG = "LoginUseCase"

    fun isSessionAvailable(): Flow<Boolean> {
        Log.d(TAG, "isSessionAvailable")

        return flow {
            var result = false
            authenticationRepo.getCurrentUserID()?.let { currentUid ->
                authenticationRepo.getDeviceTokenInFirebaseDatabase(currentUid)
                    ?.let { deviceToken ->
                        if (deviceToken.isNotEmpty()) {
                            val sessionData = authenticationRepo.loginAPI(deviceToken)
                            if (!sessionData.isEmpty())
                                saveUser(User(userId = currentUid, deviceToken = deviceToken))
                            result = !sessionData.isEmpty()
                        }
                    }
            }

            if (!result)
                logout()
            emit(result)
        }
    }

    fun authorize(
        email: String,
        password: String
    ): Flow<Boolean> {
        Log.d(TAG, "authorize email")

        return flow {
            val result = authorizeByEmail(email, password)
            if (!result)
                logout()
            emit(result)
        }
    }

    fun authorize(
        phone: String,
        activity: Activity,
        callback: PhoneAuthCallback
    ): Flow<Boolean> {
        Log.d(TAG, "authorize phone")

        return flow {
            authenticationRepo.signInWithPhoneFirebase(phone, activity, callback)
            emit(true)
        }
    }

    fun authorize(credential: PhoneAuthCred<*>): Flow<Boolean> {
        Log.d(TAG, "authorize credential")

        return flow {
            val result = authoriseWithPhoneCredentials(credential.phone, credential)
            if (!result)
                logout()
            emit(result)
        }
    }

    fun registration(login: String, password: String): Flow<Boolean> {
        Log.d(TAG, "registration")

        return flow {
            var result = false
            authenticationRepo.createUserWithEmailAndPasswordInFirebase(login, password)
                ?.let { firebaseUser ->
                    val user = User(email = login, userId = firebaseUser.id)
                    val sessionData = registrationUser(user)
                    result = !sessionData.isEmpty()
                }
            if (!result)
                logout()
            emit(result)
        }
    }

    fun sendVerifyCode(phone: String, code: String, verificationId: String): Flow<Boolean> {
        Log.d(TAG, "setVerifyCode: phone $phone code $code verificationId $verificationId")

        return flow {

            val cred = authenticationRepo.sendVerificationCode(phone, code, verificationId)
            val result = authoriseWithPhoneCredentials(phone, cred)

            if (!result)
                logout()
            emit(result)
        }
    }

    fun logout(): Flow<Boolean> {

        Log.d(TAG, "logout")
        return flow {
            emit(authenticationRepo.logout())
        }
    }

    private suspend fun authorizeByEmail(email: String, password: String): Boolean {
        var result = false

        authenticationRepo.signInWithEmailAndPasswordFirebase(email, password)?.let { iUser ->
            val deviceToken = authenticationRepo.getDeviceTokenInFirebaseDatabase(iUser.id)
            result = if (deviceToken.isNullOrEmpty()) {
                val user = User(email, password, userId = iUser.id)
                val sessionData = registrationUser(user)
                !sessionData.isEmpty()
            } else {
                val sessionData = authenticationRepo.loginAPI(deviceToken)
                if (!sessionData.isEmpty())
                    saveUser(User(deviceToken = deviceToken, userId = iUser.id))
                !sessionData.isEmpty()
            }
        }

        return result
    }

    private suspend fun registrationUser(user: User): SessionData {
        Log.d(TAG, "registrationUser")

        val registrationApiData = authenticationRepo.registrationCreateAPI()
        val deviceToken = registrationApiData.deviceToken
        user.deviceToken = deviceToken
        authenticationRepo.createUserInFirebaseDatabase(user)
        val sessionData = authenticationRepo.loginAPI(deviceToken)
        if (!sessionData.isEmpty()) {
            saveUser(user)
        }
        return sessionData
    }

    private suspend fun authoriseWithPhoneCredentials(
        phone: String,
        credential: PhoneAuthCred<*>
    ): Boolean {
        var result = false
        authenticationRepo.signInWithPhoneAuthCredential(credential)?.let { iUser ->
            val deviceToken = authenticationRepo.getDeviceTokenInFirebaseDatabase(iUser.id)
            result = if (deviceToken.isNullOrEmpty()) {
                val user = User(phone, userId = iUser.id)
                val sessionData = registrationUser(user)
                !sessionData.isEmpty()
            } else {
                val sessionData = authenticationRepo.loginAPI(deviceToken)
                if (!sessionData.isEmpty())
                    saveUser(User(deviceToken = deviceToken, userId = iUser.id))
                !sessionData.isEmpty()
            }
        }

        return result
    }

    private fun saveUser(user: User) {

        Log.d(TAG, "saveUser: userid: ${user.userId}")

        userRepo.saveUserId(user.id)
        userRepo.saveDeviceToken(user.token)
    }

    fun getUser(): Flow<User> {
        return flow {
            val user = userRepo.getUser()
            emit(user)
        }
    }

    fun updateUser(user: User): Flow<Unit> {
        return flow {
            val oldUser = userRepo.getUser()
            val newUser = oldUser.getNewUpdatedUser(user)
            val userId = userRepo.getUserId()
            newUser.userId = userId
            userRepo.saveUser(newUser)
            val unit = authenticationRepo.updateUserInFirebaseDatabase(newUser)
            emit(unit)
        }
    }
}