package com.example.where_am_i

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnGetLocation: Button
    private lateinit var tvLat: TextView
    private lateinit var tvLng: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvTimestamp: TextView

    private val locationPermissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLat = findViewById(R.id.tvLat)
        tvLng = findViewById(R.id.tvLng)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvTimestamp = findViewById(R.id.tvTimestamp)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            // Permission already granted, fetch location
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateUI(location)
                } else {
                    Toast.makeText(this, "Location wasn't available.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(location: Location) {
        tvLat.text = "Latitude: ${location.latitude}"
        tvLng.text = "Longitude: ${location.longitude}"
        tvAccuracy.text = "Accuracy: ${location.accuracy}m"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(location.time)
        tvTimestamp.text = "Timestamp: ${sdf.format(date)}"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLocation()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
