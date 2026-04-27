package com.patocodes.geoalarm.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object LocationPermissionState {

    fun hasForegroundAccess(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * On API 23–28, "always" is implied by FINE. On 29+ we need
     * [Manifest.permission.ACCESS_BACKGROUND_LOCATION] for background location.
     */
    fun hasBackgroundAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return hasForegroundAccess(context)
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Ready for a future “arm + monitor in background” flow. */
    fun canArmWithLocationPolicy(context: Context): Boolean {
        if (!hasForegroundAccess(context)) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return hasBackgroundAccess(context)
    }
}
