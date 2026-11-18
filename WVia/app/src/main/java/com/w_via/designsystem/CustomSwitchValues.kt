package com.w_via.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.w_via.R
import com.w_via.ui.theme.colorBrand

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = colorBrand,
) {
    val animatedOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        animationSpec = tween(durationMillis = 300),
        label = "thumb_offset"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Gray.copy(alpha = 0.1f),
        animationSpec = tween(durationMillis = 300),
        label = "background_color"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Gray.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 300),
        label = "border_color"
    )

    Box(
        modifier = modifier
            .width(66.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(animatedBackgroundColor)
            .border(1.5.dp, animatedBorderColor, RoundedCornerShape(22.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        // Thumb deslizante — único ícone visível
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .size(30.dp)
                .clip(CircleShape)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            if (checked)
               IconActive()
            else
                IconInative()
        }
    }
}

@Composable
fun IconInative(
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_do_not_disturb_on_24dp),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun IconActive(
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_check_circle_24dp),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CustomSwitchValues(
    modifier: Modifier = Modifier,
    initialValue: Boolean = true,
    onSelectionChange: (Boolean) -> Unit = {}
) {
    CustomSwitch(
        checked = initialValue,
        onCheckedChange = onSelectionChange,
        modifier = Modifier.padding(start = 4.dp)
    )
}