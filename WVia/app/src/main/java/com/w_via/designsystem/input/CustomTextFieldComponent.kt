package com.w_via.designsystem.input

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente de TextField personalizado com maior controle sobre aparência e comportamento.
 * Este componente é independente e pode ser usado em qualquer parte do aplicativo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldComponent(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: Int = 16,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.primary,
    placeholder: String? = null,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    onFocusChanged: (FocusState) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textAlign: TextAlign = TextAlign.Start,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    BasicTextField(
        value = value,
        onValueChange = { newValue: String ->
            // Verifica se há um limite de comprimento definido
            val isWithinMaxLength = maxLength?.let { newValue.length <= it } ?: true
            if (isWithinMaxLength) {
                onValueChange(newValue)
            }
        },
        textStyle = TextStyle(
            color = textColor,
            fontSize = fontSize.sp,
            textAlign = textAlign
        ),
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = shape
            )
            .onFocusChanged { focusState: FocusState -> 
                onFocusChanged(focusState)
            },
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                label = label?.let { { Text(text = it, color = labelColor) } },
                placeholder = placeholder?.let {
                    {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = borderColor,
                    focusedBorderColor = focusedBorderColor,
                    disabledBorderColor = borderColor.copy(alpha = 0.5f),
                    unfocusedContainerColor = backgroundColor,
                    focusedContainerColor = backgroundColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = focusedBorderColor,
                            disabledBorderColor = borderColor.copy(alpha = 0.5f)
                        ),
                        shape = shape
                    )
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CustomTextFieldComponentPreview() {
    var text by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomTextFieldComponent(
            value = text,
            onValueChange = { text = it },
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            borderColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            placeholder = "Campo personalizado",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
