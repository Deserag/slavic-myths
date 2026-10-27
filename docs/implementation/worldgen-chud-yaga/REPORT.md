# #300 — исполнение, IN_PROGRESS / OWNER_PENDING

Рабочая ветка `hell`, исходный HEAD `8cad6c125da0fefecdbefef0243ffad4f521d91f`.
27.10.2026: прямое разрешение реализации и отдельных disposable игровых миров.
`main`, PolyMC, личные миры не изменяются. Commit/push не выполнялись.

## Контракт до реализации

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|---|---|---|---|
| Baseline нового мира | ChudRegionPlanner / native pieces | Исследование новых чанков | seed30001, обычный сервер, без generate/place/NPC | native start и реальные блоки | work/issue300-baseline-runtime.log | IN_PROGRESS |
| Глубина, bedrock, гора | ChudSimplePlacement / ChudGeometry | Наружный вход → город | Обычный/горный/нижний предел | roof cover≥20, floor выше bedrock | Ожидается | NOT_RUN |
| Наружная лестница | ChudPlan / pieces | Поверхность ↔ все 7 зон | Physical collision path, пеший клиент | Ширина/высота≥3, ступени, площадки, свет | Ожидается | NOT_RUN |
| Общая сетка и оба root | ChudRegionPlanner / ChudRegionPlacement | Естественное исследование | ≥5seed, accepted/chunks/time/MSPT | Приоритет 1:1, один root/регион, старый salt/первая точка | Ожидается | NOT_RUN |
| Attached/medium/great | AbandonedPlan / Geometry | Через mineGate либо наружный спуск | Все планы, физические камеры/маршруты | Планы сохранены, самостоятельные входы доступны | Ожидается | NOT_RUN |
| Существующие боссы | underground/EncounterPopulation | Добраться до комнат | Normal, loaded rooms, restart/kill | kladovik/large_podzemnik без дублей | Ожидается | NOT_RUN |
| Терраса Яги | YagaPlacement / YagaHut | Сюжет/книга → один дом | Поляна/склон/обрыв/лес | Цельный грунт и подход, без земляных свай | Ожидается | NOT_RUN |
| Транзакции/совместимость | Cursor / records / SavedData | Повтор, отмена, save/rejoin | Защита/container/portal/old copy | Только свои изменения, старые дома сохранены | Ожидается | NOT_RUN |
| Locate/search | ChudSearchCommands | OP-диагностика отдельно | Команды до/после native generation | Нет стройки/force loading, честный candidate/physical | Ожидается | NOT_RUN |
| Поручения Яги/Баюна | ChudResearch / ChudCreationJobs / TaleScreen | NPC → книга XZ/radius/bounds → вход → исследование | Survival chain, repeat/rejoin | Достижимая цель, новый site1200–3000XZ, одна награда | Ожидается | NOT_RUN |
| Java21 / production JAR | Gradle / current resource validator | N/A | clean build + resources/JAR | Изолированный QA не входит в JAR | work/issue300-baseline-build.log, work/issue300-baseline-resources.log | PASS baseline |

Полный актуальный текст #300 и обязательные requirements прочитаны через GitHub connector.
Локального каталога docs/requirements на исходном HEAD нет; норматив прочитан из remote hell,
общие правила построек — из указанного в задаче historical main, без изменения веток.
Исторические отчёты1.5.1/1.5.2 и ERRATA о существующих боссах учтены.

## Baseline технических gates

Java21 clean build: PASS, 1m24s. Ресурсы8031 / Java21 classes1951: PASS.
JAR `build/libs/slavicmyths-2.0.8-HELL.jar`, SHA256
`8b08a2f6fd88d4646265f6bd2e65044b5e984be76570f6b345415a7d6565e6e7`.
Это исходный артефакт, ещё не исправление #300. POLYMC_NOT_INSTALLED.

## Не сделано

Реализация и перечисленные финальные игровые gates продолжаются; ни один NOT_RUN
не считается PASS. Статус Project не изменён; Issue не закрывалась.

## Как проверить в Minecraft

Финальный точный smoke checklist будет дополнен после исправлений и реальных прогонов.
Не устанавливать исходный baseline как результат #300.

## Риски/откат

Не применять незавершённый патч к личным мирам. Сохранённый baseline QA используется
только для сравнения; старые ChudPlan GenerationVersion=1 и YagaData должны читаться без ретрогена.

## Следующая задача

Продолжать только #300 в установленном порядке; другие Issues не запускать.

## Повторный runtime-аудит 27.10.2026

Склад естественного города seed30001 owner0,0 перезаписан ванильной жеодой:
FAIL исходного census3. Проверены реальные блоки MCA: calcite/amethyst_block,
см. evidence/census3-target-blocks.json. Новый адресный guard предотвращает
пересечение жеод/ванильных monster rooms/источников с занятыми объёмами Чуди.
Первый запуск guard (census4) FAIL: запрос соседних REFERENCES требовал недоступную
стадию чанка. Crash report сохранён в work/issue300-census4. Исправлено чтение
существующих STRUCTURE_STARTS с nullable lookup, без продвижения стадий.
Повторный новый мир census5 запущен; до получения результатов PASS не заявляется.

Яга: flat/slope/forest фикстуры PASS, cliff PASS отклонения до записи; настоящий
новый процесс flat подтвердил сохранение UUID/дома. Проход клиента до крыльца
подтверждён PNG в client/, вход через дверь пока NOT_RUN после остановки теста.
Полный набор пяти seed, оба поручения, книга, legacy upgrade и полные пешие
маршруты остаются обязательными незавершёнными gates.

## Подтверждения после повторных запусков

- census5 seed30001: городской склад и обе галереи после feature guard доступны;
  все обязательные зоны и внутренние дома пройдены графом реальных блоков, жители8.
  Три региона с физическими находками; четвёртый прерван QA-лимитом120s и пока UNKNOWN.
- legacy300: настоящий отдельный процесс копии baseline30001; проверены все10
  первоначальных кандидатных чанков. Сохранился один прежний native root82,30,
  Access не добавлен, второй root не появился. evidence/legacy-root.json.
- В289чанках старого bbox все block_states нижеY0 совпали с исходной копией,
  SHA2568c8c6eda6a7379adad3a9440894206dc59fae1bf5ff8549338f8cb126b3a4a94.
  evidence/legacy-blocks.json. Поверхность/light/entities в эту сверку не входят.
- Подтверждена причина отказа крупного подземника: безопасная15x15площадка
  в6блоках от центра галереи, старый радиус поиска4. Увеличен только радиус этого
  слота до8, прежний budget4кандидата/проход и условия безопасности сохранены.
  census6 seed30002: настоящий large_podzemnik UUIDaae5f5c1-8ff8-36aa-906b-15c446210fe0
  появился на4801,-24,1588 из EncounterPopulation (SPAWNED). Ни нового моба, ни
  изменения характеристик/дропа/арены не вводилось. Restart/death ещё NOT_RUN.
- census6: новый город, устье671,133,881, ground57..125, roof max59,
  lowest construction-50, minBuildHeight-64, min roof cover20, люди8, один root.
  Независимый medium: устье4987,64,1503, construction-56, roof cover20, один root.
  Это реальные normal worldgen+FULL, не dev generate. Пеший клиентский проход
  этими серверными графами НЕ заменяется.

После обращения владельца не используются Windows UI/input tools. Следующий
клиентский проход, если требует окна/управления экраном, согласуется отдельно.
Версия пока2.0.8-HELL; завершение #300 и приёмка не заявлены.
