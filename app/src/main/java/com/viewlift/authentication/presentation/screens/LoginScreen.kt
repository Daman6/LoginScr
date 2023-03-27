package com.viewlift.authentication.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.uicomponents.AppleLoginButton
import com.viewlift.authentication.presentation.uicomponents.FacebookLoginButton
import com.viewlift.authentication.presentation.uicomponents.GoogleLoginButton
import com.viewlift.authentication.presentation.uistate.ShowEmailOrMobile
import com.viewlift.authentication.presentation.uistate.SocialLoginUiState
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.authentication.theme.InsightsTheme
import com.viewlift.common.label.BootstrapColors
import com.viewlift.common.label.LoginScreenLabels


/**
 * Home
 * @author Harish
 *
 * @param viewModel
 */

@Composable
fun LoginScreen(
//    topBarState: MutableStateFlow<Boolean>? = null,
//    pageQueryModule: PageQuery.Module? = null,
//    viewModel: AuthViewModel = hiltViewModel()
) {
//    Timber.d("LoginScreen Opened")
//    viewModel.acceptIntent(AuthLoginIntent.GetCountryCode)
//
//    HandleEvents(viewModel.event)
   // viewModel.setLoginButtonState(pageQueryModule)

    val scaffoldState = rememberScaffoldState()
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp - 20.dp
    val screenWidth = configuration.screenWidthDp.dp
//    topBarState?.value = false

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .height(screenHeight)
            .width(screenWidth)
    ) {padding->
        InsightsTheme()
        {

           // val loginVideoResource = LocalContext.current.resources.getIdentifier("samplevideo", "raw", LocalContext.current.packageName)

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.padding(padding)
            ) {
              //  VideoPlayer(uri = RawResourceDataSource.buildRawResourceUri(loginVideoResource))
                ButtonSheetContent()
                HandleUIState()
                ShowErrorDialog()
            }
        }
    }
}

@Composable
fun ShowErrorDialog(viewModel: AuthViewModel = hiltViewModel()) {

    val error by viewModel.showError.collectAsStateWithLifecycle(null)

    if(error?.first== false){
        error?.second?.let {
            ShowCustomDialog(LoginScreenLabels.errorTitle, it,
                iconClose = painterResource(id = com.viewlift.common.R.drawable.ic_close),
                iconDialogType = painterResource(id = com.viewlift.common.R.drawable.ic_error)) { isClosed->
                if(isClosed){
                    viewModel.ShowError(null)
                }
            }
        }
    }

}

@Composable
fun HandleUIState(viewModel: AuthViewModel = hiltViewModel()) {
    val uiState: SocialLoginUiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = BootstrapColors.progressBarColor.parse,
                strokeWidth = Dp(value = 4F)
            )
        }
    } else if (uiState.signInTokenResponse != null) {

        val isSubscribed = uiState.signInTokenResponse!!.isSubscribed ?: false

        val phoneNumber = uiState.signInTokenResponse!!.phoneNumber ?: ""

        val userName = uiState.signInTokenResponse!!.name ?: ""

        val email = uiState.signInTokenResponse!!.email ?: ""

        val navDestination = when {
            !isSubscribed -> {
                NavigationDestination.Page.route.replace(oldValue = "{KEY_PAGE_PATH}", newValue = NavMap.pageMap[1].replace("/","*"))
            }
            phoneNumber.isEmpty() || userName.isEmpty() || email.isEmpty() -> {
                NavigationDestination.Page.route.replace(oldValue = "{KEY_PAGE_PATH}", newValue = NavMap.pageMap[2].replace("/","*"))
            }
            else -> {
                NavigationDestination.Home.route
            }
        }

        viewModel.navigationManager.navigate(object : NavigationCommand {
            override val destination = navDestination
            override val configuration: NavOptions = NavOptions.Builder().setPopUpTo(
                NavigationDestination.Page.route,
                inclusive = true,
                saveState = false
            ).build()
        })
        viewModel.ShowError(null)
        uiState.signInTokenResponse = null

    } else if (uiState.isError && uiState.error != null) {

        val errorCode = uiState.apolloApiError!!.extensions?.get("code")
        if(errorCode!=null && errorCode is String){

            val errorMessageFromCode = LoginScreenLabels().getErrorMessageFromLoginCode(errorCode)
            if(!errorMessageFromCode.isNullOrEmpty()){
                viewModel.ShowError(Pair(false, errorMessageFromCode))
            } else {
                DefaultLoginError(uiState)
            }
        } else {
            DefaultLoginError(uiState)
        }

        uiState.isError = false
    } else if(uiState.verificationKey?.isNotBlank()==true){
        viewModel.navigateToNextScreen(uiState.verificationKey!!)
        uiState.verificationKey = null
    }
}

@Composable
private fun DefaultLoginError(uiState: SocialLoginUiState) {
    if (!uiState.apolloApiError?.message.isNullOrEmpty()) {
        ShowCustomDialog(LoginScreenLabels.errorTitle, uiState.apolloApiError?.message!!,
            iconClose = painterResource(id = com.viewlift.common.R.drawable.ic_close),
            iconDialogType = painterResource(id = com.viewlift.common.R.drawable.ic_error))
    } else {
        ShowCustomDialog(LoginScreenLabels.errorTitle, "Unknown Error",
            iconClose = painterResource(id = com.viewlift.common.R.drawable.ic_close),
            iconDialogType = painterResource(id = com.viewlift.common.R.drawable.ic_error))
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ButtonSheetContent() {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val configuration = LocalConfiguration.current
    val screenHeight: Int = configuration.screenHeightDp

    val minimumHeight = ((screenHeight * 9) / 100).dp
    val maxHeight = ((screenHeight * 40) / 100).dp

    Box(
        modifier = Modifier
            .clip(LoginBottomShape)
    ) {
        Column(modifier = Modifier
            .background(color = BootstrapColors.generalBackground.parse)
            .padding(start= 15.dp, end = 15.dp, top = 20.dp, bottom = 10.dp)
            .heightIn(min = minimumHeight, max = maxHeight)
            .bringIntoViewRequester(bringIntoViewRequester)) {
            TextViewTitle (
                text = LoginScreenLabels.createAccountTitle,
                color = BootstrapColors.generalTextColor,
                isBold = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            EmailOrMobileConsent()

            EmailEditText(bringIntoViewRequester)

            SocialLoginOptions()
        }
    }
}

@Composable
private fun SocialLoginOptions(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val showEmailOrMobile: ShowEmailOrMobile by viewModel.isEmailOrMobile.collectAsStateWithLifecycle(
        ShowEmailOrMobile.NoOptions
    )

    if (showEmailOrMobile == ShowEmailOrMobile.NoOptions) {
        Spacer(modifier = Modifier.height(10.dp))

        ShowSocialLoginButtons()

//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = LoginScreenLabels.alreadyPaidText,
//                color = LoginScreenLabels.generalTextColor.parse,
//                fontSize = 13.ssp,
//                textAlign = TextAlign.Center,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.width(5.dp))
//
//            Text(
//                text = LoginScreenLabels.restorePurchaseTitle,
//                style = TextStyle(
//                    textDecoration = TextDecoration.Underline
//                ),
//                color = LoginScreenLabels.generalTextColor.parse,
//                fontSize = 13.ssp,
//                textAlign = TextAlign.Center,
//                fontWeight = FontWeight.Bold
//            )
//        }

    }
}

@Composable
private fun EmailOrMobileConsent(viewModel: AuthViewModel = hiltViewModel()) {
    val showEmailOrMobile: ShowEmailOrMobile by viewModel.isEmailOrMobile.collectAsStateWithLifecycle(
        ShowEmailOrMobile.NoOptions
    )

    if (showEmailOrMobile == ShowEmailOrMobile.NoOptions) {
        Text(
            text = LoginScreenLabels.createAccountSubTitle,
            color = BootstrapColors.generalTextColor.parse,
//            style = Typography.labelMedium.copy(
//                textAlign = TextAlign.Start
//            )
        )

        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
fun ShowSocialLoginButtons(viewModel: AuthViewModel = hiltViewModel()) {

    val listOfSocialButtons by viewModel.socialLoginButtons.collectAsStateWithLifecycle()

    val buttonsToShow = listOfSocialButtons?.filter { it.enable == true && it.title != "TVE" }

    if (buttonsToShow != null && buttonsToShow.isNotEmpty()) {

        Column(modifier = Modifier.fillMaxWidth()) {
            var modifier: Modifier = Modifier
            var gridSize = 2
            val spacing: Dp
            if(buttonsToShow.size == 1){
                gridSize = 1
                spacing = 0.dp
            } else if (buttonsToShow.size == 2 || buttonsToShow.size == 4) {
                gridSize = 2
                spacing = 20.dp
            } else {
                gridSize = 3
                modifier = Modifier.weight(1f)
                spacing = 5.dp
            }
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp

            val minimumHeight = ((screenHeight * (10)) / 100)

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.height(minimumHeight.dp),
                userScrollEnabled = true,
                // content padding
                content = {
                    items(buttonsToShow.size) { index ->
                        when (buttonsToShow[index].title) {
                            "Facebook" -> {
                                FacebookLoginButton(modifier = modifier)
                            }
                            "Google" -> {
                                GoogleLoginButton(modifier = modifier)
                            }
                            "Apple" -> {
                                AppleLoginButton(modifier = modifier)
                            }
                        }
                    }
                })

            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

data class LoginButtonData(
    val dragDropId: Int?,
    val enable: Boolean?,
    val title: String?
)

@Composable
private fun HandleEvents(events: Flow<LoginEvent>) {

    events.collectWithLifecycle {
        when (it) {
            is LoginEvent.OpenTvePage -> {
//                uriHandler.openUri(it.uri)
            }
        }
    }
}