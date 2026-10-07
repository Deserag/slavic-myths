# 05 — AI / AUDIO / ANIMATION COMMON STANDARD

---

# 1. STATE MACHINE

All redesigned mobs use explicit state data rather than scattered random booleans.

State should define:
- entry time;
- duration/cooldown;
- allowed movement;
- allowed attacks;
- animation;
- sound.

---

# 2. TARGET UPDATE RATE

Do not perform expensive target scans every tick.

Typical:
- target validity each tick is fine;
- large acquisition scan every 10–20 ticks;
- special environmental scan every 20–40 ticks.

---

# 3. ATTACK ORIENTATION

Before:
- charge;
- leap;
- dash;
- cone;
- grab;

the mob must rotate toward intended target during telegraph.

Do not allow sideways/backward attacks unless specifically designed.

---

# 4. HIT WINDOWS

Damage must happen during intended active animation window.

One swing must not hit same player multiple times unless explicitly multi-hit.

Use per-attack hit tracking.

---

# 5. RECOVERY

Heavy attack miss still causes recovery.

This is essential for fair combat.

---

# 6. ANTI-CHEESE

Per mob choose sensible responses.

Allowed:
- reposition;
- abandon impossible chase;
- ranged nuisance only if creature fantasy supports it;
- path around;
- short vertical reach if model supports.

Forbidden universal solution:
every melee animal magically shoots projectiles.

---

# 7. LEASH

Wildlife and regional spirits have a home/territory leash.

They should not chase for kilometers.

Bosses use arena/encounter leash and reset.

---

# 8. SOUNDS

Every FULL_REWORK creature:
ambient: 2–4 variations preferred;
hurt: 2+;
death: 1+;
attack telegraph: dedicated or suitable;
special ability: dedicated;
state/transform: dedicated when applicable.

If custom audio files are not available:
document temporary vanilla sound mapping.
Do not silently leave event empty.

---

# 9. SOUND DISTANCE

Avoid global booming audio for common mobs.

Boss telegraphs may travel farther.
Normal wildlife audio should be local.

---

# 10. ANIMATIONS

At minimum where model supports:
idle;
walk;
run;
hurt;
death;
main attack;
special attack;
warning;
state transition.

Do not add GeckoLib solely because it exists.
Use current renderer/animation architecture unless a real need is documented.

---

# 11. MODEL/TEXTURE

0.9.9 is not the full global art pass, but a mob whose current visual cannot support its mechanics must be corrected.

Examples:
Poludnitsa requires readable two-form transformation.
Kikimora needs correct silhouette.
Bear family needs adult/cub readable scale.
