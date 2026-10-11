# Food, chores, reactions to Baba Yaga: exact shot by shot

## Eating scene, complete 9–15s deterministic sequence
1. Walk to assigned `seat_anchor`; stop 0.20b from table, face to bowl. Play `sit_down` .95s. Hands down; seat mesh height must match character leg/torso geometry.
2. `pick_food` .65s: 0–.15 gaze to bowl; .15–.30 right shoulder pitches forward ~28°, elbow flexes 38°; .30–.45 hand reaches physical food position, attach small pie/bread prop to palm at 0.43s; .45–.65 hand returns near chest. Bowl on table remains and food count not necessarily consumes world items (ambient scene, no theft of chest stock).
3. `eat_bite` .95s: 0 body inclines 4°; .22 lift prop 0.08b; .42 prop touches mouth, jaw opens 0.5px and blink briefly; .50 register soft chew Foley; .70 hand moves away, .95 jaw closes. Trigger precisely `chew_01` at t=.44, `chew_02` occasionally.
4. `eat_chew` 1.1s: 0 jaw close→.18 open .35px→.35 close→.53 open .3px→.72 close→1.1 swallow. Cheek/head tilt ~3°, shoulders 4° tiny, no gluttonous huge arm swing. Play `chew_03` midpoint, `swallow_01` on last cycle.
5. Repeat bite+chew 3–6 times, vary between 0.95 and 1.15 playback speed ±8%, reuse only after variation. Do not repeat a voice line every mouthful. `eat_swallow` then `place_bowl` .7s to detach prop and fold hands.
6. `stand_up` .92s, leave table, state cooldown 30–90 s. Eating dialogue may play once per scene with a 45s global voice cooldown.

## `domestic_inspect` 1.9s
0 inspect shelf (head yaw +20°), .4 head pitch -12° to view floor, .75 one hand touches shelf rim (wrist 14°), 1.10 shake head -6° and shift body, 1.45 sigh and back away, 1.9 return. No changes to player storage contents.

## `domestic_sweep` 2.1s
Only if existing or lightweight decorative broom attachment is available. Hold broom in two hands at angle 28°; sweep left→right at 0.55–1.20, right→left at 1.25–1.9; body rotates ±8°, feet counterbalance, broom bristle contacts floor, Foley `broom_sweep_01` at .55. Do not wield a player-usable weapon or alter blocks.

## `react_to_yaga` 1.75s
0 look toward nearest Baba Yaga in same hut, head +20°; .45 gesture small right hand (±20°), .8 mouth motion matching `voice_yaga_01` or `02`, 1.35 hand drops, 1.75 attention returns; optional Yaga head glance only if her current NPC API supports it, no interruption of vital behavior.

## Transitions
Per-clip player arrival mid-food: finish next swallow (~1.2s), turn head by 20°, then short `notice_guest`, do NOT drop held prop midair. Yaga leaves hut: no teleport to follow her. If food position removed by players (existing world), skip `eat` gracefully, choose another activity and log structured debug once.
