# #90 — повторная проверка по актуальному ТЗ v3,1.9.7.28

IN_PROGRESS / OWNER_PENDING. Issue OPEN до ручной игровой приёмки.
Авторитетный контракт перечитан через GitHub20.10.2026:
`[HERO-HUD] HUD: отображать1–2 спутников и законного3-го при событии Полоза`.
Чертёж: docs/design/ISSUE_90_HERO_HUD_REAL_LIMITS.svg. Прежнее ТЗ с4 одновременно
отменено владельцем. Ревизия27 была построена под него и теперь заменена28.
Актуальный upstream a4c8c42a объединён обычным merge; прочие рабочие правки сохранены.

## Найденные недоделки и исправления

1.27 поддерживала4+ и многоколоночный overflow под старое ТЗ. В28 MAX_VISIBLE3,
   `.limit(3)` после owner/alive/range/UUID dedup и сортировки, layout/draw также
   защищены от четвёртой строки. Нет пустого3-го: число строк равно числу выбранных
   реальных сущностей,0 — ничего,1 — одна,2 — две,3 — три. При debug-corrupt4 HUD
   ограничивается3, не меняет/не удаляет сущности на сервере.
2.27 меняла положение панели на верхнее. В28 возвращён прежний bottom-left anchor
   над vanilla bars (65 GUIpixels), масштаб.75/ширина160 прежнего семейства.
   До3 строк в одной колонке, максимальная высота54 GUIpixels. Нормальный1/2 world
   не получает дополнительного слота или широких панелей. Существующие клиентские
   смещения остаются, координаты ограничены экраном. Главные свойства рамок/HP/status
   из предыдущего исправления сохранены; бессмысленная генерализация на12 строк убрана.
3.Добавлены missing regressions: порядок не зависит отHP и перестановки входного
   массива; unload удаляет именно строку отсутствующей сущности, world unload даёт
   пустой список, return того же UUID возвращает точный исходный порядок; UUID не
   дублируется. Кешей/таймеров/ghost rows между мирами нет. Это проверка клиентского
   списка, НЕ измерение игрового «мигания» при реальной сетевой выгрузке.
4.Источник прав — фактический syncedOwner() у загруженного героя. Другой owner/null
   исключаются. Совместный бой/союз без владельца не даёт строки. Клиент не читает
   серверные saved maps, не притворяется сервером и не пересчитывает canSummon/Полоза.
   Третья строка появляется по факту трёх полученных от сервера owned entities;
   законность их создания по-прежнему проверяет HeroCalls.

## Сверка реальной серверной логики (без её изменения)

`skim/HeroCalls.toggle`: `polozParty=PolozEvent.active(w)&&!id.equals("finist")`,
запрет при `count(p)>=(polozParty?3:2)`. Обычно2, special path3,4 запрещён.
Все четыре типа героев поддерживаются как варианты строк; это НЕ четыре слота.
Poloz event сам клиентом не угадывается и новый sync protocol не вводится.

Recovering/retreat/cursed читаются из существующих synced actions/flags. Имя,HP и
status остаются у каждой строки. Lifetime/cooldown #78 ещё не имеют подходящей
серверной синхронизации и не выдумываются. Илья/Добрыня/Алёша/Финист,HeroCalls,
KalinParty и вся summon/save/AI/balance логика проверены на неизменность.
Owner/range64/alive filter не расширен; только прежние entitiesForRendering.

HUD по-прежнему скрыт при F1/отключённой настройке/отсутствии player-level и любом
открытом экране, включая ChatScreen. Нижние health/hotbar зоны зарезервированы,
стандартная minimap справа и bossbar сверху не накрываются колонкой слева в обычном
layout. При нестандартном расположении minimap/закрытого чата доступны прежние
horizontalOffset/verticalOffset; автоматического сканирования сторонних оверлеев нет.
Фактические пересечения конкретного модпака/шрифты в игре проверяет владелец вместе.

## Доказательства

- before-19727-HeroHud*.java — реальный source27 до исправления; исходный baseline
  cap2 также сохранён в ../hero-hud-90/before-HeroHud.java.
- production-draw-trace.json — actual production JVM select/layout/draw,32 сочетания
  0–3,2 разрешения,GUI1–4; отдельные caps4+,HP/order/owner/range/unload/return cases.
- hud-1-3-scales-2-4.png —9 состояний1/2/3 × GUI2/3/4 с реальными вызовами draw и
  bitmap glyph providers установленного Minecraft1.21.1. Это CPU source preview,
  не Minecraft/OpenGL screenshot; реальных игровых скриншотов нет.
- build.log — Java21 clean build + offline progression/decay/NBT,59с PASS.
- class-ui-regression/ —6220 проверок viewport/правил,18 branch translations; источники
  текущего ClassScreen/Viewport/Mastery и language resources совпадают сJAR.
- regression.log —9249 actual command checks и13 predicate/geometry checks посоха,22с PASS.
- checks.json —7408 ресурсов/1713 классов,JSON/метаданные/source-protected gates.
- build-source-sha256.json — весь production source/resources/Gradle этой сборки,
  включая сохранённые локальные UI/command changes, а не один scoped commit #90.

JAR: build/libs/slavicmyths-1.9.7.28.jar.
SHA256: `0d5076880f145dc38ea4a74380aa9f9285611708e072ce3485ed8f25bcb7138f`.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Актуальное ТЗ1/2/3, не4 | GitHub v3 + индивидуальный SVG + server-source branch | PASS |
| Законный3-й не обрезается, нет пустых/четвёртого | select/layout/draw production JVM tests | PASS |
| Owner/UUID/alive/range64, без расширения summon logic | actual filter cases + unchanged protected source | PASS |
| Порядок приHP/перестановке/world unload/return | actual list lifecycle tests | PASS |
| Компактная колонка GUI2/3/4 | geometry bounds +9 source render fixtures | PASS |
| Recover/retreat только synced, cooldown не подделан | production source bridge/unchanged entities | PASS |
| Java21 clean build/ресурсы/JAR + пиво/classes | build.log/checks.json/class-ui-regression | PASS |
| Настоящее событие Полоза, runtime3/no4, unload/return/отсутствие мигания | Игру запускать запрещено | NOT_RUN |
| Реальные minimap/chat/hotbar/font пересечения в модпаке | Общая ручная проверка владельца | NOT_RUN |
| Установка всех текущих изменений в PolyMC | [Installed28/backup/SHA/source](../polymc-19728/REPORT.md) | PASS |
| Owner acceptance | Не подтверждена для28 | NOT_RUN |

Запусков Minecraft/PolyMC/GameTest/server0. Issue не закрывалась, labels приёмки
за владельца не ставились. Установка — TEST_PREVIEW по прямому запросу владельца.

## Установка после gates

1.9.7.28 INSTALLED_TEST_PREVIEW, source e5924bd9 + сохранённые UI/command changes.
Все текущие production изменения включены; backup25,installed SHA и одинJAR PASS.
[Точная квитанция](../polymc-19728/REPORT.md). Runtime/owner по-прежнему NOT_RUN.
