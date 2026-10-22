# Пакет5 — #171: согласованные обычные копья

Технический scope #171 реализован. Все9 IDs проверены:5 FIX (`gold_spear, iron_spear, diamond_spear, perunite_spear, netherite_spear`),4 KEEP (`ancient_spear, restored_spear, spear, silver_spear`). #87 остаётся IN_PROGRESS, Issue OPEN. Реальное поле Project не изменялось. Игровая приёмка владельца обязательна.

Причина:5 материальных копий оставались16px flat held с унаследованной боковой ориентацией, тогда как stone/silver имеют40-unit объёмное древко. Новые5 held —40 model units, деревянное древко1.2x1.2, отдельная металлическая втулка/ступенчатое широкое остриё, обмотки; индивидуальные профили металлов. Authored grip(8,6,8), TP scale0.7 даёт28 units при высоте игрока32, наклон55°. Каждый FIX имеет до применения отдельный actual JSON/UV чертёж front/profile/3-4 и GUI1x. Все существующие PNG сохранены. GUI separate perspective использует прежний sprite, root явно front-lit, как исходный item/handheld. Metadata revision сохранена отдельно, старый draft V1 не удалён.

Оригинальные stone/silver40-unit и древние/восстановленные46-unit модели полностью сохранены.14 наконечников #99/#100 только сверялись, не менялись. Уникальные pool/thunder/victor не затронуты. Урон, reach, рецепты, ID, server combat и игровые свойства не менялись. Из числовых изменений всей цепочки остаётся только утверждённый ранее пакет A.

Production изменения #171:5 root JSON заменены на separate_transforms,10 новых _held/_icon JSON. PNG edits0. Канонический tools/weapons87_spears.py включён в общий воспроизводимый слой, вызываемый историческими weapon generators. Дополнительное техническое уточнение #164: FlailClient очищает одноразовый owner binding при model reload и при resource-pack замене builtin/entity на обычную модель, когда BEWLR не вызывается; движения/числа не меняются.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все9 IDs,5 FIX/4 обоснованных KEEP |9 source A/B sheets, native input descriptors, preservation receipt | PASS |
| Индивидуальные чертежи до art | blueprints/spears/5 per-ID PNG, draft-assets/manifests, before-spear-family.zip | PASS |
| Реальная геометрия/UV и читаемый GUI | actual production JSON/UV source, старые PNG byte-identical | PASS |
| Оба физических хвата, scale/Steve/Alex |72 actual native ItemTransform contexts,180 source cases;10 новых TP pivots centered | PASS |
| Наконечник не пересекает голову в sampled idle source pose | exact triangle/head box candidates0 AFTER; не runtime clipping test | PASS |
| Сохранение4 KEEP и14 наконечников/боя | spears-after/preserved-files.json + baseline/preservation gate | PASS |
| Clean build Java21 + native attrs/grips/flails/spears | package5-build.log; test task NO-SOURCE | PASS |
| Ресурсы/JAR и воспроизводимость без чужих откатов | package5-gates.log, package5-receipt.json | PASS |
| Реальные GUI/FP/TP/drop/item frame/attack/MP/lighting | запуска Minecraft/PolyMC/GameTest/server не было | NOT_RUN / OWNER_PENDING |
| Установка в PolyMC | не поручалась для промежуточной оружейной цепочки | NOT_RUN / POLYMC_NOT_INSTALLED |

Точная промежуточная сборка: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.53.jar`,version1.9.7.53,SHA256 `d5a94042358159517734bfaa026b03fc9f5640803dccc184d248f7c2176f2fee`,23691848 bytes. Source base `32d1733457e7e90360ce16c23860304ece664c64` + working tree manifest `ecd81028070d5341fc260d4e6c6f7d692dad40959dcf7d05f96fa3f197ee991d`. Не commit/push и не установленный JAR. Артефакт source A/B заморожен в package5-spear-source-evidence.zip.

Owner-проверка: все9 копий и лёгкие сулицы рядом, GUI и выброшенные предметы/рамка, Steve/Alex/оба main arms/main-offhand, idle/walk/sneak/LMB. Проверить положение ладони, головку рядом с лицом, FP читаемость, старые иконки и lighting. Ударные движения относятся к #165 и проходят отдельный пакет, а не объявляются проверенными этим art-пакетом.

Следующий независимый scope: #173 реальная flying projectile orientation и #169/#170/#172 адресные source KEEP/FIX. Финальная согласованность #168 ещё IN_PROGRESS до #173. Балансовые B/C/D не применены без ответа владельца.
