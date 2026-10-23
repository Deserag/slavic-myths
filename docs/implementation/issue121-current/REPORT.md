# #121 — защита оружейных ресурсов от отката генераторами

## Исправление

В изолированной копии воспроизведён реальный FAIL: прямой запуск `weapon_feedback_assets.py` и `weapon_visual_repair_assets.py` перестраивал головы арбалета по историческому шаблону, после чего защитный gate #222 справедливо останавливался на изменённой геометрии. Проход был прерван внутри копии; настоящий source не затронут. Сохранены `feedback-before-failed.json`, `repair-before-failed.json` и исходные логи в work/issue121-*.log.

Оба публичных `apply()` теперь воспроизводят актуальную каноническую цепочку сразу. Старые функции сохранены как `legacy_apply()` исключительно для происхождения исходников. Кроме того, безопасный `poloz_full_assets.py --weapon-only` теперь вызывает всю оружейную цепочку: один старый whip overlay без финальных #217/#225 возвращал бы прежние материалы и GUI. Полный генератор Полоза не запускался, аудио не регенерировалось.

## Реальные точки входа

| Entrypoint | Mode | Source order |
|---|---|---|
| weapons87_flails.py | --apply | flails → grips → motion → spears → individual art overlays → P42/P43 → restored49 → thrown → spear surface → #217 → #225 |
| weapon_resources_1321.py | direct | canonical + approved cooldown source |
| rare_weapon_resources_1322.py | direct | canonical |
| weapon_patch_assets.py | direct | canonical |
| weapon_feedback_assets.py | direct / apply() | canonical; historical pass excluded |
| weapon_visual_repair_assets.py | direct / apply() | canonical; historical pass excluded |
| poloz_full_assets.py | --weapon-only | canonical; exits before armor/text/audio imports |

Обычный Gradle clean build эти Python entrypoints не запускает: `processResources` копирует проверенные ресурсы, подставляя metadata. Отдельные этапные authoring scripts (grips, whip, motion и т. д.) являются внутренними шагами, а не командами воспроизведения итогового принятого набора; запускать их отдельно для окончательной поставки нельзя. Художественные решения не менялись.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| 68 оружий + 52 компонента, registry root → models → texture, точные SHA | inventory.json, 120 строк с dependency paths/hash и vanilla parents | PASS |
| Текущие display/pivot/GUI, отдельные held и динамические состояния не откатываются | Все resource semantics сравниваются с полным baseline для каждого entrypoint; исходные JSON, 12 crossbow states и bound models остаются прежними | PASS — after-*.json |
| Все семь entrypoints и идемпотентность | Каждый выполняется дважды в отдельной копии; after-*.json | PASS — summary.json |
| Первая и повторная генерация не меняют чужие ресурсы | full-resource hash + semantic/PNG comparison, source_mutated=false | PASS — summary.json |
| Production source 1.9.7.60 не изменён этой работой | production-preservation.json: 8729 файлов; новых production files0 | PASS |
| Java21 clean build, JAR/resources | ../issue129-current/clean-build.log (44s), jar-resources.json | PASS |
| Игровая визуальная проверка | Новых art/gameplay изменений нет; последние #217/#219/#220/#225/#226 приняты владельцем; новый запуск не выполнялся | NOT_RUN — не подменяется генераторной проверкой |

Девять JSON допускают только отличие форматирования при первом replay: semantic drift0; при втором проходе byte drift0. Исторические PNG/рендеры не переписывались. Новые before/after art renders не создавались, поскольку production art не менялся; актуальные фактические изображения остаются в ../issue225/REPORT.md и ../weapons87/.

Новый gate `weapons87_generator_gate.py` фиксирует текущий HEAD и полный SHA baseline вместо ложной привязки к старому P32 commit. Копируются только потребляемые JSON чертежей, а не 600 MB архивов и игровых кадров; независимый повтор feedback/repair проверяет этот набор входов. Source HEAD1192e229c4aa9166b53661f71f5ca15299f3537e + DIRTY, commit/push не выполнялись. Весь #87/финальный #191 этим пакетом не принят. PolyMC остаётся установленной1.9.7.60, новых установок0, запусков0.

Финал: все7 entrypoints ×2 прохода PASS; семантических/PNG откатов0, idempotence drift0, реальный production source не менялся. #121 передана на «Тестирование», Issue OPEN.
