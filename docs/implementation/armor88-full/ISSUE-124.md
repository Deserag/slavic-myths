# #124 / parent #88 — source evidence

IN_PROGRESS / IN_GAME_OWNER_PENDING. Issue OPEN, приёмки нет.

Партии: bogatyr_chestplate; clear_sight_helmet, indomitability_leggings; wind_boots, seven_league_boots.

- `bogatyr_chestplate` — **REDESIGN**. Раздельные крупные ламели на кожано-красной основе и солнечная застёжка; сплошные полосы убраны. [A/B](bogatyr_chestplate.png).
- `clear_sight_helmet` — **KEEP**. Меш/UV/PNG идентичны: лицо открыто, камень и боковины самостоятельны; runtime NOT_RUN. [A/B](clear_sight_helmet.png).
- `indomitability_leggings` — **SMALL_FIX**. Сливовые бедренники и колени сохранены; внутреннее перекрытие двух ног устранено. [A/B](indomitability_leggings.png).
- `wind_boots` — **SMALL_FIX**. Боковые ступенчатые перья, отдельная подошва; внутренние грани разведены. [A/B](wind_boots.png).
- `seven_league_boots` — **REDESIGN**. Дорожные сапоги с красным отворотом и ремнями вместо целых ванильных штанин; без перьев. [A/B](seven_league_boots.png).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Точный scope и партии 1–4 ID, индивидуальные решения | inventory.json и список выше | PASS |
| Фактические ModelPart/UV/PNG, old/new SHA | before/, before-native.json, after-native.json, inventory.json | PASS |
| Front/back/right/left/3quarter, Steve/Alex, idle/walk | 40 source A/B пар на ID, тот же масштаб/камера | PASS для source |
| Raised arm/sneak | Те же нативные меши с заданными bone poses | PASS для source; runtime NOT_RUN |
| GUI icon 1×/3× nearest | ICONS-1x-3x.png и исходные PNG | PASS |
| UV/resource/JSON/production JAR/Java21 clean build | verification.json, BUILD.log | PASS |
| Воспроизводимый scoped atlas overlay | tools/armor88_art.py, idempotence/resource diff gate | PASS |
| ID/stats/recipes/loot/имена/NBT/creative не менялись задачей88 | baseline-sha256.json и verification.json; правки44 сохранены | PASS для source |
| 13 аксессуаров и woven_belt mesh не изменены | verification.json | PASS |
| Runtime clipping/z-fighting, смешанные вещи, оружие в руке, first/third person, armor stand, trim/runes/enchants, relog/MP | RUNTIME-MATRIX.md; запусков0 | NOT_RUN |
| Личная приёмка владельцем | Не получена, issue OPEN | NOT_RUN |

[Копланарные пересечения с координатами](coplanar-intersections.json): анализ геометрии фронтальных поверхностей, не игровой тест. Наружный профиль сохранён, изменены внутренние стыки.
