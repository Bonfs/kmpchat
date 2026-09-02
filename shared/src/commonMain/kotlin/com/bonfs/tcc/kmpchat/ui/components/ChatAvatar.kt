package com.bonfs.tcc.kmpchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AvatarColors = listOf(
    Color(0xFFE57373),
    Color(0xFF81C784),
    Color(0xFF64B5F6),
    Color(0xFFFFB74D),
    Color(0xFFBA68C8),
    Color(0xFF4DB6AC),
    Color(0xFFFF8A65),
    Color(0xFF9575CD)
)

@Composable
fun ChatAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val initial = extractInitial(name)
    val backgroundColor = selectColorByIdentifier(name)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}

private fun extractInitial(name: String): String {
    val clean = name.trim().removePrefix("@").removePrefix("!")
    return if (clean.isNotEmpty()) {
        clean.first().uppercase()
    } else {
        "?"
    }
}

private fun selectColorByIdentifier(identifier: String): Color {
    val index = (identifier.hashCode() and 0x7FFFFFFF) % AvatarColors.size
    return AvatarColors[index]
}
