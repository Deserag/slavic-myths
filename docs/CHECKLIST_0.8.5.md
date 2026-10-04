# 0.8.5 implementation checklist

[x] = реализация и headless/статическая проверка, не игровой QA.

- [ ] Ручной QA: MANUAL_TEST_0.8.5.md. Запусков Minecraft: 0.

## World generation
- [x] three kurgan tiers exist;
- [x] exterior mound uses vanilla palette;
- [x] Small = 1 floor;
- [x] Small = 3–6 content rooms;
- [x] Warrior/Clan = 2–3 floors;
- [x] Warrior/Clan = 10–18 content rooms;
- [x] Great = 4–5 floors;
- [x] Great = 25–40 content rooms;
- [x] same-tier structures can generate different layouts;
- [x] guaranteed entrance-to-final route exists;
- [x] floors connect physically;
- [x] piece overlaps are rejected;
- [x] Tier II/III can contain loops/shortcuts;
- [x] no broken partial labyrinth is placed after failed validation.

## Visual blocks
- [x] 8 core palette blocks implemented;
- [x] both color palettes used;
- [x] cracked variants visibly cracked;
- [x] mossy variants visibly mossy;
- [x] `sealed_kurgan_masonry` implemented;
- [x] final visible textures are >=32×32;
- [x] no missing-texture models.

## Architecture
- [x] corridor clear height 4–5;
- [x] standard corridor width 3;
- [x] wider/grand variants present;
- [x] floor stairs clear width 3;
- [x] small burial room follows reference;
- [x] large burial room follows reference;
- [x] Great final tomb is round/near-round/octagonal;
- [x] Great final tomb is much larger than normal rooms;
- [x] Great final tomb exists only on deepest floor;
- [x] three final-tomb niches exist;
- [x] protected shell cannot be survival-mined/exploded through;
- [x] no adjacent bypass gap into sealed room.

## Disturbance and curse
- [x] Disturbance is per-kurgan;
- [x] Disturbance is saved;
- [x] normal HUD does not expose numeric Disturbance;
- [x] repeated opening of same source cannot infinitely increase it;
- [x] temporary Tier-II curse implemented;
- [x] Great persistent curse implemented;
- [x] Great curse persists through death/relogin;
- [x] milk/generic clear cannot permanently cure Great curse;
- [x] debug clear removes Great curse state;
- [x] future boss-clear hook exists;
- [x] custom curse icon implemented from specified motif/design.

## Commands/docs
- [x] locate each tier;
- [x] generate each tier;
- [x] info/debug inspection;
- [x] clear curse debug command;
- [x] README updated;
- [x] README lists exact working commands;
- [x] README has manual test procedure;
- [x] README documents persistent-curse 0.8.7 limitation.

## Delivery
- [x] Minecraft client was not auto-launched;
- [x] headless build completed if environment supports it;
- [x] JAR produced;
- [x] PolyMC test mods updated if the path is available.

---

