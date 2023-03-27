package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.network.data.remote.model.response.CountryCodeResponse

@Composable
fun CustomCountryPicker(onDismiss: (CountryCodeResponse?) -> Unit, countryCodeResponses: List<CountryCodeResponse>?) {
    Dialog(onDismissRequest = { onDismiss(defaultCountry) }, properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true
    )) {
        Card(
            //shape = MaterialTheme.shapes.medium,
            shape = RoundedCornerShape(10.dp),
            // modifier = modifier.size(280.dp, 240.dp)
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 8.dp
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {

                CountryCodeList(countryCodeResponses, onDismiss)
            }
        }
    }
}
