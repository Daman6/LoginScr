package com.viewlift.authentication.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.navArgument
import com.viewlift.authentication.presentation.screens.OtpVerifyScreen
import com.viewlift.authentication.presentation.screens.UpdateAccountScreen
import com.viewlift.core.navigation.NavigationDestination
import com.viewlift.core.navigation.NavigationFactory
import javax.inject.Inject

/**
 * Home navigation factory
 * @author Anand
 *
 * @constructor Create empty Home navigation factory
 */
class AuthNavigationFactory @Inject constructor() : NavigationFactory {

    @OptIn(ExperimentalAnimationApi::class)
    override fun create(builder: NavGraphBuilder) {

        builder.composable(NavigationDestination.AuthVerifyOtp.route,
            arguments = listOf(
                navArgument("NAVIGATION_TYPE") { type = NavType.StringType },
                navArgument("NAVIGATION_DATA") { type = NavType.StringType },
                navArgument("NAVIGATION_KEY") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            }) { backStackEntry ->
            val navigationType = backStackEntry.arguments?.getString("NAVIGATION_TYPE")
            val navigationData = backStackEntry.arguments?.getString("NAVIGATION_DATA")
            val navigationKey = backStackEntry.arguments?.getString("NAVIGATION_KEY")

            if (navigationType != null && navigationData != null && navigationKey != null) {
                OtpVerifyScreen(navigationType, navigationData, navigationKey)
            }
        }

        builder.composable(NavigationDestination.Home.route,
            enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left,animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(500) ) }){
            UpdateAccountScreen()
        }
    }
}
