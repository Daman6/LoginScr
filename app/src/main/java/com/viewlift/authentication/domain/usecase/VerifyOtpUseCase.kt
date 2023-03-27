package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.data.remote.model.response.SignInTokenResponse
import com.viewlift.network.data.remote.model.request.VerifyOTPParams
import com.viewlift.network.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
fun interface VerifyOtpUseCase :  (VerifyOTPParams) -> Flow<Result<SignInTokenResponse?>>


/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
 fun verifyOtp(
    authenticationRepository: AuthenticationRepository , input: VerifyOTPParams
): Flow<Result<SignInTokenResponse?>> = authenticationRepository
    .verifyOtp(input)
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




