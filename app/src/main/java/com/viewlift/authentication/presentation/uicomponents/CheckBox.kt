package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.R
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.core.utils.sdp
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun UpdateCheckbox(viewModel: UpdateAccountViewModel = hiltViewModel()) {
    val checkboxState by viewModel.checkboxValue.collectAsStateWithLifecycle()

    if(!checkboxState){
        Image(
            painter = painterResource(com.viewlift.common.R.drawable.unchecked),
            contentDescription = "",
            modifier = Modifier
                .height(28.sdp)
                .width(28.sdp)
                .clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                },
            contentScale = ContentScale.Fit
        )
    } else {
        Image(
            painter = painterResource(com.viewlift.common.R.drawable.checked),
            contentDescription = "",
            modifier = Modifier
                .height(28.sdp)
                .width(28.sdp)
                .clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun DefaultCheckbox(viewModel: AuthViewModel = hiltViewModel(),
) {

    val checkboxState by viewModel.checkboxValue.collectAsStateWithLifecycle()

    if(!checkboxState){
        Image(
            painter = painterResource(com.viewlift.common.R.drawable.unchecked),
            contentDescription = "",
            modifier = Modifier
                .height(28.sdp)
                .width(28.dp)
                .clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                },
            contentScale = ContentScale.Fit
        )
    } else {
        Image(
            painter = painterResource(com.viewlift.common.R.drawable.checked),
            contentDescription = "",
            modifier = Modifier
                .height(28.sdp)
                .width(28.sdp)
                .clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                },
            contentScale = ContentScale.Fit
        )
    }
}



