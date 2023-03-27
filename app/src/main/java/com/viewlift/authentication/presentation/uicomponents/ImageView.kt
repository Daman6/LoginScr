package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale

@Composable
fun ImageViewDefault(icon: Int, contentDescription: String? = null, modifier: Modifier = Modifier){
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription ?: "",
        modifier = modifier,
        contentScale = ContentScale.FillHeight
    )
}


@Composable
fun ImageViewUpdate(imageUrl: String?, contentDescription: String? = null, modifier: Modifier = Modifier){
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        )
}

