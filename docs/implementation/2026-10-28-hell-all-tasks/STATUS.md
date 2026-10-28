## G checkpoint — 28.10.2026

E опубликован6cacdb7f;Fопубликованb6d8ae0c. G:12материалов,11craftinggrids,
6реальныхordinaryZharaweaponupgrades,normalizedsmithingwear,прежниеформы/анимации,
emberloot. Java21build+nativeformula/physiology/smithing/gait/manualgatesPASS;
см../nether-smithing-337/REPORT.md. 8armor/5specialskills/furnaceНЕреализованы;
C/Dworld/runtime/art/audio debtнеисчез. ЭтоIN_PROGRESS,неowneraccepted.

## Checkpoint C/D inventory + E/F implementation — 28.10.2026

Опубликовано: A1#33394c9cc3e; B#334–33609e14a53. B прошёл2cleanbuilds,
ресурсыиустановказафиксированы ../mob332/installation.json (игровыхзапусков0).
C проверенпоsource,прежниеHallsрегистрациисохранены. Dterrain/arenaнеподключены,
IIrestoreblocked,см.SOURCE_DEPENDENCIES.md. E#324код/ресурсы/raw-roasted/рецепты/
feature+Fortuneformulaреализованы,actualworld/runtime/artNOT_RUN/BLOCKED;F#328
20Nature/Traitsиобщиеотношенияинтегрированы,уронHPнеменялся,бюджетыWIP/proposed.
Подробныематрицы ../nether-ores-2.0.x/REPORT.md и ../nether-creature-balance/REPORT.md.
Gещёнеисполнен. Номеррудного/предметногопатчанеприсвоен. НиоднаIssueнезакрыта.

## Checkpoint B — 28.10.2026

#333 опубликован94c9cc3e. #334–#336: production articulated limbs, different materials,
personal poses +19 native before/after exports,19 four-view PNG/19 GIF; source offline PASS,
runtime NOT_RUN/art BLOCKED_REFERENCE. Первый Java21 clean build + manual/NBT,
IK/poses, Halls/Islands/legacyhead regressions PASS (../mob332/pass1.log).
Все изменённые пути и матрица: ../mob332/REPORT.md. Коммит текущего блока содержит
этот журнал; его SHA смотреть `git log -- STATUS.md`. Этапы C–G пока NOT_STARTED.

## Исполнение Codex — checkpoint A1

Исходный remote50236705 интегрирован обычным merge без reset; принятые локальные
boss331 изменения сохранены commitdcccbde8 и опубликованы в merge d6d2bfff.
Старая работа также сохранена D:/slavic-myths-backups/mob332-preexisting-20261028.zip.
#333: реализован отдельный per-dimension manual SavedData, без natural claims/unique
loot/XP, canonical legacy natural binding, сохранение режима/копий, Peaceful cancel.
Production build + actual manual journal binaryNBT/split/isolation negatives PASS;
лог ../mob332/posten-gates.log. Призыв/бой/перезаход/MP в игре NOT_RUN: запуск запрещён.
#334–#336 IN_PROGRESS: before native export19вариантов сохранён в ../mob332/before;
исходники суставов/палитр в работе. Этапы C–G НЕ НАЧАТЫ в новом запуске.
Все обязательные original art/audio gates остаются BLOCKED/OWNER_PENDING.

# 28.10.2026 — status журнала исполнения всех сегодняшних задач

**Статус создания файла:** создан при постановке задачи в ChatGPT; **производственный Java-код, OGG, original PNG и реальные тесты этой постановкой не выполнялись**. Эту таблицу Codex должен обновлять **после каждого checkpoint**, с реальными commit SHA и документом-доказательством. Пока `NOT_STARTED` означает только отсутствие выполнения в рамках НОВОГО мастер-запуска, а не утверждение, что весь код/биом отсутствует в `hell`.

**Главная Issue:** [#337](https://github.com/Deserag/slavic-myths/issues/337) · [обязательный порядок работ](CODEX_MASTER_EXECUTION_ORDER.md). **Целевая Git-ветка:** `hell`.

| Приоритет | Issues | Scope | Статус нового запуска | Доказательства / commit SHA |
| --- | --- | --- | --- | --- |
| P0-A | #327, #331 | Известные ошибки голов/FX/Peaceful + JAR parity | NOT_RUN / см. checkpoints выше | — |
| P0-A | #333 | Постень: ручной призыв во всех измерениях без farm | NOT_RUN / см. checkpoints выше | — |
| P0-B | #334 | 8-лапый костяной/угольный ползуны, суставная походка | NOT_RUN / см. checkpoints выше | — |
| P1-B | #335 | Разные палитры и фактуры 16 видов | NOT_RUN / см. checkpoints выше | — |
| P1-B | #336 | Видоспецифические анимации 16 видов (+4 Шептуна) | NOT_RUN / см. checkpoints выше | — |
| P1-C | #251–#257, #258–#267, #288–#294, #301–#303 | Неисправленные регрессии биомов 2.0.2–2.0.4 | NOT_RUN / см. checkpoints выше | — |
| P1-D | #304–#314, #316–#323 | 2.0.5: океанская генерация, мобы, руна, босс, голова | NOT_RUN / см. checkpoints выше | — |
| P2-E | #324 | Отдельный патч руд, природного worldgen и переработки | NOT_RUN / см. checkpoints выше | — |
| P2-F | #328 | Level/Nature/Traits 20 новых существ | NOT_RUN / см. checkpoints выше | — |
| P3-G | #325, #326, #329, #330 | Отдельный патч кузницы, 2 доспехов и 11 оружий | NOT_RUN / см. checkpoints выше | — |
| QA | #337 | Итоговый source build/JAR/ресурсы/отчёты и Owner QA | NOT_RUN / см. checkpoints выше | — |

## Checkpoint template — заполнять при каждом коммите

```text
DATE_TIME:
BRANCH:
LOCAL_SOURCE_HEAD:
REMOTE_ORIGIN_HELL_HEAD:
ISSUES_SCOPE:
CODE_AND_RESOURCE_FILES:
OWNED_NEW_UNTRACKED_FILES_COMMITTED:
WHAT_NOW_WORKS:
WHAT_PARTLY_WORKS:
WHAT_BLOCKED_AND_EXACT_SOURCE:
REFERENCE_PNG_SHA_VALIDATED:
AUDIO_OGG_RESOURCES_VERIFIED:
BUILD_COMMAND:
BUILD_RESULT: PASS | FAIL | NOT_RUN
JAR_PATH:
JAR_SHA256:
POLYMC_INSTALLED_PATH:
POLYMC_INSTALLED_SHA256:
RUNTIME_QA: NOT_RUN unless user actually performed
GIT_COMMIT_SHA:
GIT_PUSH_TO_ORIGIN_HELL: CONFIRMED | PUSH_BLOCKED
NEXT_EXACT_STEP:
```

**Правило срочного выхода:** если токены/контекст кончаются, останови новые функции, сохрани и запушь **все свои полезные изменения** в `hell`, даже честно названные WIP и `BUILD_FAIL`, и оставь эту таблицу с точными путями/проблемами. Нельзя обещать `DONE` без owner acceptance. **Не удалять чужие изменения, не force/reset/main.**
