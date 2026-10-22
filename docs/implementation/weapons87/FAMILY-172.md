# #172 — адресная source art ревизия

Технический art scope KEEP; owner runtime NOT_RUN. Issue OPEN, не принята.

| ITEM | DECISION | EVIDENCE / REASON |
|---|---|---|
| short_bow | KEEP | Короткие тонкие плечи и меньший размер относительно heavy_bow;4 реальные draw sprites сохранены. |
| heavy_bow | KEEP | Более длинные массивные плечи/усиления и отличимый масштаб;4 реальные draw sprites сохранены. |
| bone_arrow | KEEP | Костяной светлый наконечник, древко и оперение; inventory silhouette стрелы отличается от отдельного наконечника/сулицы. Flying entity остаётся стандартной vanilla Arrow (это зафиксировано, не новая модель). |

Actual source A/B: family-art-review/172-source.png; геометрия/UV и PNG сравниваются с before-source.zip, исходные поля display отдельно откалиброваны в #115. У каждого ID есть размеры, пути/SHA в review.json. Это реальные production ресурсы, не концепты. Одинаковые до/после по art — сознательный KEEP, а не неисполненная перерисовка.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все перечисленные IDs, silhouette/material/head proportions | individual reasons + actual source gallery | PASS |
| Art/UV/PNG preservation для KEEP | archived-byte/model comparison, review.json | PASS |
| Native FP/TP/обе руки | #115 source all68, grips/fp source galleries; art unchanged | PASS |
| Build/resource/JAR общий проверенный snapshot | package6-build.log/package6-receipt.json | PASS |
| Реальный GUI/FP/TP/gameplay | запусков0, OWNER_PENDING | NOT_RUN |

Четыре actual draw sprites для каждого bow показаны в short_bow-draw-states.png/heavy_bow-draw-states.png; vanilla-like разные плечи/размеры сохранены. FieldBowClient использует существующий drawPower. Камерный timing #163 ещё не исправлен/не сдан этим art QA. bone_arrow — существующий ArrowItem, в мире используется vanilla Arrow/vanilla projectile texture; кастомная flying модель здесь не заявлена. Сохраняется текущий стандартный arrow pipeline. Finist/crossbows имеют отдельные ID-specific задачи и не перерисованы.
