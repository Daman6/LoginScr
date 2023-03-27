package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.InitiateSignOtpRequestMutation
import com.viewlift.network.data.remote.model.request.SendOTPParams
import com.viewlift.network.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
fun interface SendIdentitySignOtpUseCase :  (SendOTPParams) -> Flow<Result<InitiateSignOtpRequestMutation.Data?>>


/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
 fun sendOtpRequest(
    authenticationRepository: AuthenticationRepository , input : SendOTPParams
): Flow<Result<InitiateSignOtpRequestMutation.Data?>> = authenticationRepository
    .sendIdentitySignOtp(input)
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




