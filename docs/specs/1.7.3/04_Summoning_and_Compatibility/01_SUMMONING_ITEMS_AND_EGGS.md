# NEW REQUIRED SUMMON SYSTEM + RETROFIT OLD BOGATYRS

User discovered new heroes/bosses from patches 1.7.1/1.7.2 lack straightforward summon tools. **This patch MUST repair accessibility in the smallest integration.** Do not rewrite earlier abilities or entities.

## Hero summon items — unbreakable, nonconsumable
Three items: `alyosha_call_whistle`, `ilya_call_token`, `dobrynya_call_emblem` (translate names in localization file). Existing Sivka/Burka summon workflow is behavior reference: right-click calls companion, ownership persisted, no stack consumption, cooldown to prevent rapid spam, no duplicates. If 1.7.1/1.7.2 already provide equivalent working item, keep IDs/models/recipes and FIX its behavior instead of registering redundant item; show final chosen identifiers in summary.

### Alyosha Call Whistle (new, mandatory)
Design sprite `64×64` transparent nearest pixel art; inventory icon 32+ size logical rendering. Bronze/copper short shepherd's oval whistle tilted -25°, length 42 px, top narrow wind hole with highlight 5×3px, curled outlet, dark brass #A77B3C and bright edges #E8C66A, brown grip #69462C, braided crimson-red cord #9C3A2D (12px tail), tiny azure blue enamel inlay shaped 5×5 diamond (matches eyes). No generic horn or duplicate Sivka/Burka icon. At 16px GUI scaling recognizable as brass whistle with red cord. Item title `Свисток Алёши`, tooltip `Нерасходуемый призыв. Призывает или отзывает Алёшу Поповича вне сюжетной битвы.`

### Ilya Call Token (compatibility fix)
64×64 detailed square iron token with front scarlet engraved round shield in center, pale gold small heraldic cross, thick dark metal border, small leather loop at upper-left; dominant steel #73777E/red #9D302C. Title `Знак Ильи Муромца`. Must not resemble Alyosha whistle.

### Dobrynya Call Emblem (compatibility fix)
64×64 slender silver spearhead-shaped emblem (not literal huge spear), blue-gray steel on brown wood mount and small pale ribbon, warm-gold bevel; title `Знак Добрыни Никитича`. Distinct from Ilya square token and Alyosha rounded whistle.

### Trigger behavior
- Right-click air/ground outside combat: server validates item, player not spectator, allowed dimension, headroom/solid ground within 2–3m, call if not already present. Play matching call sound, 16 gold/white particles, spawn with 24-tick appearance (not a visible egg pop), face player. When already active: right-click toggles manual recall ONLY outside boss fight; effect fade 18t, persist 3D hero health/recovery if existing model expects it.
- Cooldown **100 ticks (5s)** between attempts to prevent spam; on rejected use **no item consumption, no durability loss**. On ground click, do not place unintended block. Item stays inventory in survival, can be 1 stack max, should not be destroyed on normal activation. Optional recipe or acquisition: reuse existing hero-unlock/quest grant path if present; for dev give item in creative tab/command. Do not invent large quest overhaul or gate a newly made mechanic behind a unique boss reward from future 1.7.6.
- Place +2.0 blocks from owner along nearest safe horizontal, check empty 1×3 collision and headroom; falling into void/lava, inside wall, unloaded chunk or outside encounter arena -> reject with translatable error, no damage. If hero in another dimension/chunk, reuse existing single-instance tracking to avoid global duplicates; don't force load chunks.
- Story battle: once boss ACTIVE cannot call, dismiss, or replace heroes; right before STARTING permitted one story-matched hero. General world: max **two** different summoned heroes at same time; no duplicate of same entity. Ilya and Dobrynya call tools should work unchanged outside story fights. Owner sees messages for party full, combat lock, blocked space; no chat spam more often than once/3s.

## Boss spawn eggs — creative/admin reproducibility (MANDATORY)
- `skim_beast_spawn_egg`: eggshell dark umber #3B2A25; spots blood red #9A3430 and pale ivory micro flecks. Standard NeoForge/Minecraft spawn egg item where supported, creative tab available, dispenser summon if established egg supports, command `/summon slavicmyths:skim_beast` possible; resource texture >=32×32 (custom item texture if needed).
- `idolishche_poganoe_spawn_egg`: muddy gray-brown #59413B and bone spot #C1A48A; retrofit 1.7.1 boss only if missing, preserve entity type IDs.
- `marinka_spawn_egg`: charcoal #2F222C and purple-red spots #9B3351; retrofit 1.7.2 boss only if missing.
- Creative spawn eggs spawn entity with normal max HP and normal AI; **do not** start story/quest flags or award 1.7.6 boss victory when manually killed outside valid encounter unless legacy behavior requires separately documented exception.
- Wrong-entity spawn egg tint or reuse of wolf/spider spawn type is test FAIL. Ensure spawn egg exists in creative, translations, inventory, server command, model and registry; no promise-only text.

## Survival summon/invocation for Skim
`beast_challenge_horn` = **reusable** encounter-starting item separate from creative spawn egg; sprite 64×64: curved dark ram-horn fragment diagonally from SW to NE, tip pale ivory, base leather red wrapping, five cut notches, red-gold hanging thread, textured 4 shade bands. No movie samples. `Рог вызова Ским-зверя`. Trigger only 0–5 blocks near valley marker while no battle active and player eligible; plays low horn SFX then awakening sequence. NEVER consumes, cannot summon wild boss freely at player's base; blocked outside arena with nonspam message.
- Acquisition: provide one simple vanilla-material crafting recipe (example 1 goat horn + 1 bone + 1 red dye => item, shape positioned in 3×3; require goat horn if 1.21 available), plus creative tab. This is an **access item, not unique victory reward**. Reusing an existing lore/progression unlock allowed but don't remove testing access.
- One active boss per valley/encounter. Cooldown 15 sec on boss horn; right-click spam cannot clone boss or loot containers.

## Recipes (minimum viable no reward lock)
Use standard shaped recipes/advancement rules under established data folder. For Alyosha call whistle: center copper ingot + top iron nugget + bottom red dye (3 ingredients), or if prior hero summon recipes already exist match their unlock tier instead. Ilya/Dobrynya call tokens: make available via existing hero acquisition rather than require a new grind; also test `/give` and creative. If recipes would bypass an explicit quest gate, return a craftable inert bound item until unlock, not a completely missing item.

## UI icons and screenshots
`06_References/Technical/Summon_items_design.png` is pixel-art technical sheet of four invocation items and three eggs. Priority of exact RGB values and silhouette in THIS FILE over incidental artistic variants. No item model should display as purple/black missing texture.
