# #87 / пакет 14 — #115: точка хвата двух арбалетов в третьем лице

22.10.2026. Ветка `feat/weapons87-p14-crossbow-grips`. CI green и main объединён после сверки на конфликты/изменения. #115 и #87 остаются OPEN, владелец принимает визуал в Minecraft.

**Состояния:** `serpent_crossbow`, `three_breath_crossbow`; у каждого default, pulling_0, pulling_1, pulling_2, arrow, firework. Все 12 production JSON имеют отдельные thirdperson_left/right и cuboid grip [7,5,10]–[9,6,15], центр (8,5.5,12.5). Основа — frozen `before-crossbow-tp-grips.zip`, индивидуальные source-чертежи `blueprints/crossbow-grips/`, generator `tools/weapons87_crossbow_grips.py`, фактический `WeaponHands.crossbowModel`.

Предыдущая матрица: TP rotation[-90,0,0], scale.55, translation[0,2,0]. Grip offset после rotation/scale = [0,4.475,1.375] model units от ладони. Теперь TP translation[0,-2.475,-1.375] у обеих рук во всех 12 JSON; итоговый pivot в центре руки по математике модели. Точки first-person `WeaponHands` и все firstperson JSON не менялись. Геометрия, UV, atlas PNG, overrides, GUI, головы, ground, fixed, физика болтов, burst/charge, gameplay и баланс сохранены.

**Реальная область доказательств:** Python offline `tools/verify_weapons87_crossbows.py` сравнивает исходный ZIP, proposal canonical script и все реальные production 12 JSON, гарантирует единственные отличия в translation двух TP рук, просчитывает 48 старых/новых pivot-case и наличие PNG текстур. Это чистая геометрическая проверка, НЕ native Minecraft scene и НЕ runtime.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| 6 состояний × 2 ID × обе руки | 12 изменённых JSON; ZIP до изменений; `verify_weapons87_crossbows.py` | source PASS после CI |
| Рукоять в точке TP wrist / сохранение размеров | числовая матрица old4.6815→new0; `crossbow-grips/plan.json` | source PASS после CI |
| Geometry/UV/PNG/GUI/FP/predicates/gameplay KEEP | whitelist-diff с исходным ZIP | source PASS после CI |
| Воспроизводимость генератора и текстур | `weapons87_crossbow_grips.proposal`, asset check | source PASS после CI |
| Java21 clean build, валидный JAR | GitHub Actions `weapons87-p14-java21.yml` | PASS — [Java21 CI run 39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) |
| Steve/Alex, обе руки, ходьба, атака, правильное видимое сцепление | ручная игра владельца | NOT_RUN / OWNER_PENDING |
| PolyMC installation | установка запрещена и не проводилась | NOT_RUN |

После получения зелёного CI — проверить актуальный main и только затем решать merge. Далее #175/#176/#177/#179, затем #183–189; художественный KEEP арбалетов не означает завершения #163 timing/shot.

### Финальная публикация
PR [#194](https://github.com/Deserag/slavic-myths/pull/194) merged as `9910e5ca17e5bbf5fa14dff55fa371640fe93398` после двух последовательных успешных CI на source commits `e5060ba` и `6114df3`; итоговый run [39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) на HEAD `6114df3fe4a83a3461ab9c0043393135d1da3c47`. Java 21 `clean build` PASS; `tools/verify_weapons87_crossbows.py` PASS (12 models, 48 old/new pivots, 12 referenced textures, no unplanned changes). `slavicmyths-1.9.7.56.jar` SHA256 `73d0d04b5ef5890c8508f626022b68fc567fa30ac84a90e3d1fb208bd7d2cffc`. Только CI output; no PolyMC installation or Minecraft runtime. Further review on #115 remains OWNER_PENDING. 
