package com.example.periodvibe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.rounded.Insights
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

// 获取今日贴士
fun getTodayTip(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRATION -> listOf(
            "多喝热水，注意保暖",
            "避免剧烈运动",
            "补充铁质，吃些红肉或菠菜",
            "保证充足睡眠",
            "可以用暖水袋缓解不适"
        ).random()
        CyclePhase.FOLLICULAR -> listOf(
            "能量回升，适合开始新计划",
            "新陈代谢加快，是运动好时机",
            "皮肤状态变好，注意保湿",
            "可以尝试新的食谱",
            "精力充沛，适合社交活动"
        ).random()
        CyclePhase.OVULATION -> listOf(
            "精力最旺盛的时期",
            "注意避孕（如果需要）",
            "性欲可能会增强",
            "白带可能增多，注意清洁",
            "把握好状态，做重要的事"
        ).random()
        CyclePhase.FERTILE -> listOf(
            "处于受孕期，注意身体变化",
            "保持轻松心情",
            "如果在备孕，可以安排同房",
            "注意私处清洁",
            "保持规律作息"
        ).random()
        CyclePhase.LUTEAL -> listOf(
            "可能会有情绪波动",
            "注意放松，听听音乐",
            "可能出现乳房胀痛",
            "饮食清淡，避免过咸",
            "可以做些轻度运动缓解压力"
        ).random()
        CyclePhase.SAFE -> listOf(
            "享受这段平稳的时光",
            "可以安排些轻松的活动",
            "保持规律作息",
            "是工作学习的好时期",
            "好好休息，为下周期做准备"
        ).random()
    }
}

// 合并信息卡片：怀孕几率 + 今日贴士
@Composable
fun CombinedInfoCard(
    phase: CyclePhase,
    modifier: Modifier = Modifier
) {
    val pregnancyChance = getPregnancyChance(phase)
    val todayTip = getTodayTip(phase)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 怀孕几率部分
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BubbleChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "怀孕几率".uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = pregnancyChance.first,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = if (pregnancyChance.third) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        },
                        contentColor = if (pregnancyChance.third) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        shape = RoundedCornerShape(50),
                        tonalElevation = 0.dp
                    ) {
                        Text(
                            text = pregnancyChance.second,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 分隔线
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // 今日贴士部分
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "今日贴士".uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = todayTip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// 空状态功能预告卡片
@Composable
fun FeaturePreviewCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "记录后可获得".uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            // 功能列表
            FeatureItem(
                icon = Icons.Default.EventRepeat,
                title = "周期预测",
                description = "预测下次月经时间"
            )
            FeatureItem(
                icon = Icons.Default.Lightbulb,
                title = "个性化贴士",
                description = "根据阶段给出建议"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
