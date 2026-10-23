# #101 — адресный QA10, NO_CHANGE, 23.10.2026

Проверены все десять кандидатов из свежего Issue. Итог10KEEP/0FIX: подтверждённых source-дефектов, требующих адресной перерисовки, не обнаружено. Это допустимый прямым ТЗ NO_CHANGE, не оценка художественной приёмки за владельца. Ничего из production assets, Java/gameplay/worldgen/models/blockstates/registry/порядка каталога не менялось. #87/#92 не затрагивались.

Актуальные actual model→layer0/parent ссылки и native PNG размеры/SHA перечислены в audit.json. amber действительно item/generated, а не блок: отсутствие отдельного block amber не объявлялось ошибкой. Три руды — BlockItem→cube_all; реальные blockstates и textures существуют; верх/низ/стороны используют одну texture all и vanilla UV0..16. ore-source-cubes-faces.png использует фактическую vanilla cube geometry из установленного client JAR и реальные текстуры; это OFFLINE, не игровой мир. audit10.png показывает actual PNG1× иnearest4× на одной рамке; для руд это face images, не выдаются за GUI BlockItem. Никакой новой формы блока/прозрачности/освещения не введено.

| ID | Action | Source диагноз |
|---|---|---|
| flax_stalk | KEEP | Ступенчатый зелёный пучок стеблей с перевязью; контраст сохранён |
| flax_fiber | KEEP | Светлые длинные волокна с разошедшимися концами; отличается от колоса |
| linen_thread | KEEP | Округлый клубок с витками, отличается от плоской ткани |
| linen_cloth | KEEP | Сложенное прямоугольное полотно с кромкой и складками |
| purification_flux | KEEP | Мешочек с узким горлом и светлыми включениями |
| bog_iron_lump | KEEP | Неровный бурый рудный ком с отдельными охристыми кусками |
| bog_iron_ore | KEEP | Бурые включения в серой породе; реальный cube_all |
| silver_ore | KEEP | Светлые серебряные включения, ранее одобренный metals36 style KEEP |
| perunite_ore | KEEP | Синие включения с циановыми деталями, distinct from silver; KEEP |
| amber | KEEP | Настоящий item/generated янтарь, золотая кромка/медовая сердцевина; не BlockItem |

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все10 кандидатов, exact model/texture/native resolution/SHA | audit.json | PASS |
| KEEP/FIX по индивидуальному source-дефекту | таблица/actual audit10.png | PASS offline:10KEEP |
| Ore item/block/side/top/UV/blockstate | audit.json;ore-source-cubes-faces.png | PASS source |
| Production scope | только документы QA, source/JAR parity повторно проверена | PASS:NO_CHANGE |
| Java21 clean build/current JAR | current-clean-build.log:последний46s build #114, без новых production правок после него; resources-recheck.log повторно проверяет7631 byte-exact source resources | PASS, повторная сборка не заявляется |
| Owner-only игровой вид и ресурспаки | запусков0 | NOT_RUN / OWNER_ONLY |

Статус #101 «Тестирование», ожидает только личной проверки владельца. Issues OPEN, приёмка/owner-accepted не заявлены. Версия1.9.7.59 не менялась, PolyMC не обновлялся. Commit/push нет, исходный HEAD1192e229 + сохранённый dirty tree.

## Сводка родителя #81

- #96: реализовано ранее, «Готово» по владельцу; поздний metals36 стиль имеет приоритет над старой формой слитков.
- #98/#99: реализованы ранее, приняты и «Готово».
- #97:3 ранее реализованных металла KEEP, янтарная block texture FIX; «Тестирование».
- #100:6 crafting наконечников FIX; «Тестирование».
- #101:10 кандидатов QA-only KEEP, без новой перерисовки; «Тестирование».

Все шесть исполнительских пакетов имеют source/build/JAR результаты. Родитель #81 переведён из «В работе» в «Тестирование», не «Готово»: личная приёмка #97/#100/#101 остаётся за владельцем. Детальные матрицы: M1-96/REPORT.md, N98-T99/REPORT.md, M97-T100/REPORT.md и текущая. Металлы: ../../metals-vanilla-36/REPORT.md. Требование ручной приёмки всех пакетов для закрытия #81 сохраняется.
