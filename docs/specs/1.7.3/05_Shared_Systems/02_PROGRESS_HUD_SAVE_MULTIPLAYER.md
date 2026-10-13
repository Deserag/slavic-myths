# PERSISTENCE, PARTY LOCK, UI AND MULTIPLAYER

## Persistence fields (logical, adapt existing capability data)
Use existing player progression structure/codec if available; don't introduce a second save framework if field is there. Logical keys: `slavicmyths.skim_beast.story_defeated: boolean`, `defeat_count: int`, `first_defeat_time: long`, `alyosha_unlocked: boolean` if hero unlock exists, `ally_participation_alyosha: boolean`, `active_encounter_uuid`, `summoned_hero_owner_uuid`, `ally_status` (ACTIVE/RETREATING/RESTING), `last_party_ids`. Store server-side and sync minimal read-only HUD state.

- On qualifying boss death inside active encounter, set defeated flag for owner/player(s) who actively participated (>=1 hit or in arena for >=60 ticks), once; no loot claim. 1.7.6 can enumerate flags, retroactive rewards MUST NOT require a refight; reserve data/ID without implementing grant now.
- Arena-level: structure generated anchor/rotation, encounter state UUID and active boss UUID, cave passage unlocked. On server restart, if active boss is missing, reset to READY after checking owner state (don't duplicate). If players leave during ACTIVE, honor reset behavior and clear grab/dodge locks.
- Dedicated server client sync limited to animation IDs, current boss phase, threat VFX trigger, summon ownership and recovery status. Never accept unvalidated client packet 'spawn boss', 'grant victory', or 'remove cooldown'.

## Boss bar and UI
- Title Russian `Ским-зверь`, color dark red, only encounter participants within 80m; bar reflects actual current boss HP and disappears after completion/unload. Do not fake bar via 20 ticks chat spam.
- One-line status when Alyosha taunts: optional `Алёша отвлекает Ским-зверя` for 1s (max once per taunt), when exhausted: `Зверь выдохся — атакуй!` for 1.5s. Item tooltip explains nonconsumable and arena restrictions. During hero recovery show small icon `Алёша восстанавливается`, without false promise available in same fight.
- Subtitles per sound and English/Russian localization; no 0/1 English fallback mix visible to RU users.

## Story summon lock matrix
| Encounter | Allowed before START | Forbidden during ACTIVE |
|---|---|---|
| Idolishe | Ilya | all fresh heroes/replacements |
| Marinka | Dobrynya | all fresh heroes/replacements |
| Skim | Alyosha | all fresh heroes/replacements |
| Future Kartaus | Finist | all fresh heroes/replacements |
| Future Kalin Tsar | any two of 4 | any replacements after fight starts |
| Open world | any two unlocked | active cap 2 |

If existing story uses quest-specific ally automatically summoned, don't duplicate with reusable items; reserve exact one active hero slot, disable manually calling an unapproved hero.

## Multiplayer
- If 2+ players join same boss arena, keep one beast and one boss bar state, not one spawn per item per player. Hero ownership remains individual; for story encounter prefer session owner and Alyosha only to prevent 4 same heroes swarming. Boss damage applies independently per victim, no double hurt per attack window. If item used by second player while active: message and no consumption. Progress awarded to qualified participants only, not dimension-wide players.
- Creative egg-spawned boss independent from story session, still works with entity renderer/bossbar; killing such entity doesn't set victory or open player-specific cave lock. Commands require permission, player-driven creative eggs follow vanilla permission rules.
