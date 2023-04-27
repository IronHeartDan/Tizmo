package com.zdem.tizmo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.zdem.tizmo.screens.AuthScreen
import com.zdem.tizmo.screens.ProfileScreen
import com.zdem.tizmo.services.ForegroundService
import com.zdem.tizmo.ui.theme.LocationTrackingTheme
import com.zdem.tizmo.utils.DefaultLocationClient.getCurrentLocation
import com.zdem.tizmo.viewmodels.MainViewModel
import com.zdem.tizmo.widgets.OnAirSwipe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.listenFirebaseAuthChanges()
        setContent {
            val context = LocalContext.current
            // Location Permission Handling
            var locationPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            var locationRational by remember {
                mutableStateOf(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            }

            // Notification Permission Handling
            var notificationPermission by remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                } else {
                    mutableStateOf(true)
                }
            }

            var notificationRational by remember {
                mutableStateOf(shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
            }

            val permissionLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { permissionMap ->
                        permissionMap.forEach {
                            when (it.key) {
                                Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    locationPermission = it.value
                                    locationRational =
                                        !it.value && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                                }

                                Manifest.permission.POST_NOTIFICATIONS -> {
                                    notificationPermission = it.value
                                    notificationRational =
                                        !it.value && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    })

            LaunchedEffect(key1 = true, block = {
                val forLocation = !locationPermission && !locationRational
                val forNotification = !notificationPermission && !notificationRational
                if (forLocation && forNotification) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                } else if (forLocation) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        )
                    )
                } else if (forNotification) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                }


            })

            val coroutineScope = rememberCoroutineScope()
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

            val user by viewModel.userLiveData.observeAsState()

            LocationTrackingTheme {
                Surface {
                    BottomSheetScaffold(topBar = {
                        TopAppBar(title = {
                            Text(text = getString(R.string.app_name))
                        }, modifier = Modifier.background(Color.Black), navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Menu, contentDescription = null
                                )
                            }
                        }, actions = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                    cancel()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null
                                )
                            }
                        })
                    },
                        content = { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .padding(paddingValues)
                            ) {
                                val cameraPositionState = rememberCameraPositionState()
                                var mapProperties by remember {
                                    mutableStateOf(
                                        MapProperties(
                                            isMyLocationEnabled = false,
                                            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                                context,
                                                R.raw.dark_map
                                            )
                                        )
                                    )
                                }
                                var uiSettings by remember {
                                    mutableStateOf(
                                        MapUiSettings(
                                            myLocationButtonEnabled = false,
                                            zoomControlsEnabled = false,
                                        )
                                    )
                                }

                                LaunchedEffect(key1 = locationPermission) {
                                    if (locationPermission) {
                                        mapProperties = MapProperties(
                                            isMyLocationEnabled = true,
                                            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                                context,
                                                R.raw.dark_map
                                            )
                                        )
                                        uiSettings = MapUiSettings(
                                            myLocationButtonEnabled = true,
                                            zoomControlsEnabled = false,
                                        )
                                    }
                                }

                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = uiSettings,
                                    properties = mapProperties,
                                    onMapLoaded = {
                                        getCurrentLocation(
                                            LocationServices.getFusedLocationProviderClient(
                                                applicationContext
                                            )
                                        ).addOnSuccessListener {
                                            val latLng = LatLng(it.latitude, it.longitude)
                                            cameraPositionState.move(
                                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                            )
                                        }
                                    }
                                )

                                var swipeState by remember {
                                    mutableStateOf(ForegroundService.RUNNING)
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(start = 14.dp, end = 14.dp, bottom = 16.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    OnAirSwipe(currentState = swipeState, onSwipeFinished = {
                                        swipeState = true
                                        Intent(
                                            context, ForegroundService::class.java
                                        ).apply {
                                            action = ForegroundService.ACTION_START
                                            context.startService(this)
                                        }
                                    }, onReset = {
                                        swipeState = false
                                        Intent(
                                            context, ForegroundService::class.java
                                        ).apply {
                                            action = ForegroundService.ACTION_STOP
                                            context.stopService(this)
                                        }
                                    })
                                }
                            }
                        },
                        scaffoldState = bottomSheetScaffoldState,
                        sheetShadowElevation = 10.dp,
                        sheetPeekHeight = 0.dp,
                        sheetContent = {
                            if (user == null) {
                                AuthScreen()
                            } else {
                                ProfileScreen()
                            }

                        })

                    if (notificationRational) {
                        AlertDialog(title = {
                            Text(text = "Permission Required")
                        }, text = {
                            Text("Notification Permission is required by post live tracking")
                        }, onDismissRequest = {}, confirmButton = {
                            Button(onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                )
                            }) {
                                Text(text = "Grant")
                            }
                        })
                    }

                    if (locationRational) {
                        AlertDialog(title = {
                            Text(text = "Permission Required")
                        }, text = {
                            Text("Location Permission is required by to keep track of you and nearby people")
                        }, onDismissRequest = {}, confirmButton = {
                            Button(onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    )
                                )
                            }) {
                                Text(text = "Grant")
                            }
                        })
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun Preview() {
    LocationTrackingTheme {
        OnAirSwipe(currentState = false, onSwipeFinished = {}, onReset = {})
    }
}