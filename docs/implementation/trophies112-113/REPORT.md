# #112 / #113 — адресные трофеи, 23.10.2026

Старт по прямому «продолжай дальше». Прочитаны свежие Issue body/comments; первоначальный scope сверен с реальными model→texture и поздним стандартом владельца. 16 ID:11FIX,5KEEP. Старые полноценные модели не подменялись одноимённым PNG. Изменены только11 существующих PNG, нативные16/32/64px и alpha0/255 сохранены. Ни Java, ни модели/UV/display, ни registry/gameplay/recipes/loot/локализации/миры не менялись.

Партии выполнены последовательно: T3-A3, T3-B2, T4-A2FIX+mor_crystal KEEP, T4-B1FIX+2KEEP, T4-C3. Каждая имеет отдельную source A/B панель с16px,3× и64px одинаковыми рамками; исходные PNG сохранены до записи. Это offline actual assets, не игровые скриншоты. Для5KEEP имеются source причины в audit.json. Никакой автоматической приёмки или закрытия.

Конкретные дефекты: прядь Кикиморы была крупным предметом с креплением вместо тонких спутанных волос; ткань и лента имели мало материальных деталей; знамя было почти чёрной плоской плашкой; перо — почти только стержнем. Пыль Мора была единым комком; глаз — круглым жетоном; сердце — ровным красным символом; ядро Полоза — плоским рунным значком; голова — короной без читаемой морды/клыков; череп — коротким человеческим. FIX сделаны индивидуальной геометрией пикселей, не общей recolor-заготовкой.

Nightингale lock/mark: KEEP actual8/7cuboids и8texture links; локон и подвеска различаются. Сохранены материалы и transforms. special-models-KEEP.png — воспроизводимый offline actual JSON/UV multi-material raycast (tools/preview_trophies112_113.py), GUI/isolated1P/3P; без игрока, AO и игровых анимаций. Это НЕ доказательство runtime. mor_crystal: та же actual block geometry/texture, angular cluster отличим от shard; на этой панели source form также отражает установленный блок, но реальный world QA owner-only. Сердце холода: builtin/entity → FrostClient → *_icon/held; его ступенчатое ледяное сердце и светлая сердцевина сохранены. Shard: нынешний длинный неровный фиолетовый осколок сохранён.

Генераторы: отдельный trophies112_113_art.py, scoped hooks vanilla_item_style.sprite, kurgan_086.sprite, Poloz sprites/full texture loop и последний overlay create_resources. Исторические audio generators не запускались. Проверены два exact writer replay и реальные FunctionDef vanilla/kurgan в temp-root через AST без импортов с побочными эффектами. В poloz_full_assets hook до im-generation только для owned item PNG, весь audio/bootstrap не запускался.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все7ID #112 /9ID #113 учтены, существующее сначала проверено | audit.json, production-before.json, исходные model/PNG | PASS source |
| Индивидуальные материальные формы, 11FIX/5KEEP | T3-A/B.png, T4-A/B/C.png, special-models-KEEP.png | PASS offline review |
| Сохранить multi-texture Соловья / реальный block crystal / Frost renderer | actual links; production-diff.json: models/Java unchanged | PASS source |
| Native resolution, alpha, old/new SHA | audit.json; before/; baseline | PASS |
| Не откатить sprites legacy writers | verification.json:26 scoped gates; actual AST sprite routes | PASS |
| Сохранить остальные production bytes, включая прежние #97/#100 | production-before.json vs current, production-diff.json | PASS: ровно11PNG |
| Java21 clean build | build.log, SUCCESSFUL47s | PASS |
| Current NeoForge production JAR | resources.log:7631resources/1371PNG/1809classes; smoke excluded | PASS |
| Actual in-game inventory/hands/world lighting, эстетическая приёмка | только владелец; в этой итерации запусков0 | NOT_RUN / OWNER_ONLY |

Local version1.9.7.58, JAR SHA2566e9bad807a94a21125b5787c3270816e605545b365a6af6a3a4c7202dcc96cae. Source1192e229+dirty tree, без commit/push. PolyMC не установлен/не обновлялся в этой итерации. Исполнительский scope передан в «Тестирование», готовность принимается владельцем. Issues OPEN, owner-accepted не выставлялся. Исторические игровые проверки других задач не считаются новым runtime этих PNG.

## Поимённые файлы

| ID | Action | Actual PNG / причина KEEP |
|---|---|---|
| kikimora_lock | FIX | src/main/resources/assets/slavicmyths/textures/item/kikimora_lock.png |
| grave_cloth_scrap | FIX | src/main/resources/assets/slavicmyths/textures/item/grave_cloth_scrap.png |
| torn_burial_ribbon | FIX | src/main/resources/assets/slavicmyths/textures/item/torn_burial_ribbon.png |
| mat_banner_remnant | FIX | src/main/resources/assets/slavicmyths/textures/item/mat_banner_remnant.png |
| mysterious_black_feather | FIX | src/main/resources/assets/slavicmyths/textures/item/mysterious_black_feather.png |
| mor_dust | FIX | src/main/resources/assets/slavicmyths/textures/item/mor_dust.png |
| eye_of_mor | FIX | src/main/resources/assets/slavicmyths/textures/item/eye_of_mor.png |
| ashen_heart | FIX | src/main/resources/assets/slavicmyths/textures/item/ashen_heart.png |
| poloz_core | FIX | src/main/resources/assets/slavicmyths/textures/item/poloz_core.png |
| poloz_head | FIX | src/main/resources/assets/slavicmyths/textures/item/poloz_head.png |
| cursed_volkolak_skull | FIX | src/main/resources/assets/slavicmyths/textures/item/cursed_volkolak_skull.png |
| nightingale_lock | KEEP | Actual multi-texture hair bundle, no missing reference; silhouette retained. |
| nightingale_mark | KEEP | Actual hanging medallion with metal insert; distinct from hair, retained. |
| mor_crystal | KEEP | Actual angular cluster/block inherited model, distinct from shard; retained. |
| heart_of_eternal_cold | KEEP | Actual FrostClient icon/held renderer, stepped icy heart/light core; retained. |
| shard_of_last_night | KEEP | Actual long angular purple shard, distinct silhouette from Mor cluster; retained. |
