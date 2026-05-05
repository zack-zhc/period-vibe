# 历史记录页面重新设计 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 根据 HTML 原型调整历史记录页面，添加统计卡片，移除单独删除按钮，保留批量编辑功能

**Architecture:** 修改 HistoryScreen.kt 中的组件，保持 ViewModel 不变

**Tech Stack:** Kotlin, Jetpack Compose, Hilt

---

### Task 1: 添加统计卡片区域 - TimelineHeader 重构

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt:265-325`

- [ ] **Step 1: 修改 TimelineHeader 为统计卡片网格**

替换 TimelineHeader 函数为：

```kotlin
@Composable
private fun StatsCards(
    totalCycles: Int,
    avgCycleLength: Int?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 总周期数卡片
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "总周期数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalCycles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = periodColor
                )
            }
        }

        // 平均周期卡片
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "平均周期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (avgCycleLength != null) "$avgCycleLength 天" else "-- 天",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = periodColor
                )
            }
        }
    }
}
```

- [ ] **Step 2: 更新 HistoryContent 中的调用**

在 HistoryContent 的 LazyColumn 中，将 `TimelineHeader` 替换为 `StatsCards`，并计算平均周期：

```kotlin
item {
    val avgCycleLength = remember(cycles) {
        val validCycles = cycles.mapNotNull { it.cycleLengthDays }
        if (validCycles.isNotEmpty()) validCycles.average().toInt() else null
    }
    StatsCards(
        totalCycles = cycles.size,
        avgCycleLength = avgCycleLength,
        isDark = isDark
    )
}
```

- [ ] **Step 3: 检查编译**

Run: `./gradlew compileDebugKotlin`
Expected: 编译成功

---

### Task 2: 修改 TimelineCycleCard - 移除删除按钮并调整样式

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt:327-469`

- [ ] **Step 1: 修改 TimelineCycleCard 签名**

移除 `onDeleteClick` 参数，更新函数签名：

```kotlin
@Composable
private fun TimelineCycleCard(
    cycleWithRecords: CycleWithRecords,
    isExpanded: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    isLatest: Boolean,  // 新增：是否是最新周期
    onClick: () -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 2: 更新 TimelineCycleCard 实现**

替换完整实现，重点修改：
1. 移除删除按钮
2. 调整卡片背景色（最新周期用 surface-container-high，其他用 surface-container-low）
3. 调整时间轴圆点样式
4. 添加周期天数标签

```kotlin
@Composable
private fun TimelineCycleCard(
    cycleWithRecords: CycleWithRecords,
    isExpanded: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    isLatest: Boolean,
    onClick: () -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded) 2.dp else 0.dp,
        label = "card_elevation"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isSelected -> periodColor
                    isLatest -> periodColor
                    else -> periodColor.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(modifier = Modifier.weight(1f))
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = when {
                isSelected -> periodColor.copy(alpha = 0.1f)
                isExpanded -> periodColor.copy(alpha = 0.05f)
                isLatest -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
            tonalElevation = cardElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditMode) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) periodColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = cycleWithRecords.startDateFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // 周期天数标签
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isLatest) periodColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${cycleWithRecords.periodDaysCount} 天",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isLatest) periodColor else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // 周期长度描述
                Text(
                    text = if (cycleWithRecords.cycleLengthDays != null) {
                        "周期长度: ${cycleWithRecords.cycleLengthDays} 天"
                    } else {
                        "周期长度: -- 天"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedVisibility(
                    visible = isExpanded && !isEditMode,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "每日记录",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        cycleWithRecords.records.forEach { record ->
                            DailyRecordRow(
                                record = record,
                                onEditClick = { onRecordEditClick(record) },
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: 更新 HistoryContent 中的调用**

修改 items 块中的调用，添加 `isLatest` 参数（第一个元素为最新）：

```kotlin
itemsIndexed(cycles, key = { _, it -> it.cycle.id }) { index, cycleWithRecords ->
    TimelineCycleCard(
        cycleWithRecords = cycleWithRecords,
        isExpanded = selectedCycleId == cycleWithRecords.cycle.id,
        isEditMode = isEditMode,
        isSelected = selectedCycles.contains(cycleWithRecords.cycle.id),
        isLatest = index == 0,
        onClick = { onCycleClick(cycleWithRecords.cycle.id) },
        onRecordEditClick = onRecordEditClick,
        isDark = isDark
    )
}
```

同时需要移除 `onDeleteClick` 参数的传递。

- [ ] **Step 4: 移除 HistoryScreen 中的删除对话框相关代码**

删除以下内容：
1. `cycleToDelete` state（第93行）
2. `showDeleteDialog` 的 collectAsState（第87行）
3. `HistoryContent` 调用中的 `onDeleteClick` 参数（第161-164行）
4. 底部的 `DeleteConfirmDialog`（第182-193行）
5. ViewModel 中不再需要的参数引用

- [ ] **Step 5: 检查编译**

Run: `./gradlew compileDebugKotlin`
Expected: 编译成功

---

### Task 3: 清理 ViewModel 中不再需要的代码（可选但推荐）

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryViewModel.kt`

- [ ] **Step 1: 移除单个删除相关代码**

保留 `deleteSelectedCycles()`，可以移除：
- `showDeleteDialog` / `hideDeleteDialog`（第77-91行）
- `deleteCycle`（第101-112行）

但是为了保持代码兼容性，这一步可以跳过，或者只做清理。如果选择清理：

```kotlin
// 移除这两个 state
// private val _showDeleteDialog = MutableStateFlow<Long?>(null)
// val showDeleteDialog: StateFlow<Long?> = _showDeleteDialog.asStateFlow()

// 移除这两个函数
// fun showDeleteDialog(cycleId: Long) { ... }
// fun hideDeleteDialog() { ... }
// fun deleteCycle(cycleId: Long) { ... }
```

- [ ] **Step 2: 检查编译**

Run: `./gradlew compileDebugKotlin`
Expected: 编译成功

---

### Task 4: 整体测试和验证

**Files:** 无需修改

- [ ] **Step 1: 构建并安装应用**

Run: `./gradlew installDebug`
Expected: 安装成功

- [ ] **Step 2: 手动测试功能**

检查项：
- [ ] 统计卡片正确显示总周期数和平均周期
- [ ] 点击周期可以展开/折叠查看每日记录
- [ ] 编辑模式可以多选并批量删除
- [ ] 没有单独的删除按钮
- [ ] 深色/浅色主题正常
- [ ] 空状态显示正常

---

### Task 5: 提交更改

**Files:** 所有修改过的文件

- [ ] **Step 1: 查看修改的文件**

Run: `git status`
Expected: HistoryScreen.kt 被修改，可能 HistoryViewModel.kt 被修改

- [ ] **Step 2: 添加并提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git add docs/superpowers/specs/2026-05-05-history-screen-redesign.md
git add docs/superpowers/plans/2026-05-05-history-screen-redesign.md
git commit -m "feat: 重新设计历史记录页面 - 添加统计卡片，移除单独删除按钮"
```

---

## 自审检查

**1. Spec Coverage:**
- ✅ 统计卡片区域（Task 1）
- ✅ 移除单独删除按钮（Task 2）
- ✅ 保留批量编辑功能（Task 2）
- ✅ 时间轴样式调整（Task 2）
- ✅ 所有文本使用中文

**2. Placeholder Scan:**
- ✅ 无占位符，所有代码完整

**3. Type Consistency:**
- ✅ 函数签名一致，参数匹配
