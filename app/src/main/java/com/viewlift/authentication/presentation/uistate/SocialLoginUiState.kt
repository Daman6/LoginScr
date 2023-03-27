package com.viewlift.authentication.presentation.uistate

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.viewlift.common.model.ApolloApiError
import com.viewlift.network.data.remote.model.response.CountryCodeResponse
import com.viewlift.network.data.remote.model.response.SignInTokenResponse
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Page ui state
 * @author Anand
 *
 * @property isLoading
 * @property signInTokenResponse
 * @property pageId
 * @property isError
 * @constructor Create empty Page ui state
 */
@Immutable
@Parcelize
data class SocialLoginUiState(
    val isLoading: Boolean = false,
    @IgnoredOnParcel var signInTokenResponse: SignInTokenResponse? = null,
    @IgnoredOnParcel var countryCode: List<CountryCodeResponse>? = null,
    var verificationKey: String? = null,
    var isError: Boolean = false,
    @IgnoredOnParcel val apolloApiError: ApolloApiError? = null,
    @IgnoredOnParcel val error: Throwable? = null
) : Parcelable {

    /**
     * Partial state
     *
     * @constructor Create empty Partial state
     */
    sealed class SocialLoginPartialState {
        object Loading : SocialLoginPartialState() // for simplicity: initial loading & refreshing

        /**
         * Fetched
         *
         * @property signInTokenResponse
         * @constructor Create empty Fetched
         */
        data class Fetched(val signInTokenResponse: SignInTokenResponse?) : SocialLoginPartialState()

        data class VerificationKey(val verificationKey: String?) : SocialLoginPartialState()

        data class GetCountryCode(val countryCode: List<CountryCodeResponse>?) : SocialLoginPartialState()

        /**
         * Error
         *
         * @property throwable
         * @constructor Create empty Error
         */
        data class Error(val throwable: Throwable) : SocialLoginPartialState()
    }
}
