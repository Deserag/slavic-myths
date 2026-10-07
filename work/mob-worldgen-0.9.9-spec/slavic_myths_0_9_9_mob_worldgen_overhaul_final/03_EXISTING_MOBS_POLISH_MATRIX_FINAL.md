# 03 — EXISTING MOBS: POLISH, DO NOT NEEDLESSLY REWRITE

The goal is to preserve already-good mechanics and normalize quality.

---

# UPYR

Keep:
- claws;
- dash;
- bite/lifesteal;
- debuff identity.

Improve:
- clear pre-dash facing;
- dash telegraph;
- bite tell;
- recovery after missed dash;
- navigation through Kurgan rooms;
- no attack stacking in same tick window.

Target HP:
use current value unless obviously below 35 or above intended tier.
Desired strong-normal/elite range: 40–60.

Reward:
existing undead/ritual drops;
rare fang/blood component if already present.

---

# NAV

Keep:
- slowing;
- displacement/ethereal identity.

Improve:
- readable visual cue before displacement;
- minimum cooldown;
- no chain-locking player;
- clearer ambient/attack sounds.

---

# KURGAN DRUZHINNIK

Keep:
- sword;
- frontal shield;
- shield bash;
- counter strike.

Improve:
- group spacing in corridors;
- do not all stand in same block;
- shield direction correctness;
- turn before bash;
- recovery after bash.

Do not globally redesign in 0.9.9 after 0.9.6 dungeon work.

---

# KURGAN VOIVODE

Keep:
- approximately 150 HP if current design still uses it;
- axe;
- shield charge;
- wide attack;
- stronger second half.

Improve:
- arena navigation;
- anti-corner;
- telegraph;
- endgame armor tuning without giant unavoidable damage.

---

# BURIED VOLKHV

Keep:
- projectile;
- false silhouettes;
- seal;
- limited summon.

Improve:
- summon hard cap;
- decoy lifetime;
- projectile clarity;
- no spell spam through walls.

---

# RESTLESS PRINCE

Keep existing boss identity and multi-phase design.

Expected boss-tier HP remains around current ~350 if that is the actual implementation.

Improve:
- phase pacing;
- telegraphs;
- endgame armor pressure;
- anti-cheese;
- arena reset;
- reward reliability.

Do not reduce to simple melee boss.

---

# OVINNIK

Already mechanically developed.
Polish only:
- attack cues;
- hitbox consistency;
- rain/water reactions;
- audio;
- animations;
- reward table review.

---

# VOLKOLAK

Keep:
- leap;
- combo;
- howl;
- flank;
- retreat;
- blood phase;
- silver weakness.

Fix:
- leap facing;
- no wall embedding;
- pack/encounter state cleanup;
- telegraph consistency.

---

# FIRE SERPENT

Keep:
- aerial orbit;
- dive;
- fire pass;
- Falling Star;
- false copies if present;
- water/rain response.

Improve:
- aerial path stability;
- target selection;
- no indefinite unreachable hovering;
- arena leash;
- phase audio.

---

# PODVEY

Keep:
- vortex;
- projectile interaction;
- cone;
- dash;
- environmental mechanics.

Improve:
- projectile reflection safety;
- cooldowns;
- dash facing;
- visual tells.

---

# LIKHO ONE-EYED

Keep world-boss identity.
Preserve existing high-tier phase kit:
- grab;
- gaze;
- zones;
- chase;
- jump.

Polish:
- boss arena leash;
- anti-pillar;
- damage tuning vs endgame;
- reliable trophy/unique reward;
- sound/state transitions.

---

# TUGARIN ZMEY

Keep multi-phase world boss.
Polish:
- flight/ground transitions;
- attack tells;
- avoid overlap of unavoidable attacks;
- reward reliability.

---

# RUSALKA

If 0.9.7 already reworked her:
DO NOT overwrite that implementation.

Keep observe/song/charm/pull or improved 0.9.7 design.

0.9.9:
- standardize state framework;
- test water navigation;
- fix sound/animation;
- ensure no permanent control lock.

---

# VODYANOY

Primarily social/reputation/water NPC or special creature.
Do not force always-hostile combat.

Improve:
- water navigation;
- idle behavior;
- interaction reliability;
- animations/sounds.

---

# ELDER VODYANOY

Keep boss/elite phase identity.
Polish:
- 3-phase pacing if current;
- push/pull/wave readability;
- water arena logic;
- loot.

---

# BANDITS FROM 0.9.8

DO NOT redesign again.

Preserve exact 0.9.8 ladder:
- weak/common role around 30 HP;
- standard ~36;
- veteran ~44;
- heavy veteran ~50;
- Ataman = 150 HP;
- Solovey-Razboynik = BOSS, 250 HP.

0.9.9 only:
- shared navigation fixes;
- sound consistency;
- test group AI;
- verify rewards and boss phases;
- do not replace with generic mob AI.

---

# BABA YAGA / DOMOVOY

Non-standard combat NPCs.
Only bugfix:
- navigation;
- idle;
- interaction;
- animation/sound lifecycle.

Do not turn them into ordinary hostile mobs.
