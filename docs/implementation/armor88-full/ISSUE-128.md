# #128 / parent #88 — source evidence

IN_PROGRESS / IN_GAME_OWNER_PENDING. Issue OPEN, приёмки нет.

Партии: trophy_idol_chest; trophy_marinka_cloak; trophy_kartaus_mantle.

- `trophy_idol_chest` — **SMALL_FIX**. Раздельные костяные рёбра с промежутками и посаженные на руки плечи, бронзовая основа. [A/B](trophy_idol_chest.png).
- `trophy_marinka_cloak` — **REDESIGN**. Открытый ворот, два самостоятельных нижних хвоста и безопасный наклон от ног; зеркало сохранено. [A/B](trophy_marinka_cloak.png).
- `trophy_kartaus_mantle` — **SMALL_FIX**. Светлый открытый ворот, единый красный стяг движется с тяжёлой мантией; отличен от Маринки. [A/B](trophy_kartaus_mantle.png).

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
