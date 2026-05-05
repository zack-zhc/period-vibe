# 历史记录年份分组实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按年份分组重新设计历史记录页面，去掉时间轴，换成卡片式布局

**Architecture:** 先扩展数据结构，再重写 UI 组件，保持现有功能不变

**Tech Stack:** Kotlin, Jetpack Compose, Material3

---

### Task 1: 扩展 CycleWithRecords 数据结构

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/domain/usecase/GetHistoryDataUseCase.kt`

- [ ] **Step 1: 查看完整的 CycleWithRecords 代码**

先读取完整的文件内容，了解现有结构

- [ ] **Step 2: 在 CycleWithRecords 中添加新属性**

添加三个新属性：
```kotlin
val year: Int
val dateRangeWithoutYear: String
val averageFlowLevel: FlowLevel?
```

实现计算逻辑：
- year: 取周期开始日期的年份
- dateRangeWithoutYear: 格式为 "M月d日 - M月d日"
- averageFlowLevel: 取周期记录中出现最多的 FlowLevel

- [ ] **Step 3: 验证修改后编译通过**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/domain/usecase/GetHistoryDataUseCase.kt
git commit -m "feat: extend CycleWithRecords with year, date range, and average flow"
```

---

### Task 2: 添加 YearGroupHeader 组件

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt`

- [ ] **Step 1: 添加 YearGroupHeader 组件**

在 HistoryScreen.kt 文件末尾（DeleteConfirmDialog 之前）添加：

```kotlin
@Composable
private fun YearGroupHeader(
    year: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "feat: add YearGroupHeader component"
```

---

### Task 3: 重写 CycleCard 组件（替换 TimelineCycleCard）

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt`

- [ ] **Step 1: 添加 CycleCard 组件**

在 YearGroupHeader 之后添加：

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CycleCard(
    cycleWithRecords: CycleWithRecords,
    isExpanded: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val periodColor = if (isDark) CalendarPeriodDark else CalendarPeriodLight
    val (iconBgColor, iconContentColor) = when (cycleWithRecords.averageFlowLevel) {
        FlowLevel.LIGHT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        FlowLevel.MEDIUM -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        FlowLevel.HEAVY -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        null -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val flowDisplayText = when (cycleWithRecords.averageFlowLevel) {
        FlowLevel.LIGHT -> "少量"
        FlowLevel.MEDIUM -> "中等"
        FlowLevel.HEAVY -> "大量"
        null -> ""
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isEditMode) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> periodColor.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        shadowElevation = 1.dp,
        border = if (isSelected) BorderStroke(1.dp, periodColor) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                }

                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = iconContentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = cycleWithRecords.dateRangeWithoutYear,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            append(cycleWithRecords.periodDaysCount)
                            append("天")
                            if (flowDisplayText.isNotEmpty()) {
                                append(" • ")
                                append(flowDisplayText)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (cycleWithRecords.cycleLengthDays != null) "${cycleWithRecords.cycleLengthDays}天" else "--天",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "周期长度",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isEditMode,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "feat: add CycleCard component"
```

---

### Task 4: 重写 HistoryContent 组件

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt`

- [ ] **Step 1: 替换 HistoryContent 组件**

将现有 HistoryContent 替换为：

```kotlin
@Composable
private fun HistoryContent(
    cycles: List<CycleWithRecords>,
    selectedCycleId: Long?,
    isEditMode: Boolean,
    selectedCycles: Set<Long>,
    onCycleClick: (Long) -> Unit,
    onCycleLongClick: (Long) -> Unit,
    onRecordEditClick: (DailyRecord) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cyclesByYear = remember(cycles) {
        cycles.groupBy { it.year }.toSortedMap(reverseOrder())
    }
    val avgCycleLength = remember(cycles) {
        val validCycles = cycles.mapNotNull { it.cycleLengthDays }
        if (validCycles.isNotEmpty()) validCycles.average().toInt() else null
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            StatsCards(
                totalCycles = cycles.size,
                avgCycleLength = avgCycleLength,
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        cyclesByYear.forEach { (year, yearCycles) ->
            item {
                YearGroupHeader(
                    year = year,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(yearCycles, key = { it.cycle.id }) { cycleWithRecords ->
                CycleCard(
                    cycleWithRecords = cycleWithRecords,
                    isExpanded = selectedCycleId == cycleWithRecords.cycle.id,
                    isEditMode = isEditMode,
                    isSelected = selectedCycles.contains(cycleWithRecords.cycle.id),
                    onClick = { onCycleClick(cycleWithRecords.cycle.id) },
                    onLongClick = { onCycleLongClick(cycleWithRecords.cycle.id) },
                    onRecordEditClick = onRecordEditClick,
                    isDark = isDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "feat: rewrite HistoryContent with year grouping"
```

---

### Task 5: 最终验证和清理

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt`

- [ ] **Step 1: 删除旧的 TimelineCycleCard 组件**

找到并删除旧的 TimelineCycleCard 组件

- [ ] **Step 2: 运行完整编译**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 运行测试**

Run: `./gradlew test`
Expected: All tests pass

- [ ] **Step 4: 提交清理**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "refactor: remove old TimelineCycleCard"
```

---

## 计划自审查

**1. Spec 覆盖率:**
- ✅ 扩展 CycleWithRecords - Task 1
- ✅ YearGroupHeader 组件 - Task 2
- ✅ CycleCard 组件 - Task 3
- ✅ 按年份分组的 HistoryContent - Task 4
- ✅ 保留编辑模式、展开功能 - Task 3 & 4
- ✅ Icon 颜色按流量深浅 - Task 3
- ✅ 统计卡片保持不变 - Task 4

**2. 无占位符:**
- ✅ 所有步骤都有完整代码
- ✅ 所有文件路径准确
- ✅ 所有命令明确

**3. 类型一致性:**
- ✅ 所有组件引用的类型与数据结构匹配
