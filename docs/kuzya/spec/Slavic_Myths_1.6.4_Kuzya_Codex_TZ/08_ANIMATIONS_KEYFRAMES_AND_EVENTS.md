# Exact animation keyframe contract and debug tools

`manifests/animation_clips.csv` contains every required clip, duration, loop flag, layering, audio event and acceptance screenshot label. `manifests/animation_keyframes.csv` defines **at least five** example key states per important clip; expand to renderer-native animation binary/JSON using correct armature. The numeric values below are in **degrees of LOCAL joint rotation**, `pelvis_y` is **model pixels relative to pose origin**, never absolute world coordinates.

### Critical reference pose samples

| animation | normalized t | head pitch | torso pitch | right shoulder | left shoulder | pelvis Y | right knee | left knee | what player must see |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| walk | .00 | 1 | 3 | -16 | 16 | 0 | 12 | -12 | left-foot stance |
| walk | .25 | -1 | 0 | 3 | -3 | .12 | 21 | -5 | passing stance |
| walk | .50 | 1 | 3 | 16 | -16 | 0 | -12 | 12 | right-foot stance |
| walk | .75 | -1 | 0 | -3 | 3 | .12 | -5 | 21 | passing stance |
| walk | 1.00 | 1 | 3 | -16 | 16 | 0 | 12 | -12 | seamless loop |
| lie_down | .00 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | stand |
| lie_down | .25 | -8 | 20 | 35 | 8 | -0.15 | 25 | 22 | reach edge |
| lie_down | .55 | 10 | 48 | 44 | 18 | -0.85 | 45 | 50 | lower body |
| lie_down | .82 | 18 | 70 | 17 | 22 | -1.45 | 64 | 60 | roll side |
| lie_down | 1 | 8 | 90 | 8 | -12 | -1.65 | 55 | 50 | side rest |
| eat_bite | .00 | 0 | 4 | 6 | 0 | 0 | 70 | 70 | food hand low |
| eat_bite | .25 | -6 | 8 | 34 | -2 | 0 | 70 | 70 | arm lift |
| eat_bite | .48 | -3 | 10 | 70 | 0 | 0 | 70 | 70 | mouth meets food |
| eat_bite | .73 | 3 | 4 | 38 | 0 | 0 | 70 | 70 | chew |
| eat_bite | 1 | 0 | 4 | 9 | 0 | 0 | 70 | 70 | hand lowers |
| grumble_large | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | face player |
| grumble_large | .25 | -8 | 8 | 26 | 12 | 0 | 10 | 0 | prepare |
| grumble_large | .50 | -4 | 9 | 58 | 28 | -.03 | 10 | 0 | point exit |
| grumble_large | .75 | 10 | 6 | 74 | 5 | 0 | 0 | 0 | full wave |
| grumble_large | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | settle |

Degrees are posed visual targets; convert sign per renderer coordinate system. Never rotate entire player/camera on an NPC key. Foot placement is enforced by IK or explicit planted-phase correction.

### State transitions
- `walk → sit_down`: only when within .25b seat anchor, yaw error <15°. `walk → climb_stove`: grounded and navigation step available.
- `sleep_loop → notice`: queued `wake_up` unless player has only spent short time in hut (should ignore).
- `eat_* → grumble`: finish bite then pause food prop safely, otherwise AI will freeze with bread in hand.
- `voice_*` not full-body state; use additive jaw/eyebrows/neck, action clip wins lower-body priority.

### Trigger frames and VFX/audio
Actual manifest events stored in `manifests/animation_events.csv`. Examples: `walk` feet at 0.00/0.45s; `sit_down` rustle .50; `climb_stove` 0.18/0.67/1.35s; `eat_bite` chew .43s; `eat_swallow` .29s; `grumble_large` voice at .60s, end at .60 + clip duration; `sleep_loop` optional sound once per scheduled interval, NOT each repeat.

### Debug commands / visual QA
Temporary dev-only HUD/state overlay: `[KUZYA] state / subclip / t / targetAnchor / ownerHut / guestTime / audioID`; toggleable by existing debug if present. `/kill`/structure reload should not make persistent animation state invalid. Store deterministic frame capture from first-person and third-person; show stable non-floating body and no 1-frame position snapping.
