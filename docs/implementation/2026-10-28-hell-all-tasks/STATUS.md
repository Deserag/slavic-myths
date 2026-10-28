# 28.10.2026 — status журнала исполнения всех сегодняшних задач

**Статус создания файла:** создан при постановке задачи в ChatGPT; **производственный Java-код, OGG, original PNG и реальные тесты этой постановкой не выполнялись**. Эту таблицу Codex должен обновлять **после каждого checkpoint**, с реальными commit SHA и документом-доказательством. Пока `NOT_STARTED` означает только отсутствие выполнения в рамках НОВОГО мастер-запуска, а не утверждение, что весь код/биом отсутствует в `hell`.

**Главная Issue:** [#337](https://github.com/Deserag/slavic-myths/issues/337) · [обязательный порядок работ](CODEX_MASTER_EXECUTION_ORDER.md). **Целевая Git-ветка:** `hell`.

| Приоритет | Issues | Scope | Статус нового запуска | Доказательства / commit SHA |
| --- | --- | --- | --- | --- |
| P0-A | #327, #331 | Известные ошибки голов/FX/Peaceful + JAR parity | NOT_STARTED | — |
| P0-A | #333 | Постень: ручной призыв во всех измерениях без farm | NOT_STARTED | — |
| P0-B | #334 | 8-лапый костяной/угольный ползуны, суставная походка | NOT_STARTED | — |
| P1-B | #335 | Разные палитры и фактуры 16 видов | NOT_STARTED | — |
| P1-B | #336 | Видоспецифические анимации 16 видов (+4 Шептуна) | NOT_STARTED | — |
| P1-C | #251–#257, #258–#267, #288–#294, #301–#303 | Неисправленные регрессии биомов 2.0.2–2.0.4 | NOT_STARTED | — |
| P1-D | #304–#314, #316–#323 | 2.0.5: океанская генерация, мобы, руна, босс, голова | NOT_STARTED | — |
| P2-E | #324 | Отдельный патч руд, природного worldgen и переработки | NOT_STARTED | — |
| P2-F | #328 | Level/Nature/Traits 20 новых существ | NOT_STARTED | — |
| P3-G | #325, #326, #329, #330 | Отдельный патч кузницы, 2 доспехов и 11 оружий | NOT_STARTED | — |
| QA | #337 | Итоговый source build/JAR/ресурсы/отчёты и Owner QA | NOT_STARTED | — |

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
