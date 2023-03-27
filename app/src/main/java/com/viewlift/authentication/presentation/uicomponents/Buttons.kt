package com.viewlift.authentication.presentation.uicomponents

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.R
import com.viewlift.authentication.presentation.authUtils.AuthenticationSDK
import com.viewlift.authentication.presentation.authUtils.EditTextShape
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.common.label.LoginScreenLabels
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.common.ui.AppCMSTheme
import com.viewlift.core.utils.sdp

@Composable
private fun LoginCard(
    modifier: Modifier = Modifier,
    loginIcon: Int,
    loginText: String
) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val minimumHeight = ((screenHeight * (10)) / 100)


    RoundCornerSurface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .height(minimumHeight.sdp)
                .fillMaxWidth()
        ) {

            ImageViewDefault(
                loginIcon,
                modifier = Modifier.height(25.sdp)
            )

            Spacer(Modifier.height(5.sdp))

            Text(
                text = loginText,
                color = Color.Black,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

        }
    }
}

@Composable
fun ButtonDefaultLayout(
    modifier: Modifier = Modifier,
    buttonTitle: String,
    buttonTextColor: String,
    isEnabled: Boolean,
    backgroundColor : String,
    disabledBackgroundColor: String,
    onClick: () -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor.parse,
            contentColor = buttonTextColor.parse,
            disabledBackgroundColor = disabledBackgroundColor.parse
        ),
        modifier = modifier.fillMaxWidth().height(40.dp),
        onClick = { onClick() },
        shape = EditTextShape,
        enabled = isEnabled
    ) {
        TextViewSubtitle(
            buttonTitle,
            color = buttonTextColor
        )
    }
}


@Composable
fun GoogleLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val dialogState by viewModel.googleLoginButtonClick.collectAsStateWithLifecycle(initialValue = false)

    Column(
        modifier = modifier.clickable { viewModel.googleLoginButtonClick.value = true },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginCard(
            modifier = modifier,
            com.viewlift.common.R.drawable.ic_google_login,
            LoginScreenLabels.googleSignInButtonTitle
        )
    }

    if (dialogState) {
        AuthenticationSDK(setTveDestination = "").GoogleLoginHelper()
    }
}


@Composable
fun AppleLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val dialogState by viewModel.appleLoginButtonClick.collectAsStateWithLifecycle(initialValue = false)

    Column(
        modifier = modifier.clickable { viewModel.appleLoginButtonClick.value = true },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginCard(
            modifier = modifier,
            com.viewlift.common.R.drawable.ic_apple_login,
            LoginScreenLabels.appleSignInButtonTitle
        )
    }

    if (dialogState) {
        AuthenticationSDK().AppleLoginHelper()
    }
}

@Composable
fun FacebookLoginButton(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val dialogState by viewModel.facebookLoginButtonClick.collectAsStateWithLifecycle(initialValue = false)

    Column(
        modifier = modifier.clickable { viewModel.facebookLoginButtonClick.value = true },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginCard(
            modifier = modifier,
            com.viewlift.common.R.drawable.ic_facebook, stringResource(
                R.string.sign_in_facebook
            )
        )
    }
    if (dialogState) {
        AuthenticationSDK().FacebookLoginHelper()
    }
}


@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardPreview() {
    AppCMSTheme {
        Row {
//            ButtonDefaultLayout(
//                buttonColor = "#818EA0",
//                buttonTitle = "Verify",
//                buttonTextColor = AuthString.generalTextColor,
//                {
//
//            })
//            val modifier = Modifier.weight(1f)
//            AppleLoginButton(modifier = modifier)
//            GoogleLoginButton(modifier = modifier)
//            FacebookLoginButton(modifier = modifier)
        }
    }
}
