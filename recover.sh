#!/usr/bin/env bash
set -Eeuo pipefail

cd "$(git rev-parse --show-toplevel)"
git symbolic-ref -q HEAD >/dev/null || { echo "ERROR: detached HEAD"; exit 1; }

# 1. Проверка безопасности
if ! git diff --quiet || ! git diff --cached --quiet || [[ -n "$(git ls-files --others --exclude-standard -- src/main/java src/main/resources)" ]]; then
  echo "ERROR: сначала сохраните текущие изменения через commit или stash."
  exit 1
fi

original_sha=$(git rev-parse HEAD)
stamp="$(date -u +%Y%m%dT%H%M%SZ)-$$"
backup="backup/2.3-recovery-$stamp"

git branch "$backup" "$original_sha"

report_dir="$(git rev-parse --git-path "recovery-$stamp")"
mkdir -p "$report_dir/variants"
log="$report_dir/report.txt"

printf 'START %s\nBACKUP %s\n' "$original_sha" "$backup" | tee "$log"

# 2. Получение всех доступных удалённых веток
while IFS= read -r remote; do
  git fetch "$remote" "+refs/heads/*:refs/remotes/$remote/*" --tags || echo "WARN: fetch failed: $remote" | tee -a "$log"
done < <(git remote)

# 3. Переименование локальных веток 3.3 -> 2.3
declare -a renamed=() sources=()

while IFS= read -r old; do
  [[ "$old" == *3.3* ]] || continue
  new="${old/3.3/2.3}"

  if git show-ref --verify --quiet "refs/heads/$new"; then
    echo "WARN: ветка уже существует: $new; сохранена $old" | tee -a "$log"
  elif git branch -m "$old" "$new"; then
    renamed+=("$new")
    echo "RENAME: $old -> $new" | tee -a "$log"
  else
    echo "WARN: невозможно переименовать $old" | tee -a "$log"
  fi
done < <(git for-each-ref --format='%(refname:short)' refs/heads)

# 4. Автоматический поиск исходных веток
declare -A seen=()

collect() {
  local pattern="$1" ref name sha

  while IFS= read -r ref; do
    case "$ref" in
      refs/heads/*)
        name="${ref#refs/heads/}"
        ;;
      refs/remotes/*)
        name="${ref#refs/remotes/}"
        name="${name#*/}"
        ;;
      *)
        continue
        ;;
    esac

    [[ "$name" == $pattern ]] || continue
    [[ "$name" == HEAD ]] && continue

    sha=$(git rev-parse "$ref^{commit}")
    [[ "$sha" == "$original_sha" ]] && continue
    [[ -v 'seen[$sha]' ]] && continue

    seen[$sha]=1
    sources+=("$ref")
  done < <(git for-each-ref --format='%(refname)' refs/heads refs/remotes)
}

# Адские жители, Правитель, Гусь
collect 'integration/2.3-nether-goose-recovery-*'
collect 'integration/2.3-nether-posad-recovery-*'

# Яйца спавна боссов
collect 'feat/2.3-boss-creative-spawn-eggs-*'

# Баук и другие боссы
collect 'fix/2.2.18-boss-*'
collect 'fix/1.9-parallel-minions-bosses-*'
collect '*bauk*'
collect '*bauq*'

# Экономика, ингредиенты, блюда
collect '*econom*'
collect '*food*'
collect '*ingredients*'
collect '*recipe*'
collect '*dish*'
collect '*cuisine*'

# Ошибочно пронумерованные ветки
for renamed_branch in "${renamed[@]}"; do
  collect "$renamed_branch"
done
collect '*3.3*'

(( ${#sources[@]} )) || {
  echo "ERROR: подходящие исходные ветки не найдены." | tee -a "$log"
  exit 1
}

# 5. Перенос файлов с трёхсторонним слиянием
# Не перезаписываем конфликтующие версии и не удаляем существующий код.

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

imported=0
conflicts=0
skipped=0
source_no=0

for ref in "${sources[@]}"; do
  ((source_no+=1))

  base=$(git merge-base "$original_sha" "$ref" || true)

  if [[ -z "$base" ]]; then
    echo "WARN: нет общего предка: $ref" | tee -a "$log"
    continue
  fi

  echo "SOURCE $source_no: $ref ($(git rev-parse --short "$ref"))" | tee -a "$log"

  while IFS= read -r -d '' file; do
    git cat-file blob "$ref:$file" > "$tmp/theirs"

    if [[ ! -e "$file" && ! -L "$file" ]]; then
      if git cat-file -e "$base:$file" 2>/dev/null; then
        reason="deleted-in-target"
      else
        mkdir -p "$(dirname "$file")"
        git restore --source="$ref" --worktree -- "$file"
        ((imported+=1))
        printf 'ADD [%s] %s\n' "$ref" "$file" >> "$log"
        continue
      fi

    elif cmp -s "$file" "$tmp/theirs"; then
      ((skipped+=1))
      continue

    elif ! git cat-file -e "$base:$file" 2>/dev/null; then
      reason="independent-additions"

    else
      git cat-file blob "$base:$file" > "$tmp/base"

      if cmp -s "$file" "$tmp/base"; then
        git restore --source="$ref" --worktree -- "$file"
        ((imported+=1))
        printf 'RESTORE [%s] %s\n' "$ref" "$file" >> "$log"
        continue
      fi

      reason="content-conflict"

      if LC_ALL=C grep -Iq . "$file" &&
         LC_ALL=C grep -Iq . "$tmp/base" &&
         LC_ALL=C grep -Iq . "$tmp/theirs" &&
         git merge-file -p "$file" "$tmp/base" "$tmp/theirs" > "$tmp/merged"; then
        cat "$tmp/merged" > "$file"
        ((imported+=1))
        printf 'MERGED [%s] %s\n' "$ref" "$file" >> "$log"
        continue
      fi
    fi

    # При конфликте текущий файл сохраняется.
    # Альтернативная версия помещается в .git/recovery-.../variants/
    ((conflicts+=1))
    variant="$report_dir/variants/$source_no/$file"

    mkdir -p "$(dirname "$variant")"
    cp "$tmp/theirs" "$variant"

    printf 'CONFLICT [%s] %s (%s) saved=%s\n' "$ref" "$file" "$reason" "$variant" | tee -a "$log"

  done < <(
    git diff --name-only --diff-filter=AMR -z "$base" "$ref" -- \
      src/main/java/ \
      src/main/resources/
  )
done

# 6. Результаты
printf '\nIMPORTED=%d CONFLICTS=%d IDENTICAL=%d\n' \
  "$imported" "$conflicts" "$skipped" | tee -a "$log"

git status --short | tee -a "$log"

# 7. Проверка компиляции NeoForge
if (( conflicts == 0 )) && [[ -f ./gradlew ]]; then
  echo "Запуск Gradle classes..."

  if bash ./gradlew --no-daemon classes > "$report_dir/build.log" 2>&1; then
    echo "BUILD SUCCESS" | tee -a "$log"
  else
    echo "BUILD FAILED: $report_dir/build.log" | tee -a "$log"
  fi
else
  echo "BUILD SKIPPED: есть конфликты или отсутствует gradlew" | tee -a "$log"
fi

echo "======================================"
echo "RECOVERY COMPLETE"
echo "Imported: $imported"
echo "Conflicts: $conflicts"
echo "Backup branch: $backup"
echo "Report: $log"
echo "======================================"