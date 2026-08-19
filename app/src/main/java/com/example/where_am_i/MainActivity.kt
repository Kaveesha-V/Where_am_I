package com.example.where_am_i

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
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
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivStaticMap: ImageView
    private lateinit var tvMapPlaceholder: TextView

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

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
            checkAndRequestLocationPermission()
        }
    }

    // --- Member 3: Permission Handling Logic ---

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkAndRequestLocationPermission() {
        if (hasLocationPermission()) {
            getLocation()
        } else {
            tvStatus.visibility = android.view.View.VISIBLE
            tvStatus.text = "Requesting permission..."
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun showPermissionDeniedMessage() {
        tvStatus.visibility = android.view.View.VISIBLE
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            tvStatus.text = "Location permission is required to use this feature."
        } else {
            tvStatus.text = "Permission permanently denied. Please enable it in Settings."
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                        (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                getLocation()
            } else {
                showPermissionDeniedMessage()
            }
        }
    }

    // --- End of Member 3 logic ---

    private fun getLocation() {
        // Double check permission before calling FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                    // Optionally hide status after a delay or success
                    tvStatus.visibility = android.view.View.GONE
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
        tvLatitude.text = "${location.latitude}"
        tvLongitude.text = "${location.longitude}"
        tvAccuracy.text = "${location.accuracy}m"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(location.time)
        tvTimestamp.text = "${sdf.format(date)}"

        // Load Static Map
        val mapUrl = "https://static-maps.yandex.ru/1.x/?ll=${location.longitude},${location.latitude}&z=14&l=map&size=600,300&pt=${location.longitude},${location.latitude},pm2rdm"
        ivStaticMap.load(mapUrl) {
            crossfade(true)
            listener(
                onSuccess = { _, _ -> tvMapPlaceholder.visibility = android.view.View.GONE },
                onError = { _, _ -> 
                    tvMapPlaceholder.visibility = android.view.View.VISIBLE
                    tvMapPlaceholder.text = "Map failed to load" 
                }
            )
        }
    }
}
