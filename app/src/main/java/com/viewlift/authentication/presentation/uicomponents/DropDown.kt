package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.common.label.LoginScreenColors
import com.viewlift.common.R
import com.viewlift.network.data.remote.model.response.CountryCodeResponse

var defaultCountry: CountryCodeResponse? = null
@Composable
fun CountryCodeDropDown(modifier: Modifier, countryCodeResponses: List<CountryCodeResponse>?) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CommonValues.COMMON_EDITTEXT_HEIGHT)
            .wrapContentSize(Alignment.Center)
            .clip(RoundedCornerShape(8.dp))
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(LoginScreenColors.inputBgColor.parse)
                .clickable(
                    onClick = { expanded = true }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = defaultCountry?.image ?: R.drawable.emptyflag,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(start = 3.dp),
                )
            }

            Image(
                painter = painterResource(R.drawable.arrow_down),
                contentDescription = "",
                modifier = Modifier.weight(0.8f),
                contentScale = ContentScale.FillHeight
            )
        }

        if (expanded) {
            CustomCountryPicker ({ countryCode ->
                defaultCountry = countryCode
                expanded = false
            }, countryCodeResponses)
        }
    }

}
