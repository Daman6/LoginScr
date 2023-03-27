package com.viewlift.authentication.presentation.services.apple

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.viewlift.authentication.presentation.authUtils.AuthCommonUtils
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.intent.AuthLoginIntent
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.common.label.BootstrapColors
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import java.util.*

val APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize"
val SCOPE = "name%20email"

val CLIENT_ID = "com.viewlift.monumentalsports.applesignin"
val REDIRECT_URI = "https://develop.monumentalsportsnetwork.com/"

val stateCode = UUID.randomUUID().toString()
val url = "$APPLE_AUTH_URL?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&response_type=code%20id_token&scope=$SCOPE&response_mode=form_post&state=$stateCode";

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppleLoginComposeDialog(
    viewModel: AuthViewModel = hiltViewModel()
) {

    val dialogState by viewModel.appleLoginButtonClick.collectAsStateWithLifecycle(initialValue = false)

    if(dialogState){
        Dialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = {
                viewModel.appleLoginButtonClick.value = false
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val webData = WebContent.Url(url)
                val state = WebViewState(webData)

                val context = LocalContext.current
                // WebView Toolbar for apple login webView
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp)
//                    .background("#201C1D".parse)
//                    .clickable { showDialog = false },
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Apple Login",
//                    color = AuthString.generalTextColor.parse,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(start = 15.dp)
//                )
//                Text(
//                    text = "Cancel",
//                    color = "#5B91F7".parse,
//                    fontSize = 14.sp,
//                    textAlign = TextAlign.End,
//                    modifier = Modifier.padding(end = 15.dp)
//                )
//          }

                Box(){
                    WebView(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onCreated = { webView ->
                            webView.settings.javaScriptEnabled = true

                            val formInterceptorInterface =
                                FormInterceptorInterface(stateCode) { signInResult ->
                                    viewModel.appleLoginButtonClick.value = false
                                    when (signInResult) {
                                        is SignInWithAppleResult.Success -> {
                                            Log.d(
                                                "SAMPLE_APP",
                                                "onCreate: " + signInResult.authorizationCode + signInResult.idToken + signInResult.user
                                            )
                                            Log.d(
                                                "SAMPLE_APP",
                                                "Optional user details (JSON): ${signInResult.user}"
                                            )

                                            viewModel.acceptIntent(
                                                AuthLoginIntent.GetAppleLogin(
                                                    appleToken = signInResult.idToken,
                                                    deviceId = AuthCommonUtils.getDeviceId(context) ?: ""
                                                )
                                            )
                                        }
                                        is SignInWithAppleResult.Failure -> {
                                            Log.d(
                                                "SAMPLE_APP",
                                                "Received error from Apple Sign In ${signInResult.error.message}"
                                            )
                                        }
                                        is SignInWithAppleResult.Cancel -> {
                                            Log.d("SAMPLE_APP", "User canceled Apple Sign In")
                                        }
                                    }
                                }
                            webView.addJavascriptInterface(
                                formInterceptorInterface,
                                FormInterceptorInterface.NAME
                            )

                            webView.settings.javaScriptCanOpenWindowsAutomatically = true


                        },
                        client = SignInWebViewClient(
                            FormInterceptorInterface.JS_TO_INJECT,
                            viewModel
                        )
                    )
                    FullScreenLoader()
                }
            }

            BackHandler(enabled = true) {
                viewModel.appleLoginButtonClick.value = false
            }
        }
    }
}

@Composable
fun FullScreenLoader(viewModel: AuthViewModel = hiltViewModel()) {

    val fullScreenLoader by viewModel.fullScreenLoader.collectAsStateWithLifecycle(initialValue = false)

    if(fullScreenLoader){
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
    }

}
