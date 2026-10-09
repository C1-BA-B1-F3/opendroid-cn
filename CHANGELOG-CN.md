# 变更记录 — opendroid-cn

本文件记录**本分支相对上游**的改动。上游自身的变更记录见
[上游仓库](https://github.com/yashab-cyber/opendroid) 的 Release 说明。

本分支遵循 Apache-2.0，改动声明见 [`NOTICE`](NOTICE)。

---

## v1.0.7-cn — 2026-10-09

基于上游 `v1.0.7`。

### 新增：界面中文化

- **运行时翻译层**（不改动业务逻辑）：
  - `app/src/main/assets/i18n/zh.json` — 994 条「英文 → 中文」映射，未命中回退英文；
  - `app/src/main/java/com/opendroid/ai/i18n/I18n.kt` — `tr("English")` 查表；
  - `app/src/main/java/com/opendroid/ai/i18n/AppText.kt` — 包装 Compose `Text`，
    用于翻译「计算型」字符串（枚举标题、状态标签等）；
  - `OpenDroidApp.onCreate` 中初始化翻译表。
- 覆盖范围：界面文案、弹窗与错误提示、通知、无障碍描述、隐私政策、
  使用条款、帮助中心、社媒模块。
- 应用显示名改为 **「OpenDroid 中文版」**。

### 新增：可重放的上游同步流程

汉化改造全部由脚本确定性生成，上游更新后可低成本重放：

| 脚本 | 作用 |
|---|---|
| `tools/localize.sh` | 一键重放本地化流水线 |
| `tools/sync-upstream.sh` | 拉取并合并上游，然后重放本地化 |
| `tools/apply_i18n.py` | 字面量包 `tr(...)`、`Text(` → `AppText(` |
| `tools/apply_templates.py` | 含 `$` 插值的文案原地翻译 |
| `tools/apply_bootstrap.py` | 启动接入、应用名、README 声明 |
| `tools/apply_fork_fixes.py` | 分支配套修复（见下） |
| `tools/build_worksheet.py` | 生成带上下文的翻译工作表 |
| `tools/merge_translations.py` | 合并译文并做一致性校验 |
| `tools/update_lint_baseline.py` | 从 CI 的 lint 报告并入 baseline 条目 |

### 新增：签名 APK 发布流水线

- `.github/workflows/release-apk.yml`：推送 `v*` 标签时构建**已签名**的 release APK，
  用 `apksigner verify` 校验签名后创建 GitHub Release 并上传 APK 与 `SHA256SUMS.txt`。
- 支持 `workflow_dispatch` 手动触发以验证密钥配置（不创建 Release）。
- 复用上游 `app/build.gradle` 既有的签名逻辑（凭据经 `ORG_GRADLE_PROJECT_*`
  环境变量注入），**未修改构建脚本**；keystore 不入库。

### 修复

- **单元测试**：`ModelDownloadSchedulingTest` 通知标题断言改用
  `tr("Model download in progress")`。原因：部分 Robolectric 测试未覆盖
  `application`，会实例化真实 `OpenDroidApp` 并调用 `I18n.init`，
  静态状态在同一沙箱内泄漏，导致断言拿到中文。
- **Android Lint `DefaultLocale`**：`SocialActions.kt` 中 3 处
  `String.format("%.1f", x)` → `String.format(Locale.ROOT, "%.1f", x)`。
  该文件未被汉化改动，属上游既有问题。
- **Android Lint 其余既有问题**：上游存在 37 个未纳入 baseline 的 Error
  （`DefaultLocale` / `SdCardPath` / `UseKtx` / `ObsoleteSdkInt` /
  `NonObservableLocale` / `ModifierParameter` / `AutoboxingStateCreation`），
  导致仓库自带的三层 lint 门失败。按仓库自身机制并入 `app/lint-baseline.xml`
  （53 条）。baseline 只掩盖其中列出的条目，**新引入的问题仍会使 lint 失败**。

### 未改动

- 业务逻辑与功能行为与上游一致。
- 大模型提示词保持英文（中文化会改变智能体行为，需独立评估与回归测试）。
- 许可证正文、第三方库列表保持原文。

---

## 维护要点

- 同步上游后需重跑 `tools/localize.sh`；若上游引入新的 lint 问题，
  用新的 lint 报告重跑 `tools/update_lint_baseline.py`。
- 升级安装必须使用同一签名密钥；更换密钥会导致用户无法覆盖安装。
- 上游自带的 `v1.0.3`–`v1.0.7` 标签随 fork 带入，**请勿删除**；
  本分支的发布使用 `v<上游版本>-cn` 形式（如 `v1.0.7-cn`）。
