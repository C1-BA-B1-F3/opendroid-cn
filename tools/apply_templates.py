#!/usr/bin/env python3
"""
apply_templates.py — 含变量插值（$var / ${expr}）的界面文案「原地翻译」。

为什么需要它：
    tr("Connected to ${platform.displayName}!") 在运行时拿到的是替换后的字符串
    （"Connected to Twitter!"），查表永远命中不了。因此这类文案不走翻译表，
    而是直接把源码字面量改写成中文（保留 $ 插值原样）。

映射文件：i18n/zh/templates.json
    { "<源码英文原样>": "<中文，$ 插值原样保留>" }

替换是**精确整串替换**，且替换后英文原串即消失，因此天然幂等、可重放。
"""
from __future__ import annotations

import json
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
SRC_ROOT = os.path.join(REPO_ROOT, "app", "src", "main", "java", "com", "opendroid", "ai")
MAP_PATH = os.path.join(REPO_ROOT, "i18n", "zh", "templates.json")


def main() -> int:
    if not os.path.exists(MAP_PATH):
        print("未找到 i18n/zh/templates.json，跳过。")
        return 0
    mapping = json.load(open(MAP_PATH, encoding="utf-8"))

    files = []
    for dp, _d, fns in os.walk(SRC_ROOT):
        for fn in fns:
            if fn.endswith(".kt"):
                files.append(os.path.join(dp, fn))

    total = 0
    hit_keys: set[str] = set()
    for path in sorted(files):
        src = open(path, encoding="utf-8").read()
        orig = src
        for en, zh in mapping.items():
            if en.startswith("_"):
                continue
            needle = '"' + en + '"'
            if needle in src:
                src = src.replace(needle, '"' + zh + '"')
                hit_keys.add(en)
                total += 1
        if src != orig:
            open(path, "w", encoding="utf-8").write(src)

    unused = [k for k in mapping if not k.startswith("_") and k not in hit_keys]
    print(f"原地翻译替换次数 : {total}")
    print(f"命中不同文案数   : {len(hit_keys)}")
    if unused:
        print(f"未命中的映射     : {len(unused)}")
        for k in unused[:20]:
            print(f"   {k!r}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
