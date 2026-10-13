# Story state, heroic party locks, tracking, persistence

## Required owner records
Use current project player persistent data/attachments/capabilities, not new global file IO. Keep `slavicmyths.story.finist_kartaus.victory` bool, `...first_victory_at` world long tick (optional), `...finist_participated` bool, `...clear_count` integer (for tests; don't use it as loot multiplier), `...pending_reward_1_7_6` bool set TRUE on qualifying story victory (claimed only by future patch), and optional `...hero_retreat_during_fight` bool. Namespace keys to existing schema, prefer existing registry of bosses; no forced migration of old flags. Data per player, not per chunk.

## Active encounter record
`encounterUUID`, `playerUUID`, `finistUUID`, `kartausUUID`, `dimension`, `center BlockPos`, `phase`, `startedGameTime`, `status ACTIVE/ENDING/ABANDONED/DONE`, `bannerIds` <=2, `guardIds` <=3, `curseAnchorId` <=1. If current state manager already stores equivalents, reuse it. All transitions idempotent. State updates after server guard/hero join/leaves. One active personal encounter/player and max one active boss in a 48-block overlap zone to avoid mixed minions/aggro.

## Hero limits
- Normal exploration: at most TWO heroes total as established pre-1.7.4. Summon item can recall existing owned Finist instead of duplicating him.
- Personal story event against Kartaus: only Finist in owner's party. Start encounter only if Finist is already summoned and active, no Ilya/Dobrynya/Alyosha active. Display `Призовите Финиста и отзовите других богатырей` rather than silently change ownership. If another hero appears by command/admin while active, neutralize participation and never overwrite user's own entity slots; enforce existing story lock checks.
- No summon replacement in ongoing combat. If Finist reaches <=15% retreat, slot stays taken and boss fight still continues with player. Later boss-specific final Kalin Tsar any-two rule remains unimplemented, do not accidentally globally restrict 2-hero system to 1.

## Death, unload and test cases
- Player death during active encounter => cleanup enemy/banners, last-stand ally recovers after reset, no reward flag; restore location via vanilla death handling.
- Logout/restart/dimension change -> freeze/resume only if pre-existing encounter system supports it robustly; default abort/cleanup tagged boss and minions, preserve already earned victory flag. Do not duplicate when re-entering server.
- Villagers/pets/neutral mobs never coerced into target. Kartaus guards attack encounter owner/hero only and cleanup prevents overworld persistent zombies.
- Second player's splash attack may deal damage if within fight, but only owner receives story credit unless existing group quest logic exists and explicit participation is tracked. Avoid accidental party reward exploit.
- Boss spawn egg is debug: does not create quest flags, can test all AI/attacks, but is not `storyActive` and cannot lock unrelated heroes.

## Required UI
Existing bossbar label `Картаус Рыжий Ус — Чёрных знамён: 0/2`; when cursed `Сокол скован — разрушьте печать!` and 12-point anchor gauge; short skill titles with cooldown only in debug if HUD already supports it. In final phase bossbar pulses dark red no screen flashes. Network sync only at actual state change/banner count/phase, not every frame. Colorblind-friendly outline of active banners (distinct banner silhouette even when no red detection). No giant quest UI rewrite.
