#!/usr/bin/env bash
# localize.sh — OpenDroid 中文版本地化流水线（可重放、幂等）
#
#   bash tools/localize.sh          # 全量重建：还原上游 -> bootstrap -> codemod
#   bash tools/localize.sh --apply  # 不还原，直接在现状上重放（用于上游合并后）
#
# 上游同步后推荐流程见 tools/sync-upstream.sh。
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "${1:-}" != "--apply" ]]; then
  echo "[1/3] 还原到上游原始状态 (git checkout -- .)"
  git checkout -- .
else
  echo "[1/3] 跳过还原（--apply）"
fi

echo "[2/3] 接入运行时 (apply_bootstrap.py)"
python3 tools/apply_bootstrap.py

echo "[3/4] 施加界面文案本地化 (apply_i18n.py)"
python3 tools/apply_i18n.py --report i18n/extracted.json

echo "[4/5] 原地翻译插值文案 (apply_templates.py)"
python3 tools/apply_templates.py

echo "[5/5] 重建翻译工作表 + 合并译文"
python3 tools/build_worksheet.py
python3 tools/merge_translations.py

echo
echo "完成。"

# opendroid-cn localization pipeline

# retry
