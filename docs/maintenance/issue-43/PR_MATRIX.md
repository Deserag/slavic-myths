# Матрица всех17 открытых PR

Source of truth: main `fb2994e958911169317523e1066b8a1ca7a155af`. Все PR остаются открытыми; автоматического закрытия/слияния нет.

|PR|Unique / behind|Production files|Решение|Зависимость / конфликты / обоснование|
|---|---|---:|---|---|
|[#41](https://github.com/Deserag/slavic-myths/pull/41)|2 unique / 0 behind|0|SUPERSEDE|README включён в интеграцию; прежний план заменяется измеренным manifest.|
|[#40](https://github.com/Deserag/slavic-myths/pull/40)|2 unique / 0 behind|169|SUPERSEDE_BY_V3_REVIEW|55-card layout отклонён; локальная завершённая V3 сохранена; не подменять её #40.|
|[#39](https://github.com/Deserag/slavic-myths/pull/39)|1 unique / 0 behind|0|HOLD_EVIDENCE|391 файлов измерений,0 production; не нужен для внедрения #37; сохранить evidence.|
|[#38](https://github.com/Deserag/slavic-myths/pull/38)|2 unique / 0 behind|1|INTEGRATE|Минимальный accepted-target guard; точная source parity с #38; не менять melee balance.|
|[#37](https://github.com/Deserag/slavic-myths/pull/37)|1 unique / 0 behind|1|INTEGRATE|Только устранение массивов куба; 10000 offline vertex cases; без утверждения о приросте FPS.|
|[#36](https://github.com/Deserag/slavic-myths/pull/36)|1 unique / 0 behind|0|HOLD_EVIDENCE|Расследование эффектов,0 production; runtime проблемы не исправлены этим PR.|
|[#32](https://github.com/Deserag/slavic-myths/pull/32)|1 unique / 0 behind|0|HOLD_EVIDENCE|Аудит производительности,0 production; не выдавать отчёт за патч.|
|[#31](https://github.com/Deserag/slavic-myths/pull/31)|1 unique / 0 behind|0|SUPERSEDE_BY_V3_REVIEW|Отвергнутый дизайн55; не вливать как актуальную спецификацию.|
|[#27](https://github.com/Deserag/slavic-myths/pull/27)|1 unique / 0 behind|18|HOLD_GAMEPLAY_REVIEW|18 production files с изменениями баланса; не смешивать с crash/allocation fixes.|
|[#25](https://github.com/Deserag/slavic-myths/pull/25)|1 unique / 0 behind|6|HOLD_FOR_V3|Четыре fixes уже включены в #40/V3; не cherry-pick повторно; основной путь — reviewed V3.|
|[#24](https://github.com/Deserag/slavic-myths/pull/24)|2 unique / 0 behind|0|HOLD_DOCS_RECONCILIATION|Final QA documentation,0 production; локальные классовые16 файлов сохранены/включены отдельно, runtime claims не переносить без контекста.|
|[#21](https://github.com/Deserag/slavic-myths/pull/21)|3 unique / 2 behind|0|SUPERSEDE_IMPLEMENTED|Classes3 уже main b577; старый spec не заменяет реализацию.|
|[#14](https://github.com/Deserag/slavic-myths/pull/14)|1 unique / 3 behind|10|SUPERSEDE_MAIN|Storm включён в main; текущая реализованная версия authoritative.|
|[#13](https://github.com/Deserag/slavic-myths/pull/13)|1 unique / 3 behind|4|SUPERSEDE_MAIN|Flame VFX включён в main; старые files не накладывать поверх.|
|[#12](https://github.com/Deserag/slavic-myths/pull/12)|1 unique / 3 behind|7|SUPERSEDE_MAIN|Frost fixes включены в main; не повторять старую реализацию.|
|[#11](https://github.com/Deserag/slavic-myths/pull/11)|1 unique / 3 behind|4|SUPERSEDE_MAIN|Crossbow fixes уже в текущем main1.9.7.5; ветка устарела, не откатывать новые изменения.|
|[#2](https://github.com/Deserag/slavic-myths/pull/2)|4 unique / 3 behind|17|SUPERSEDE_MAIN|Старый этап оружия; последующие принятые модели уже в main.|

## Порядок попадания в main

1. Один новый integration PR содержит production части #38 и #37, README #41, сохранённые локальные class reports и измеренный cleanup manifest. Их отдельные PR не требуется сначала merge-ить.
2. После review merge нового PR обычным merge/squash без force. Сверить remote main SHA и два production blob hashes. Только тогда main обновлён.
3. #38/#37/#41 после проверки результата могут быть закрыты как incorporated, по решению владельца.
4. Далее единственный путь достижений — review сохранённой V3 относительно обновлённого main с её dependency #40/#25; не сливать сначала отвергнутый55-card layout. В этой задаче V3 не публикуется и не меняется.
5. #27 — отдельная балансная приёмка. #32/#36/#39 — сохранённые исследования; #42 остаётся открытой задачей реального качества.

Rollback: до merge интеграционная ветка не меняет main. После merge — обычный revert merge/commit через новый PR, без reset/force и без изменения миров.

## Проверенные Git-конфликты относительно main

`git merge-tree --write-tree --name-only` не изменял index, working tree или ветки. Он может создавать недостижимые временные tree objects; backup выполнен заранее.

- #41: CLEAN against main.
- #40: CLEAN against main.
- #39: CLEAN against main.
- #38: CLEAN against main.
- #37: CLEAN against main.
- #36: CLEAN against main.
- #32: CLEAN against main.
- #31: CLEAN against main.
- #27: CLEAN against main.
- #25: CLEAN against main.
- #24: CLEAN against main.
- #21: CLEAN against main.
- #14: docs/ARCHITECTURE.md, docs/PROJECT_STATUS.md, docs/ROADMAP.md, src/main/java/org/slavicmyths/client/GorynychStormVfx.java, src/main/java/org/slavicmyths/gorynych/GorynychContent.java, src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java, src/main/resources/slavicmyths.equipment.mixins.json, tools/reward-issues-tests/org/slavicmyths/verify/GorynychStormIssueTests.java.
- #13: docs/ARCHITECTURE.md, docs/PROJECT_STATUS.md, docs/ROADMAP.md, src/main/java/org/slavicmyths/client/GorynychFlameVfx.java, src/main/java/org/slavicmyths/gorynych/GorynychContent.java.
- #12: docs/ARCHITECTURE.md, docs/PROJECT_STATUS.md, docs/ROADMAP.md, src/main/resources/assets/slavicmyths/lang/en_us.json, src/main/resources/assets/slavicmyths/lang/ru_ru.json, tools/morozko_assets.py, tools/reward_weapon_texts.py.
- #11: docs/ARCHITECTURE.md, docs/PROJECT_STATUS.md, docs/ROADMAP.md, src/main/java/org/slavicmyths/equipment/mixin/GorynychArrowDamageMixin.java, src/main/java/org/slavicmyths/gorynych/CrossbowDamage.java, src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java, src/main/resources/slavicmyths.equipment.mixins.json, tools/reward-issues-tests/org/slavicmyths/verify/RewardIssueTests.java.
- #2: CHANGELOG.md, docs/ARCHITECTURE.md, docs/PROJECT_STATUS.md, docs/ROADMAP.md, src/main/java/org/slavicmyths/client/NightClient.java, src/main/resources/assets/slavicmyths/models/item/karachun_scythe_held.json, src/main/resources/assets/slavicmyths/textures/item/karachun_scythe.png, tools/weapon_patch_assets.py.
