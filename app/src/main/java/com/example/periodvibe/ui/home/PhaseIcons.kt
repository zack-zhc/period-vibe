package com.example.periodvibe.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.ui.graphics.vector.ImageVector

// 简单封装 - 使用Material Icons Rounded版本
object PhaseIcons {
    val Menstruation: ImageVector = Icons.Rounded.Bloodtype
    val Follicular: ImageVector = Icons.Rounded.Eco
    val Ovulation: ImageVector = Icons.Rounded.Favorite
    val Fertile: ImageVector = Icons.Rounded.LocalFlorist
    val Luteal: ImageVector = Icons.Rounded.Nightlight
    val Safe: ImageVector = Icons.Rounded.Shield
}
