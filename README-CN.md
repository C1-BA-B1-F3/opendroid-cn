# OpenDroid 中文版（opendroid-cn）

> **非官方中文分支。** 本项目基于 [OpenDroid](https://github.com/yashab-cyber/opendroid) 修改，
> 与上游项目**无隶属或背书关系**。仅以 GitHub 形式提供 APK，供**个人使用与交流分享**，**不作商用**。
> `OpenDroid` 这一名称仅用于说明作品来源（Apache-2.0 第 6 条）。

---

## 这是什么

OpenDroid 是一款运行在 Android 手机上的**自主 AI 智能体**：它通过无障碍服务读取屏幕、
操作应用，并借助本地或云端大模型完成任务。

本分支做了两件事：

1. **把界面完整汉化为简体中文**（界面文案、弹窗、错误提示、通知、无障碍描述、隐私政策与使用条款等）；
2. **把汉化做成可重放、可自动同步上游的工程流程**，让上游更新时可以低成本跟进。

除汉化外，**不修改任何业务逻辑**：功能行为与上游一致。

---

## 下载与安装

- 从本仓库的 [Releases](../../releases) 页面下载最新的 `app-debug.apk`（或 CI 构建产物）。
- 安装后按应用内引导授予权限：麦克风、通知使用权、**无障碍服务**、悬浮窗等。
- 在「设置 → 服务商 API 密钥」中填入你自己的 LLM API 密钥（或使用本地 Ollama）。

> 本分支未在应用商店上架；APK 由本仓库的 GitHub Actions 构建，见 `.github/workflows/android-ci.yml`。

---

## 汉化是怎么做的

核心思路：**在渲染层翻译，而不改动逻辑**。

| 机制 | 文件 | 作用 |
|---|---|---|
| 运行时翻译表 | `app/src/main/assets/i18n/zh.json` | 英文原文 → 中文；查不到则回退英文 |
| 翻译函数 | `app/src/main/java/com/opendroid/ai/i18n/I18n.kt` | `tr("English")` |
| 渲染层包装 | `app/src/main/java/com/opendroid/ai/i18n/AppText.kt` | 替换 Compose 的 `Text`，可翻译"计算型"字符串（枚举标题、状态标签等） |
| 字面量改写 | `tools/apply_i18n.py` | 把界面文案包成 `tr(...)`，`Text(...)` 改名为 `AppText(...)` |
| 插值文案 | `tools/apply_templates.py` + `i18n/zh/templates.json` | 含 `$var` 的文案原地翻译 |
| 启动接入 | `tools/apply_bootstrap.py` | 加载翻译表、应用名、README 声明 |

**为什么这样设计**：汉化改造是**确定性、可重放**的——上游同步后重跑脚本即可重新生成，
不需要手工解冲突；手工维护的只有译文 JSON。

### 目录

```
i18n/
  zh/part*.json         译文（英文 → 中文）
  zh/templates.json     含插值的文案（原地替换）
  worksheet.json        带上下文的文案清单（翻译工作表）
  MISSING.md            尚未翻译的清单
tools/
  localize.sh           一键重放本地化流水线
  sync-upstream.sh      同步上游并重放本地化
  apply_i18n.py         字面量改写 / AppText 重命名
  apply_templates.py    插值文案原地翻译
  apply_bootstrap.py    启动接入、应用名、README 声明
  build_worksheet.py    生成翻译工作表
  merge_translations.py 合并译文并校验
```

---

## 同步上游

```bash
git remote add upstream https://github.com/yashab-cyber/opendroid.git   # 首次
bash tools/sync-upstream.sh
```

脚本会：拉取上游 → 合并（Kotlin 冲突一律取上游）→ 重跑本地化流水线 → 列出新增的未翻译文案。

---

## 许可与合规

- 本分支遵循上游的 **Apache License 2.0**（见 `LICENSE`），**未修改许可条款**。
- 依据 Apache-2.0 第 4(b) 条，被修改的文件带有醒目声明；改动说明见 [`NOTICE`](NOTICE)。
- 依据 Apache-2.0 第 6 条，本分支**不主张** "OpenDroid" 商标权，仅将其用于说明来源。
- 上游版权声明予以保留：`Copyright (c) 2026 OpenDroid Contributors`。

---

## 致谢

原始项目由 [Yashab Alam](https://github.com/yashab-cyber) 及 OpenDroid 贡献者开发。
本分支仅做中文本地化与工程化改造。
