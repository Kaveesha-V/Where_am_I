package com.example.where_am_i

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnGetLocation: Button
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivStaticMap: ImageView
    private lateinit var tvMapPlaceholder: TextView


    private val locationPermissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        tvStatus = findViewById(R.id.tvStatus)
        ivStaticMap = findViewById(R.id.ivStaticMap)
        tvMapPlaceholder = findViewById(R.id.tvMapPlaceholder)

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
            tvStatus.visibility = android.view.View.VISIBLE
            tvStatus.text = "Requesting permission..."
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tvStatus.visibility = android.view.View.VISIBLE
            tvStatus.text = "Permission not granted"
            return
        }

        tvStatus.visibility = android.view.View.VISIBLE
        tvStatus.text = "Fetching location..."
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updateUI(location)
                    tvStatus.text = "Location updated successfully"
                } else {
                    tvStatus.text = "Location not available"
                    Toast.makeText(this, "Location wasn't available.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                tvStatus.text = "Failed: ${it.message}"
                Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(location: Location) {
        tvLatitude.text = "Latitude: ${location.latitude}"
        tvLongitude.text = "Longitude: ${location.longitude}"
        tvAccuracy.text = "Accuracy: ${location.accuracy}m"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(location.time)
        tvTimestamp.text = "Last updated: ${sdf.format(date)}"
        tvStatus.visibility = android.view.View.GONE

        // Load Static Map
        val mapUrl = "https://static-maps.yandex.ru/1.x/?ll=${location.longitude},${location.latitude}&z=14&l=map&size=600,300&pt=${location.longitude},${location.latitude},pm2rdm"
        ivStaticMap.load(mapUrl) {
            crossfade(true)
            listener(
                onSuccess = { _, _ -> tvMapPlaceholder.visibility = android.view.View.GONE },
                onError = { _, _ -> tvMapPlaceholder.text = "Map failed to load" }
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                tvStatus.visibility = android.view.View.VISIBLE
                tvStatus.text = "Permission denied"
                Toast.makeText(this, "Permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
