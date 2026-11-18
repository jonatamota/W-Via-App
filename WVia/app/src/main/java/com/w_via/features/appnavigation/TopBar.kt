package com.w_via.features.appnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.w_via.features.utils.ClickDebounce
import com.w_via.R
import com.w_via.ui.theme.colorBrand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackPress: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    userScaleFactor: Float = 1f
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    if (showBackButton) {
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .align(Alignment.CenterStart)
                                .size(24.dp)
                                .clip(RoundedCornerShape(0.dp))
                                .clickable(onClick = { if (ClickDebounce.canClick()) onBackPress?.invoke() })
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.back_arrow_previous_svgrepo_com),
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = (22.sp * userScaleFactor),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = if (showBackButton) 48.dp else 0.dp)
                    )
                }
            },
            actions = {
                Row(
                    modifier = Modifier.padding(end = 12.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions?.invoke(this@TopAppBar)
                }
            },
            modifier = Modifier
                .height(54.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorBrand,
                scrolledContainerColor = Color.Unspecified,
                navigationIconContentColor = Color.Unspecified,
                titleContentColor = MaterialTheme.colorScheme.primary,
                actionIconContentColor = Color.Unspecified
            ),
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar(title = "Atividades", showBackButton = true)
}

@Preview(showBackground = true)
@Composable
fun TopBarNoBackButtonPreview() {
    TopBar(title = "Atividades", showBackButton = false)
}
