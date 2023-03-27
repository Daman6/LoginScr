package com.viewlift.authentication.presentation.events

/**
 * Page event
 * @author Anand
 *
 * @constructor Create empty Page event
 */
sealed class LoginEvent {
    /**
     * Open details page
     *
     * @property uri
     * @constructor Create empty Open details page
     */
    data class OpenTvePage(val uri: String) : LoginEvent()
}
