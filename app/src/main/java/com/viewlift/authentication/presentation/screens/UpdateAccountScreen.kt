package com.viewlift.authentication.presentation.screens

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import com.viewlift.authentication.presentation.authUtils.*
import com.viewlift.authentication.presentation.uicomponents.*
import com.viewlift.authentication.presentation.uistate.UpdateAccountUiState
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel.Companion.EMAIL_TEXTFIELD
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel.Companion.NAME_TEXTFIELD
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel.Companion.PHONE_TEXTFIELD
import com.viewlift.common.ui.composable.ShowCustomDialog
import com.viewlift.common.label.BootstrapColors
import com.viewlift.common.label.BootstrapLabels
import com.viewlift.common.label.UpdateScreenColors
import com.viewlift.common.label.UpdateScreenLabels
import com.viewlift.common.utils.WindowInfo
import com.viewlift.common.utils.rememberWindowInfo
import com.viewlift.common.R
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.core.navigation.NavigationCommand
import com.viewlift.core.navigation.NavigationDestination
import com.viewlift.common.ui.AppCMSTheme
import com.viewlift.common.ui.Typography
import com.viewlift.network.BootStrapQuery
import com.viewlift.network.PageQuery
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun UpdateAccountScreen(
    topBarState: MutableStateFlow<Boolean>? = null,
    pageQueryModule: PageQuery.Module? = null,
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {
    viewModel.handlePageQuery(pageQueryModule)
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    topBarState?.value = false
    val configuration = LocalConfiguration.current

    val windowInfo = rememberWindowInfo()
    val screenHeight = when (windowInfo.screenHeightInfo) {
        is WindowInfo.WindowType.Compact -> {
            configuration.screenHeightDp
        }
        is WindowInfo.WindowType.Medium -> {
            configuration.screenHeightDp
        }
        else -> {
            configuration.screenHeightDp - 25
        }
    }

    val screenWidth = configuration.screenWidthDp.dp

    OnLifecycleEvent { owner, event ->
        // do stuff on event
        when (event) {
            Lifecycle.Event.ON_RESUME -> {

                val permissionGranted = checkIfPermissionGranted(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if(permissionGranted){
                    viewModel.isLocationEnabled = true
                    viewModel.isLocationPermissionValid.value = true
                    var locationManager: LocationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val lastKnownLocationByNetwork =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    lastKnownLocationByNetwork?.let {
                        viewModel.location =
                            "Latitude = ${it.latitude}, Longitude = ${it.longitude}"
                    }
                }
            }
            else                      -> { /* other stuff */ }
        }
    }

    AppCMSTheme {
        Box(
            modifier = Modifier
                .requiredHeight(screenHeight.dp)
                .width(screenWidth)
                .background(color = BootstrapColors.generalBackground.parse)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Column() {
                        Text(
                            text = UpdateScreenLabels.updateAccountTitle,
                            color = White,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        NameEmailTextBox()

                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .background(
                                    color = "#1E3453".parse,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)
                            ) {
                                ToggleBtn()
                                AddInterest()
                                GridItems()
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .align(Alignment.BottomCenter)
                    .background(color = BootstrapColors.generalBackground.parse),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(.25f))
                    SaveContinue(modifier = Modifier.weight(9.5f))
                    Spacer(modifier = Modifier.weight(.25f))
                }
            }

            OpenAgeCheck(Modifier.align(Alignment.BottomCenter))
            OpenPermissionCheck()
            PermissionTestUI(scaffoldState)
            ShowError()
            HandleUIState(this)
            viewModel.handleMultipleValidations()
        }
    }
}

@Composable
fun HandleUIState(boxScope: BoxScope, viewModel: UpdateAccountViewModel = hiltViewModel()) {
    val uiState: UpdateAccountUiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        boxScope.apply {
            ShowProgressbar(this)
        }
    } else if (uiState.updateAccountResponse != null) {
        val cmd = object : NavigationCommand {
            override val destination = NavigationDestination.Home.route
            override val configuration: NavOptions = NavOptions.Builder().setPopUpTo(
                NavigationDestination.Page.route, true, false
            ).build()
        }
        viewModel.navigationManager.navigate(cmd)
        viewModel.progressbar.value = false
        uiState.updateAccountResponse = null

    } else if (uiState.isError && uiState.error != null) {
        viewModel.progressbar.value = false

        val emailAlreadyExists = uiState.apolloApiError!!.extensions?.get("code")
        if(emailAlreadyExists!=null && emailAlreadyExists is String){
            val errorMessageFromCode = UpdateScreenLabels().getErrorMessageFromUpdateCode(emailAlreadyExists)
            if(!errorMessageFromCode.isNullOrEmpty()){
                ShowCustomDialog(UpdateScreenLabels.errorTitle,  errorMessageFromCode,
                    iconClose = painterResource(id = R.drawable.ic_close),
                    iconDialogType = painterResource(id = R.drawable.ic_error)
                )
            } else {
                DefaultErrorHandling(uiState)
            }
        } else {
            DefaultErrorHandling(uiState)
        }
    }
}

@Composable
private fun DefaultErrorHandling(uiState: UpdateAccountUiState) {
    if (!uiState.apolloApiError?.message.isNullOrEmpty()) {
        ShowCustomDialog(UpdateScreenLabels.errorTitle,  uiState.apolloApiError?.message!!,
            iconClose = painterResource(id = R.drawable.ic_close),
            iconDialogType = painterResource(id = R.drawable.ic_error)
        )
    } else {
        ShowCustomDialog(UpdateScreenLabels.errorTitle,  "Unknown Error",
            iconClose = painterResource(id = R.drawable.ic_close),
            iconDialogType = painterResource(id = R.drawable.ic_error)
        )
    }
}

@Composable
fun ShowProgressbar(boxScope: BoxScope, viewModel: UpdateAccountViewModel = hiltViewModel()) {
    val progressbar = viewModel.progressbar.collectAsStateWithLifecycle(initialValue = false)
    if (progressbar.value) {
        boxScope.apply {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = BootstrapColors.progressBarColor.parse,
                strokeWidth = Dp(value = 4F)
            )
        }
    }


}

@Composable
fun ShowError(viewModel: UpdateAccountViewModel = hiltViewModel()) {
    val error by viewModel.showError.collectAsStateWithLifecycle(initialValue = "")
    val context = LocalContext.current

    if (error.isNotBlank()) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        viewModel.showError.value = ""
    }
}

@Composable
fun NameEmailTextBox(viewModel: UpdateAccountViewModel = hiltViewModel()) {

    val editTextData by viewModel.editTextData.collectAsStateWithLifecycle(initialValue = null)
    val error by viewModel.showEditTextError.collectAsStateWithLifecycle(null)

    Box() {
        Column {
            editTextData?.let {
                val nameError =
                    if (error != null && error?.first == NAME_TEXTFIELD) error?.second else ""
                if(viewModel.isNameRequired){
                    UpdateAccountName(
                        hint = UpdateScreenLabels.nameTextField,
                        textValue = it.usernameInPref ?: "",
                        errorMessage = nameError
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (it.phoneInUserPref.isNullOrBlank() && viewModel.isMobileRequired) {
                    val phoneError =
                        if (error != null && error?.first == PHONE_TEXTFIELD) error?.second else ""

                    PhoneEdittext(it.phoneInUserPref, errorMessage = phoneError)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (it.emailInPref.isNullOrBlank() && viewModel.isEmailRequired) {
                    val emailError =
                        if (error != null && error?.first == EMAIL_TEXTFIELD) error?.second else ""

                    UpdateAccountName(
                        hint = UpdateScreenLabels.emailTextField,
                        textValue = it.emailInPref ?: "",
                        errorMessage = emailError
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MarketingCheckBox()
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PhoneEdittext(
    phoneData: String?,
    errorMessage: String? = "",
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {

    val countryCodes by viewModel.countryCodes.collectAsStateWithLifecycle(initialValue = null)

    if (countryCodes != null) {
        Row(
            modifier = Modifier
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            // Show Country Code When we have Mobile
            defaultCountry = countryCodes?.find { it.code == viewModel.countryCode }

            CountryCodeDropDown(modifier = Modifier.weight(0.16f), countryCodes)

            Spacer(modifier = Modifier.width(5.dp))

            Column(
                modifier = Modifier.weight(0.84f)
            ) {
                UpdateAccountName(
                    hint = UpdateScreenLabels.phoneTextField,
                    textValue = phoneData ?: "",
                    errorMessage = errorMessage
                )
            }
        }
    }
}

@Composable
fun OpenAgeCheck(
    modifier: Modifier = Modifier,
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {
    val checkState by viewModel.isBettingValid.collectAsStateWithLifecycle(false)

    if (checkState) {
        CustomAgeDialog(
            modifier = modifier
        )
    }
}

@Composable
fun OpenPermissionCheck(viewModel: UpdateAccountViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val permission by viewModel.permissionCheck.collectAsStateWithLifecycle(initialValue = false)

    val permissionGranted = checkIfPermissionGranted(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    if(!permissionGranted){
        if (permission) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = UpdateScreenLabels.locationPermissionDisclaimer,
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                            shape = RoundedCornerShape(20),
                            colors = ButtonDefaults.buttonColors(backgroundColor = UpdateScreenColors.activeCTAColor.parse),
                            onClick = {
                                viewModel.permissionCheck.value = false
                                viewModel.setPerformLocationAction(true)
                            }) {
                            Text(
                                text = UpdateScreenLabels.enableLocation,
                                color = BootstrapColors.generalTextColor.parse
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                            shape = RoundedCornerShape(20),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            onClick = {
                                viewModel.permissionCheck.value = false
                                viewModel.isBettingValid.value = false
                            }) {
                            Text(text = UpdateScreenLabels.cancel, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                }
            }
        }
    } else {
        viewModel.isLocationEnabled = true
        viewModel.isLocationPermissionValid.value = true
        viewModel.setPerformLocationAction(false)

        var locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            viewModel.location =
                "Latitude = ${it.latitude}, Longitude = ${it.longitude}"
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomAgeDialog(
    viewModel: UpdateAccountViewModel = hiltViewModel(),
    modifier: Modifier
) {
    val bringIntoViewRequester: BringIntoViewRequester = remember { BringIntoViewRequester() }

    val showCustomDialog = remember { mutableStateOf(true) }

    if (showCustomDialog.value) {
        // Creating a Bottom Sheet
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .background(color = White, shape = BottomSheetShape)
                .bringIntoViewRequester(bringIntoViewRequester)
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = UpdateScreenLabels.confirmAgeTitle,
                color = Color.Black,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                UpdateAccountAge(
                    modifier = Modifier
                        .width(80.dp),
                    "MM",
                    type = "MONTH",
                    bringIntoViewRequester = bringIntoViewRequester
                )
                Divider(this)

                UpdateAccountAge(
                    modifier = Modifier
                        .width(80.dp),
                    "DD",
                    type = "DAY",
                    bringIntoViewRequester = bringIntoViewRequester
                )
                Divider(this)

                UpdateAccountAge(
                    modifier = Modifier
                        .width(100.dp),
                    "YYYY",
                    type = "YEAR",
                    bringIntoViewRequester = bringIntoViewRequester
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(modifier = Modifier
                .fillMaxWidth(),
                shape = RoundedCornerShape(20),
                colors = ButtonDefaults.buttonColors(backgroundColor = UpdateScreenColors.activeCTAColor.parse),
                onClick = {
                    viewModel.validateAge(showCustomDialog)
                }) {
                Text(text = "Confirm", color = BootstrapColors.generalTextColor.parse)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Button(modifier = Modifier
                .fillMaxWidth(),
                shape = RoundedCornerShape(20),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                onClick = {
                    viewModel.isBettingValid.value = false
                    showCustomDialog.value = false
                }) {
                Text(text = UpdateScreenLabels.cancel, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Divider(rowScope: RowScope) {
    rowScope.apply {
        Text(
            text = "/", fontSize = 18.sp, color = "#1A000000".parse,
            modifier = Modifier
                .width(20.dp)
                .align(Alignment.CenterVertically)
                .padding(start = 5.dp)
        )
    }
}


@Composable
fun GridItems(viewModel: UpdateAccountViewModel = hiltViewModel()) {
    val configuration = LocalConfiguration.current

    val windowInfo = rememberWindowInfo()
    val screenWidth = when (windowInfo.screenHeightInfo) {
        is WindowInfo.WindowType.Compact -> {
            // Screens size 573 DP height
            (configuration.screenWidthDp / 2) - 20
        }
        is WindowInfo.WindowType.Medium -> {
            // Screen size 773 DP height
            configuration.screenWidthDp - 70
        }
        else -> {
            // Screen size 805 dp height
            configuration.screenWidthDp - 40
        }
    }

    val teams: BootStrapQuery.Teams? by viewModel.teams.collectAsStateWithLifecycle(initialValue = null)
    val showPersonalizationCategories by viewModel.showPersonalizationCategories.collectAsStateWithLifecycle(initialValue = true)

    if (teams != null && !teams!!.values.isNullOrEmpty()) {

        if(showPersonalizationCategories){
            val state = rememberLazyGridState(
                initialFirstVisibleItemIndex = 0
            )
            Column (modifier = Modifier.height(screenWidth.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    state = state,
                    content = {
                        items(teams!!.values!!.size) { index ->
                            Box(
                                modifier = Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                UpdateAccountTeamsCard(
                                    content = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            ImageViewUpdate(
                                                teams!!.values?.get(index)?.imgUrl,
                                                "",
                                                modifier = Modifier
                                            )
                                        }
                                    },
                                    viewModel = viewModel,
                                    teams = teams,
                                    index = index
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SaveContinue(
    modifier: Modifier = Modifier,
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {

    val isUpdateScreenValidated by viewModel.isUpdateAccountValid.collectAsStateWithLifecycle(
        initialValue = null
    )

    var isEnabled by remember {
        mutableStateOf(false)
    }

    if (isUpdateScreenValidated?.isEmailValid == true &&
        isUpdateScreenValidated?.isMobileFieldValid == true &&
        isUpdateScreenValidated?.isNameFieldValid == true
    ) {
        if (isUpdateScreenValidated?.isBettingEnabled == true) {
            isEnabled = isUpdateScreenValidated?.isAgeAbove18 == true && isUpdateScreenValidated?.isLocationPermissionGiven == true
        } else {
            isEnabled = true
        }
    } else {
        isEnabled = false
    }

    Button(modifier = modifier,
        shape = RoundedCornerShape(20),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = UpdateScreenColors.activeCTAColor.parse,
            disabledBackgroundColor = UpdateScreenColors.disabledCTAColorLogin.parse
        ),
        enabled = isEnabled,
        onClick = {
            viewModel.progressbar.value = true
            viewModel.openUpdateAccount()
        }) {
        Text(text = UpdateScreenLabels.saveAndContinue, color = White)
    }
}

@Composable
fun MarketingCheckBox(
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {

    val showEmailConsent by viewModel.showEmailConsent.collectAsStateWithLifecycle(initialValue = false)

    if(showEmailConsent){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            UpdateCheckbox()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = BootstrapLabels.emailConsentText,
                color = White,
                style = Typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
fun ToggleBtn(viewModel: UpdateAccountViewModel = hiltViewModel()) {

    val checkState by viewModel.isBettingValid.collectAsStateWithLifecycle(initialValue = false)
    val enableSportsBetting by viewModel.enableSportsBetting.collectAsStateWithLifecycle(initialValue = true)

    val backgroundColor = if (checkState) White else UpdateScreenColors.disabledCTAColor.parse
    val textColor = if (checkState) Color.Black else White

    if(enableSportsBetting){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = UpdateScreenLabels.enableBetting,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 30.dp)
            )
            SwitchCaseDefault(viewModel)
        }
    }
}


@Composable
fun AddInterest() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(2f))
        Text(
            text = UpdateScreenLabels.addYourInterests,
            color = White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp)
                .weight(6f)
        )
        Spacer(modifier = Modifier.weight(2f))
    }

}

