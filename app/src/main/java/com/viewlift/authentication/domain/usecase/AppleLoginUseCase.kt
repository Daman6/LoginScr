package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.data.remote.model.request.AppleSignInParams
import com.viewlift.network.data.remote.model.response.SignInTokenResponse
import com.viewlift.network.domain.repository.AuthenticationRepository
import com.viewlift.network.type.AppleSignInInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
fun interface AppleLoginUseCase :  (AppleSignInParams) -> Flow<Result<SignInTokenResponse?>>


/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
 fun appleLoginResult(
    authenticationRepository: AuthenticationRepository , input: AppleSignInParams
): Flow<Result<SignInTokenResponse?>> = authenticationRepository
    .sendSignInAppleRequest(input)
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




