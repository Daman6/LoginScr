package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.common.label.UpdateScreenColors
import com.viewlift.core.extensions.collectAsStateWithLifecycle

@Composable
fun SwitchCaseDefault(viewModel: UpdateAccountViewModel) {

    val checkState by viewModel.isBettingValid.collectAsStateWithLifecycle(initialValue = false)

    Switch(
        checked = checkState,
        onCheckedChange = {
            viewModel.isBettingValid.value = it
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = UpdateScreenColors.activeCTAColor.parse,
            uncheckedThumbColor = Color.Gray,
            checkedTrackColor = UpdateScreenColors.activeCTAColor.parse,
            uncheckedTrackColor = Color.Gray)
    )
}