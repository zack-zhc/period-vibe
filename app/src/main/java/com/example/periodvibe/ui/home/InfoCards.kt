package com.example.periodvibe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodvibe.domain.model.CyclePhase

// 通用信息卡片组件
@Composable
fun InfoCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.material3.CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = elevation,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
            content()
        }
    }
}

// 下一阶段卡片组件
@Composable
fun NextPhaseCard(
    phaseName: String,
    daysUntil: Int,
    modifier: Modifier = Modifier
) {
    InfoCard(
        icon = Icons.Default.AutoAwesome,
        iconTint = MaterialTheme.colorScheme.primary,
        title = "下一阶段",
        modifier = modifier
    ) {
        Text(
            text = phaseName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (daysUntil == 0) "今天" else "$daysUntil 天后",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 怀孕几率卡片组件
@Composable
fun PregnancyChanceCard(
    chanceLevel: String,
    chanceLabel: String,
    isFertile: Boolean,
    modifier: Modifier = Modifier
) {
    InfoCard(
        icon = Icons.Default.BubbleChart,
        iconTint = MaterialTheme.colorScheme.tertiary,
        title = "怀孕几率",
        modifier = modifier
    ) {
        Text(
            text = chanceLevel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Surface(
                color = if (isFertile) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                },
                contentColor = if (isFertile) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                shape = RoundedCornerShape(50),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = chanceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// 空状态卡片组件
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    InfoCard(
        icon = icon,
        iconTint = MaterialTheme.colorScheme.primary,
        title = title,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontStyle = FontStyle.Italic
        )
        Spacer(
            modifier = Modifier
                .height(4.dp)
                .width(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }
}

// 获取下一阶段信息
fun getNextPhase(currentPhase: CyclePhase): Pair<String, Int> {
    val nextPhaseName = when (currentPhase) {
        CyclePhase.MENSTRATION -> "卵泡期"
        CyclePhase.FOLLICULAR -> "排卵期"
        CyclePhase.OVULATION, CyclePhase.FERTILE -> "黄体期"
        CyclePhase.LUTEAL, CyclePhase.SAFE -> "月经期"
    }
    // 这里的天数是示例，实际应该根据周期数据计算
    return Pair(nextPhaseName, 12)
}

// 获取怀孕几率信息
fun getPregnancyChance(currentPhase: CyclePhase): Triple<String, String, Boolean> {
    return when (currentPhase) {
        CyclePhase.OVULATION, CyclePhase.FERTILE -> Triple("高", "受孕期", true)
        CyclePhase.FOLLICULAR -> Triple("低", "非受孕期", false)
        CyclePhase.LUTEAL, CyclePhase.SAFE -> Triple("很低", "非受孕期", false)
        CyclePhase.MENSTRATION -> Triple("最低", "非受孕期", false)
    }
}
