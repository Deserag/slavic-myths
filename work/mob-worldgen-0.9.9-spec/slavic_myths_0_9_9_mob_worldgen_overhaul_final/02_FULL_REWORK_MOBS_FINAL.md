# 02 — FULL / MAJOR MOB REWORK
## NO GENERIC MELEE PLACEHOLDERS

All numbers are gameplay targets.
If an actual current implementation already has a mechanically superior behavior, preserve the superior part but align with this design.

---

# A. KIKIMORA — FULL REWORK

## Role
Night/house/swamp-edge ambusher and nuisance spirit.

## Visual/model direction
Preserve approved core concept:
- shorter than Enderman;
- long bird-like legs;
- hunched narrow torso;
- oversized villager-like head;
- long sharp nose;
- cap/clothing variants;
- eerie silhouette.

Do not make her simply a reskinned zombie.

## Stats
HP: 36
Armor: 2
Movement: fast burst, normal stalking slower
Attack: 4–6 depending move
Follow range: 24–28

## States
IDLE
STALK
HIDE
PEEK
AMBUSH
SCRATCH_COMBO
FLEE_REPOSITION
COOLDOWN

## Behavior
- notices player at medium range;
- prefers partial cover/dark edges;
- approaches indirectly when possible;
- periodically hides behind blocks/objects;
- ambushes from 4–7 blocks;
- 2-hit claw sequence;
- after combo, backs away/repositions.

## Special
Short "startle" debuff:
- brief slowness OR camera-neutral gameplay-safe disorientation;
- no forced camera rotation.

## Anti-cheese
If player pillars 3+ blocks:
- Kikimora should not stare forever;
- she repositions, disengages, or uses a weak nuisance action if such action is implemented.

## Sounds
ambient whisper/chitter;
soft foot/claw;
attack hiss;
hurt;
death;
ambush cue.

## Drops
Common:
- existing spirit/cloth/herb materials if available.

Rare:
- `kikimora_thread` only if no equivalent item exists.
Purpose: ritual/crafting component, not useless trophy.

---

# B. POLUDNITSA — FULL REWORK

## Role
Daytime field elite spirit built around transformation and heat pressure.

## Visual
Two states are mandatory.

FAR / CALM:
- visually resembles a beautiful field maiden;
- traditional light clothing;
- kokoshnik/headpiece;
- human proportions.

NEAR / HOSTILE:
- elongated skeletal/zombie-like form;
- visibly taller;
- stretched limbs;
- same kokoshnik identity;
- unsettling face;
- transformation clearly readable.

Do not fake this with only a texture color swap.

## Stats
HP: 60
Armor: 4
Damage: 7 base
Speed: 0.28 normal equivalent, faster during short chase burst
Follow range: 32

## Spawn
- daylight;
- plains/field-like eligible areas;
- low frequency;
- no constant nighttime spawn.

## States
DISTANT_WATCH
APPROACH_THRESHOLD
TRANSFORM
CHASE
SCYTHE_SWEEP or CLAW_SWEEP
HEAT_PRESSURE
RECOVERY
RETURN/CALM if player escapes far enough

## Transformation
At roughly 10–14 blocks:
- stop briefly;
- 0.8–1.2 sec telegraph;
- transformation animation/sound;
- hostile state begins.

## Attacks
Sweep:
- 1.0 sec telegraph;
- frontal arc;
- damage 7;
- recovery 0.8 sec.

Heat pressure:
- every 10–16 sec;
- clear sound/visual cue;
- short area effect;
- applies brief weakness/slowness or exhaustion-style pressure;
- not permanent.

## Drops
Thematic:
- field herb bundle / ritual material;
- rare `midday_veil_fragment` only if no current equivalent.

---

# C. POLEVIK — FULL REWORK

## Role
Territorial field spirit, not another Poludnitsa.

## Attitude
Neutral by default.

Aggro triggers:
- player attacks it;
- player repeatedly destroys crops/natural field vegetation nearby;
- special encounter trigger if existing progression requires it.

## Stats
HP: 44
Armor: 3
Damage: 5–7
Speed: 0.30
Follow: 24

## AI
- wanders field;
- inspects crop/grass patches;
- avoids dense forest/water;
- warns before hostility.

## Combat
Dust dash:
- turns to face target first;
- 0.6 sec telegraph;
- dash 5–7 blocks;
- hit 6;
- recovery 1 sec.

Grass snare:
- targeted patch under/near player;
- clear visual;
- brief slow;
- cooldown 12 sec.

## Drops
Small amount of field/nature materials.
Rare charm component.
Do not make killing neutral Polevik the only way to get common progression materials.

---

# D. BANNIK — FULL REWORK

## Role
Territorial bathhouse spirit / close-quarters elite.

## Spawn/encounter
Primarily connected to bathhouse structure/appropriate interior.
Do not spam natural wilderness.

## Stats
HP: 80
Armor: 5
Damage: 7
Speed: 0.26
Follow: 20

## States
DORMANT
WARNING
HOSTILE
STEAM_PREP
STEAM_BURST
SLAP_COMBO
HEAT_ENRAGE
RECOVERY

## Aggro
- player disturbs bathhouse;
- opens protected container / attacks;
- enters forbidden inner area depending existing structure logic.

## Attacks
Heavy slap:
telegraph 0.7;
damage 7–9;
knockback.

Steam burst:
- 1.2 sec audible hiss buildup;
- cone/radius;
- moderate damage 4 + short blindness/weakness style effect;
- cooldown 10–14 sec.

Heated state:
below 35% HP:
- attack cadence increases ~15%;
- NOT a damage doubling.

## Environmental counter
Water/splash/wet interaction may reduce heat or interrupt steam if practical.

## Drops
Bannik-specific ritual material;
rare bathhouse charm/trophy if none exists.

---

# E. IGOSHA — FULL REWORK

## Role
Trickster spirit / harassment encounter.

## Design
Small unsettling humanlike/child-spirit silhouette.
Do not turn into cute pet.

## Stats
HP: 30
Armor: 0
Damage: 3–5
Speed: fast
Follow: 24

## AI
Avoid direct tanking.
Uses:
- stealing/reposition behavior if existing inventory-safe implementation exists;
- feint approach;
- short vanish/reappear;
- nuisance hit then flee.

## Rules
Never permanently delete valuable player items.
If "steal" implemented:
- only temporarily hold eligible low-risk item OR steal a generated token mechanic;
- guaranteed recoverable on defeat/timeout.

## Drops
Low combat reward, higher utility/ritual clue.
Rare trickster token if no equivalent.

---

# F. LESHY — MAJOR/FULL REWORK

## Role
Forest master / high-tier supernatural encounter.

Not an ordinary zombie-like hostile.

## Attitude
Neutral-watchful until:
- attacked;
- player commits configured forest aggression;
- quest/ritual provokes him.

## Stats
HP: 100
Armor: 8
Damage: 8
Speed: 0.30
Follow: 36

No boss bar by default unless current encounter design explicitly classifies him as miniboss.
May use elite bar in special encounter.

## States
WATCH
WARN
VANISH
REPOSITION
ROOT_SNARE
WOODLAND_STRIKE
FALSE_PRESENCE
CHASE
DISENGAGE

## Vanish
- telegraph with sound/particles;
- remove direct targetability for short duration;
- reposition 8–16 blocks;
- never teleport into wall/lava.

## Root snare
- visible roots/ground effect;
- 0.8 sec warning;
- slows for max ~2 sec;
- cooldown 12–16 sec.

## False presence
Create 1–2 non-damaging decoys/sounds.
Decoys exist briefly.
Do not spam entities permanently.

## Disengage
If player runs sufficiently far:
Leshy returns to forest state instead of chasing for kilometers.

## Reward
Meaningful forest-spirit reward:
existing ancient sign/charm/woodland ritual component.
Rare trophy/component only if not already represented.

---

# G. BROWN BEAR — MAJOR WILDLIFE REWORK

## Core role
Territorial wildlife, not permanently hostile monster.

## Standard adult
HP: 50
Armor: 3
Damage: 7
Speed: moderate
Follow/territory: 20–24

## Behavior
IDLE_FORAGE
WANDER
WARNING
CHARGE
MAUL
DEFEND_CUB
RETREAT/RETURN

Bear does NOT instantly attack every player.

Warning triggers:
- player approaches too close;
- attacks bear;
- approaches cubs;
- repeatedly follows.

Warning:
- stand/growl animation;
- 1.0–1.5 sec;
- gives player chance to back off.

Charge:
- face target;
- short acceleration;
- strong hit;
- cooldown.

## One spawn egg
Only one Bear spawn egg.

See file 04 for family/miniboss egg behavior.

## Drops
normal:
- hide/leather equivalent;
- meat only if project intends wildlife food;
- claw chance.

Do not make bear farm mandatory for core progression.

---

# H. WILD BOAR — MAJOR REWORK

## Role
Territorial charge animal.

## Stats
HP: 36
Armor: 2
Damage:
normal 4
charge 7
Speed: 0.28
Follow: 20

## Behavior
Neutral unless threatened / player too close.
Warn with snort.
Charge requires:
- face target;
- 0.5–0.8 sec paw/snort telegraph;
- straight burst;
- misses have 1 sec recovery.

No side/backward charge.

Can disengage after player escapes territory.

## Drop
meat/hide/tusk according to existing items.
Rare tusk only if useful.

---

# I. FOREST WOLF — MAJOR PACK AI REWORK

## Role
Pack predator.

## Individual stats
HP: 30
Armor: 1
Damage: 5
Speed: fast

## Pack
Typical group:
2–4.

Do not make every wolf suicidal.

Roles selected dynamically:
- pressure/front;
- flank left;
- flank right;
- hesitate/recover.

At least one wolf attempts side approach while another has aggro.

## Morale
If pack loses members or receives heavy damage:
chance to back away/flee.

## Attack
Short lunge:
telegraph 0.35–0.5 sec;
damage 5;
recovery 0.7 sec.

## Drops
ordinary animal drops only.
No need for legendary loot.

---

# J. COMMON FULL-REWORK RULES

All above:
- server-authoritative states;
- state timers saved only if required;
- no permanent helper entity leaks;
- no full AI scan every tick;
- audio mapped;
- animations mapped;
- loot mapped;
- natural/structure spawn rules explicitly tested.
