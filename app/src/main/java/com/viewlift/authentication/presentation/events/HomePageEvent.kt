package com.viewlift.authentication.presentation.events

/**
 * Page event
 * @author Anand
 *
 * @constructor Create empty Page event
 */
sealed class HomePageEvent {
    /**
     * Open details page
     *
     * @property uri
     * @constructor Create empty Open details page
     */
    data class OpenDetailsPage(val uri: String) : HomePageEvent()
}
