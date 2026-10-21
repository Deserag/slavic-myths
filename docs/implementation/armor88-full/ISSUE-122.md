# #122 / parent #88 — source evidence

IN_PROGRESS / IN_GAME_OWNER_PENDING. Issue OPEN, приёмки нет.

Партии: linen_headscarf, linen_shirt, linen_ports, bast_shoes; sarafan, linen_apron, folk_vest.

- `linen_headscarf` — **REDESIGN**. Открытая лобная лента, волосы/лицо видны, отдельный задний узел. [A/B](linen_headscarf.png).
- `linen_shirt` — **SMALL_FIX**. Вырез, отдельные рукава, задний шов; ткань поверх skin overlay. [A/B](linen_shirt.png).
- `linen_ports` — **SMALL_FIX**. Независимые штанины и внутренние края, низ выше лаптей. [A/B](linen_ports.png).
- `bast_shoes` — **SMALL_FIX**. Низкий плетёный верх, отдельная подошва, внутренние грани разделены. [A/B](bast_shoes.png).
- `sarafan` — **REDESIGN**. Лямки и раздельные передние/задние панели подола на костях ног. [A/B](sarafan.png).
- `linen_apron` — **SMALL_FIX**. Передняя панель, независимый низ; сзади только пояс/завязки. [A/B](linen_apron.png).
- `folk_vest` — **SMALL_FIX**. Открытые передние полы, отдельные плечи и короткая цельная спинка. [A/B](folk_vest.png).

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

[Ансамбль четырёх вещей](folk_ensemble.png). ArmorStandClothingLayer использует те же семь мешей и RuneGlowBuffers.clothing из44. Реальный event/render NOT_RUN. Skin outer layer не отключается. Между штанинами есть фон в разнесённых позах; в idle сами ванильные ноги соприкасаются.
