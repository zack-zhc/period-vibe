# Period Vibe - UI/UX 设计指南

## 1. 设计系统概述

### 1.1 设计理念
Period Vibe 遵循 Google Material Design 3 (Material You) 设计规范，打造符合 Android 原生体验的现代化界面。设计核心原则：

- **直观性**：用户无需学习即可理解界面
- **一致性**：遵循 Android 原生设计语言
- **可访问性**：确保所有用户都能使用
- **美观性**：简洁优雅的视觉呈现

### 1.2 设计目标
- 符合 Material Design 3 规范
- 支持 Dynamic Color（动态取色）
- 适配不同屏幕尺寸和折叠屏
- 流畅的动画和过渡效果
- 优秀的可访问性支持

---

## 2. Material Design 3 规范应用

### 2.1 色彩系统（Material 3 Color Scheme）

#### 2.1.1 基础色彩方案
采用 Material 3 的色调板（Tonal Palette）系统，基于主色调生成完整的色彩体系。

**主色调（Primary）**：
- Primary: `#FF8BA7`
- On Primary: `#FFFFFF`
- Primary Container: `#FFD9E0`
- On Primary Container: `#3E001D`

**次要色调（Secondary）**：
- Secondary: `#74565F`
- On Secondary: `#FFFFFF`
- Secondary Container: `#FFD9E2`
- On Secondary Container: `#2B151B`

**第三色调（Tertiary）**：
- Tertiary: `#7C5635`
- On Tertiary: `#FFFFFF`
- Tertiary Container: `#FFDCC1`
- On Tertiary Container: `#2C1500`

**中性色调（Neutral）**：
- Background: `#FFF8F8`
- On Background: `#2C1B1E`
- Surface: `#FFF8F8`
- On Surface: `#2C1B1E`
- Surface Variant: `#F2DEE3`
- On Surface Variant: `#514347`

**错误色调（Error）**：
- Error: `#BA1A1A`
- On Error: `#FFFFFF`
- Error Container: `#FFDAD6`
- On Error Container: `#410002`

#### 2.1.2 功能性色彩
**周期状态色彩**：
- 经期标记: `#FF6B6B` (红色系)
- 排卵期标记: `#4ECDC4` (青色系)
- 安全期标记: `#95E1D3` (绿色系)
- 易孕期标记: `#FFB6C1` (粉色系)

#### 2.1.3 动态取色（Dynamic Color）
支持 Android 12+ 的动态取色功能，从用户壁纸提取主题色：
```kotlin
// 动态取色实现示例
val dynamicColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    dynamicColorScheme(LocalContext.current)
} else {
    staticColorScheme
}
```

### 2.2 字体系统（Typography）

#### 2.2.1 字体族
使用 Google Fonts 的 Roboto 字体家族：

```kotlin
// Material 3 Typography Scale
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize: 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

#### 2.2.2 字体使用规范
- **页面标题**：headlineLarge (32sp)
- **卡片标题**：titleLarge (22sp)
- **列表标题**：titleMedium (16sp)
- **正文内容**：bodyLarge (16sp)
- **辅助文字**：bodyMedium (14sp)
- **按钮文字**：labelLarge (14sp)
- **标签文字**：labelMedium (12sp)

### 2.3 形状系统（Shape）

Material 3 使用圆角形状系统，提供更友好的视觉体验：

```kotlin
// Material 3 Shape Scale
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
    full = CircleShape
)
```

**形状使用规范**：
- **按钮**：full（圆形）或 large（16dp）
- **卡片**：medium（12dp）或 large（16dp）
- **输入框**：small（8dp）
- **对话框**：large（16dp）
- **底部弹窗**：extraLarge（28dp，仅顶部圆角）
- **FAB**：full（圆形）

### 2.4 间距系统（Spacing）

使用 8dp 基础网格系统：

```kotlin
// 间距规范
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
```

**间距使用场景**：
- **页面边距**：lg (16dp)
- **卡片内边距**：md (12dp) 或 lg (16dp)
- **元素间距**：sm (8dp)
- **列表项间距**：xs (4dp)
- **按钮内边距**：水平 lg (16dp)，垂直 sm (8dp)

### 2.5 图标系统（Icon）

使用 Material Symbols 图标库，统一使用 Outlined 风格：

**常用图标**：
- 首页: `home_outlined`
- 日历: `calendar_month_outlined`
- 历史: `history_outlined`
- 设置: `settings_outlined`
- 添加: `add_outlined`
- 编辑: `edit_outlined`
- 删除: `delete_outlined`
- 通知: `notifications_outlined`
- 统计: `bar_chart_outlined`
- 锁: `lock_outlined`
- 主题: `palette_outlined`

**图标尺寸**：
- 导航栏图标: 24dp
- FAB 图标: 24dp
- 列表图标: 24dp
- 小图标: 18dp
- 大图标: 48dp

---

## 3. 组件设计规范

### 3.1 按钮（Buttons）

#### 3.1.1 Filled Button（填充按钮）
用于主要操作，如保存、确认等。

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.full
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

#### 3.1.2 Outlined Button（轮廓按钮）
用于次要操作，如取消、返回等。

```kotlin
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = ButtonDefaults.outlinedButtonBorder,
        shape = MaterialTheme.shapes.full
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

#### 3.1.3 FAB（Floating Action Button）
用于快速记录功能，固定在右下角。

```kotlin
@Composable
fun RecordFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.full
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "记录",
            modifier = Modifier.size(24.dp)
        )
    }
}
```

### 3.2 卡片（Cards）

#### 3.2.1 Elevated Card（提升卡片）
用于展示重要信息，带有阴影效果。

```kotlin
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}
```

#### 3.2.2 Outlined Card（轮廓卡片）
用于次要信息展示，无边框。

```kotlin
@Composable
fun SecondaryCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}
```

### 3.3 输入框（Text Fields）

#### 3.3.1 Outlined Text Field（轮廓输入框）
```kotlin
@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            shape = MaterialTheme.shapes.small,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
```

### 3.4 开关（Switch）

```kotlin
@Composable
fun PeriodSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
```

### 3.5 选择器（Chips）

#### 3.5.1 Filter Chip（筛选芯片）
用于经量选择等场景。

```kotlin
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
```

#### 3.5.2 Input Chip（输入芯片）
用于已选症状展示。

```kotlin
@Composable
fun InputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
```

### 3.6 底部导航栏（Bottom Navigation）

```kotlin
@Composable
fun PeriodBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Calendar,
            BottomNavItem.History,
            BottomNavItem.Settings
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        label = "首页",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    object Calendar : BottomNavItem(
        route = "calendar",
        label = "日历",
        unselectedIcon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth
    )
    object History : BottomNavItem(
        route = "history",
        label = "历史",
        unselectedIcon = Icons.Outlined.History,
        selectedIcon = Icons.Filled.History
    )
    object Settings : BottomNavItem(
        route = "settings",
        label = "设置",
        unselectedIcon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
}
```

### 3.7 顶部应用栏（Top App Bar）

```kotlin
@Composable
fun PeriodTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = {
            if (actions != null) {
                actions()
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
```

### 3.8 底部弹窗（Bottom Sheet）

```kotlin
@Composable
fun PeriodBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}
```

### 3.9 对话框（Dialog）

```kotlin
@Composable
fun PeriodDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface
    )
}
```

---

## 4. 页面布局设计

### 4.1 首页布局

```kotlin
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PeriodTopAppBar(
                title = "Period Vibe"
            )
        },
        floatingActionButton = {
            RecordFAB(
                onClick = { viewModel.showRecordSheet() }
            )
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "home",
                onNavigate = { /* 导航逻辑 */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 周期状态卡片
            item {
                CycleStatusCard(
                    cycleDay = uiState.cycleDay,
                    daysUntilPeriod = uiState.daysUntilPeriod,
                    cyclePhase = uiState.cyclePhase
                )
            }

            // 今日记录摘要
            item {
                TodayRecordCard(
                    record = uiState.todayRecord,
                    onClick = { viewModel.showRecordSheet() }
                )
            }

            // 快捷操作
            item {
                QuickActions(
                    onCalendarClick = { /* 导航到日历 */ },
                    onHistoryClick = { /* 导航到历史 */ }
                )
            }
        }
    }
}
```

### 4.2 日历页面布局

```kotlin
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PeriodTopAppBar(
                title = "日历",
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "calendar",
                onNavigate = { /* 导航逻辑 */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 月份切换
            MonthSelector(
                currentMonth = uiState.currentMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            // 日历网格
            CalendarGrid(
                month = uiState.currentMonth,
                records = uiState.records,
                predictions = uiState.predictions,
                onDateClick = { date -> viewModel.showRecordSheet(date) }
            )

            // 选中日期信息
            SelectedDateInfo(
                date = uiState.selectedDate,
                record = uiState.selectedDateRecord
            )
        }
    }
}
```

### 4.3 历史记录页面布局

```kotlin
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PeriodTopAppBar(
                title = "历史记录"
            )
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "history",
                onNavigate = { /* 导航逻辑 */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.cycles) { cycle ->
                CycleCard(
                    cycle = cycle,
                    onClick = { viewModel.showCycleDetail(cycle.id) },
                    onLongClick = { viewModel.showDeleteDialog(cycle.id) }
                )
            }
        }
    }
}
```

### 4.4 设置页面布局

```kotlin
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PeriodTopAppBar(
                title = "设置"
            )
        },
        bottomBar = {
            PeriodBottomNavigation(
                currentRoute = "settings",
                onNavigate = { /* 导航逻辑 */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 周期参数设置
            item {
                SettingsSection(title = "周期参数") {
                    CycleSettings(
                        cycleLength = uiState.cycleLength,
                        periodLength = uiState.periodLength,
                        onCycleLengthChange = { viewModel.updateCycleLength(it) },
                        onPeriodLengthChange = { viewModel.updatePeriodLength(it) }
                    )
                }
            }

            // 提醒设置
            item {
                SettingsSection(title = "提醒设置") {
                    NotificationSettings(
                        notificationsEnabled = uiState.notificationsEnabled,
                        reminderTime = uiState.reminderTime,
                        onNotificationsToggle = { viewModel.toggleNotifications(it) },
                        onReminderTimeChange = { viewModel.updateReminderTime(it) }
                    )
                }
            }

            // 主题设置
            item {
                SettingsSection(title = "主题设置") {
                    ThemeSettings(
                        darkMode = uiState.darkMode,
                        onDarkModeChange = { viewModel.setDarkMode(it) }
                    )
                }
            }

            // 数据管理
            item {
                SettingsSection(title = "数据管理") {
                    DataManagement(
                        onExportData = { viewModel.exportData() },
                        onImportData = { viewModel.importData() },
                        onClearData = { viewModel.showClearDataDialog() }
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    AboutSection(
                        version = uiState.version,
                        onPrivacyPolicyClick = { /* 打开隐私政策 */ },
                        onUserAgreementClick = { /* 打开用户协议 */ }
                    )
                }
            }
        }
    }
}
```

---

## 5. 交互设计

### 5.1 点击反馈（Ripple Effect）

所有可点击元素必须提供涟漪效果反馈：

```kotlin
// Material 3 自动提供涟漪效果
// 使用以下组件时会自动应用：
// - Button
// - Card (with onClick)
// - NavigationBarItem
// - ListItem
// - IconButton

// 自定义可点击区域
@Composable
fun ClickableRipple(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ),
        content = content
    )
}
```

### 5.2 过渡动画（Transitions）

#### 5.2.1 页面切换动画
```kotlin
// 使用 AnimatedContent 实现页面切换动画
@Composable
fun AnimatedPageSwitch(
    targetState: Any,
    content: @Composable (targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it }
            ) + fadeIn() with slideOutHorizontally(
                targetOffsetX = { -it }
            ) + fadeOut()
        },
        label = "page_transition"
    ) { state ->
        content(state)
    }
}
```

#### 5.2.2 底部弹窗动画
```kotlin
// 使用 AnimatedVisibility 实现弹窗动画
@Composable
fun AnimatedBottomSheet(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        content()
    }
}
```

### 5.3 手势操作（Gestures）

#### 5.3.1 滑动切换月份
```kotlin
@Composable
fun SwipeableMonthView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeableState(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .swipeable(
                state = swipeState,
                anchors = mapOf(
                    -1f to -1,
                    0f to 0,
                    1f to 1
                ),
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.5f) }
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // 点击处理
                }
            }
    ) {
        content()

        LaunchedEffect(swipeState.currentValue) {
            if (swipeState.currentValue != 0) {
                onMonthChange(
                    if (swipeState.currentValue > 0) {
                        currentMonth.plusMonths(1)
                    } else {
                        currentMonth.minusMonths(1)
                    }
                )
                swipeState.snapTo(0)
            }
        }
    }
}
```

#### 5.3.2 长按操作
```kotlin
@Composable
fun LongPressableItem(
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        content()
    }
}
```

---

## 6. 动效设计

### 6.1 动画时长标准

遵循 Material Design 的动画时长规范：

```kotlin
object AnimationDuration {
    const val ExtraShort = 150  // 快速反馈
    const val Short = 200       // 常规过渡
    const val Medium = 300      // 复杂动画
    const val Long = 400        // 重要动画
    const val ExtraLong = 500   // 特殊场景
}
```

### 6.2 缓动函数（Easing）

```kotlin
// Material 3 标准缓动函数
val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
val LinearEasing = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
```

### 6.3 弹簧动画（Spring）

```kotlin
// 使用弹簧动画实现自然效果
val springSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)
```

---

## 7. 适配规范

### 7.1 屏幕尺寸适配

```kotlin
// 响应式尺寸定义
@Composable
fun WindowSizeClass.currentWindowSize(): WindowSize {
    val widthSizeClass = when {
        widthSizeClass == WindowWidthSizeClass.Compact -> WindowSize.Compact
        widthSizeClass == WindowWidthSizeClass.Medium -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
    return widthSizeClass
}

enum class WindowSize {
    Compact,   // 手机竖屏
    Medium,    // 平板/折叠屏展开
    Expanded   // 大屏设备
}
```

### 7.2 横竖屏适配

```kotlin
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
```

### 7.3 折叠屏适配

```kotlin
@Composable
fun FoldableLayout(
    modifier: Modifier = Modifier,
    content: @Composable (WindowSize) -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass(LocalActivity.current)

    content(windowSizeClass.widthSizeClass)
}
```

---

## 8. 可访问性设计

### 8.1 内容描述（Content Description）

所有图标和交互元素必须提供内容描述：

```kotlin
Icon(
    imageVector = Icons.Outlined.Add,
    contentDescription = "添加记录"  // 必须提供
)

Button(
    onClick = { /* ... */ }
) {
    Icon(
        imageVector = Icons.Outlined.Save,
        contentDescription = null  // 按钮文字已说明，可为null
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text("保存")
}
```

### 8.2 语义化标签（Semantics）

```kotlin
@Composable
fun AccessibleCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.semantics {
            this.heading()
            this.contentDescription = "$title, $description"
        }
    ) {
        // 卡片内容
    }
}
```

### 8.3 最小触摸目标

所有可点击元素的最小尺寸为 48dp x 48dp：

```kotlin
@Composable
fun MinTouchTargetButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)  // 确保最小触摸目标
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
```

### 8.4 颜色对比度

确保文字和背景的对比度符合 WCAG AA 标准：

- **普通文字**：至少 4.5:1
- **大号文字（18pt+）**：至少 3:1
- **UI 组件**：至少 3:1

### 8.5 字体缩放

支持系统字体缩放：

```kotlin
// 使用 Compose 的 Text 组件会自动支持字体缩放
Text(
    text = "可缩放文字",
    style = MaterialTheme.typography.bodyLarge
)
```

---

## 9. 深色模式（Dark Mode）

### 9.1 深色模式色彩方案

```kotlin
@Composable
fun PeriodVibeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)
```

### 9.2 深色模式适配要点

- 使用深色背景减少眼部疲劳
- 降低文字对比度，避免刺眼
- 卡片和背景使用相近的深色调
- 阴影效果减弱或移除
- 图标颜色适配深色主题

---

## 10. 开发实现指南

### 10.1 主题设置

在 `Theme.kt` 中设置 Material 3 主题：

```kotlin
@Composable
fun PeriodVibeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 10.2 组件库组织

建议的组件目录结构：

```
ui/
├── components/
│   ├── buttons/
│   │   ├── PrimaryButton.kt
│   │   ├── SecondaryButton.kt
│   │   └── RecordFAB.kt
│   ├── cards/
│   │   ├── InfoCard.kt
│   │   ├── CycleStatusCard.kt
│   │   └── TodayRecordCard.kt
│   ├── navigation/
│   │   └── PeriodBottomNavigation.kt
│   ├── inputs/
│   │   ├── OutlinedTextField.kt
│   │   └── PeriodSwitch.kt
│   └── sheets/
│       └── PeriodBottomSheet.kt
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   ├── Type.kt
│   └── Shape.kt
└── screens/
    ├── home/
    ├── calendar/
    ├── history/
    └── settings/
```

### 10.3 依赖配置

在 `build.gradle.kts` 中添加 Material 3 依赖：

```kotlin
dependencies {
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
}
```

---

## 11. 设计资源

### 11.1 Material Design 3 官方资源
- [Material Design 3 指南](https://m3.material.io/)
- [Material Symbols 图标库](https://fonts.google.com/icons)
- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)

### 11.2 设计工具
- [Figma Material Design 3 Kit](https://www.figma.com/community/file/1035203688168086539)
- [Android Studio Layout Editor](https://developer.android.com/studio/write/layout-editor)

---

## 12. 设计检查清单

开发完成后，请对照以下清单进行设计审查：

### 12.1 视觉设计
- [ ] 所有页面遵循统一的色彩系统
- [ ] 字体大小和层级符合规范
- [ ] 圆角半径使用一致
- [ ] 间距符合 8dp 网格系统
- [ ] 图标风格统一（Outlined 风格）

### 12.2 交互设计
- [ ] 所有可点击元素有涟漪效果
- [ ] 页面切换有流畅的过渡动画
- [ ] 手势操作响应灵敏
- [ ] 长按操作有视觉反馈

### 12.3 可访问性
- [ ] 所有图标提供内容描述
- [ ] 最小触摸目标 48dp
- [ ] 颜色对比度符合 WCAG AA 标准
- [ ] 支持系统字体缩放

### 12.4 适配性
- [ ] 支持不同屏幕尺寸
- [ ] 支持横竖屏切换
- [ ] 支持深色模式
- [ ] 支持动态取色（Android 12+）

### 12.5 性能
- [ ] 动画流畅度 60fps
- [ ] 页面切换无卡顿
- [ ] 列表滚动流畅
- [ ] 内存占用合理

---

**文档版本**：v1.0
**创建日期**：2025-12-31
**作者**：资深 UI/UX 设计师
**状态**：待评审
