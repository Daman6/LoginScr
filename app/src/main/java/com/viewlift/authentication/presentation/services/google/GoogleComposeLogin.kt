package com.viewlift.authentication.presentation.services.google

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.viewlift.authentication.presentation.authUtils.AuthCommonUtils
import com.viewlift.authentication.presentation.viewmodel.VerifyOTPViewModel
import com.viewlift.authentication.presentation.authUtils.AuthenticationSDK
import com.viewlift.authentication.presentation.events.LoginEvent
import com.viewlift.authentication.presentation.intent.AuthLoginIntent
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.core.extensions.collectWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber


@Composable
fun GoogleLoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {

    val login by viewModel.googleLogin.collectAsStateWithLifecycle(initialValue = false)

    GoogleLogin({ login })

    viewModel.googleLogin.value = true

//    AuthenticationSDK().navigateToNextScreen(viewModel)
}

@Composable
fun GoogleLogin(
    onClick:() -> Boolean,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    HandleEvents(viewModel.event)

    Timber.d("GOOGLE ENTERED GoogleLogin")

    val signInRequestCode = 1

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = AuthResultContract()) { task ->
            try {

                val account = task?.getResult(ApiException::class.java)
                val lastLoggedIn = GoogleSignIn.getLastSignedInAccount(context)

                if (lastLoggedIn != null) {
                    viewModel.acceptIntent(
                        AuthLoginIntent.GetGoogleLogin(
                            googleToken = lastLoggedIn.idToken ?: "",
                            deviceId = AuthCommonUtils.getDeviceId(context) ?: ""
                        )
                    )
                } else if (account != null) {
                    viewModel.acceptIntent(
                        AuthLoginIntent.GetGoogleLogin(
                            googleToken = account.idToken ?: "",
                            deviceId = AuthCommonUtils.getDeviceId(context) ?: ""
                        )
                    )
                    viewModel.googleLoginButtonClick.value = false
                } else {
                    viewModel.googleLoginButtonClick.value = false
//                    error?.value = "Google Sign In Failed"
//                    failure(
//                        "Google sign in failed"
//                    )
                }
            } catch (e: ApiException) {
                viewModel.googleLoginButtonClick.value = false
//                error?.value = "Error ${e.localizedMessage}"

                Timber.d("Error ${e.localizedMessage}")
            }
        }

//    viewModel.handleClick(onClick())

    if (onClick()) {
        Timber.d("GOOGLE COMPOSE BUTTON PRESSED")
        authResultLauncher.launch(signInRequestCode)
    }

}

@Composable
private fun HandleEvents(events: Flow<LoginEvent>) {
    val uriHandler = LocalUriHandler.current

    events.collectWithLifecycle {
        when (it) {
            is LoginEvent.OpenTvePage -> {
//                uriHandler.openUri(it.uri)
            }
        }
    }
}
