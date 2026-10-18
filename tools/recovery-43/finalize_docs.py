"""Update issue-43 delivery documents from verified local receipts (no Git mutation)."""
import csv,io,json
from pathlib import Path
R=Path(__file__).resolve().parents[2]
B=Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018')
def write(path,text):(R/path).write_text(text,'utf8')

summary='''## Консолидация и очистка main — issue #43 (2026-10-18)

PR #44 объединяет target-lifecycle guard Полоза из #38, allocation-only cube из #37,
подробный README, каталог текущего содержимого, CHANGELOG и VERSIONING. Версия
исходников остаётся 1.9.7.5; gameplay, баланс, registry ID, src resources и art сохранены.
17 старых JAR, 560 логов и 666 legacy work-файлов сняты с индекса с проверенным архивом;
31 зависимый исторический install/verify инструмент остановлен явным архивным режимом.
Реально удалены 55 временных каталогов (2381687605 bytes) и 10 неактивных worktree;
unique/ignored данные, старые refs и незакоммиченные изменения сохранены. V3 активна.
Независимый publish clone сохранён: автоматическая проверка запретила его удаление.
Java21 clean build PASS (compileJava FROM-CACHE), 3919 JSON/1325 PNG/6804 resources/
1685 classes, production JAR parity и 10000 offline cube cases PASS. Minecraft/сервер/
GameTest:0 запусков. Визуальная, звуковая, полная игровая и MP приёмка NOT_RUN.
Merge доставляется через PR #44; master только FF после архивирования старой вершины.
V3/75 карточек и баланс #27 в интеграцию не включены;55-card #40 не подменяет V3.
[Фактические результаты, архивы, ограничения и путь main/master](maintenance/issue-43/REPORT.md).

'''
for path in ['docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md']:
    p=R/path;text=p.read_text('utf-8-sig');at=text.find('\n## ',1)
    assert at>0;write(path,summary+text[at+1:])
data=json.loads((B/'FINAL_LOCAL_RESULTS.json').read_text('utf8'))
checks=json.loads((R/'docs/maintenance/issue-43/FINAL_GATES.json').read_text('utf8'))
totals=json.loads((R/'docs/maintenance/issue-43/CLEANUP_TOTALS.json').read_text('utf8'))
totals['previous_plan_actual_freed_bytes']=totals.pop('actual_freed_bytes',0)
totals['actual_safe_removed_bytes']=sum(x['bytes'] for x in data['safe_directories_removed'])
totals['actual_worktrees_removed']=len(data['worktrees_removed'])
totals['actual_net_free_bytes_before_final_checkout']=data['net_free_bytes_including_new_archives']
totals['new_unique_archives_bytes']=data['new_unique_archives_bytes']
totals['publish_clone_removed']=False
write('docs/maintenance/issue-43/CLEANUP_TOTALS.json',json.dumps(totals,ensure_ascii=False,indent=2)+'\n')
csvpath=R/'docs/maintenance/issue-43/LOCAL_SIZES_BEFORE_AFTER.csv'
rows=list(csv.DictReader(io.StringIO(csvpath.read_text('utf-8-sig'))))
for row in rows:
    row['actual_state']='REMOVED' if not Path(row['path']).exists() else 'KEPT'
    row['actual_after']=row['actual_state']
    if row['decision']=='SAFE_TO_REMOVE' and row['actual_state']=='REMOVED':row['actual_freed_bytes']=row['bytes']
output=io.StringIO(newline='');writer=csv.DictWriter(output,fieldnames=list(rows[0]),lineterminator='\n');writer.writeheader();writer.writerows(rows);write(csvpath.relative_to(R),output.getvalue())
report='''# Issue #43 — фактическая очистка и доставка в main

## Изменения и публикация

Использован существующий [PR #44](https://github.com/Deserag/slavic-myths/pull/44), ветка `codex/main-consolidation-43`. Новых клонов, PR и рабочих копий не создано. Начальный main — `fb2994e958911169317523e1066b8a1ca7a155af`. Итоговый merge SHA и завершение последнего шага доставки проверяются в PR и финальном ответе; commit не может содержать собственный будущий merge SHA.

Production diff — ровно два Java-файла: живой корректный target тела Полоза из #38 и cube без временных массивов из #37. Поведение вершин проверено 10000 случаями. Ресурсы, art, баланс, ID и версия 1.9.7.5 не менялись. Подробный README, CONTENT_CATALOG, VERSIONING и пересобранный CHANGELOG включены в этот же PR. Локальные classes3 reports/tools сохранены; сырые логи восстановимы из архива, а итоговые JSON/отчёты остаются в source-tree.

## Физическая очистка

- **Все 55 SAFE-каталогов удалены:** 2381687605 bytes (2.218 GiB), из них C:687664516 и D:1694023089. Перед удалением сверены архив и каждый текущий файл; пропусков нет.
- **10 неактивных worktree удалены штатным `git worktree remove`, без `--force`.** Список ниже. Данные и refs сохранены до удаления.
- В момент подготовки PR остались основная копия, V3 и временно используемая интеграционная копия с проверенным JAR. После доставки интеграционная копия снимается тем же штатным способом; итоговый список и показания free-space фиксируются в PR #44 и финальном локальном receipt.
- Новые архивы уникальных ignored/untracked данных, исторических tracked артефактов и archive-commits занимают около 815.8 MiB на C. Прежние проверенные backup занимали 4.177 GiB ещё до начала фактического удаления и оставлены.
- На контрольном замере после 10 worktree с учётом новых архивов свободное место увеличилось примерно C:+7.17 GiB, D:+6.63 GiB. Последний замер делается после checkout основного проекта и снятия интеграционной копии; логические размеры каталогов не выдаются за точное изменение свободного места тома.

Все перечисленные SAFE пути и фактическое существование — [CSV](LOCAL_SIZES_BEFORE_AFTER.csv). Он содержит исторические размеры до очистки; parent rows нельзя повторно суммировать с удалёнными descendants.

Удалены рабочие копии:

| Каталог | Сохранение |
|---|---|
| Codex advancements-2-implementation | archive/local-01-20261018 + уникальные данные |
| Codex guardian-cube | archive/local-02-20261018 + JFR/инструменты |
| Codex performance-2-release | исходная ветка + профили |
| Codex poloz-body-melee | archive/local-04-20261018 + fixtures/тестовые сохранения |
| Codex poloz-effects | archive/local-05-20261018 + профили/сохранения |
| slavic-myths-audit | archive/local-08-20261018 |
| slavic-myths-balance | исходная ветка PR #27 + тестовые данные |
| slavic-myths-balance-baseline | archive/local-10-20261018 |
| slavic-myths-integrated | archive/local-11-20261018 |
| slavic-myths-performance | исходная ветка PR #32 + профили |

Основной незакоммиченный результат отдельно сохранён обычным commit `26782e06ae63d0046db8de6b391432c7c5c3d2cf`, ветка `archive/local-primary-20261018`. Все восемь новых archive-refs дополнительно в проверенном `retired-local-work.bundle`; bundle опирается на сохранённую исходную Git-историю. Основные пользовательские миры и PolyMC не тронуты. Уникальные вторичные тестовые данные сохранены, а не отброшены вместе с копией. Активная V3 с 144 изменёнными файлами не коммитится и не снимается.

## Конкретный блокер

`D:/slavic-myths/.tools/publish-1.9.4` **не удалён**. Его SHA256-архив и все 26535 текущих файлов (1547367832 bytes) проверены, отличий нет. Автоматическая проверка отклонила рекурсивное удаление с причиной `blocked by policy`. Запрет не обходился. Эти 1.441 GiB не включены в освобождённое место. Архив `historical-publish-1.9.4.zip` остаётся у владельца. Дополнительное согласование/разрешённый механизм нужен только для этого фактически заблокированного удаления, а не для уже выполненных SAFE и штатных worktree removals.

## Очистка текущего Git-дерева

Из индекса сняты **1243 файла**: 17 исторических JAR (271826740 bytes), 560 логов, 666 legacy scratch-файлов `work/`. Все сохранены с per-file SHA256; уникальные WAV, рисунки, спецификации и инструменты не потеряны. Текущее Git-дерево стало меньше; старые packs/history не переписаны и их уменьшение не заявляется. Notes/checksums в release сохранены, реальные src/art/assets не удалялись.

31 исторический install/verify инструмент остановлен до IO явным архивным сообщением. Старые проверки зависят от конкретных baseline и не могут выдавать PASS для новой версии. Восстановление оригиналов и входов: [HISTORICAL_ARTIFACTS](HISTORICAL_ARTIFACTS.md), [per-file manifest](TRACKED_ARTIFACTS.json).

## GitHub и master

PR #44 доставляет нужные части #37/#38 и документацию #41. После проверки результата полностью устаревшие #2/#11/#12/#13/#14/#21/#31 и включённые #37/#38/#41 закрываются без слияния конфликтующих старых версий. Их старые вершины заранее сохранены опубликованными тегами `archive/pr-N-before-consolidation-20261018`. Ветки не удаляются: это сохраняет прежние зависимости и соответствует приоритетному требованию issue.

Остаются отдельно: #40/#25 как контекст и зависимости V3; #27 как неподтверждённый баланс; #24 как согласование исторических QA docs; #32/#36/#39 как исследования, не production patches. Их нельзя автоматически считать исправлениями или готовыми релизами. V3 нужно переносить отдельным review поверх принятого main, не предварительным merge отвергнутого 55-card дерева.

У master было 0 уникальных коммитов,26 behind main. Старый tip `e64b24b8c2f27ed3e4f93353feec913a88d7fd9e` сохранён удалённым tag `archive/master-before-consolidation-20261018`. После принятия #44 выполняется только обычный FF на проверенный main с повторной проверкой ancestry. Default branch остаётся main; force/history rewrite не используются.

## Проверки и границы

Java21 `gradlew.bat clean build` **PASS**; compileJava FROM-CACHE честно отмечен, clean выполнен, итоговый production JAR проверен повторно. 3919 JSON,1325 PNG,6804 resource parity,1685 Java21 production classes и2352 локальные Markdown-ссылки PASS;31 archive stops и PS syntax PASS. Чистая офлайн-проба 10000 cube cases PASS.

JAR: `slavicmyths-1.9.7.5.jar`, SHA256 `aee4b68b26010cdef3f9a67b7a35f3c0d53178195d01f77752a18111b60e49ca`. [FINAL_GATES.json](FINAL_GATES.json). Игра/сервер/GameTest: **0 запусков**. Новая приёмка моделей, звука, GUI, истории, MP, FPS/MSPT не выполнялась. Никакой 2.0 или новой GitHub Release не опубликовано.
'''
write('docs/maintenance/issue-43/REPORT.md',report)
for name in ['LOCAL_CLEANUP_MANIFEST.md','LARGE_FILES_AND_EXTENSIONS.md','DIRECTORY_DETAILS.md','MASTER_PLAN.md','PR_MATRIX.md']:
    p=R/'docs/maintenance/issue-43'/name
    if not p.exists():continue
    text=p.read_text('utf-8-sig')
    if not text.startswith('> Исторический'):
        p.write_text('> Исторический снимок до фактической очистки. Условия «не удалено», «ожидает согласования» и состояния PR ниже заменены новой инструкцией владельца. Актуальный результат: [REPORT.md](REPORT.md), последняя доставка: [PR #44](https://github.com/Deserag/slavic-myths/pull/44).\n\n'+text,'utf8')
print('Updated delivery docs; final merge and last volume measurement remain external verifiable receipts')
