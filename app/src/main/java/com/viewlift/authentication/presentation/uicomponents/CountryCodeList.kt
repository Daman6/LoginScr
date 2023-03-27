package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.viewlift.common.ui.Typography
import com.viewlift.network.data.remote.model.response.CountryCodeResponse


@Composable
fun CountryCodeList(countryCodeResponses: List<CountryCodeResponse>?, onDismiss: (CountryCodeResponse?) -> Unit) {

    Column {

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                Text("Select a Country", modifier = Modifier.padding(horizontal = 10.dp), style = Typography.labelMedium)
            }

            Row {

                Icon(Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        onDismiss(defaultCountry)
                    })
                Spacer(modifier = Modifier.width(10.dp))
            }

        }

        countryCodeResponses?.let{countryCodes->
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(countryCodes) { model ->
                    CountryCard(model, onDismiss)
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
fun CountryCard(code: CountryCodeResponse?, onDismiss: (CountryCodeResponse?) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
            onDismiss(code)
        }
    ) {
        val modifier: Modifier = Modifier
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.weight(8f)
            ) {
            AsyncImage(
                model = code?.image ?: com.viewlift.common.R.drawable.emptyflag,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(25.dp)
                    .padding(start = 3.dp),
            )

            Spacer(modifier = Modifier.width(10.dp))
            Text(text = code?.name ?: "",
                style = Typography.bodySmall)
        }
        Spacer(modifier = modifier.weight(.4f))

        Text(text = code?.dial_code ?: "", modifier = modifier.weight(1.6f), style = Typography.bodySmall)
    }
}