package com.telematics.domain.repository

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow

interface TrackingApiRepo {

    fun setContext(context: Context)
    fun setDeviceToken(deviceId: String)

    fun checkPermissions(): Flow<Boolean>
    fun checkPermissionAndStartWizard(activity: Activity)

    fun startTracking()
    fun setEnableTrackingSDK(enable: Boolean)

    fun logout()
}