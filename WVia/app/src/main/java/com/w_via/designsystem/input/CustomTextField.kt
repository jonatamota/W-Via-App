package com.w_via.designsystem.input

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.w_via.R

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    maxLength: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    showClearButton: Boolean = true,
    borderRadius: Dp = 22.dp,
    cursorColor: Color = MaterialTheme.colorScheme.onBackground,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    placeholderColor: Color = MaterialTheme.colorScheme.outline
) {
    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isLabelUp = value.isNotEmpty() || isFocused

    val reserveHeight by animateDpAsState(
        targetValue = if (isLabelUp) 20.dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "reserveHeight"
    )

    val labelOffsetY by animateDpAsState(
        targetValue = if (isLabelUp) (-20).dp else 16.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "labelOffsetY"
    )

    val labelScale by animateFloatAsState(
        targetValue = if (isLabelUp) 0.75f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "labelScale"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (isLabelUp) 1f else 0.5f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "labelAlpha"
    )

    // Efeito de brilho na borda quando focado
    val borderGlow by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "borderGlow"
    )

    // Cor da borda com animação
    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> focusedBorderColor
            else -> unfocusedBorderColor
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "borderColor"
    )

    val verticalPadding by animateDpAsState(
        targetValue = if (isLabelUp) 12.dp else 16.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "verticalPadding"
    )

    Column(modifier = modifier.fillMaxWidth()) {

        Spacer(modifier = Modifier.height(reserveHeight))

        Box(modifier = Modifier.fillMaxWidth()) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isFocused) 8.dp else 1.dp,
                        shape = RoundedCornerShape(borderRadius),
                        clip = false,
                        ambientColor = focusedBorderColor.copy(alpha = borderGlow * 0.2f),
                        spotColor = focusedBorderColor.copy(alpha = borderGlow * 0.1f)
                    ),
                shape = RoundedCornerShape(borderRadius),
                color = backgroundColor,
                border = BorderStroke(1.dp, animatedBorderColor)
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = if (showClearButton || isPassword) 8.dp else 16.dp,
                                top = verticalPadding,
                                bottom = verticalPadding
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Ícone inicial (opcional)
                        if (leadingIcon != null) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                leadingIcon()
                            }
                        }

                        // Campo de texto principal
                        Box(modifier = Modifier.weight(1f)) {
                            // Placeholder
                            if (value.isEmpty() && placeholder.isNotEmpty() && !isFocused) {
                                Text(
                                    text = placeholder,
                                    color = placeholderColor,
                                    fontSize = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            val visualTransformation = if (isPassword && !passwordVisible) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            }

                            BasicTextField(
                                value = if (maxLength != Int.MAX_VALUE && value.length > maxLength) {
                                    value.take(maxLength)
                                } else {
                                    value
                                },
                                onValueChange = { newValue ->
                                    if (newValue.length <= maxLength) {
                                        onValueChange(newValue)
                                    }
                                },
                                cursorBrush = SolidColor(cursorColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        isFocused = focusState.isFocused
                                    },
                                enabled = enabled,
                                readOnly = readOnly,
                                singleLine = singleLine,
                                maxLines = maxLines,
                                textStyle = TextStyle(
                                    color = textColor,
                                    fontSize = 16.sp
                                ),
                                visualTransformation = visualTransformation,
                                keyboardOptions = keyboardOptions,
                                keyboardActions = keyboardActions,
                                decorationBox = { innerTextField ->
                                    innerTextField()
                                }
                            )
                        }

                        // Botão de visibilidade (para senhas)
                        if (isPassword && value.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(30.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar senha"
                                        else "Mostrar senha",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Botão de limpar (X)
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (value.isNotEmpty() && showClearButton && !isPassword)
                                        MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (value.isNotEmpty() && showClearButton && !isPassword) {
                                IconButton(
                                    onClick = {
                                        onValueChange("")
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_doc_svgrepo_com),
                                        contentDescription = "Limpar",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Contador de caracteres (se maxLength definido)
                    if (maxLength != Int.MAX_VALUE) {
                        val charCount = value.length
                        val charColor by animateColorAsState(
                            targetValue = when {
                                charCount > maxLength * 0.9 -> MaterialTheme.colorScheme.error
                                charCount > maxLength * 0.7 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                            animationSpec = tween(300),
                            label = "charColor"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "$charCount/$maxLength",
                                color = charColor,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Label como sibling do Surface — pode ultrapassar a borda
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    color = labelColor.copy(alpha = labelAlpha),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp)
                        .offset(y = labelOffsetY)
                        .graphicsLayer {
                            scaleX = labelScale
                            scaleY = labelScale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .background(backgroundColor)
                        .padding(horizontal = 4.dp)
                        .zIndex(1f),
                    maxLines = 1
                )
            }
        }

        // Mensagem de erro
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 12.dp, top = 4.dp)
                    .fillMaxWidth()
            )
        }
    }
}