# PERSONAL STORY DUELS / BOGATYR SUMMON RESTRICTION

## Design principle
The existing world summon mechanic may normally allow two active bogatyrs, including earlier Ilya. **Personal rival quests override ONLY inside their own story encounters**. Each personal rival boss authorizes exactly the matching single hero, and no replacement after knockout/transform/retreat. For this patch implement Marinka/Dobrynya and extend the shared data-driven lock for other encounters if they already exist WITHOUT rewriting their fights. Future final Kalin-Tsar may authorize any two of four, but DO NOT implement him now.

| Rival quest | Authorized bogatyr | Other hero behavior |
|---|---|---|
| Idolishche Poganoe | Ilya Muromets | all other hero summon denied during boss fight |
| Marinka | Dobrynya Nikitich | all other hero summon denied during boss fight |
| Skim Beast | Alyosha Popovich (future) | only declarative registry metadata, no entity added now |
| Kartaus | Finist Yasny Sokol (future) | only declarative registry metadata, no entity added now |
| Kalin-Tsar final | ANY 2 of 4 (future) | metadata/design note ONLY, not runtime encounter now |

## Story encounter entry
- Required valid personal quest or explicit admin testing method. Interact with courtyard ritual center to enter. Verify player in dimension/arena footprint, no other active encounter, Dobrynya eligible and present/summonable, owner/party relationship valid.
- If player brought Ilya, he is moved to a **non-combat standby** outside encounter footprint / safely dismissed using existing summon system, not killed/deleted; do not strand entity UUID or lose his health. Mark temporary suspension and restore/re-enable after boss concludes. Do not have both heroes combat during this boss.
- Ensure no changing party via item, UI, command, relog, dimension travel, respawn, or alternate class slot while lock held. Show localized message: RU `В этом сказании с вами может сражаться только Добрыня Никитич.` EN `Only Dobrynya Nikitich may fight beside you in this tale.`
- If matched hero not yet unlocked, issue user-facing prerequisite hint instead of granting free summon or crash. If hero already present, avoid creating second copy.
- Once fight begins: summon-slot lock persists even if Dobrynya is temporarily cursed as an aurochs, drinks elixir or is incapacitated. No other bogatyr replacement. No pre-fight summon rule affecting normal world.

## Failure and release
- Legitimate boss defeat releases lock and turns ordinary summon mechanic back on after combat ends. Player death, logout, world reload, leaving event boundary, hero incapacitation or arena unload must release or restore lock deterministically per current active encounter state. Avoid blocked summon forever.
- Multi-player: only owner and explicitly cooperating player(s) as current quest framework supports; no boss can be double-started by second party. Do not bind unrelated players entering ruin to another group's hero lock. Team claim/difficulty scaling may be handled by existing multiplayer defaults.
- Implement as a small `encounterPolicy` table and `canSummon(hero, player, encounter)` predicate, not 4 hard-coded scattered `if (bossID=...)` copies. Server owns decision and returns translated reason code.

## Test situations
Normal 2-hero summon before visiting yard works. Start Marinka with Dobrynya+Ilya -> Ilya safely parks, only Dobrynya fights. Attempt to summon Ilya after curse transform -> denied. Kill Dobrynya or force <=15% recovery -> no replacement. Leave fight legitimately -> heroes work normally. Client relogs or server crashes between transform and recover -> cap remains correct or cleanly releases after rollback.
