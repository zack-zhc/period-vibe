# 历史记录页面空状态调整实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为历史记录页面添加公用顶部标题区域，并按原型重新设计空状态

**Architecture:** 修改 HistoryScreen.kt 文件，添加顶部标题组件，重写 EmptyState 组件，保持其他功能不变

**Tech Stack:** Kotlin, Jetpack Compose, Material3

---

### Task 1: 添加公用顶部标题区域

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt`

- [ ] **Step 1: 添加 Header 组件**

在 HistoryScreen.kt 文件末尾（DeleteConfirmDialog 之前）添加：

```kotlin
@Composable
private fun HistoryHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "你的旅程",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "追踪你的周期，发现规律和洞察",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

- [ ] **Step 2: 在 HistoryContent 中添加 Header**

修改 HistoryContent 函数的 LazyColumn，在 stats cards 之前添加 header：

```kotlin
item {
    HistoryHeader()
}
```

放在第 245 行 `item {` 之前的位置。

- [ ] **Step 3: 在 EmptyState 中添加 Header**

修改 EmptyState 函数，把 Header 放在内容的最上方。

- [ ] **Step 4: 验证修改**

检查代码编译无误，header 在有数据和无数据状态下都显示。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "feat(history): add common header section"
```

---

### Task 2: 重写 EmptyState 组件

**Files:**
- Modify: `app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt:520-572`

- [ ] **Step 1: 添加 Icons.Default.Add 导入**

在文件顶部的 import 区域添加：
```kotlin
import androidx.compose.material.icons.filled.Add
```

- [ ] **Step 2: 替换 EmptyState 组件**

用以下代码替换现有的 EmptyState：

```kotlin
@Composable
private fun EmptyState(
    onNavigateHomeToRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        HistoryHeader()
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp, 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "还没有记录",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "记录你的第一次经期，开始追踪你的旅程并发现你个人的周期规律。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateHomeToRecord,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("去记录")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: 验证修改**

检查代码编译无误，空状态按原型显示。

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/example/periodvibe/ui/history/HistoryScreen.kt
git commit -m "feat(history): redesign empty state per prototype"
```

---

### Task 3: 最终验证

**Files:**
- 无新文件修改

- [ ] **Step 1: 运行应用验证**

构建并运行应用，验证：
- 有数据时显示顶部标题 + 统计卡片 + 时间线
- 无数据时显示顶部标题 + 新的空状态卡片
- 深色/浅色主题都正常显示
- 编辑模式功能正常

- [ ] **Step 2: 运行测试**

```bash
./gradlew test
```
确保所有测试通过。

- [ ] **Step 3: 最终提交（可选）**

如果需要的话，合并提交或创建最终验证提交。

---

## 计划自审查

**1. Spec 覆盖率:**
- ✅ 公用顶部区域 - Task 1
- ✅ 空状态设计按原型 - Task 2
- ✅ 统计卡片不变 - 确认不修改
- ✅ 其他功能保持不变 - 确认不修改

**2. 无占位符:**
- ✅ 所有代码完整
- ✅ 所有步骤明确
- ✅ 文件路径准确

**3. 类型一致:**
- ✅ 组件名称一致
- ✅ 导入语句完整
