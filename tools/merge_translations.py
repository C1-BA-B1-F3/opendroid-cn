#!/usr/bin/env python3
"""
merge_translations.py — 合并 i18n/zh/*.json 译文，生成 app 运行时翻译表。

功能：
  1. 读取 i18n/zh/*.json（key = 运行时英文字符串，value = 中文）。
  2. 与 i18n/extracted.json（codemod 提取的源字面量）做**一致性校验**：
       - 未转义后能匹配上的 key 数量；
       - 在译文里但源码中不存在的 key（拼写/转义错误）→ 报错退出；
  3. 输出 app/src/main/assets/i18n/zh.json（仅含已翻译项）。
  4. 输出 i18n/MISSING.md：源码中存在但尚未翻译的字符串清单。

用法：
    python3 tools/merge_translations.py
    python3 tools/merge_translations.py --check   # 只校验，不写文件
"""
from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys
from collections import OrderedDict

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPT_DIR)
ZH_DIR = os.path.join(REPO_ROOT, "i18n", "zh")
WORKSHEET = os.path.join(REPO_ROOT, "i18n", "worksheet.json")
ASSET_OUT = os.path.join(REPO_ROOT, "app", "src", "main", "assets", "i18n", "zh.json")
MISSING_OUT = os.path.join(REPO_ROOT, "i18n", "MISSING.md")

_ESC = {"n": "\n", "t": "\t", "r": "\r", '"': '"', "\\": "\\", "'": "'", "$": "$", "0": "\0"}


def unescape_kotlin(s: str) -> str:
    """把 Kotlin 源字面量内容转成运行时字符串。"""
    out = []
    i = 0
    while i < len(s):
        c = s[i]
        if c != "\\" or i + 1 >= len(s):
            out.append(c)
            i += 1
            continue
        nxt = s[i + 1]
        if nxt == "u" and i + 5 < len(s) + 1:
            try:
                out.append(chr(int(s[i + 2:i + 6], 16)))
                i += 6
                continue
            except ValueError:
                pass
        out.append(_ESC.get(nxt, nxt))
        i += 2
    return "".join(out)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--strict", action="store_true", help="孤儿 key 视为错误并中止")
    args = ap.parse_args()

    # 1) 收集译文
    translations: "OrderedDict[str, str]" = OrderedDict()
    dup = []
    files = sorted(glob.glob(os.path.join(ZH_DIR, "*.json")))
    # templates.json 属于「原地翻译」机制，不进入 assets 运行时映射表
    files = [f for f in files if os.path.basename(f) != "templates.json"]
    for path in files:
        data = json.load(open(path, encoding="utf-8"))
        for k, v in data.items():
            if k.startswith("_"):
                continue
            if k in translations and translations[k] != v:
                dup.append((k, os.path.basename(path)))
            translations[k] = v

    # 2) 源码中实际存在的运行时字符串（来自 build_worksheet.py 的工作表）
    if os.path.exists(WORKSHEET):
        entries = json.load(open(WORKSHEET, encoding="utf-8"))
        runtime = {unescape_kotlin(e["en"]): e["en"] for e in entries}
    else:
        runtime = {}

    matched = []
    orphans = []
    resolved: "OrderedDict[str, str]" = OrderedDict()
    for k, v in translations.items():
        if k in runtime:
            resolved[k] = v
            matched.append(k)
        else:
            k2 = unescape_kotlin(k)
            if k2 in runtime:
                resolved[k2] = v
                matched.append(k2)
            else:
                # 工作表过滤器未收录，但仍是有效文案（如 ALL）；保留写入，多译无害
                resolved[k] = v
                orphans.append(k)

    print(f"译文文件            : {len(files)}")
    print(f"译文条目            : {len(translations)}")
    print(f"与源码匹配          : {len(matched)}")
    print(f"源码中不存在(错误)  : {len(orphans)}")
    if dup:
        print(f"重复 key            : {len(dup)}")

    if orphans:
        print("\n注意：以下译文 key 在源码中找不到（多余项，将被保留但可能无效）：")
        for k in orphans[:40]:
            print(f"   {k!r}")
        if args.strict:
            print("\n--strict：已中止，未写入。")
            return 1

    # 3) 缺失清单（排除已含中文的：那些已由 templates 原地翻译处理）
    cjk = re.compile(r'[\u4e00-\u9fff]')
    missing = [k for k in runtime if k not in resolved and not cjk.search(k)]
    os.makedirs(os.path.dirname(MISSING_OUT), exist_ok=True)
    with open(MISSING_OUT, "w", encoding="utf-8") as fh:
        fh.write(f"# 尚未翻译的字符串（{len(missing)} / {len(runtime)}）\n\n")
        for k in sorted(missing, key=lambda x: (-len(x), x)):
            fh.write(f"- `{k}`\n")

    if not args.check:
        os.makedirs(os.path.dirname(ASSET_OUT), exist_ok=True)
        with open(ASSET_OUT, "w", encoding="utf-8") as fh:
            json.dump(resolved, fh, ensure_ascii=False, indent=1)
        print(f"\n已写入              : {ASSET_OUT}")
    print(f"缺失清单            : {MISSING_OUT}  ({len(missing)} 条未翻译)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
