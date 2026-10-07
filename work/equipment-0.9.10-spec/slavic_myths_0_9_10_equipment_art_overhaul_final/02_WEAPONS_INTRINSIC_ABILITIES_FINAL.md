# 02 — WEAPONS & INTRINSIC ABILITIES

Rework existing matching IDs instead of creating duplicates.

# W01 — КЛИНОК ВОРА

Role: fast sustained physical DPS.

Stats:
- damage target: 7–8 displayed normal hit depending attribute conventions;
- attack speed ~2.0;
- durability ~1500–1700.

Visual:
- short broad single-edged blade;
- darkened steel;
- angular/clipped tip;
- compact dark guard;
- black-brown leather;
- small copper/red rivets;
- hook/ring pommel.

## ПОРЕЗ

Every valid melee hit adds 1 Bleed stack.

Duration:
5.0 sec from latest application.

Max:
5 stacks.

New hit:
+1 up to 5 and refreshes duration.

Tick:
once per second.

Damage:
`0.25 * stacks`.

So max = 1.25 HP/sec.

No effect on `slavicmyths:bleed_immune`.

No giant blood particles.
At 5 stacks use slightly stronger cue.

Tooltip:
`Порез: до 5 зарядов, 5 сек.`

---

# W02 — КЛИНОК ОГНЕННОГО ЗМЕЯ

Stats:
- damage ~9;
- speed ~1.5;
- durability ~2000.

Visual:
- long slightly curved blackened blade;
- 2–3 ember fissures in blade;
- dark guard with serpent language;
- dark red/brown grip.

Passive:
normal hit ignites target 2 sec.
Never places fire blocks.

## ОГНЕННЫЙ УДАР

Activation:
Shift + RMB.

Windup:
~0.6 sec.

Prime window:
5 sec.

Next melee hit:
+4 fire damage;
burn 6 sec.

Cooldown:
12 sec after use/expiry.

Against `slavicmyths:wooden_entities`:
+6 fire damage;
burn 8 sec.

---

# W03 — ГРОМОВОЙ ТОПОР

Rework existing Thunder Axe if present.

Stats:
- damage 10.5–11.5;
- speed ~0.9;
- durability ~2000.

Visual:
- long haft;
- large asymmetrical head;
- cold steel + perunite inset;
- rune groove;
- leather lower grip.

## ГРОМОВОЙ РАЗРЯД

Only on fully cooled melee hit.

Cooldown:
8 sec.

Primary:
normal damage.

Secondary:
up to 2 additional hostile targets within radius 4 receive 3 electric damage.

If wet/in water:
secondary electric damage +25%.

No vanilla lightning entity required.
No fire/block damage.

---

# W04 — МЕЧ НЕУПОКОЕННОГО КНЯЗЯ

Stats:
- damage ~9;
- speed ~1.5;
- durability ~2200–2500.

Visual:
- long straight cold-steel blade;
- broad lower third;
- dark-blue/silver funerary inlay;
- massive guard;
- red/dark cloth accent.

## ЦАРСКИЙ ОТПОР

Trigger:
a real incoming hostile attack is successfully blocked with a shield.

Window:
3 sec.

Next hit with this sword:
+3 physical damage;
+35% knockback;
consumes state.

Cooldown:
8 sec after consumed counter.

Do not trigger from fall/hunger/self damage.

---

# W05 — ЛУК СОЛОВЬЯ-РАЗБОЙНИКА

Durability:
~550–650.

Visual:
- dark laminated wood;
- deep-red bindings;
- black feather/metal accents;
- strong recurved silhouette.

## СВИСТЯЩАЯ СТРЕЛА

Only fully drawn successful hits increment counter.

Every 5th successful hit:
AoE radius 3.5 around impact;
secondary hostiles take 3 damage + small knockback.

Misses do not count.
Partial draws do not count.
No through-wall AoE.

---

# W06 — ГАРПУН СТАРШЕГО ВОДЯНОГО

Stats:
- damage ~8;
- speed ~1.3;
- durability ~1800.

Visual:
- shaft >65% total length;
- three-prong head;
- dark steel;
- blue-green oxidation;
- rope/cloth bindings.

Passive:
+2 damage if target is aquatic OR currently in water.
Never double-count both.

## ПРИЛИВ

RMB.
Cooldown: 10 sec.

Short frontal knockback impulse.
In water: ~35% stronger and slightly farther.
Damage 0–2 max.
No terrain damage.

---

# W07 — СЕРЕБРЯНОЕ ОРУЖИЕ

Raw power about diamond tier.

Trait:
+30% damage to `slavicmyths:silver_vulnerable`.

Normal targets receive normal damage.

---

# W08 — ПЕРУНИТ

Target:
between diamond and netherite in raw stats.

Suggested:
- sword ~8 / 1.6;
- axe ~10 / 1.0;
- mining speed ~9;
- durability ~1800–1950;
- high enchantability.

---

# W09 — CATEGORY IDENTITY

Dagger = fastest/shortest/lower raw damage.
Sword = balanced.
Spear = visibly longest/thrust identity.
Axe = slow heavy.

Do not reuse one blade model and scale it.

---

# W10 — TOOLS

Weak recolor-only pickaxe/axe/shovel/hoe:
FULL_REDRAW.

Keep function readable.
