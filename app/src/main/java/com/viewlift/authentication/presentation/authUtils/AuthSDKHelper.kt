package com.viewlift.authentication.presentation.authUtils

import androidx.compose.runtime.Composable
import com.viewlift.authentication.presentation.services.apple.AppleLoginComposeDialog
import com.viewlift.authentication.presentation.services.facebook.FacebookLoginScreen
import com.viewlift.authentication.presentation.services.google.GoogleLoginScreen
import kotlinx.coroutines.flow.MutableStateFlow

class AuthenticationSDK(
    setTveDestination: String? = null,
    setViewPlanDestination: String? = null,
    setHomePageDestination: String? = null,

    ) {

    @Composable
    fun GoogleLoginHelper() {
        GoogleLoginScreen()
    }

    @Composable
    fun AppleLoginHelper() {
        AppleLoginComposeDialog()
    }

    @Composable
    fun FacebookLoginHelper() {
        FacebookLoginScreen()
    }


    companion object {
        var tveDestination: String? = null
        var viewPlanDestination: String? = null
        var homePageDestination: String? = null
    }

    init {
        tveDestination = setTveDestination
        viewPlanDestination = setViewPlanDestination
        homePageDestination = setHomePageDestination
    }

}