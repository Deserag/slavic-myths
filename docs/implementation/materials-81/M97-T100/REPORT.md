# #97 / #100 — 23.10.2026

Сначала проверена фактическая реализация. Три металла #97 уже реализованы в metals36_assets.py по позднему решению владельца о vanilla topology; они KEEP, byte/pixel parity подтверждена. Старая просьба о другой форме слитка не применялась повторно.

Изменены только семь production PNG: block/amber_block и diamond/perunite/netherite пары spearhead/light_spearhead. Янтарь — непрерывные медовые слои и включения вместо нарисованного куба на грани. Наконечники — широкая и лёгкая формы, отдельные профили материалов, кристаллические грани/локальные жилы/тяжёлый графит. Native16/32px и бинарная alpha сохранены; блок по-прежнему opaque cube_all, тот же BlockItem. Никаких Java/model/recipe/registry/gameplay изменений.

До/после: AB-seven.png, сравнение четырнадцати наконечников: all-fourteen.png. Это actual PNG offline изображения, не игровые кадры. Before восстановлен из HEAD для семи ранее неизменённых PNG; отдельный проверенный manifest предыдущей сборки доказывает production-diff.json: ровно7 PNG. Существующие чужие dirty изменения сохранены.

Генератор materials81_m97_t100.py с точным whitelist подключён к materials81_m1, generate_046 и финальному create_resources; generate_045/weapon_resources_1321 уже используют m1. Два scoped replay через реальный m1.write_owned в temp-root совпадают. Полные исторические генераторы с побочными системами/карантинным аудио не запускались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Не переделывать уже реализованные три металла | metals36_assets.py, verification.json KEEP | PASS |
| Янтарь: реальные block/item references, единая грань, прежняя форма/alpha/коллизия | cube_all/item parent сохранены, AB-seven.png, production-diff.json | PASS source |
| Шесть наконечников, ordinary шире light, разные материальные профили | AB-seven.png, all-fourteen.png, verification.json | PASS source |
| Сохранить принятые #98/#99 | 12 canonical pixel comparisons | PASS |
| Native resolution и воспроизводимость | verification.json:29 checks, два scoped replay | PASS |
| Source scope | предыдущий finist223/source-manifest.json; production-diff.json | PASS: ровно7 PNG, иных production изменений0 |
| Java21 clean build | materials97-100-build.log, SUCCESSFUL48s | PASS |
| Production NeoForge JAR | materials97-100-resources.log:7631resources,1371PNG,1809classes, no QA classes | PASS |
| Игровой вид янтаря в мире и инвентаре/наконечников, личная эстетическая приёмка | владелец проверяет; в этой итерации Minecraft/server/GameTest запусков0 | NOT_RUN / OWNER_ONLY |

Исполнительский scope готов к личной проверке; Project «Тестирование», НЕ «Готово». Issues OPEN, owner-accepted не назначен. PolyMC НЕ обновлялся: новая установка не запрашивалась. Версия оставлена1.9.7.58; local JAR SHA25636626fdc962afa37e4423dc5b4ba85bcb1c7244b741907c109c013c2926eeeef. Source1192e229 + dirty snapshot, commit/push нет. Сборка не выдаётся за runtime.
