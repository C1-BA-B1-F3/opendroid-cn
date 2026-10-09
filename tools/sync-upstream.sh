#!/usr/bin/env bash
# sync-upstream.sh — 同步上游 OpenDroid 并自动重放中文本地化
#
# 用法：
#   bash tools/sync-upstream.sh            # 拉取并合并上游，然后重放本地化
#   bash tools/sync-upstream.sh --check    # 只检查上游是否有更新，不做改动
#
# 设计：本地化改造是**确定性、可重放**的（见 tools/localize.sh）。
#       因此同步时若 Kotlin 侧出现冲突，一律「以上游为准」，再重跑本地化流水线，
#       无需手工解冲突。手工维护的只有 i18n/zh/*.json 与 tools/*。
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 拉取上游"
git fetch upstream --tags

if [[ "${1:-}" == "--check" ]]; then
  echo "==> 上游与当前分支的差异："
  git log --oneline HEAD..upstream/main | head -30
  exit 0
fi

echo "==> 合并 upstream/main"
if ! git merge --no-edit upstream/main; then
  echo "!! 合并冲突。按设计「以上游为准」处理被本地化改写的 Kotlin 文件："
  echo "   冲突文件一律取上游版本，随后由本地化流水线重新生成。"
  git diff --name-only --diff-filter=U | while read -r f; do
    case "$f" in
      *.kt|*/res/values/strings.xml)
        echo "   取上游: $f"
        git checkout --theirs -- "$f" 2>/dev/null || true
        git add -- "$f"
        ;;
      *)
        echo "   !! 需要人工处理: $f"
        ;;
    esac
  done
  if git diff --name-only --diff-filter=U | grep -qv '\.kt$'; then
    echo "!! 仍有非 Kotlin 冲突，请人工解决后重新运行本脚本。"
    exit 1
  fi
  git commit --no-edit
fi

echo "==> 重放本地化流水线"
bash tools/localize.sh --apply

echo "==> 检查是否出现新的未翻译文案"
python3 tools/merge_translations.py | tee /tmp/opendroid-cn-sync.log | grep -E '缺失|已写入' || true

echo
echo "完成。若存在新的未翻译文案，请补充到 i18n/zh/*.json 后重新运行："
echo "    python3 tools/merge_translations.py"
