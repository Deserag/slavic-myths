# #114 — QA31 и сводный64-ID контроль, 23.10.2026

Проверен фактический каталог:64 ID,17ordinary/16elite/31legendary. В QA-only перечне31:30KEEP и1FIX, без массовой перерисовки. catalog64-audit.json содержит пути, actual parent/layers и SHA каждого источника; порядок и creative rows сохранены. Не перепутаны одноимённые PNG с реальной многотекстурной моделью: учтены6 separate_transforms GUI, old_hook4cuboids,2трофея Соловья, блочный mor_crystal и FrostClient сердце.

Единственный подтверждённый FIX — firebird_down: прежняя лучистая форма солнца вместо пуха. Сначала сохранены отдельные16px/64px blueprint, затем применён оригинальный четырёхцветный пучок с коротким стержнем и расходящимися бородками. Native64/model/UV/registry/gameplay сохранены. Ровно1 production PNG изменён. Scoped writer защищён в firebird_resources, vanilla_item_style и последнем overlay create_resources. Двойной replay и actual vanilla FunctionDef проверены в temp-root без полного генераторного bootstrap/аудио.

QA-1..4-before/current-unlabelled.png — попарные actual A/B,1×/nearest3× без подписей. catalog64-unlabelled.png — сводный source-render, НЕ игровой скриншот. Воспроизведение: tools/audit_trophy114.py. Реальные JSON/UV/PNG, но без AO/game shaders; тонкие3D предметы требуют личной проверки освещения в игре. KEEP означает отсутствие доказанного source-дефекта, не приёмку владельца.

| ID | Action | Индивидуальный результат |
|---|---|---|
| old_hook | KEEP | actual4-cuboid hook, multi-texture; тонкая форма, game lighting owner-only; actual пути/SHA в catalog64-audit.json |
| noon_ear | KEEP | колос с отдельными зёрнами; actual пути/SHA в catalog64-audit.json |
| druzhinnik_blade_fragment | KEEP | сломанный узкий клинок; actual пути/SHA в catalog64-audit.json |
| pottery_fragment | KEEP | separate_transforms: охристый гнутый черепок; actual пути/SHA в catalog64-audit.json |
| old_arrowhead | KEEP | separate_transforms: острый наконечник; actual пути/SHA в catalog64-audit.json |
| ancient_fibula | KEEP | separate_transforms: открытая застёжка; actual пути/SHA в catalog64-audit.json |
| ancient_comb | KEEP | separate_transforms: гребень с зубьями; actual пути/SHA в catalog64-audit.json |
| ancient_beads | KEEP | separate_transforms: подвесные бусины; actual пути/SHA в catalog64-audit.json |
| ovinnik_claw | KEEP | коготь с тёмным основанием; actual пути/SHA в catalog64-audit.json |
| ognennoe_pero | KEEP | широкое оранжевое перо; actual пути/SHA в catalog64-audit.json |
| vihrevaya_nit | KEEP | светлая изогнутая нить; actual пути/SHA в catalog64-audit.json |
| kost_likha | KEEP | длинная кость с расширенными концами; actual пути/SHA в catalog64-audit.json |
| nit_durnoy_doli | KEEP | красно-бурая узловатая нить; actual пути/SHA в catalog64-audit.json |
| firebird_down | FIX | FIX: солнце заменено пучком пуха по индивидуальному blueprint; actual пути/SHA в catalog64-audit.json |
| firebird_feather | KEEP | длинное золотое перо со стержнем; actual пути/SHA в catalog64-audit.json |
| smoldering_feather | KEEP | обожжённое короткое перо с разрывами; actual пути/SHA в catalog64-audit.json |
| mat_bone_plates | KEEP | несколько плоских костяных пластин; actual пути/SHA в catalog64-audit.json |
| mat_mirror_shards | KEEP | угловатые отражающие осколки; actual пути/SHA в catalog64-audit.json |
| mat_skim_fang | KEEP | длинный изогнутый клык; actual пути/SHA в catalog64-audit.json |
| mat_royal_iron | KEEP | несколько тёмных металлических сколов; actual пути/SHA в catalog64-audit.json |
| us_tugarina | KEEP | короткий ус с острыми концами; actual пути/SHA в catalog64-audit.json |
| zmeinaya_pryazhka | KEEP | латунная пряжка со змеиным знаком; actual пути/SHA в catalog64-audit.json |
| naboyka_tugarina | KEEP | железная набойка с центральным креплением; actual пути/SHA в catalog64-audit.json |
| idol_fragment | KEEP | коричневый обломок идола с лицом; actual пути/SHA в catalog64-audit.json |
| ancient_rune_fragment | KEEP | расщеплённые серо-зелёные резные фрагменты; actual пути/SHA в catalog64-audit.json |
| sacred_ember | KEEP | оранжевый уголёк в угольной оболочке; actual пути/SHA в catalog64-audit.json |
| pure_spring_drop | KEEP | бутылочка с голубой каплей; actual пути/SHA в catalog64-audit.json |
| spring_tear | KEEP | отдельная капля с ореолом, отличается от бутылки; actual пути/SHA в catalog64-audit.json |
| poloz_scale | KEEP | золотая чешуя с красным узором; actual пути/SHA в catalog64-audit.json |
| gorynych_scale | KEEP | светлая чешуя с тёплой трещиной; actual пути/SHA в catalog64-audit.json |
| ancient_serpent_steel | KEEP | тёмный слиток с красной диагональной жилой; actual пути/SHA в catalog64-audit.json |

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Поимённый QA31, только доказанный FIX | таблица, ids.json | PASS offline |
| Все64 ID, порядок17/16/31 | catalog.json/catalog64-audit.json | PASS |
| Индивидуальный blueprint до production, A/B1×/3× | firebird-down-blueprint16/64.png;QA-2-before/current | PASS source |
| Actual model/block/renderer links | audit_trophy114.py;catalog64-unlabelled.png | PASS offline |
| Native64, exact replay/actual legacy function | verification.json | PASS |
| Остальные production bytes сохранены | production-before.json/production-diff.json:1PNG | PASS |
| Java21 clean build | build.log:46s | PASS |
| Production NeoForge JAR | resources.log:7631resources/1809classes, QA excluded | PASS |
| Игровой вид64 и личная эстетическая приёмка | запусков0; владелец проверяет | NOT_RUN / OWNER_ONLY |

#110/#111 приняты ранее; #112/#113 ожидают личной проверки. #92 остаётся открытой, #114 фактически «Тестирование» (project-after.json). Ни Issues, ни owner-accepted не менялись. Source1192e229 +dirty tree, commit/push нет. Local version1.9.7.59 JAR SHA256913ed4a897cca68a4a581dece5e22a3b5fc1ffda9263fd008d4a5d11c2b2b5ee. Новый JAR не установлен: в PolyMC прежний тестовый1.9.7.59 SHAf211c5e5b00d626f1093b6e6ff1dcb7f87aa1051d520b155e939b71b08fd294b. Minecraft/server/GameTest запусков0.
