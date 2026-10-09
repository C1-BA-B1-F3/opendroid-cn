#!/usr/bin/env python3
"""
build_worksheet.py — 收集所有「可能显示在界面上」的英文字符串，生成翻译工作表。

覆盖两类：
  A. kind=tr  ：codemod 用 tr("...") 包裹的字面量（精确）。
  B. kind=ui  ：ui/ 目录下其余字符串字面量（由渲染层 AppText 在显示时查表），
                包含枚举标题、状态映射、自定义组件参数等「计算型」文案。

同时给出上下文：所在文件、所属函数、行号、代码片段。

输出：
  i18n/worksheet.json   每条: {en, file, fun, line, snippet, kind, count}
  i18n/WORKSHEET.md     按文件分组的可读版
"""
from __future__ import annotations

import json
import os
import re
import sys
from collections import OrderedDict

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
SRC_ROOT = os.path.join(REPO_ROOT, "app", "src", "main", "java")
TARGET_PREFIX = os.path.join(SRC_ROOT, "com", "opendroid", "ai")

STR = r'"((?:[^"\\]|\\.)*)"'
TR_RX = re.compile(r'\btr\(\s*' + STR)
FUN_RE = re.compile(r'^\s*(?:@\w+[\w.]*\s*)?(?:private\s+|internal\s+|public\s+)?'
                    r'(?:suspend\s+)?fun\s+([A-Za-z_][\w]*)')

# B 类过滤：排除非界面文案
NON_UI = re.compile(
    r'^(?:https?://|content://|file://|android\.|com\.|java\.|kotlin\.|'
    r'[a-z][a-z0-9_]*$|'                        # 纯小写标识符/路由名/键名
    r'@[a-z]+/|'
    r'#|'
    r'[A-Za-z0-9_]+\.(?:kt|java|xml|json|png|jpg|db|mp3|txt)$|'
    r'.*(?:yyyy|MM-dd|HH:mm|%\.?\d?[dfs]).*$|'  # 日期/格式串
    r'[A-Z0-9_]{1,3}$)'                         # 仅排除极短大写常量（TAG/API 等）
)
SKIP_EXACT = {"UTF-8", "GET", "POST", "PUT", "DELETE", "OK"}


def ui_plausible(v: str) -> bool:
    if len(v) < 2 or not re.search(r'[A-Za-z]', v):
        return False
    if v in SKIP_EXACT:
        return False
    if NON_UI.match(v):
        return False
    if re.match(r'^[A-Za-z0-9_]+=[A-Za-z0-9_]+$', v):
        return False
    return True


def enclosing_fun(lines: list[str], idx: int) -> str:
    for j in range(idx, max(-1, idx - 400), -1):
        m = FUN_RE.match(lines[j])
        if m:
            return m.group(1)
    return "?"


def main() -> int:
    entries: "OrderedDict[str, dict]" = OrderedDict()

    for dirpath, _dirs, files in os.walk(TARGET_PREFIX):
        for fn in sorted(files):
            if not fn.endswith(".kt"):
                continue
            path = os.path.join(dirpath, fn)
            rel = os.path.relpath(path, REPO_ROOT).replace(
                "app/src/main/java/com/opendroid/ai/", "")
            text = open(path, encoding="utf-8").read()
            lines = text.splitlines()
            line_starts, pos = [], 0
            for ln in lines:
                line_starts.append(pos)
                pos += len(ln) + 1

            def line_of(off: int, _ls=line_starts) -> int:
                lo, hi = 0, len(_ls) - 1
                while lo < hi:
                    mid = (lo + hi + 1) // 2
                    if _ls[mid] <= off:
                        lo = mid
                    else:
                        hi = mid - 1
                return lo

            def record(value: str, ln: int, kind: str) -> None:
                e = entries.get(value)
                if e is None:
                    entries[value] = {
                        "en": value, "file": rel, "fun": enclosing_fun(lines, ln),
                        "line": ln + 1, "snippet": lines[ln].strip()[:160],
                        "kind": kind, "count": 1,
                    }
                else:
                    e["count"] += 1

            # A 类：tr("...")
            for m in TR_RX.finditer(text):
                v = m.group(1)
                if not v.strip() or len(v) > 4000:
                    continue
                record(v, line_of(m.start(1)), "tr")

            # B 类：ui/ 下全部疑似界面字面量
            if "/ui/" in "/" + rel.replace("\\", "/"):
                for m in re.finditer(STR, text):
                    v = m.group(1)
                    if not v.strip() or len(v) > 4000:
                        continue
                    if not ui_plausible(v):
                        continue
                    before = text[max(0, m.start() - 4):m.start()]
                    record(v, line_of(m.start(1)), "tr" if before.endswith("tr(") else "ui")

    out_json = os.path.join(REPO_ROOT, "i18n", "worksheet.json")
    with open(out_json, "w", encoding="utf-8") as fh:
        json.dump(list(entries.values()), fh, ensure_ascii=False, indent=2)

    by_file: "OrderedDict[str, list[dict]]" = OrderedDict()
    for e in entries.values():
        by_file.setdefault(e["file"], []).append(e)

    out_md = os.path.join(REPO_ROOT, "i18n", "WORKSHEET.md")
    with open(out_md, "w", encoding="utf-8") as fh:
        fh.write("# OpenDroid 中文版 · 翻译工作表（带上下文）\n\n")
        fh.write(f"共 {len(entries)} 条去重文案，来自 {len(by_file)} 个文件。\n\n")
        for f, items in by_file.items():
            fh.write(f"\n## {f}  ({len(items)} 条)\n\n")
            for e in sorted(items, key=lambda x: x["line"]):
                fh.write(f"- `{e['en']}`\n")
                fh.write(f"  - {e['fun']} · L{e['line']} · {e['kind']} · x{e['count']}\n")
                fh.write(f"  - `{e['snippet']}`\n")

    kinds: dict[str, int] = {}
    for e in entries.values():
        kinds[e["kind"]] = kinds.get(e["kind"], 0) + 1
    print(f"文案条数      : {len(entries)}   {kinds}")
    print(f"涉及文件      : {len(by_file)}")
    print(f"JSON          : {out_json}")
    print(f"Markdown      : {out_md}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
