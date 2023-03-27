package com.viewlift.authentication.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.presentation.authUtils.AuthCommonUtils
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.intent.VerifyOTPIntent
import com.viewlift.authentication.presentation.services.mobileOtp.startSMSListener
import com.viewlift.authentication.presentation.uicomponents.*
import com.viewlift.authentication.presentation.uistate.VerifyOTPScreenUiState
import com.viewlift.authentication.presentation.viewmodel.VerifyOTPViewModel
import com.viewlift.common.ui.composable.ShowCustomDialog
import com.viewlift.common.R
import com.viewlift.common.label.BootstrapColors
import com.viewlift.common.label.LoginScreenLabels
import com.viewlift.core.navigation.NavigationCommand
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.core.navigation.NavigationDestination
import com.viewlift.common.ui.AppCMSTheme


@Composable
fun OtpVerifyScreen(
    navigationType: String,
    navigationData: String,
    navigationKey: String,
    viewModel: VerifyOTPViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    viewModel.navigationKey = navigationKey

    Scaffold(
        scaffoldState = scaffoldState
    ) {padding->
        AppCMSTheme {
            val context = LocalContext.current
            startSMSListener(context, viewModel.receiveOTP)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BootstrapColors.generalBackground.parse)
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val titleText = if(navigationType == AuthCommonUtils.EMAIL_TYPE) LoginScreenLabels.verifyEmailTitle else LoginScreenLabels.verifyMobileTitle
                    Text (
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        color = (BootstrapColors.generalTextColor).parse,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val subtitle = if(navigationType == "EMAIL") {
                        "${LoginScreenLabels.verifyEmailSubtitle} ${navigationData}\n${LoginScreenLabels.enterCodeToVerifyEmail}"
                    } else {
                        "${LoginScreenLabels.verifyMobileSubtitle} ${navigationData}\n${LoginScreenLabels.enterCodeToVerifyMobile}"
                    }

                    Text(
                        text = subtitle,
                        color = BootstrapColors.generalTextColor.parse,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OtpPinView()

                    Spacer(modifier = Modifier.height(30.dp))

                    ShowResentOrTimer(navigationType, navigationData, navigationKey)

                    Spacer(modifier = Modifier.height(30.dp))

                    val differentEmailOrMobile = if(navigationType == "EMAIL") LoginScreenLabels.useDifferentEmailText else LoginScreenLabels.useDifferentMobileText

                    TextViewUnderLined(
                        text = differentEmailOrMobile,
                        color = BootstrapColors.generalTextColor,
                        modifier = Modifier.clickable {
                            viewModel.navigationManager.navigate(object : NavigationCommand {
                                override val destination = NavigationDestination.PopBackStack.route
                            })
                        }
                    )
                }
                HandleUpdateUIState()
            }
        }

    }
}

@Composable
fun ShowResentOrTimer(
    navigationType: String,
    navigationData: String,
    navigationKey: String,
    viewModel: VerifyOTPViewModel = hiltViewModel()) {
    val showTimerOrResend by viewModel.startTimerCountDown.collectAsStateWithLifecycle(initialValue = true)

    if(showTimerOrResend){
        Show30SecTimer()
    } else {
        TextViewUnderLined(
            text = LoginScreenLabels.resendCodeButtonText,
            color = BootstrapColors.generalTextColor,
            modifier = Modifier.clickable {
                viewModel.acceptIntent(
                    VerifyOTPIntent.GetReVerificationCode(
                        navigationType,
                        navigationData,
                        navigationKey
                    )
                )
            }
        )
    }
}

@Composable
fun Show30SecTimer(viewModel: VerifyOTPViewModel = hiltViewModel()) {
    val timeValue by viewModel.timerValue.collectAsStateWithLifecycle(initialValue = 30)

    CountdownScreen(timeValue) {
        viewModel.startTimer().start()
    }
}

@Composable
private fun CountdownScreen(time: Int, onAction: (Boolean) -> Unit) {

    LaunchedEffect(true){
        onAction.invoke(true)
    }

    val text = String.format("%02d", time)

    Box(){
        Text(
            text = "00:$text",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HandleUpdateUIState(viewModel: VerifyOTPViewModel = hiltViewModel()) {
    val uiState: VerifyOTPScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    if (uiState.isLoading) {
        keyboardController?.hide()
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = BootstrapColors.progressBarColor.parse,
                strokeWidth = Dp(value = 4F)
            )
        }
    } else if (uiState.signInTokenResponse?.authorizationToken != null) {
        viewModel.navigateToTVEScreen()
        uiState.signInTokenResponse?.authorizationToken = null
    } else if (uiState.isError && uiState.error != null) {
        keyboardController?.hide()

        uiState.apolloApiError?.message?.let{
            ShowCustomDialog(LoginScreenLabels.errorTitle,  it,
                iconClose = painterResource(id = R.drawable.ic_close),
                iconDialogType = painterResource(id = R.drawable.ic_error))
        }

    } else if (!uiState.tokenResend?.identityInitiateSignOtp?.key.isNullOrEmpty()){
//        Toast.makeText(LocalContext.current, stringResource(R.string.verification_code_sent), Toast.LENGTH_LONG).show()
        viewModel.navigationKey = uiState.tokenResend?.identityInitiateSignOtp?.key.toString()
    }
}

@Composable
fun OtpPinView(viewModel: VerifyOTPViewModel = hiltViewModel()) {

    val otpState by viewModel.otpState.collectAsStateWithLifecycle(initialValue = "")

    PinView()

    if (otpState.length == 6) {

        val deviceId = AuthCommonUtils.getDeviceId(LocalContext.current)

        viewModel.acceptIntent(
            VerifyOTPIntent.GetVerificationCode(
                viewModel.navigationKey,
                deviceId ?: ""
            )
        )

    }
}

