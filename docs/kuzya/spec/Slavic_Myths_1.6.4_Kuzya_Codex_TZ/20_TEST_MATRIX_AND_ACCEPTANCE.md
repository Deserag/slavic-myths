# Acceptance checklist — required proof, not compile-only

## Visual verification
- [ ] Hero reference vs model front, side, rear, 3/4: dark mop 28+ layered locks, dark chocolate puppet face, large expressive eyes, old russet tunic; screenshots at night/fire/day.
- [ ] 0.64 b character next to Steve 1.8 b; facial eyebrows, eyes, nose, patched garment remain legible in actual game; no missing textures/duplicated head or hair.
- [ ] Warm and crowded hut with true stove bed, Yaga workstation, table/bench, Kuzya corner, barrel and decorations. Existing outside chicken legs and doors unmodified.

## Clip-level QA
- [ ] 33 named motion clips present and not aliases; `manifests/animation_keyframes.csv` references truly animated bones.
- [ ] `walk`: feet planted during contact, no ice-skating or torso teleport. `climb_stove`: hand contacts corner and body changes height physically. `lie_down`: smooth roll onto blanket. `sleep`: eyelids close, body breaths, random tiny shift, no abrupt waking.
- [ ] `wake_up`: distinct yawn and elbow raise. `eat`: hand takes visible prop, touches mouth, chews, swallows; audio timed within ±60ms of pose.
- [ ] `grumble_small/large`: notice→prepare→accent gestures→recover, eyes track prolonged visitor and hand clearly points door, no instant arm pose flip.
- [ ] At least 10 sec gameplay recording of each: resting, walking/climb, eating, speaking to player, staying idle with Yaga, overstay shoo; capture F3+actual time if possible.

## Behavioral QA
- [ ] Kuzya ignores visitors 0–30s, glances after ≥30, complaints after ≥120, escalates after ≥210 with meaningful cooldown; exit+return recovery.
- [ ] He cannot be hit with fist/sword/projectile/AoE/creative attacks and no hurt sound/knockback or HP drop; right click still responds. He cannot be leashed/traded/bred/removed from hut.
- [ ] No extra quests/gifts/XP introduced; Baba Yaga retains prior AI and interactions.
- [ ] 48 actual OGGs included in built resource registry, footstep/food/snore/voice categories audibly different, no audio spam or missing/silent clip; distinct Russian textual subtitles.
- [ ] Vanilla barrel contains thematic moderate loot and does not refill upon reloading; exactly one per hut; can still access it when both NPCs present.
- [ ] Existing debug command verified, OR new one implemented, exact tested syntax printed; complete hut generated (all rotations).
- [ ] 1 Kuzya per hut, no duplicate on relog, reload, teleport, visiting same hut with two players; no changing in-world player-edited building unexpectedly.
- [ ] Gradle project build, dedicated server initial load and multiplayer tests pass; jar copied to correct PolyMC mods target only after identifying actual target instance.

## Evidence for delivery
Codex handoff includes short test report table (Pass/Fail), actual module and asset paths, Gradle task output, screenshots for front/3/4, animation GIF/video or sequential screenshots of important clips, known limitations and **real generation command**. Any failed mandatory point = not complete.
