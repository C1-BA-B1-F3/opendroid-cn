#!/usr/bin/env python3
"""
apply_i18n.py — OpenDroid 中文版 (opendroid-cn) 的界面文案本地化 codemod。

作用
----
把 UI 调用点里的英文字面量包一层运行时翻译函数 `tr(...)`：

    Text("Settings")                   -> Text(tr("Settings"))
    Text(text = "Settings")            -> Text(text = tr("Settings"))
    contentDescription = "Back"        -> contentDescription = tr("Back")
    placeholder = "Type here"          -> placeholder = tr("Type here")
    Toast.makeText(ctx, "Saved", ...)  -> Toast.makeText(ctx, tr("Saved"), ...)
    showSnackbar("Done")               -> showSnackbar(tr("Done"))
    setContentTitle("OpenDroid")       -> setContentTitle(tr("OpenDroid"))
    NotificationChannel(id, "Name", ..)-> NotificationChannel(id, tr("Name"), ..)

`tr()` 运行时查 assets/i18n/zh.json：命中返回中文，未命中回退英文，永不抛异常。
因此多包一层是安全的，漏包才会漏翻译 —— 本脚本宁多勿漏。

关键性质
--------
* 幂等：已包过 tr(...) 的调用点不会被重复包裹（哨兵保护）。
* 可重放：上游同步后重跑本脚本即可重新施加改造，无需手工解冲突。
* 可报告：--report 导出所有被包裹的英文字符串及出处，供翻译使用。

用法
----
    python3 tools/apply_i18n.py --dry-run
    python3 tools/apply_i18n.py
    python3 tools/apply_i18n.py --report i18n/extracted.json

License: Apache-2.0 (same as upstream)
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
from collections import OrderedDict

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
SRC_ROOT = os.path.join(REPO_ROOT, "app", "src", "main", "java")
TARGET_PREFIX = os.path.join(SRC_ROOT, "com", "opendroid", "ai")

EXTRA_FILES = [
    os.path.join(TARGET_PREFIX, "core", "service", "OpenDroidService.kt"),
    os.path.join(TARGET_PREFIX, "core", "llm", "ModelDownloadForegroundInfoFactory.kt"),
]

IMPORT_LINE = "import com.opendroid.ai.i18n.tr"
APP_TEXT_IMPORT = "import com.opendroid.ai.i18n.AppText"
TEXT_CALL = re.compile(r'(?<![A-Za-z0-9_.])Text\(')
NOTICE = ("// Modified by opendroid-cn (Chinese localization fork): UI strings routed "
          "through i18n.tr(). See NOTICE.")

SENTINEL = "\x00TR_OPEN\x00"                 # 保护既有 tr("
STR = r'"((?:[^"\\]|\\.)*)"'                 # 双引号字符串（含转义）

SKIP_VALUE = re.compile(
    r'^(?:https?://|content://|file://|android\.|com\.|java\.|kotlin\.|'
    r'@string/|@drawable/|@mipmap/|'
    r'[A-Za-z0-9_]+\.(?:kt|java|xml|json|png|jpg|db|mp3|txt))$'
)

# 每条规则只需匹配到"字面量开始处"；替换时把该字面量整体换成 tr("...")
RULES: list[re.Pattern] = [
    re.compile(r'\b(?:App)?Text\(\s*' + STR),                      # Text("...") / AppText("...")
    re.compile(r'\b(?:App)?Text\(\s*text\s*=\s*' + STR),           # Text(text = "...")
    re.compile(r'contentDescription\s*=\s*' + STR),                # contentDescription = "..."
    re.compile(r'placeholder\s*=\s*' + STR),                       # placeholder = "..."
    re.compile(r'Toast\.makeText\([^,]+,\s*' + STR),               # Toast.makeText(ctx, "...")
    re.compile(r'showSnackbar\(\s*' + STR),                        # showSnackbar("...")
    re.compile(r'setContent(?:Title|Text)\(\s*' + STR),            # .setContentTitle("...")
    re.compile(r'NotificationChannel\([^,]+,\s*' + STR),           # NotificationChannel(id, "...")
]

# 仅在 ui/ 目录下应用：这些参数名在非 UI 代码里可能是数据字段或状态变量
RULES_UI: list[re.Pattern] = [
    re.compile(r'\btitle\s*=\s*' + STR),                           # title = "..."
    re.compile(r'\bsubtitle\s*=\s*' + STR),                        # subtitle = "..."
    re.compile(r'\bdescription\s*=\s*' + STR),                     # description = "..."
    re.compile(r'\bactionLabel\s*=\s*' + STR),                     # actionLabel = "..."
    re.compile(r'\.value\s*=\s*' + STR),                           # _statusMessage.value = "..."
    re.compile(r'(?m)^([ \t]*)' + STR + r'(?:[ \t]*[+,)]?[ \t]*)$'),  # 行首字面量（长文/拼接续行，含以 , ) 结尾）
]

MAX_LEN = 4000


def find_targets() -> list[str]:
    out = []
    for dirpath, _dirs, files in os.walk(TARGET_PREFIX):
        for fn in files:
            if fn.endswith(".kt"):
                out.append(os.path.join(dirpath, fn))
    for f in EXTRA_FILES:
        if os.path.isfile(f) and f not in out:
            out.append(f)
    return sorted(out)


def should_skip(value: str) -> bool:
    if not value.strip():
        return True
    if SKIP_VALUE.match(value):
        return True
    if len(value) > MAX_LEN:
        return True
    # 含未转义 $ 的插值串：运行时值已被替换，查表永远命中不了，
    # 交由 tools/apply_templates.py 做原地翻译。
    if re.search(r'(?<!\\)\$', value):
        return True
    return False


def ensure_import(src: str, line: str = IMPORT_LINE) -> str:
    if line in src:
        return src
    m = re.search(r'^package\s+[\w.]+\s*$', src, re.M)
    if not m:
        return src
    rest = src[m.end():].lstrip("\n")
    return src[: m.end()] + "\n\n" + line + "\n\n" + rest


def ensure_notice(src: str) -> str:
    return src if NOTICE in src else NOTICE + "\n" + src


def transform(src: str, collected: "OrderedDict[str, list[str]]", relpath: str) -> tuple[str, int]:
    protected = src.replace('tr("', SENTINEL)
    counter = {"n": 0}
    rules = list(RULES)
    if "/ui/" in relpath.replace("\\", "/"):
        rules += RULES_UI

    for rx in rules:
        def _sub(m: re.Match, _rx=rx) -> str:
            gi = m.lastindex
            if gi is None:
                return m.group(0)
            value = m.group(gi)
            if should_skip(value):
                return m.group(0)
            offset = m.start(gi) - m.start(0)
            prefix = m.group(0)[: offset - 1]     # 排除开引号，保留字面量之前的原文
            suffix = m.group(0)[m.end(gi) - m.start(0) + 1:]   # 闭引号之后的原文（如 " +"）原样保留
            counter["n"] += 1
            bucket = collected.setdefault(value, [])
            if relpath not in bucket:
                bucket.append(relpath)
            return prefix + 'tr("' + value + '")' + suffix

        protected = rx.sub(_sub, protected)

    out = protected.replace(SENTINEL, 'tr("')

    # ui/ 下的 Compose Text 调用统一改为 AppText：渲染层查表，覆盖"计算型"字符串
    # （枚举标题、状态映射、自定义组件的字符串参数等），且不影响任何比较/键值逻辑。
    renamed = 0
    if "/ui/" in relpath.replace("\\", "/"):
        out, renamed = TEXT_CALL.subn("AppText(", out)
        if renamed:
            out = ensure_import(out, APP_TEXT_IMPORT)

    n = counter["n"]
    if n:
        out = ensure_import(out)
    if n or renamed:
        out = ensure_notice(out)
    return out, n


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--report", metavar="PATH")
    args = ap.parse_args()

    targets = find_targets()
    collected: "OrderedDict[str, list[str]]" = OrderedDict()
    changed = 0
    total = 0

    for path in targets:
        rel = os.path.relpath(path, REPO_ROOT)
        with open(path, encoding="utf-8") as fh:
            src = fh.read()
        new, n = transform(src, collected, rel)
        if n and new != src:
            changed += 1
            total += n
            if not args.dry_run:
                with open(path, "w", encoding="utf-8") as fh:
                    fh.write(new)

    print(f"扫描 .kt 文件        : {len(targets)}")
    print(f"被改写的文件         : {changed}")
    print(f"包裹的字符串调用点   : {total}")
    print(f"去重后的字符串       : {len(collected)}")
    if args.dry_run:
        print("(dry-run，未写入)")

    if args.report:
        os.makedirs(os.path.dirname(args.report), exist_ok=True)
        data = OrderedDict()
        for k, files in sorted(collected.items(), key=lambda kv: (-len(kv[0]), kv[0])):
            data[k] = {"files": files[:4], "len": len(k)}
        with open(args.report, "w", encoding="utf-8") as fh:
            json.dump(data, fh, ensure_ascii=False, indent=2)
        print(f"清单已写入           : {args.report}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
