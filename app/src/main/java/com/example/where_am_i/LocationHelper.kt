package com.example.where_am_i

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Helper class to handle location fetching using FusedLocationProviderClient.
 * This implementation fulfills Member 4's task.
 */
class LocationHelper(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Fetches the current location once.
     * @param onLocationResult Callback function that receives the Location object or null if failed.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(onLocationResult: (Location?) -> Unit) {
        val cts = CancellationTokenSource()
        
        // Using getCurrentLocation for a one-time fresh location fix
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                onLocationResult(location)
            }
            .addOnFailureListener {
                onLocationResult(null)
            }
    }
}
