package com.viewlift.authentication.presentation.services.apple

import java.io.Serializable

interface SignInWithAppleCallback {

    fun onSignInWithAppleSuccess(authorizationCode: String, idToken: String, user: SignInWithAppleResult.User)

    fun onSignInWithAppleFailure(error: Throwable)

    fun onSignInWithAppleCancel()
}

internal fun SignInWithAppleCallback.toFunction(): (SignInWithAppleResult) -> Unit =
    { result ->
        when (result) {
            is SignInWithAppleResult.Success -> onSignInWithAppleSuccess(result.authorizationCode, result.idToken, result.user)
            is SignInWithAppleResult.Failure -> onSignInWithAppleFailure(result.error)
            is SignInWithAppleResult.Cancel -> onSignInWithAppleCancel()
        }
    }

sealed class SignInWithAppleResult {
    data class Success(val authorizationCode: String, val idToken: String, val user: User) :
        SignInWithAppleResult()

    data class Failure(val error: Throwable) : SignInWithAppleResult()

    object Cancel : SignInWithAppleResult()

    data class User(val name: Name, val email: String) : Serializable

    data class Name(val firstName: String, val middleName: String?, val lastName: String) :
        Serializable

}
