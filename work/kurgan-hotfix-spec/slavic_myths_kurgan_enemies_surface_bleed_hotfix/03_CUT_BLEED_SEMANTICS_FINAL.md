# 03 — `ПОРЕЗ / CUT` EXACT SEMANTICS
## THIS OVERRIDES AMBIGUOUS WORDING IN THE 0.9.10 EQUIPMENT SPEC

The intended mechanic is ONE stacking debuff with a shared timer.

Do NOT implement five unrelated EffectInstances with independent hidden timers.

---

# 1. FIRST HIT

At time T=0:

player hits an eligible target with the relevant dagger.

Immediately apply:

`Cut stacks = 1`

and start:

`remaining duration = 5.0 seconds`

The debuff icon/state is active immediately.

Periodic damage may begin on the first normal one-second tick after application.

---

# 2. SECOND HIT BEFORE EXPIRY

Example:

T=0:
1 stack, timer 5 sec.

T=2:
player hits again.

Result immediately after hit:

`stacks = 2`

`timer = 5 sec again`

NOT:
3 seconds remaining.

The new hit refreshes the whole debuff duration.

---

# 3. MULTIPLE FAST HITS

If the player lands several valid hits while Cut is active:

each hit:
- adds exactly 1 stack;
- maximum 5;
- refreshes shared timer to 5 sec.

Example:

T=0.0 -> stack 1, 5 sec
T=0.8 -> stack 2, 5 sec
T=1.5 -> stack 3, 5 sec
T=2.0 -> stack 4, 5 sec
T=2.6 -> stack 5, 5 sec

At T=2.6 the target has:
5 stacks,
and the debuff expires at approximately T=7.6 if no new hit occurs.

---

# 4. HITS AT MAXIMUM STACK

If target already has 5 stacks:

another valid dagger hit:
- does NOT create stack 6;
- keeps stack count = 5;
- refreshes duration to 5 sec.

This allows a skilled player to maintain maximum Cut by continuing pressure.

---

# 5. EXPIRATION

If no valid refreshing hit occurs for full 5 seconds:

ALL Cut stacks expire together.

Example:

target has 4 stacks.

No dagger hit for 5 sec.

Result:
`stacks = 0`

Do not leave 3/2/1 stacks falling off one at a time.

---

# 6. DAMAGE

Once per second while active:

`damage = 0.25 HP * stackCount`

Therefore:

1 = 0.25 HP/sec
2 = 0.50
3 = 0.75
4 = 1.00
5 = 1.25

The normal melee hit still deals its normal weapon damage separately.

---

# 7. IMMUNE ENTITIES

If target EntityType belongs to:

`slavicmyths:bleed_immune`

then:
- no Cut stack;
- no Cut duration;
- no Cut periodic damage;
- no fake particles suggesting it worked.

Use tags, not a long hardcoded class chain.

---

# 8. MULTIPLE PLAYERS

The target has ONE shared Cut state.

Global maximum:
5 stacks total.

Example:
Player A creates 3 stacks.
Player B lands 2 valid hits.
Result = 5 stacks.

The last valid attacker refreshing Cut becomes current damage attribution owner for periodic damage.

Do not allow:
5 stacks per attacker.

---

# 9. PVP

Respect normal server PvP/team rules.

If PvP is disabled:
Cut cannot be applied to another player.

If PvP is allowed:
same 5-stack / 5-second mechanic applies.

---

# 10. UI / PARTICLES

Effect presentation should communicate stack count.

Preferred:
tooltip/status line:
`Порез III`
or equivalent amplifier level.

Do not show five separate identical icons.

Particles:
restrained dark-red hit cue.
No gore fountain.

At stack 5:
slightly stronger cue allowed.

---

# 11. SAVING / UNLOAD

Use normal safe effect/state persistence.

Do not create one scheduled task per hit that survives after entity removal.

When entity dies:
clear Cut state normally.

---

# 12. ACCEPTANCE TEST

Automated timeline test:

Hit at 0.0 -> stack1 exp~5.0
Hit at 2.0 -> stack2 exp~7.0
Hit at 4.0 -> stack3 exp~9.0
Hit at 4.5 -> stack4 exp~9.5
Hit at 5.0 -> stack5 exp~10.0
Hit at 8.0 -> stack5 exp~13.0
No further hit -> zero stacks after ~13.0

This behavior is REQUIRED.
