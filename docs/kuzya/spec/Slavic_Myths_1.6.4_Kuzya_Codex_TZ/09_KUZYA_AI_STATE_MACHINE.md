# AI / schedule: exact states, priorities and routing

## Top-level states
`HUT_BOUND_IDLE`, `MOVING_TO_STOVE`, `CLIMBING_STOVE`, `RESTING`, `SLEEPING`, `WAKING`, `MOVING_TO_TABLE`, `SITTING`, `EATING`, `MOVING_TO_CHORes`, `INSPECTING`, `SWEEPING`, `NOTICING_GUEST`, `GRUMBLING`, `INTERACTION_RESPONSE`, `SAFE_RECOVER`.

Order of priority: stuck recovery / no-bounds safety > finish uninterruptible physical transition > interaction queue > guest grumble if eligible > daytime/night schedule > ambient idle. Never use random teleport as navigation shortcut. If outside hut bounds after bug/structure replacement, try nav return; only administrative recovery at safe spawn anchor after unloaded/out-of-world case, log once.

## Weighted activity cycle, one pick every 55–140s after completion
- Night 12500–23000 world daytime ticks: `sleep/lie` 60%, `quiet idle on stove` 20%, `short snacks` 10%, `domestic` 10%.
- Day 0–12000 ticks: `rest/lie` 32%, `eat` 25%, `walk/inspect` 24%, `sweep` 9%, `stand` 10%.
- Early morning & evening transitions: interpolate probabilities linearly. Force no 'sleep' if bed unreachable, default `idle_sit` nearby.
- During storm 20% preference toward warm stove, not a new task branch.
- Maximum walking 8–18s, maximum sustained rest 2–5min in observed loaded time, avoid overactive arcade character.

## Hunger is atmospheric, not survival meter
Food animations are scheduled activities, no player item consumption or damage. Choose items from list actually in repository (bread, pies if implemented, porridge if exists, berries, milk). Only spawn tiny **visual prop** from existing resource or new non-collectible embedded mesh. No new recipes required. If food label exists in localizations, respect it.

## Look and talk rules
Kuzya generally ignores player. If player enters briefly (<30 seconds), he does not redirect tasks. If player approaches close (<2.5b) or right-clicks, a SHORT contextual reply is allowed with voice cooldown but no new quest. Stranger timer per player controls annoyance. During sleep avoid attention until 60s, unless right-click triggers wake. Baba Yaga remains high-level priority for her own AI; do not hijack her target selectors.

## Ownership and leash logic
Per hut save stable UUID/index; Kuzya has ownerHut ID/bounds/anchors, no follower owner player. Disable pickup, trading, riding, leads, breeding and despawn via mobcap; path target never outside interior floor polygon; collisions with player nonviolent, prevent piston/boat/minecart movement by refusing vehicle mount. Allow physically normal interaction by looking and voice only.

## Navigation anti-stuck requirements
- Build hut-relative anchor graph, shortest legal path between floor/seat/stove/stairs. Before moving check block safety, 2D+height clearance based on .70b character and .52b width, no open hearth collision.
- If path obstructed, retry alternate waypoint after 4s, pause and look around 3s, then select another valid household action. Never continuously slam through counter or cauldron.
- World save/reload: resume stable idle at last safe location, not in middle of one-off clip on roof; fall back to nearest anchor.
- Dedicated server: authoritative state changes; animation playback client side, visual interpolation and sound packet once to nearby players.
