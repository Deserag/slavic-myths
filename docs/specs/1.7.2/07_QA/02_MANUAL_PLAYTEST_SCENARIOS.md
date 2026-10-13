# Human test flow — reproducible tasks

## Scenario A: First exploration and visual proof
Start creative dev world with current mod. Use existing quest/admin debug facility to place exactly one witch yard in a safe dry/flat biome. Screenshot top down and each major architecture angle with grid boundaries. Compare house/well/barn/circle/trees/cart entrance to `Yard_top_down_60x60.png`. Walk the 60x60 ground in survival no flying, check paths ~40–45b across. Enter not-yet-open hatch: must remain shut. At x0,z0 interact and see matching hero requirement.

## Scenario B: Ally render on day/night
Spawn Ilya and Dobrynya side-by-side in non-story world. Compare armor: Ilya distinct heavy tank, Dobrynya broad silver-white warrior with **short pale stubble**, no full beard. Zoom face. Verify 3.6b shaft longer than 2.4b figure (1.5x), no clipping of 2.4b head/shoulder. Verify running, ward, sweep, parry, retreat animation exact transitions. Run in rain, nighttime torch-lit court, with shader off.

## Scenario C: Shadow investigation
With Marinka at HP 520, wait shadow. Record slow-motion video showing 3 bodies at arrival and check no instant original hint. Hit form A with 12 HP equivalent, it remains; hit another 12 (false expires if A false) or real loses original bossbar health if A real. Check independent clone HP 24 and full cooldown. Disable particles: body equality still holds. Attempt to identify by F3 before hit: internal debug data is admin-only, normal UI does not leak.

## Scenario D: Transform and rescue
At HP 395->385 with valid Dobrynya, witch casts golden footprints, visible gold ritual anchor spawns. Photograph transformation and shape of black-brown aurochs with gold horns. Hit anchor 3x or deplete 80HP; spell breaks, Dobrynya human and shield/spear correctly restored, Marinka shield down for 8sec. Repeat at HP 30% after server reload; transformation must not replay or desync hero. Try player shot when aurochs charging: no friendly fire.

## Scenario E: Fair damage / buff matrix
Use 24HP test avatar, full unenchanted diamond, no Protection or regen effects. Record before+after HP on full Witchfire direct hit: >=6HP. Side wing ~3HP, trace ~3.5HP, floor ember per second <=1HP; verify short mercy frames. Then add Protection and Resistance, observe lower resulting damage. Repeat unarmored and netherite optionally for comparative scaling, no unexpected forced floor against protected players.

## Scenario F: Win and persistent basement
Win normally. Observe rune light fade and a 2x3 passage open after 2.8s (within net latency tolerance). Descend safely with 2b head clearance. Screenshot distinct workshop, transformation hall and secret store. Loot ordinary chest, exit/relog/revisit: never resets. Verify story completion flag and future eligibility stored but NO unique boss reward. Kill only a clone in debug and check hatch stays sealed.

## Scenario G: Failures and multiplayer
Die during mirror stage, log out while touring, use an invalid dimension, unload target chunk then approach, second player clicks altar simultaneously, restart dedicated server during hatch. All temporary summons/locked party/persistent flags converge to sane state. Normal summon works afterward. Debug command used to inspect should not ship with broad open permission if project policy forbids.

## Deliverables to user
Provide current built JAR path, actual mod-loader version, reference screenshot evidence for both characters, 3-form effect and building+basement, main test pass/fail table, and list of unresolved mismatches. Do not say 'done' if only assets copied but entities not coded.
