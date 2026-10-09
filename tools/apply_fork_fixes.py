#!/usr/bin/env python3
"""
apply_fork_fixes.py — opendroid-cn 分支必需的、与汉化配套的少量修复（幂等、可重放）。

处理两类问题：

1) 上游既有 lint 错误（未进 baseline，会导致 CI lint 失败）
   app/src/main/java/com/opendroid/ai/actions/SocialActions.kt
     String.format("%.1f", x)  ->  String.format(Locale.ROOT, "%.1f", x)
   注：这是上游代码既有的 DefaultLocale 问题（该文件未被汉化改动），
       本分支为保证 CI 绿色顺带修复；不改变功能语义（数字格式更确定）。

2) 单元测试断言需适配汉化后的用户可见文案
   app/src/test/java/com/opendroid/ai/core/llm/ModelDownloadSchedulingTest.kt
     通知标题断言改为 tr("Model download in progress")
   原因：部分 Robolectric 测试未覆盖 application，会实例化真实 OpenDroidApp
       并调用 I18n.init；同一沙箱内静态状态会泄漏，使该断言拿到中文。
       改用 tr(...) 后与是否已初始化无关，断言稳定且语义正确。
"""
from __future__ import annotations

import os
import re
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)

SOCIAL = os.path.join(REPO_ROOT, "app", "src", "main", "java", "com", "opendroid", "ai",
                      "actions", "SocialActions.kt")
TEST = os.path.join(REPO_ROOT, "app", "src", "test", "java", "com", "opendroid", "ai",
                    "core", "llm", "ModelDownloadSchedulingTest.kt")

NOTICE = ("// Modified by opendroid-cn (Chinese localization fork): lint fix "
          "(Locale.ROOT for String.format). See NOTICE.")
FORMAT_OLD = re.compile(r'String\.format\("(%.1f)", ')
FORMAT_NEW = r'String.format(Locale.ROOT, "\1", '

TEST_OLD = 'assertEquals("Model download in progress", notification.extras.getCharSequence(Notification.EXTRA_TITLE))'
TEST_NEW = 'assertEquals(tr("Model download in progress"), notification.extras.getCharSequence(Notification.EXTRA_TITLE))'


def fix_social() -> None:
    src = open(SOCIAL, encoding="utf-8").read()
    orig = src
    n = len(FORMAT_OLD.findall(src))
    if n:
        src = FORMAT_OLD.sub(FORMAT_NEW, src)
    if "import java.util.Locale" not in src:
        src = src.replace("import javax.inject.Inject",
                          "import java.util.Locale\nimport javax.inject.Inject", 1)
    if NOTICE not in src:
        src = NOTICE + "\n" + src
    if src != orig:
        open(SOCIAL, "w", encoding="utf-8").write(src)
    print(f"SocialActions.kt: 修复 String.format 处数 = {n}")


def fix_test() -> None:
    if not os.path.exists(TEST):
        print("ModelDownloadSchedulingTest.kt: 不存在，跳过")
        return
    src = open(TEST, encoding="utf-8").read()
    orig = src
    changed = 0
    if TEST_OLD in src:
        src = src.replace(TEST_OLD, TEST_NEW)
        changed = 1
    if "import com.opendroid.ai.i18n.tr" not in src:
        src = src.replace("import org.junit.Assert.assertEquals",
                          "import com.opendroid.ai.i18n.tr\nimport org.junit.Assert.assertEquals", 1)
    if src != orig:
        open(TEST, "w", encoding="utf-8").write(src)
    print(f"ModelDownloadSchedulingTest.kt: 断言适配 = {changed}")


def main() -> int:
    fix_social()
    fix_test()
    return 0


if __name__ == "__main__":
    sys.exit(main())
