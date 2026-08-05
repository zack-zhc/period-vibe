package com.example.periodvibe.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Lightbulb
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodvibe.domain.model.CyclePhase
import com.example.periodvibe.ui.theme.HomeCardShape
import com.example.periodvibe.ui.theme.cardBorderStroke

// 获取怀孕几率信息（中性表述：受孕期/非受孕期）
data class PregnancyInfo(
    val isFertile: Boolean,
    val label: String
)

fun getPregnancyInfo(currentPhase: CyclePhase): PregnancyInfo {
    return when (currentPhase) {
        CyclePhase.OVULATION, CyclePhase.FERTILE -> PregnancyInfo(isFertile = true, label = "受孕期")
        else -> PregnancyInfo(isFertile = false, label = "非受孕期")
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
    nextPhaseName: String = "",
    daysUntilNextPhase: Int = 0,
    modifier: Modifier = Modifier
) {
    val pregnancyInfo = getPregnancyInfo(phase)
    val todayTip = getTodayTip(phase)

    Card(
        modifier = modifier,
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = cardBorderStroke()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 下一阶段部分
            if (nextPhaseName.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "下一阶段".uppercase(),
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
                            text = nextPhaseName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50),
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = if (daysUntilNextPhase == 0) "今天" else "$daysUntilNextPhase 天后",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 分隔线
            if (nextPhaseName.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )
            }

            // 怀孕几率部分
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BubbleChart,
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
                Text(
                    text = pregnancyInfo.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (pregnancyInfo.isFertile) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
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
                        imageVector = Icons.Rounded.AutoAwesome,
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

// 空状态功能预告卡片
@Composable
fun FeaturePreviewCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = cardBorderStroke()
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
                    imageVector = Icons.Rounded.AutoAwesome,
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
                icon = Icons.Rounded.EventRepeat,
                title = "周期预测",
                description = "预测下次月经时间"
            )
            FeatureItem(
                icon = Icons.Rounded.Lightbulb,
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
