# ARENA — movement, event choreography, encounter states, camera and lighting

## Locational timeline
- `UNVISITED`: ruin exists as a physical place, ambient dark lanterns, no fight; player may explore public exterior and ruined porch but not enter underground hatch. No infinite player pulling.
- `INVITATION`: when quest/personal-story flag + matching Dobrynya are valid, player enters >=2.5-block ritual radius and confirms a contextual interaction. Do not auto-trigger on merely walking past the ruin. At most one active encounter per arena; protect by encounter UUID.
- `PRE_FIGHT` 3.5s: 0.0s lanterns flicker; 0.7s Marinka silhouette atop ritual altar or at house porch; 1.1s Dobrynya steps in beside player; 1.6s first original dialogue; 2.6s red circle appears; 3.5s battle enabled. Skip option for repeat attempts while keeping state logic intact.
- `COMBAT` phases 1–3: central ring is a visual anchor. Cover objects keep line-of-sight diverse but the real boss cannot hide infinitely in closed house. Player can move around the well, broken cart and trees; center is generally open. Intro and phase warnings avoid sudden 6HP untelegraphed damage.
- `RECOVERY/FAIL`: when all valid participants defeated, or player abandons fight beyond leash for >12s, reset temporary boss, clone and glyph entities, clear ritual anchor, allow a fair restart with healed boss and hero via existing story rules (do not exploit ally healing). NOT a victory; do not open hatch.
- `WON`: boss death leads 2.8s altar collapse hatch, then `EXPLORE`. Bossbar disappears and battle summons unlock according to original regular-world rules once outside story region.

## Environmental cues in active fight
1. As `shadow_dissolve` begins, 3 dark smoke funnels form at candidate positions; all equally large. Don't put only the real copy on a special ritual tile.
2. As Marinka teleports, mark target with a brief red feather-spark trail. Endpoint must be player-visible and legal, and a visual cooldown warns against instant chain teleport.
3. At aurochs ritual, dark-gold footstep symbols travel along ground to matched Dobrynya, anchor stone glows; arena lamp reaction is synchronized. Player can traverse full path to break it, even if a ruined cart blocks straight trajectory.
4. On `witch_fire`, show directional fire lane decals ~.35s before damage and a larger 1s warning for high-damage centerline.
5. At phase 3, 3 previously dim side rune stones light in staggered order with 1.0s minimum per-zone cue. No permanent red full-screen filter or seizure-frequency flash; particles respect reduce-particles setting.
6. House windows may flicker but never dynamically destroy load-bearing floors or trap player; fire damage should not ignite every wooden prop permanently. Use harmless visual fire particles or non-spreading special source where appropriate.

## Camera / UI constraints
No forced long third-person cutscene in active gameplay unless current mod already has opt-in cinematics. Camera shake max 2 degrees / <=0.25s for close heavy spells, user setting to disable if available. Plain Minecraft UI remains readable and chat not spammed. Optional boss healthbar and matched ally health: bar anchors and labels in HUD spec. For immersive sound, ambient wind/creak only within localized yard, no global sound channel.

## World persistence
Persist three independent elements: (1) global player quest victory/participation, (2) per-arena finished/unlocked vs in-progress, (3) optional ordinary chest inventory claim. World load of `COMBAT` after server restart either reconstruct exactly with participants and UUIDs or rolls back to PRE_FIGHT cleanly. Never delete house or basement on defeat, never respawn basement as second instance, never duplicate merchant/loot drops.
