# AGENTS.md

## 版本号定义

应用版本号采用 `xx.xx.xx` 三段式格式：`major.minor.bugfix`。

| 位 | 含义 | 何时递增 |
|---|---|---|
| 第一位（major） | 主版本 | 破坏性更新：不向后兼容的改动，如数据格式/数据库结构不兼容、移除既有功能、需要用户重置数据 |
| 第二位（minor） | 次版本 | 功能变化：新增功能、行为变化、依赖/库升级 |
| 第三位（bugfix） | 修订版本 | Bug 修复、性能优化、文案/样式修正等不改变行为的改动 |

### 规则

- 递增高一位时，低位归零（例如：`1.2.5` → `2.0.0`；`1.2.5` → `1.3.0`）。
- 每次提交更新 `app/build.gradle.kts` 中的 `versionName`。
- `versionCode` 单调递增（每次发布 +1，不随 major/minor 归零）。

## 构建与验证

改完代码后**只需验证编译**，不要跑完整打包：

```bash
# 验证代码可编译（最快，日常改代码后必跑）
./gradlew :app:compileDebugKotlin

# 涉及逻辑改动时，补充单元测试
./gradlew :app:testDebugUnitTest

# 仅发布/提交前做一次完整打包验证（耗时，日常不用）
./gradlew :app:assembleDebug
```

- `assembleDebug`（KSP + dex + 打包签名）很耗时，仅在需要产出 APK 时执行。
- 验证编译用 `--console=plain`，任务显示 `BUILD SUCCESSFUL` 即通过。
