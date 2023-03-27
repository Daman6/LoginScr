package com.viewlift.authentication.presentation.uistate


sealed class ShowHideBottomSheet{
    object CollapseBottomSheet : ShowHideBottomSheet()
    object ExpandBottomSheet : ShowHideBottomSheet()
}

sealed class ShowEmailOrMobile {
    object NoOptions: ShowEmailOrMobile()
    object DefaultOptions: ShowEmailOrMobile()
    object EmailOptions: ShowEmailOrMobile()
    object MobileOptions: ShowEmailOrMobile()
}