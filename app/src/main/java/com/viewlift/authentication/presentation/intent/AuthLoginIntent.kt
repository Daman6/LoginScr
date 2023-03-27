package com.viewlift.authentication.presentation.intent

/**
 * Page intent
 * @author Anand
 *
 * @constructor Create empty Page intent
 */
sealed class AuthLoginIntent {

    data class GetGoogleLogin(val googleToken: String, val deviceId: String) : AuthLoginIntent()
    data class GetAppleLogin(val appleToken: String, val deviceId: String) : AuthLoginIntent()
    data class GetFacebookLogin(val facebookToken: String, val deviceId: String) : AuthLoginIntent()
    object SendVerificationCode : AuthLoginIntent()
    object GetCountryCode : AuthLoginIntent()
}
