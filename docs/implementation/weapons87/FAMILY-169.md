# #169 — адресная source art ревизия

Технический art scope KEEP; owner runtime NOT_RUN. Issue OPEN, не принята.

| ITEM | DECISION | EVIDENCE / REASON |
|---|---|---|
| bone_knife | KEEP | Короткое костяное лезвие и простая рукоять; узнаваемо отличается от длинных мечей. |
| silver_dagger | KEEP | Узкое короткое металлическое лезвие, компактная рукоять/перекрестие; материал читается. |
| ancient_carolingian_sword | KEEP | Длинный прямой исторический клинок с выраженным перекрестием/навершием; изношенность уместна. |
| restored_carolingian_sword | KEEP | Восстановленный вариант того же исторического семейства; цельный клинок/светлый металл оправдывают родство формы. |
| silver_sword | KEEP | Тонкий прямой длинный клинок и широкое перекрестие; класс меча различим относительно кинжала. |
| perunite_sword | KEEP | Длинный клинок с тёмным центральным участком/синим металлом; форма того же мечевого класса уместна. |

Actual source A/B: family-art-review/169-source.png; геометрия/UV и PNG сравниваются с before-source.zip, исходные поля display отдельно откалиброваны в #115. У каждого ID есть размеры, пути/SHA в review.json. Это реальные production ресурсы, не концепты. Одинаковые до/после по art — сознательный KEEP, а не неисполненная перерисовка.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все перечисленные IDs, silhouette/material/head proportions | individual reasons + actual source gallery | PASS |
| Art/UV/PNG preservation для KEEP | archived-byte/model comparison, review.json | PASS |
| Native FP/TP/обе руки | #115 source all68, grips/fp source galleries; art unchanged | PASS |
| Build/resource/JAR общий проверенный snapshot | package6-build.log/package6-receipt.json | PASS |
| Реальный GUI/FP/TP/gameplay | запусков0, OWNER_PENDING | NOT_RUN |
