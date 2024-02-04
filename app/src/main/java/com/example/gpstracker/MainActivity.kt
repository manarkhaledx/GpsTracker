package com.example.gpstracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
               checkSettingsQualified()
            } else {
                showLocationPermissionRational()
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkSettingsQualified()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showLocationPermissionRational()
            }

            else -> {
                requestLocationPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun showDialog(
        message: String,
        posActionName: String? = null,
        posActionCallBack: (() -> Unit)? = null,
        negActionName: String? = null,
        negActionCallBack: (() -> Unit)? = null,
        isCancelable: Boolean = true // user can cancel
    ) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(message)

        posActionName?.let {
            alertDialogBuilder.setPositiveButton(it) { dialog, _ ->
                dialog.dismiss()
                posActionCallBack?.invoke()
            }
        }

        negActionName?.let {
            alertDialogBuilder.setNegativeButton(it) { dialog, _ ->
                dialog.dismiss()
                negActionCallBack?.invoke()
            }
        }

        alertDialogBuilder.setCancelable(isCancelable)
        alertDialogBuilder.show()
    }


    private fun showLocationPermissionRational() { //added to fun as it might repeat
        showDialog(
            "we need location to request nearest driver",
            posActionName = "ShowAgain",
            posActionCallBack = {
               requestLocationPermissionLauncher.launch( android.Manifest.permission.ACCESS_FINE_LOCATION)
            },
            negActionName = "cancel",

            )
    }


    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
//        Toast.makeText(this, "can Access Uer Location", Toast.LENGTH_LONG).show()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        fusedLocationClient.lastLocation
//                    .addOnSuccessListener { location : Location? ->
//                // Got last known location. In some rare situations this can be null.
//                Log.e("location","{${location?.latitude} ${location?.longitude}}")
//            }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            LocationListener { newLocation ->
                Log.e(
                    "newlocation",
                    "${newLocation.longitude} " +
                            "${newLocation.latitude}")
            },
            Looper.getMainLooper()
        )



    }
    lateinit var locationRequest:LocationRequest

    private fun checkSettingsQualified() {
       locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).setMinUpdateIntervalMillis(2000)
           .setMinUpdateDistanceMeters(10f)
           .build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getUserLocation()
            } else {
                val exception=task.exception
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            this@MainActivity,
                            LOCATION_REQUEST_CHECK_CODE
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }

            }

        }
    }
    val LOCATION_REQUEST_CHECK_CODE=200





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestLocationPermission()


    }
}