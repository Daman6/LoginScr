package com.viewlift.authentication.presentation.uicomponents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.common.ui.AppCMSTheme
import com.viewlift.common.ui.Typography


@Composable
fun TextViewSubtitle(
    text: String,
    color: String,
    centerAligned: Boolean = false,
    isBold: Boolean = false
) {
    // 14 sp text size
    Text(
        text = text,
        color = color.parse,
        fontSize = 12.sp,
        textAlign = if(centerAligned) TextAlign.Center else TextAlign.Start,
        fontWeight = if(isBold) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun TextViewTitle(
    text: String,
    color: String,
    centerAligned: Boolean = false,
    isBold: Boolean = false
) {
    // 24 sp text size
    Text(
        text = text,
        color = color.parse,
        style = Typography.titleLarge,
        textAlign = if(centerAligned) TextAlign.Center else TextAlign.Start,
        fontWeight = if(isBold) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
fun TextViewUnderLined(
    modifier: Modifier = Modifier,
    text: String,
    color: String,
    centerAligned: Boolean = false,
    isBold: Boolean = false,
) {
    // 12 sp text size
    Text(
        text = text,
        style = TextStyle(
            textDecoration = TextDecoration.Underline
        ),
        color = color.parse,
        fontSize = 12.sp,
        textAlign = if(centerAligned) TextAlign.Center else TextAlign.Start,
        fontWeight = if(isBold) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
    )
}


@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
private fun CardPreview() {
    AppCMSTheme {
        Row {
//            AppleLoginButton()
//            GoogleLoginButton()
        }
    }
}
