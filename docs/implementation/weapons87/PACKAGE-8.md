# Пакет8 — #174: грозовой посох с отдельным силуэтом

Технический art scope5 IDs реализован:4 KEEP и storm_staff FIX. Общая #87 IN_PROGRESS, Issue OPEN, owner runtime pending, реальное поле Project не менялось.

| ITEM | DECISION | REASON |
|---|---|---|
| berdysh | KEEP | Длинное древко и широкая боковая плоскость лезвия,21 элемент; класс бердыша различим. |
| noon_sickle | KEEP | Короткая рукоять/серповидное лезвие; vanilla generated sprite сохраняет правильную форму серпа. |
| carved_staff | KEEP | Деревянная многоветочная развилка — собственный природный силуэт. |
| storm_staff | FIX | Старые11 элементов повторяли fork геометрию carved. Новая14-элементная металлическая оправа с закреплённым синим фокусом отличается формой, глубиной и материалом. |
| veles_staff | KEEP | Искривлённая верхняя ось и светлые детали на деревянном древке,15 элементов; отдельный силуэт. |

Storm actual bounds были10x30x7, стали7.2x30x3.2. Фокус соединён с нижней/верхней оправой, связный компонент1. Древко1.5,grip(8,10,8). TP/FP/ground/fixed transforms неизменны, GUI front-lit и центрирован. Никакой перекраски общих staff_wood/binding/crystal PNG: они и4 KEEP модели побайтно сохранены. Материалы следуют реальному Rituals.STORM_STAFF (carved staff +perunite +thunder stone +ancient sign), не выдуманному JSON-рецепту. Ритуал/эффекты/числа/CD не менялись; предложение B всё ещё не согласовано/не применено.

Production edit1 JSON. Канонический tools/weapons87_storm_staff.py — последний overlay, зафиксированы отдельный pre-production blueprint/draft/manifest и архив before-polearms174.zip. Первичная source-проба с обрезанным верхом оставлена как probe; основной чертёж использует одинаковый fixed camera offsetY-7 для полного предмета, без auto-fit.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все5 IDs, обоснованные KEEP/FIX | table above, actual source5 A/B sheets | PASS |
| Индивидуальный source-чертёж до art | blueprints/storm-staff/storm_staff.png/plan/draft-assets | PASS |
| Storm не duplicate/recolor carved | old same11-element shape; new distinct14-element frame/focus | PASS |
| Связность/толщина/GUI1x, actual UV | geometry-gates.json, source blueprint, actual existing PNG references | PASS |
| Обе руки/Steve-Alex/FP-TP/scale |100 native source cases; existing28 grip-native checks; AFTER fixed-idle head candidates0 | PASS |
|4 KEEP/общие PNG/серверные свойства не менялись | preserved-files.json, baseline+68 native snapshots | PASS |
| Java21 clean build + applicable native fixtures | package8-build.log, test NO-SOURCE | PASS |
| Actual resource/JAR/applicable historical generator replay | package8-gates.log/package8-receipt.json, exact draft matches | PASS |
| Реальные GUI/FP/TP/drop/frame/motion/lighting | launches0, owner-only | NOT_RUN / OWNER_PENDING |
| PolyMC установка | не поручена для промежуточной цепочки | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный промежуточный JAR `D:\slavic-myths\build\libs\slavicmyths-1.9.7.53.jar`,version1.9.7.53,SHA256 `0133194a32447492b7daf15303c8b7721be9ce331183c496d5d6e507ee200454`,23692215 bytes. Source base `32d1733457e7e90360ce16c23860304ece664c64` +uncommitted manifest SHA `19c14d35fdd1c528a4267ba7bea13361c4dbca1b3481b4643fea12860e9fb235`. Без commit/push/установки, не финальная сборка87. Source evidence frozen package8-polearm-source-evidence.zip.

Owner: пять предметов рядом, отдельно carved vs storm; GUI/FP/TP/оба main arms/Steve-Alex/ходьба/рамка/ground. Проверить компактный кристалл, крепление к оправе, прежний хват и отсутствие перекрытия лица/камеры. Существующие способности проверить без изменения цифр. Анимации/эффекты #165/#160/#161 — отдельные ещё продолжающиеся задачи.

Далее индивидуальная ревизия15 uniques #175–189. Fresh descriptions сохранены unique-live-issues.json и сверены с полностью прочитанным исходным snapshot: изменений0. Ранее принятые удачные модели сохраняются, для подтверждённых FIX готовятся отдельные чертежи до production. #180 whip — вероятный отдельный art FIX, затем #190.
