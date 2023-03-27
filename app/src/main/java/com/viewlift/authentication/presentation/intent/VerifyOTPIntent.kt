package com.viewlift.authentication.presentation.intent

/**
 * Page intent
 * @author Anand
 *
 * @constructor Create empty Page intent
 */
sealed class VerifyOTPIntent {

    data class GetVerificationCode(val verificationCode: String, val deviceId: String) : VerifyOTPIntent()

    data class GetReVerificationCode(val navigationType: String, val navigationData: String, val navigationKey: String) : VerifyOTPIntent()

}
