package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoGray
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoRed

@Composable
fun DuoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = DuoGrayLight,
    shadowColor: Color = DuoGrayLight.copy(alpha = 0.5f),
    borderWidth: Dp = 2.dp,
    shadowHeight: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = shadowHeight)
                .clip(shape)
                .background(shadowColor)
        )
        // Card foreground layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = shadowHeight)
                .clip(shape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, shape)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DuoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = DuoGreen,
    shadowColor: Color = DuoGreenDark,
    contentColor: Color = Color.White,
    borderColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shadowHeight = 4.dp
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = shadowHeight)
                .clip(shape)
                .background(if (enabled) shadowColor else DuoGrayLight.copy(alpha = 0.5f))
        )
        // Foreground
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = shadowHeight)
                .clip(shape)
                .background(if (enabled) containerColor else DuoGrayLight)
                .then(
                    if (borderColor != null && enabled) Modifier.border(2.dp, borderColor, shape) else Modifier
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun DuoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    activeColor: Color = DuoGreen,
    inactiveColor: Color = DuoGrayLight,
    errorColor: Color = DuoRed
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> errorColor
        isFocused -> activeColor
        else -> inactiveColor
    }

    val shadowColor = when {
        isError -> errorColor.copy(alpha = 0.25f)
        isFocused -> activeColor.copy(alpha = 0.25f)
        else -> DuoGrayLight.copy(alpha = 0.5f)
    }

    val labelColor = when {
        isError -> errorColor
        isFocused -> activeColor
        else -> DuoGray
    }

    val shape = RoundedCornerShape(16.dp)
    val shadowHeight = 3.dp

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = shadowHeight)
                    .clip(shape)
                    .background(shadowColor)
            )
            // Foreground input container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = shadowHeight)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, borderColor, shape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = labelColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DuoGray.copy(alpha = 0.6f)
                            )
                        }
                        
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            singleLine = singleLine,
                            visualTransformation = visualTransformation,
                            keyboardOptions = keyboardOptions,
                            interactionSource = interactionSource,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                if (trailingIcon != null) {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        trailingIcon()
                    }
                }
            }
        }
    }
}
