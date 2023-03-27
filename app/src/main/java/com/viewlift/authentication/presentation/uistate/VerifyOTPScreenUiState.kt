package com.viewlift.authentication.presentation.uistate

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.viewlift.common.model.ApolloApiError
import com.viewlift.network.InitiateSignOtpRequestMutation
import com.viewlift.network.data.remote.model.response.SignInTokenResponse
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject


/**
 * Home ui state
 * @author Anand
 *
 * @property isLoading
 * @property bootstrap
 * @property isError
 * @constructor Create empty Home ui state
 */
@Immutable
@Parcelize
data class VerifyOTPScreenUiState @Inject constructor(
    val isLoading: Boolean,
    @IgnoredOnParcel val signInTokenResponse: SignInTokenResponse? = null,
    @IgnoredOnParcel val tokenResend: InitiateSignOtpRequestMutation.Data? = null,
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


        data class Fetched(val signInTokenResponse: SignInTokenResponse?) : PartialState()

        data class TokenReSend(val status: InitiateSignOtpRequestMutation.Data?) : PartialState()

        /**
         * Error
         *
         * @property throwable
         * @constructor Create empty Error
         */
        data class Error(val throwable: Throwable) : PartialState()

    }
}