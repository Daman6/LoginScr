package com.viewlift.authentication.domain.usecase

import com.viewlift.core.extensions.resultOf
import com.viewlift.network.*
import com.viewlift.network.data.remote.model.request.UpdateInitialProfileParams
import com.viewlift.network.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Get page use case
 *
 * @constructor Create empty Get page use case
 */
 fun interface UpdateAccountUseCase :  (UpdateInitialProfileParams) -> Flow<Result<UpdateUserProfileInitialMutation.Data?>>


/**
 * Get page
 * @author Anand
 *
 * @param pageRepository
 * @param pageParams
 * @return
 */
fun updateAccountProfile(
    authenticationRepository: UserProfileRepository, input: UpdateInitialProfileParams
): Flow<Result<UpdateUserProfileInitialMutation.Data?>> = authenticationRepository
    .updateUserProfileInitial(input)
    .map {
        resultOf { it }
    }.catch {
        emit(Result.failure(it))
    }




