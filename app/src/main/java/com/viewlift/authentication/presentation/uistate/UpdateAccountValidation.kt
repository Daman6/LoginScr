package com.viewlift.authentication.presentation.uistate

data class UpdateAccountValidation(
    var isEmailValid: Boolean = false,
    var isMobileFieldValid: Boolean = false,
    var isNameFieldValid: Boolean = false,
    var isBettingEnabled: Boolean = false,
    var isLocationPermissionGiven: Boolean = false,
    var isAgeAbove18: Boolean = false
)