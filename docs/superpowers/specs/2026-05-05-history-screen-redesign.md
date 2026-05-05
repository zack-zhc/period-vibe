# 历史记录页面重新设计

## 概述

根据提供的 HTML 原型调整历史记录页面，保留批量编辑功能，移除单独删除按钮，优化界面布局。

## 修改内容

### 1. 顶部统计卡片区域

- 将原有的 TimelineHeader（单个卡片）改为两列网格布局
- 左侧卡片：显示"总周期数"（Total Cycles）
- 右侧卡片：显示"平均周期"（Avg. Cycle）
- 样式参考原型：surface-container-high 背景，圆角卡片

### 2. 时间轴卡片调整

- **移除单独删除按钮**：移除每个周期右上角的删除图标
- **优化时间轴样式**：
  - 左侧时间轴圆点保持突出显示
  - 卡片背景色调整（最新周期用 surface-container-high，历史用 surface-container-low）
  - 日期范围显示
  - 周期天数标签（如"6天"）
  - 周期长度描述
- **保留功能**：
  - 点击展开/折叠查看每日记录
  - 编辑模式下的多选功能

### 3. 批量编辑功能

- 完整保留顶部"编辑"按钮
- 完整保留 EditModeBottomBar（删除和取消按钮）
- 保持选中状态的视觉反馈

## 组件修改列表

| 组件 | 修改内容 |
|------|---------|
| HistoryScreen.kt | 重构 HistoryContent，添加统计卡片区域，修改 TimelineCycleCard |
| MiniTimeline.kt | 可能需要调整样式以匹配原型（可选） |
| HistoryViewModel.kt | 无需修改 |
| DailyRecordRow.kt | 无需修改 |
| EditModeBottomBar.kt | 无需修改 |

## 数据需求

- 总周期数：已存在（cycles.size）
- 平均周期长度：需要计算所有周期的平均值（cycleLengthDays）

## 注意事项

- 所有文本使用中文
- 保持深色/浅色主题支持
- 保持动画效果
