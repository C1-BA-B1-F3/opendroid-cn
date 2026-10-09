#!/usr/bin/env python3
"""
apply_bootstrap.py — 幂等地把中文版运行时接入 Application 启动流程。

改动（幂等）：
  1. OpenDroidApp.kt 顶部加 Apache-2.0 §4(b) 要求的"已修改"声明；
  2. 注入 `import com.opendroid.ai.i18n.I18n`；
  3. 在 onCreate 中调用 `I18n.init(this)`（加载 assets/i18n/zh.json）。

该脚本与 apply_i18n.py 一起构成可重放的本地化流水线：上游同步后重跑即可。
"""
from __future__ import annotations

import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
APP = os.path.join(REPO_ROOT, "app", "src", "main", "java", "com", "opendroid", "ai",
                   "OpenDroidApp.kt")
STRINGS_XML = os.path.join(REPO_ROOT, "app", "src", "main", "res", "values", "strings.xml")
README_MD = os.path.join(REPO_ROOT, "README.md")

README_BANNER = """> **这是 OpenDroid 的**非官方中文分支**（opendroid-cn）。**
> 界面已汉化，仅以 GitHub 形式提供 APK，供个人使用与交流分享，**不作商用**。
> 本项目与上游 OpenDroid 项目无隶属或背书关系；"OpenDroid" 仅用于说明来源（Apache-2.0 §6）。
> 上游：https://github.com/yashab-cyber/opendroid · 中文说明见 [README-CN.md](README-CN.md)

"""

APP_NAME_OLD = '<string name="app_name">OpenDroid</string>'
APP_NAME_NEW = '<string name="app_name">OpenDroid 中文版</string>'
A11Y_OLD = ("Provides autonomous UI interaction, screen reading, and navigation assistance "
            "for OpenDroid Agent control.")
A11Y_NEW = "为 OpenDroid 智能体提供自主界面交互、屏幕读取与导航辅助。"

NOTICE = ("// Modified by opendroid-cn (Chinese localization fork): loads the Chinese UI "
          "translation table.\n// See NOTICE.")
IMPORT = "import com.opendroid.ai.i18n.I18n"
IMPORT_AFTER = "import com.opendroid.ai.data.crash.CrashLogRepository"
INIT_ANCHOR = "        installCrashHandler()"
INIT_BLOCK = (
    "        installCrashHandler()\n"
    "\n"
    "        // opendroid-cn: load the Chinese UI translation table before any UI is composed.\n"
    "        // Safe: missing/empty table falls back to English.\n"
    "        I18n.init(this)"
)


def main() -> int:
    src = open(APP, encoding="utf-8").read()
    orig = src
    changed = []

    if NOTICE not in src:
        src = NOTICE + "\n" + src
        changed.append("notice")

    if IMPORT not in src:
        if IMPORT_AFTER not in src:
            print("ERROR: 找不到 import 锚点", file=sys.stderr)
            return 1
        src = src.replace(IMPORT_AFTER, IMPORT_AFTER + "\n" + IMPORT, 1)
        changed.append("import")

    if "I18n.init(this)" not in src:
        if INIT_ANCHOR not in src:
            print("ERROR: 找不到 onCreate 锚点", file=sys.stderr)
            return 1
        src = src.replace(INIT_ANCHOR, INIT_BLOCK, 1)
        changed.append("init")

    if src != orig:
        open(APP, "w", encoding="utf-8").write(src)
    print(f"OpenDroidApp.kt 改动: {changed or '无（已是最新）'}")

    # 应用名与无障碍服务描述（清单/系统界面可见）
    xml = open(STRINGS_XML, encoding="utf-8").read()
    xml2 = xml.replace(APP_NAME_OLD, APP_NAME_NEW).replace(A11Y_OLD, A11Y_NEW)
    if xml2 != xml:
        open(STRINGS_XML, "w", encoding="utf-8").write(xml2)
        print("strings.xml 改动: ['app_name', 'accessibility_service_desc']")
    else:
        print("strings.xml 改动: 无（已是最新）")

    # README 顶部加非官方分支声明（幂等）
    if os.path.exists(README_MD):
        md = open(README_MD, encoding="utf-8").read()
        if README_BANNER not in md:
            open(README_MD, "w", encoding="utf-8").write(README_BANNER + md)
            print("README.md 改动: ['banner']")
        else:
            print("README.md 改动: 无（已是最新）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
