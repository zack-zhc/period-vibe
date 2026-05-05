# 历史记录年份分组设计

## 概述

按年份分组重新设计历史记录页面，去掉时间轴设计，换成卡片式布局。

## 修改内容

### 1. 数据结构扩展

在 `CycleWithRecords` 中添加：
- `year: Int` - 用于分组的年份（取周期开始日期的年份）
- `dateRangeWithoutYear: String` - 不含年份的日期范围（如 "12月12日 - 12月17日"）
- `averageFlowLevel: FlowLevel?` - 周期的平均经量级别（取记录中出现最多的流量级别，用于确定 icon 背景色深浅）

### 2. UI 组件调整

#### 2.1 YearGroupHeader（新增）
- 年份标题（headline-md 样式，primary 颜色）
- 右侧分隔线（outline-variant 颜色）

#### 2.2 CycleCard（重写 TimelineCycleCard）
- 左侧：WaterDrop icon（圆形背景，颜色根据流量深浅变化：LIGHT → surface-variant，MEDIUM → primary-container，HEAVY → primary）
- 中间上部：日期范围（不含年份）
- 中间下部：天数 + 流量级别（如 "6天 • 中等"）
- 右侧：周期长度（如 "28天"）
- 编辑模式：左侧复选框，选中时卡片高亮
- 点击：展开/收起每日记录
- 长按：删除单个周期（非编辑模式下）

#### 2.3 DailyRecordList（保持现有）
- 展开后显示每日记录，保持现有样式和功能

### 3. 布局结构

```
LazyColumn
  ├─ StatsCards (保持不变)
  ├─ YearGroupHeader (2023)
  ├─ CycleCard (12月12日 - 12月17日)
  ├─ [可选展开的 DailyRecordList]
  ├─ CycleCard (11月14日 - 11月19日)
  ├─ YearGroupHeader (2022)
  └─ CycleCard (12月20日 - 12月25日)
```

### 4. 保持不变的功能

- 统计卡片样式
- 编辑模式（多选删除）
- 点击展开查看每日记录
- 长按删除单个周期
- 编辑每日记录
- 深色/浅色主题支持
- EmptyState

## 修改文件列表

| 文件 | 修改内容 |
|------|---------|
| GetHistoryDataUseCase.kt | 扩展 CycleWithRecords，添加 year、dateRangeWithoutYear、averageFlowLevel |
| HistoryScreen.kt | 重写 HistoryContent、CycleCard，添加 YearGroupHeader |

## 注意事项

- 所有文字使用中文
- 流量级别显示：LIGHT → "少量"，MEDIUM → "中等"，HEAVY → "大量"
- 日期格式："M月d日 - M月d日"
- Icon 背景色映射：
  - LIGHT → surfaceVariant
  - MEDIUM → primaryContainer
  - HEAVY → primary
