# ALYOSHA — SERVER AI, weak DPS + strong attention + evasion

## Behavioral contract
Alyosha is the **agile, annoying decoy ally**. He draws attention via small, fast hits, makes the beast waste heavy attacks, survives by position and dodges, creates brief windows for player. **NOT** Ilya-style damage sponge; **NOT** player-carried invulnerable DPS. In future two-hero fights, never overrule tank taunts from Ilya forever.

Stats (actual Minecraft internal HP): health=175; armor=6 (light leather accents), armor toughness=0; follow range=28; base move speed=0.34 (slightly above vanilla player sprint walking comparison subject to source implementation), attack reach=2.5, arrow range=18, knockback resistance=0.12. Effective base close DPS target ~3–4 HP/sec before armor vs ordinary unarmored mobs when uninterrupted; boss DPS average ~2.0–3.0 HP/sec and under threat with dodging. Contrast: heavy Skim hits hurt him, as desired.

## Threat table / aggro
- Existing aggro system first; if none, introduce **encounter-scoped lightweight threat map** to server boss and nearby mobs; do not globally mutate mobs outside battle.
- Threat Alyosha per successful light hit +6; bow hit +4; successful feint +8; successful dodge from a targeting heavy windup +12. Player's successful close hit +8, ranged +5. Alyosha taunt at t11 +30 and applies +1.8 multiplier to his *current* threat for 90 ticks. Stacks are *not* additive: re-taunt replaces older effect. Every 20 ticks decay all non-currently-attacking threat 10%.
- Boss retarget recalculation once each 15 ticks; keep target at least 30 ticks unless target lost >18 blocks or invalid, preventing frantic target switching. Taunt drives 60–75% of heavy windups toward Alyosha when he remains reachable, **NOT** unbreakable infinite hard-lock; after three boss missed heavy attacks in last 10s, boss gains `adapting` for 8s: +30% target weight against *player*, reduce follow-through bait probability to avoid cheesing.
- Target validity: owner + eligible foes only; do not override peaceful mobs, tamed allies, neutral without provocation, other players in PvP-off, NPC quest allies. Cap 6 aggro-affected mobs 14-block radius, refresh no faster than 20 ticks, clear on logout/unload/retreat.
- After taunt, audible tell and golden transient exclamation above Alyosha (alpha 0.75, total 12 ticks). No constant glow outline that reveals a hidden target.

## Attack selection / cooldown / vulnerability
| Ability | Cooldown | Preconditions | Action/effect |
|---|---:|---|---|
| Barbed taunt | 240 ticks | enemy <=14m, LoS, current HP>15%, idle/no recover | Threat burst described; does not deal damage |
| Quick dodge | 36 ticks | targeted direct heavy attack windup, attacker 2–9m, 1+ clean lateral path | 2.3–3m side motion, 4-tick evade window, no AOE invulnerability |
| Flickering saber | 76 ticks | close enemy and safe combo path | 4 little hits 2 /2 /2.4 /2.6 raw HP, cancel on stagger/retreat |
| Feint | 160 ticks | 2–4.5m, aggressive enemy | enemy may waste one strike; 4 HP weak followup only when target open; no boss lock |
| Bow triple | 200 ticks | LoS 6–18m, no melee pressure | 3 arrows, 2 HP each raw; bow max <=15% time during boss fight (melee/distraction priority) |
| Dodge counter | 100 ticks | valid dodge versus hitbox actually misses | raw 4 HP, not repeated by animation loops |
| Battle daring passive | 0 | three accurate weak hits within 6s without being hit | move +12% and attack animation speed +10% for 4s; max trigger once 20s, lose on real hit |

## Decision intervals
- Every 10 ticks: assess target dist/danger; pick role: PROVOKE/BAIT/DODGE/POKE/REPOSITION/BOW. Avoid stacked performance-wide path search.
- Every 20 ticks: refresh aggro; every 40 ticks: check party ownership, last stand safe point; every 60 ticks: ensure stuck recovery (teleport only if far from player outside active fight and environment safe; never inside boss arena in sight).
- Default positioning: remain within 6–11 blocks of Skim when provoking; do not stand directly next to player (gap >=2.5 blocks), leave boss striking lane so player has clear attack route. Step sideways rather than complete circle forever. Avoid hiding beyond arena boundary.
- If boss begins `PREPARE_LEAP`, Alyosha can dodge at most once; if boss switches to player on leap during tell, Alyosha can intervene only if within 5 blocks and not on dodge cooldown, no instant guaranteed save.

## 15% HP Last Stand
At `hp <=26.25` (15% 175): enter `RETREATING`, remove threat, no damage abilities, party slot remains occupied. Move to safe node radius >=18 blocks from boss or nearest exterior cover, drink tonic animation, recover 4 HP every 40 ticks to 70% base (=122.5 HP), **remain unavailable for rest of that active story fight**. After fight or owner leaves/ends encounter, return to owner if valid. If pathfinding impossible because arena sealed, return to dormant summon item-linked state and spawn near owner post-combat. Never die from passive retreat process; if lethal burst skips 15% threshold (e.g. 45 HP hit when 30 HP), intercept at 1 HP once and force retreat. One interception per activation, reset out of combat; no infinite invulnerability and no revival of dead entity in unrelated context.

## Ownership and targeting safety
- Only call item owner can command/dismiss Alyosha, owner UUID persists. Repeated use toggles **dismiss only if not active boss fight**; otherwise message 'Cannot recall hero during story encounter.' Hero call item works at a sufficiently clear spot near player, not wall/void.
- In story fight Skim only Alyosha can be summoned. Another hero's call item gives local translatable error and does not consume; on attempt to call second Alyosha, focus existing entity, no duplicate.
- Hero must not die permanently via despawn on unload; persist minimal owner UUID, optional last-recovery time. No duplicate on changing dimensions/relogging.

## Combating exploit patterns
- Boss picks fights with player too; taunt can't hold 100% for minutes. No permanent invincible roll or teleport dodge into solid blocks. Cooldown authoritative and non-reset on logout/relog within encounter. Ally cannot destroy scenery/loot, open chest, attack vanilla passive mobs or alter PvP.
- Boss contact damage should *not* ignore his 175 HP; his survival comes from evasion, not hidden 80% damage reduction. No attack-scanning for every entity every tick.
