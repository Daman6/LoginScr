package com.viewlift.authentication.presentation.services.facebook

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.viewlift.authentication.presentation.authUtils.AuthCommonUtils
import com.viewlift.authentication.presentation.intent.AuthLoginIntent
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import timber.log.Timber

@Composable
fun FacebookLoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {

    Timber.d("ENTERED facebookLogin")
    val context = LocalContext.current

    FacebookSdk.sdkInitialize(context)

    val callbackManager = CallbackManager.Factory.create()
    val loginManager = LoginManager.getInstance()

    loginManager.logIn(
        context as ActivityResultRegistryOwner,
        callbackManager,
        listOf("email")
    )

    loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
        override fun onCancel() {
            Timber.d("FACEBOOK CANCELLED")
            viewModel.facebookLoginButtonClick.value = false
        }

        override fun onError(error: FacebookException) {
            Timber.d("FACEBOOK ONERROR")
            viewModel.facebookLoginButtonClick.value = false
        }

        override fun onSuccess(result: LoginResult) {
            viewModel.facebookLoginButtonClick.value = false
            Timber.d("FACEBOOK ONSUCCESS ${result}")

            viewModel.acceptIntent(
                AuthLoginIntent.GetFacebookLogin(
                    facebookToken = result.accessToken.token,
                    deviceId = AuthCommonUtils.getDeviceId(context) ?: ""
                )
            )
        }
    })
}
