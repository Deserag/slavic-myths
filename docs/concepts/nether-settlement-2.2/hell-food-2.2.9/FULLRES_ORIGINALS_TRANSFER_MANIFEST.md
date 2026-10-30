# Манифест исходных постеров 2.2.9 — проверка передачи Codex
**ВАЖНО:** это реестр **существующих полноразмерных PNG, собранных из предыдущего разговора**. Сами PNG **на GitHub в этой ветке ещё не загружены**. Не выдавать реестр за наличие бинарных файлов.

В архиве передачи владельцу `slavic_myths_2_2_9_original_concept_posters_2026-10-30.zip` содержатся следующие **реальные исходные PNG 1122×1402**. Для CodeX: только после фактической передачи архива в его рабочую среду/публикации PNG в репозитории проверить размеры и SHA-256.

| Ожидаемый путь в ZIP | SHA-256 |
|---|---|
| `approved_concepts/01_hell_cow_poster.png` | `3ff51fc18edaa276f017407411f2832bf63d215c42113152470f1444fa069a84` |
| `approved_concepts/02_hell_chicken_poster.png` | `bdafc628e1b3225679dc402cce7c34e7e7fa7a9387aacb5204bd1771ccdedfe7` |
| `approved_concepts/03_heatroot_poster.png` | `65c618166004c444530f7701d9802883174b83f63ccf122f39707f2010782dbf` |
| `approved_concepts/04_crimson_pepper_poster.png` | `b4aba5980b0169609ccf23971a09e3ff0085c529aad5d649fa0756b91c06c747` |
| `approved_concepts/05_ash_mushroom_poster.png` | `21a47f438814e0e508bef9430cb88b189d04041ed84d31c2d951f829fc9b1d37` |
| `deferred_foods/06_heatroot_pie_poster.png` | `4992a6b48bf0de52d9da4738d2b8b0e9c932ada3485b9463fc536f795c73a891` |

**В текущий 2.2.9 входят только первые 5 постеров, не пирожок.** Это изображения-ориентиры, не texture atlas или готовые Blockbench модели; `ART_PASS` требует UV/игровых скриншотов/владельца. Новые культуры `ash_horseradish` и `coal_garlic` пока не имеют утверждённых fullres постеров — только реально опубликованные технические PNG-иконки и `CROP_GROWTH_STAGES_R1.svg`.

## Как передать без повторного BLOCKED «нет оригинала»
1. Из текущего чата скачать файл ZIP и приложить его к рабочей сессии Codex **или** распаковать в `docs/concepts/nether-settlement-2.2/hell-food-2.2.9/fullres/` в рабочей копии, не в `main`.
2. Codex проверяет SHA-256 и размер реальных файлов, включая совпадение имён, заполняет `ACTUAL_REF_PATH` для каждого ресурса.
3. Отдельные контракты по #362/#363 должны ссылаться на проверенный новый GitHub commit/path; ссылки на sandbox или чат **не доказывают доступность** в Codex.
4. Если архива пока нет в его рабочей среде, **не блокировать код, регистрацию, дроп, рост, спавн и доступные технические текстуры**; `ORIGINAL_FULLRES_COMPARE_NOT_RUN` только для финальной художественной сверки.

Статус на 30.10.2026: **ARCHIVE_PREPARED_IN_CHAT, NOT_IN_GITHUB, CODEX_ACCESS_UNVERIFIED**.
