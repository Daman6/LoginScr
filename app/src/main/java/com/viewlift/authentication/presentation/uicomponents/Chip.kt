package com.viewlift.authentication.presentation.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.viewlift.authentication.presentation.authUtils.parse
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.common.label.UpdateScreenColors
import com.viewlift.common.R
import com.viewlift.network.BootStrapQuery

@Composable
fun UpdateAccountTeamsCard(
    content: @Composable () -> Unit,
    selectedChipBgColor : Color = Color.White,
    modifier : Modifier = Modifier,
    viewModel: UpdateAccountViewModel,
    teams: BootStrapQuery.Teams?,
    index: Int
) {


    val isSelect = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val backgroundColor = if (isSelect.value) selectedChipBgColor else UpdateScreenColors.disabledCTAColor.parse
    val borderColor = if (isSelect.value) UpdateScreenColors.activeCTAColor.parse else Transparent

    if(isSelect.value){
        // Team selected
        if(!viewModel.listOfTeams.contains(teams?.values?.get(index)?.title)){
            viewModel.listOfTeams.add(teams?.values?.get(index)?.title!!)
        }
    } else {
        if(viewModel.listOfTeams.contains(teams?.values?.get(index)?.title)){
            viewModel.listOfTeams.remove(teams?.values?.get(index)?.title!!)
        }
    }

    Column(
        modifier = modifier
            .border(
                width = 3.dp,
                color = borderColor,
                shape = shape
            )
            .background(
                color = backgroundColor,
                shape = shape
            )
            .clip(shape = shape)
            .toggleable(
                value = isSelect.value,
                onValueChange = {
                    isSelect.value = it
            })
            .aspectRatio(1f)
    ) {
        Box(contentAlignment = Alignment.TopEnd){
            content()
            if (isSelect.value){
                Image (
                    painter = painterResource(R.drawable.checked),
                    contentDescription = "",
                    modifier = Modifier
                        .height(25.dp)
                        .width(25.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

    }
}


