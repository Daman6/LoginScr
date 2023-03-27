package com.viewlift.authentication.presentation.uistate

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.viewlift.common.model.ApolloApiError
import com.viewlift.network.UpdateUserProfileInitialMutation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Immutable
@Parcelize
data class UpdateAccountUiState @Inject constructor(
    val isLoading: Boolean,
    @IgnoredOnParcel var updateAccountResponse: UpdateUserProfileInitialMutation.Data? = null,
    val isError: Boolean,
    @IgnoredOnParcel val apolloApiError: ApolloApiError? = null,
    @IgnoredOnParcel val error: Throwable? = null
) : Parcelable {

    /**
     * Partial state
     *
     * @constructor Create empty Partial state
     */
    sealed class PartialState {
        object Loading : PartialState() // for simplicity: initial loading & refreshing
        /**
         * Error
         *
         * @property throwable
         * @constructor Create empty Error
         */
        data class Fetched(val updateAccountResponse: UpdateUserProfileInitialMutation.Data?) : PartialState()


        data class Error(val throwable: Throwable) : PartialState()

    }
}