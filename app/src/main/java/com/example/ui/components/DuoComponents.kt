package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuoGrayLight
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark

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
