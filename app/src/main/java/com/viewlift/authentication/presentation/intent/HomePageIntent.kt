package com.viewlift.authentication.presentation.intent

import com.viewlift.network.data.remote.model.request.UpdateInitialProfileParams

/**
 * Page intent
 * @author Anand
 *
 * @constructor Create empty Page intent
 */
sealed class HomePageIntent {

    data class UpdateAccount(val updateAccount: UpdateInitialProfileParams) : HomePageIntent()


}
