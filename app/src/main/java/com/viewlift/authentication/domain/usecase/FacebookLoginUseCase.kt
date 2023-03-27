package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.data.remote.model.request.FacebookSignInParams
import com.viewlift.network.data.remote.model.response.SignInTokenResponse
import com.viewlift.network.domain.repository.AuthenticationRepository
import com.viewlift.network.type.SignInFacebookInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
fun interface FacebookLoginUseCase :  (FacebookSignInParams) -> Flow<Result<SignInTokenResponse?>>

/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
 fun facebookLoginResult(
    authenticationRepository: AuthenticationRepository , input: FacebookSignInParams
): Flow<Result<SignInTokenResponse?>> = authenticationRepository
    .sendSignInFacebookRequest(input)
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




