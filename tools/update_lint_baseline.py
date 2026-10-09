#!/usr/bin/env python3
"""
update_lint_baseline.py — 把 lint 报告中的 Error 级问题并入 lint-baseline.xml。

用途
----
本分支（opendroid-cn）继承了上游若干**未纳入 baseline 的既有 lint 问题**
（DefaultLocale / SdCardPath / UseKtx / ObsoleteSdkInt / NonObservableLocale /
ModifierParameter / AutoboxingStateCreation），导致仓库自带的三层 lint 门失败。
按仓库自身机制，这类"冻结的历史问题"应写入 `app/lint-baseline.xml`。

这些与汉化无关，不改动源码行为；baseline 只掩盖其中列出的条目，
**新引入的问题仍会失败**，门禁依然有效。

用法
----
    # 在 CI 产出 lint 报告后（artifact: lint-report）
    python3 tools/update_lint_baseline.py /path/to/lint-results-debug.xml

幂等：已存在的条目不会重复添加。
"""
from __future__ import annotations

import os
import sys
import xml.etree.ElementTree as ET

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
BASELINE = os.path.join(REPO_ROOT, "app", "lint-baseline.xml")

KEEP_ATTRS = ("id", "message", "errorLine1", "errorLine2")


def entry_key(issue: ET.Element) -> tuple:
    """按 (id, file, line, column) 唯一标识一条发现。

    注意：不能只用 (id, file, message) —— 同一文件内同类问题消息完全相同，
    会被错误折叠成一条，导致其余位置未被 baseline 覆盖。
    file 统一为模块相对路径，便于与 baseline 中既有条目比对。
    """
    loc = issue.find("location")
    if loc is None:
        return (issue.get("id"), "", "", "")
    f = loc.get("file", "")
    marker = "/app/"
    idx = f.find(marker)
    if idx != -1:
        f = f[idx + len(marker):]
    return (issue.get("id"), f, loc.get("line", ""), loc.get("column", ""))


def main() -> int:
    if len(sys.argv) < 2:
        print("用法: update_lint_baseline.py <lint-results-debug.xml>", file=sys.stderr)
        return 2
    report = sys.argv[1]

    rep_root = ET.parse(report).getroot()
    base_root = ET.parse(BASELINE).getroot()

    existing = {entry_key(i) for i in base_root.findall("issue")}
    added = 0
    for issue in rep_root.findall("issue"):
        if issue.get("severity") != "Error":
            continue
        if entry_key(issue) in existing:
            continue
        new = ET.SubElement(base_root, "issue")
        for a in KEEP_ATTRS:
            v = issue.get(a)
            if v:
                new.set(a, v)
        for loc in issue.findall("location"):
            new_loc = ET.SubElement(new, "location")
            for a in ("file", "line", "column"):
                v = loc.get(a)
                if not v:
                    continue
                if a == "file":
                    # CI 报告是绝对路径；baseline 需要模块相对路径
                    marker = "/app/"
                    idx = v.find(marker)
                    if idx != -1:
                        v = v[idx + len(marker):]
                new_loc.set(a, v)
        existing.add(entry_key(issue))
        added += 1

    ET.indent(base_root, space="    ")
    base_root.tail = "\n"
    ET.ElementTree(base_root).write(BASELINE, encoding="utf-8", xml_declaration=True)
    print(f"lint-baseline.xml: 新增条目 {added}（现有 {len(existing)}）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
