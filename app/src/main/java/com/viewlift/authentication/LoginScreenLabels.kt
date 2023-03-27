package com.viewlift.common.label

import androidx.annotation.Keep

@Keep
class LoginScreenLabels {

    fun getErrorMessageFromLoginCode(errorCode: String): String? {
        return when(errorCode){
            LoginScreenKeys.DEVICE_LIMIT_EXCEEDED -> DEVICE_LIMIT_EXCEEDED
            LoginScreenKeys.EMAIL_ALREADY_LINKED -> EMAIL_ALREADY_LINKED
            LoginScreenKeys.EMAIL_NOT_EXIST -> EMAIL_NOT_EXIST
            LoginScreenKeys.EMAIL_NOT_REGISTERED -> EMAIL_NOT_REGISTERED
            LoginScreenKeys.EMAIL_OR_PASSWORD_INCORRECT -> EMAIL_OR_PASSWORD_INCORRECT
            LoginScreenKeys.EMAIL_VERIFICATION_FAILED -> EMAIL_VERIFICATION_FAILED
            LoginScreenKeys.INVALID_REQUEST_PARAMS -> INVALID_REQUEST_PARAMS
            LoginScreenKeys.NAME_NOT_VALID -> NAME_NOT_VALID
            LoginScreenKeys.OTP_MISMATCH -> OTP_MISMATCH
            LoginScreenKeys.OTP_SENT_FAILED -> OTP_SENT_FAILED
            LoginScreenKeys.PASSWORD_NOT_VALID -> PASSWORD_NOT_VALID
            LoginScreenKeys.PHONE_ALREADY_LINKED -> PHONE_ALREADY_LINKED
            LoginScreenKeys.PHONE_NOT_LINKED -> PHONE_NOT_LINKED
            LoginScreenKeys.PHONE_NOT_VALID -> PHONE_NOT_VALID
            LoginScreenKeys.VERIFY_OTP_FAILED -> VERIFY_OTP_FAILED
            else -> null
        }
    }

    companion object {

        var createAccountTitle = "Create an account or sign in"
        var createAccountSubTitle = "Enter your phone number or email address to receive a single-use, secure verification code. You can also sign in with your Apple ID or Google account."
        var emailTextFieldPlaceHolder = "Enter your email or phone number"
        var appleSignInButtonTitle = "Sign In With Apple"
        var googleSignInButtonTitle = "Sign In With Google"
        var getVerificationCodeButtonTitle = "Get Verification Code"
        var verifyMobileTitle = "Verify your mobile number"

        var googleError = "Google login failed"
        var enterCodeToVerifyEmail = "Enter your code below to continue."
        var enterCodeToVerifyMobile = "Please enter the code below to verify your number."
        var resendCodeButtonText = "Resend Code"
        var verifyEmailTitle = "Verify your Email Address"
        var verifyMobileSubtitle = "We have sent a verification code to"
        var verifyEmailSubtitle = "A one time code has been sent to"
        var useDifferentEmailText = "Use a different Email Address"
        var useDifferentMobileText = "Use a different Mobile Number"
        var alreadyPaidText = "Already paid?"
        var restorePurchaseTitle = "Restore Purchase"

        var CROSS_COUNTRY_PHONE = "This phone number is not allowed to use in this region"
        var DEVICE_LIMIT_EXCEEDED = "You have exceeded the maximum number of devices allowed for your subscription. Please log out of one or more devices"
        var EMAIL_ALREADY_LINKED =  "This email address is already linked to an existing account. Please login using the account."
        var EMAIL_NOT_EXIST =  "Failed to find user with the provided email."
        var EMAIL_NOT_REGISTERED =  "Sorry, we can't find an account with this email address. Please enter a registered email address and try again."
        var EMAIL_OR_PASSWORD_INCORRECT =  "Your email or password is not correct. Please try again."
        var EMAIL_VERIFICATION_FAILED =  "Email address is NOT valid.. Please check your email and use the valid one. If you still get this error reach out to customer support."
        var INVALID_REQUEST_PARAMS =  "Error Processing Request"
        var NAME_NOT_VALID =  "Name should contain minimum 3 characters & maximum 100 characters"
        var OTP_MISMATCH =  "OTP entered is incorrect"
        var OTP_SENT_FAILED =  "Sending OTP to your Number has failed. Please try again using a different number"
        var PASSWORD_NOT_VALID =  "Password should contain minimum 6 & maximum 50 characters"
        var PHONE_ALREADY_LINKED =  "This Phone Number is Already Linked to an Existing Account."
        var PHONE_NOT_LINKED =  "This Phone Number is not Linked to any account"
        var PHONE_NOT_VALID =  "Phone number is invalid"
        var VERIFY_OTP_FAILED =  "OTP IS NOT CORRECT. PLEASE ENTER THE CORRECT OTP"

        var errorTitle = "Something went wrong!"

    }

}

@Keep
object LoginScreenColors {
    // Login Screen colors
    var activeCTAColorLogin = "#D2393B" // red
    var disabledCTAColorLogin = "#818DA0" // disable button color
    var inputBgColor = "#2B426E" // input background
    var moduleBackgroundColorLogin = "#0D2148" // dark background

    var errorcolor : String = "#F5D7D7"
}

@Keep
object LoginScreenKeys {

    const val createAccountTitle = "createAccountTitle"
    const val createAccountSubTitle = "createAccountSubTitle"
    const val appleSignInButtonTitle = "appleSignInButtonTitle"
    const val googleSignInButtonTitle = "googleSignInButtonTitle"
    const val googleError = "googleError"
    const val enterCodeToVerifyEmail = "enterCodeToVerifyEmail"
    const val enterCodeToVerifyMobile = "enterCodeToVerifyMobile"
    const val getVerificationCodeButtonTitle = "getVerificationCodeButtonTitle"
    const val resendCodeButtonText = "resendCodeButtonText"
    const val verifyMobileTitle = "verifyMobileTitle"
    const val verifyEmailTitle = "verifyEmailTitle"
    const val verifyMobileSubtitle = "verifyMobileSubtitle"
    const val emailTextFieldPlaceHolder = "emailTextFieldPlaceHolder"
    const val verifyEmailSubtitle = "verifyEmailSubtitle"
    const val useDifferentEmailText = "useDifferentEmailText"
    const val useDifferentMobileText = "useDifferentMobileText"
    const val alreadyPaidText = "alreadyPaidText"
    const val restorePurchaseTitle = "restorePurchaseTitle"
    const val progressBarBackgroundColor = "progressBarBackgroundColor"

    const val CROSS_COUNTRY_PHONE = "CROSS_COUNTRY_PHONE"
    const val DEVICE_LIMIT_EXCEEDED = "DEVICE_LIMIT_EXCEEDED"
    const val EMAIL_ALREADY_LINKED = "EMAIL_ALREADY_LINKED"
    const val EMAIL_NOT_EXIST = "EMAIL_NOT_EXIST"
    const val EMAIL_NOT_REGISTERED = "EMAIL_NOT_REGISTERED"
    const val EMAIL_OR_PASSWORD_INCORRECT = "EMAIL_OR_PASSWORD_INCORRECT"
    const val EMAIL_VERIFICATION_FAILED = "EMAIL_VERIFICATION_FAILED"
    const val INVALID_REQUEST_PARAMS = "INVALID_REQUEST_PARAMS"
    const val NAME_NOT_VALID = "NAME_NOT_VALID"
    const val OTP_MISMATCH = "OTP_MISMATCH"
    const val OTP_SENT_FAILED = "OTP_SENT_FAILED"
    const val PASSWORD_NOT_VALID = "PASSWORD_NOT_VALID"
    const val PHONE_ALREADY_LINKED = "PHONE_ALREADY_LINKED"
    const val PHONE_NOT_LINKED = "PHONE_NOT_LINKED"
    const val PHONE_NOT_VALID = "PHONE_NOT_VALID"
    const val VERIFY_OTP_FAILED = "VERIFY_OTP_FAILED"

    // Color keys
    const val activeCTAColor = "activeCTAColor"
    const val disabledCTAColor = "disabledCTAColor"
    const val inputBgColor = "inputBgColor"
    const val moduleBackgroundColor = "moduleBackgroundColor"

}