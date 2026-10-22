# #170 — адресная source art ревизия

IN_PROGRESS: silver_mace требует FIX, пакет не сдаётся.

| ITEM | DECISION | EVIDENCE / REASON |
|---|---|---|
| club | KEEP | Массивная широкая деревянная ударная часть с обвязкой, не клинок/топор. |
| wooden_mace | KEEP | Компактная деревянная головка на узкой рукояти, пропорции отличаются от массивной дубины. |
| stone_mace | KEEP | Крупная каменная головка с перевязкой на древке, материал/назначение читаются. |
| mace | KEEP | Отдельная светлая металлическая ребристая головка, не деревянный набалдашник. |
| silver_mace | FIX_PENDING_BLUEPRINT | FIX: #silver ссылается на light_gray_terracotta, фактически розовато-коричневую; две тёмные полосы выступают отдельно перед головкой. Нужны настоящий серебристый материал и связная гранёная головка, не простая перекраска. |
| perunite_mace | KEEP | Объёмная синяя гранёная головка с выраженными рёбрами и длинной рукоятью. |
| ancient_chekan | KEEP | Асимметричная рабочая часть/клюв; принятая владельцем ориентация сохраняется. |
| chekan | KEEP | Восстановленный асимметричный чекан; принятая ориентация и рабочая плоскость сохраняются. |
| battle_axe | KEEP | Широкая боковая плоскость лезвия на отдельной рукояти; отличается от чекана и булавы. |

Actual source A/B: family-art-review/170-source.png; геометрия/UV и PNG сравниваются с before-source.zip, исходные поля display отдельно откалиброваны в #115. У каждого ID есть размеры, пути/SHA в review.json. Это реальные production ресурсы, не концепты. Одинаковые до/после по art — сознательный KEEP, а не неисполненная перерисовка.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все перечисленные IDs, silhouette/material/head proportions | individual reasons + actual source gallery | FAIL: silver_mace;8 остальных PASS |
| Art/UV/PNG preservation для KEEP | archived-byte/model comparison, review.json | PASS |
| Native FP/TP/обе руки | #115 source all68, grips/fp source galleries; art unchanged | PASS |
| Build/resource/JAR общий проверенный snapshot | package6-build.log/package6-receipt.json | PASS |
| Реальный GUI/FP/TP/gameplay | запусков0, OWNER_PENDING | NOT_RUN |

Удачная ориентация обоих чеканов сохранена по решению владельца. Silver mace исправляется отдельным следующим пакетом после source-чертежа; никаких числовых изменений это решение не разрешает.


## Технический FIX реализован

Silver_mace исправлена по отдельному pre-production чертежу; connected components3→1, реальные холодные серебряные UV, прежние hand transforms. Старые FAIL строки выше — диагноз исходного состояния, не текущий gate. Текущая полная матрица всех9: PACKAGE-7.md/package7-receipt.json; executor art/native/build/resource/JAR/generator gates PASS, только owner runtime NOT_RUN. review.json содержит актуальные hashes и FIX_IMPLEMENTED. Рекомендуемое Тестирование, поле Project не менялось, Issue OPEN.
