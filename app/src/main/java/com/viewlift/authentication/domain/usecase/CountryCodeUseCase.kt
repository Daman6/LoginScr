package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.data.remote.model.response.CountryCodeResponse
import com.viewlift.network.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
fun interface CountryCodeUseCase : () -> Flow<Result<List<CountryCodeResponse>?>>


/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
 fun countryCodeResult(
    authenticationRepository: AuthenticationRepository
): Flow<Result<List<CountryCodeResponse>?>> = authenticationRepository
    .getCountryCodes()
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




