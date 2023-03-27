package com.viewlift.authentication.presentation.authUtils

import android.provider.Settings
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.common.label.BootstrapColors
import com.viewlift.common.label.UpdateScreenColors
import com.viewlift.common.label.UpdateScreenLabels
import kotlinx.coroutines.launch

@Composable
fun PermissionTestUI(
    scaffoldState: ScaffoldState,
    updateAccountViewModel: UpdateAccountViewModel = hiltViewModel()
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val performLocationAction by updateAccountViewModel.performLocationAction.collectAsState()

    if (performLocationAction) {
//        Log.d(TAG, "Invoking Permission UI")

        val permissionGranted = checkIfPermissionGranted(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (!permissionGranted) {
            PermissionUI(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) { permissionAction ->
                when (permissionAction) {
                    is PermissionAction.OnPermissionGranted -> {
                        updateAccountViewModel.isLocationPermissionValid.value = true
                        updateAccountViewModel.isLocationEnabled = true
                        updateAccountViewModel.setPerformLocationAction(false)
                        //Todo: do something now as we have location permission
                        Log.d(TAG, "Location has been granted")
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Location permission granted!")
                        }

                        var locationManager: LocationManager =
                            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val lastKnownLocationByNetwork =
                            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        lastKnownLocationByNetwork?.let {
                            updateAccountViewModel.location =
                                "Latitude = ${it.latitude}, Longitude = ${it.longitude}"
                        }

                    }
                    is PermissionAction.OnPermissionDenied -> {
                        updateAccountViewModel.setPerformLocationAction(false)
                        updateAccountViewModel.isBettingValid.value = false
                        updateAccountViewModel.isLocationEnabled = false
                        updateAccountViewModel.isLocationPermissionValid.value = false

                    }
                }
            }
        } else {
            updateAccountViewModel.isLocationEnabled = true
            updateAccountViewModel.isLocationPermissionValid.value = true
            updateAccountViewModel.setPerformLocationAction(false)

            var locationManager: LocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocationByNetwork =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastKnownLocationByNetwork?.let {
                updateAccountViewModel.location =
                    "Latitude = ${it.latitude}, Longitude = ${it.longitude}"
            }
        }
    }

}

private const val TAG = "PermissionUI"

@Composable
fun PermissionUI(
    context: Context,
    permission: String,
    permissionAction: (PermissionAction) -> Unit
) {


    val permissionGranted = checkIfPermissionGranted(
        context,
        permission
    )

    if (permissionGranted) {
        Log.d(TAG, "Permission already granted, exiting..")
        permissionAction(PermissionAction.OnPermissionGranted)
        return
    }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permission provided by user")
            // Permission Accepted
            permissionAction(PermissionAction.OnPermissionGranted)
        } else {
            Log.d(TAG, "Permission denied by user")
            // Permission Denied
            permissionAction(PermissionAction.OnPermissionDenied)
        }
    }


    val showPermissionRationale = shouldShowPermissionRationale(
        context,
        permission
    )


    if (showPermissionRationale) {
        Log.d(TAG, "Showing permission rationale for $permission")

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            val showPermissionRational = remember {
                mutableStateOf(true)
            }
            if (showPermissionRational.value) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = BottomSheetShape)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = UpdateScreenLabels.turnOnLocation,
                        color = Color.Black,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = UpdateScreenColors.activeCTAColor.parse),
                        onClick = {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri: Uri = Uri.fromParts("package", context.getPackageName(), null)
                            intent.data = uri
                            (context as Activity).startActivity(intent)
                            showPermissionRational.value = !showPermissionRational.value
                        }) {
                        Text(text = "Grant Permissions", color = BootstrapColors.generalTextColor.parse)
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                }
            }
        }
    } else {
        //Request permissions again
        Log.d(TAG, "Requesting permission for $permission")
        SideEffect {
            launcher.launch(permission)
        }

    }


}