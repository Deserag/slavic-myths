# Матрица приёмки 1.6.0

Отмечены только реально проверенные исходы. NOT TESTED не означает PASS. Полная игровая приёмка OPEN.

| ID | Priority | Result | Original scenario | Evidence |
|---|---|---|---|---|
| V01 | P0 | NOT TESTED | Unshaded day screenshot: body predominantly white/ivory, large gold-orange mane and tail, not dark horse. Matches approved reference silhouette. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V02 | P0 | NOT TESTED | Side-by-side in same scene with vanilla horse and Steve: SIVKA visibly 1.5–2× larger than vanilla horse in linear perception, carries Steve effortlessly. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V03 | P0 | NOT TESTED | Side / front / rear all have unique non-flat volumetric mane and tail, correct four legs, ears, muzzle, no detached geometry or broken UV. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V04 | P0 | NOT TESTED | No visible saddle, reins, wings, horse armor or unwanted second rider model. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V05 | P0 | PASS | At least 128×128 real texture atlas and 64×64 original whistle texture, not 16×16 stretched. | resource-gates.json: atlas256/item64 |
| V06 | P1 | NOT TESTED | Night/fast-graphics (no shader) render readable with subtle emissive accent, no excessive neon bloom. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V07 | P1 | NOT TESTED | Horse fully animates while turning to camera with no mane clipping into muzzle and no eye/ear teleport. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| V08 | P1 | NOT TESTED | Walk collision with wall and fence at large scale does not cause prolonged visual wall intersection or suffocation. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A01 | P0 | NOT TESTED | Idle: independent breath, ears/tail, occasional blink, no constant frantic repetitive motion. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A02 | P0 | NOT TESTED | 10s normal walk video: four-beat distinctive footfalls, independent joints, no skating. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A03 | P0 | NOT TESTED | 10s trot video: clear alternating diagonal pairs, head and torso opposition. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A04 | P0 | NOT TESTED | 10s full gallop video: airborne stride component, body pitch, animated mane/tail, not vanilla animation scaled up. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A05 | P0 | NOT TESTED | Takeoff sequence lasts ~0.8 s and visibly compresses rear legs, extends forward and begins airy gait. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A06 | P0 | NOT TESTED | 10s mid-flight third-person video: each 0.6–0.9s horse moves fore/hind legs in repeating coordinated air stride, render-body waves 0.25–0.4 block total, rider camera smooth. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A07 | P0 | NOT TESTED | Controlled ascent and descent preserve air gallop; no T-pose, no frozen straight sliding. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A08 | P0 | NOT TESTED | Land on level grass: front hooves then rear hooves contact, smooth transition to canter or walk without snapping. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A09 | P1 | NOT TESTED | Animation transitions 0.1–0.4s without joints snapping; no gait driven by key press while blocked by wall. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A10 | P1 | NOT TESTED | Eating plays muzzle, chewing/jaw and small acceptance animation at least 1.2 s, wrong food turns head briefly. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| A11 | P1 | NOT TESTED | Summon/exit clips show horse motion and FX rather than instant pop with no feedback. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F01 | P0 | NOT TESTED | Natural rare single horse spawn on appropriate grassland biome without command, not filling ordinary plains like cows. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F02 | P0 | NOT TESTED | Wild SIVKA remains approachable and follows food, not instantly despawning every time a player comes close. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F03 | P0 | NOT TESTED | Feed stage 1 with one **regular apple**: exactly one item consumed, temp whistle granted, 60m timer created. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F04 | P0 | NOT TESTED | Stage 2 wheat, stage 3 carrot: exactly right accepted each, timer resets on every accepted stage. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F05 | P0 | NOT TESTED | Stage 4 uses actual existing blueberry item ID or documented legitimate fallback; obtainable in survival. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F06 | P0 | NOT TESTED | Stage 5 uses existing pancake/mod dish or documented legitimate fallback; obtainable in survival. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F07 | P0 | NOT TESTED | Stage 6 normal honey bottle, bottle behavior correct, timer renewed. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F08 | P0 | NOT TESTED | Stage 7 **normal golden apple** accepted: permanent status, temp countdown removed, ordinary golden apple consumed once. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F09 | P0 | NOT TESTED | Wrong or out-of-order item never gets consumed, never advances or resets bond, shows rate-limited localized hint. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F10 | P0 | NOT TESTED | Enchanted golden apple is NOT mandatory nor mistakenly treated as stage-7 food. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F11 | P0 | NOT TESTED | 60 active-play minutes with no successful next food: temporary whistle expires, old token invalid, **stage progress preserved**, player shown clear notice. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F12 | P0 | NOT TESTED | Re-find original or new unbound SIVKA, feed one regular apple to renew after expiry; prior feeding stages remain complete. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F13 | P0 | NOT TESTED | During offline/logout/server stopped, temporary active-play countdown does NOT continue. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F14 | P1 | NOT TESTED | If inventory full at stage 1, whistle not lost due to silent null; explicit safe overflow delivery. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| F15 | P1 | NOT TESTED | Repeated rapid right-click cannot consume multiple foods or advance two stages in one moment. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W01 | P0 | NOT TESTED | Full bond: right-click summons actual bound SIVKA within visible local safe space on normal grass. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W02 | P0 | NOT TESTED | Summon nearby existing horse (<=24b): exactly one horse remains, it approaches; 10 rapid clicks do not make 10 horses. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W03 | P0 | NOT TESTED | Summon under 2-block ceiling / solid wall / lava: fails gracefully; no clipping, no player death, no item loss. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W04 | P0 | NOT TESTED | Whistle is linked to owner's canonical bond; other player cannot summon or steal mount using copied item. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W05 | P0 | NOT TESTED | Saddle not required; vanilla horse screen and horse armor are not opened. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W06 | P0 | NOT TESTED | Horse never attaches to lead, permanent pen or boat exploitation; localized feedback. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W07 | P0 | NOT TESTED | After dismount ~25s horse begins natural leave animation and disappears; calling later recreates same bond with no new taming. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| W08 | P1 | NOT TESTED | Whistle item UI shows status, next food, timer and RU text; all item icons registered, no missing texture. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M01 | P0 | NOT TESTED | Sprint Ctrl+W ~3s at actual speed >=9 b/s, press Space → takeoff. Trying at low speed → only ordinary jump. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M02 | P0 | NOT TESTED | Flight W moves toward look direction, speed rises to ~14–16 b/s, no sudden teleport or rubberband. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M03 | P0 | NOT TESTED | Holding Space while in air ascends, releasing stops climbing, G descends, Shift dismount remains explicit. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M04 | P0 | NOT TESTED | Horse does not noclip through blocks or land in lava/void; wall/low-ceiling obstruction handled safely. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M05 | P0 | NOT TESTED | Flight camera doesn't shake violently; first-person horse mane does not cover most of forward view. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M06 | P0 | NOT TESTED | Controlled landing on sloped meadow / mountain with collision-aware legs and no high-impact fall death. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M07 | P0 | NOT TESTED | Server rejects flight toggle not actually on mounted bound SIVKA, speed spoof and stale input packets. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| M08 | P1 | NOT TESTED | Turning at speed has plausible lean, no instant 180-degree spin and no momentumless teleport. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| S01 | P0 | NOT TESTED | Sounds assets are **new OGGs**, include credible horse breathing, neigh, snort, hoof grass/stone/snow/wood, feed chew, whistle, takeoff, air stride, land, disappear, bond completion. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| S02 | P0 | NOT TESTED | Sounds fire on correct 3D event/animation keyframes, local audio attenuation and creature volume slider work; no missing sound warnings. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| S03 | P1 | NOT TESTED | New sound variants, pitch/volume moderate, no mechanical endless loop or ear-piercing constant bell; provenance documented. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| S04 | P1 | NOT TESTED | Particle density not excessive with reduced particles; actual visual horse visible beneath sparkles. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P01 | P0 | NOT TESTED | Singleplayer save/reload during stage 4: exact food stage and timer restored. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P02 | P0 | NOT TESTED | Save/reload after final stage: whistle still permanent, ownership unchanged, horse summon works. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P03 | P0 | NOT TESTED | Save/reload in or immediately after summoned state: one physical horse at most, no permanently unavailable bond. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P04 | P0 | NOT TESTED | Multiplayer two players bonded to different horses: summons don't swap, duplicate, cross-own, misrender. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P05 | P0 | NOT TESTED | Owner moves dimension and calls: no stale ghost horse left from previous dimension. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P06 | P0 | NOT TESTED | Server restart, dying while mounted, disconnect mid-flight: no crashes/force-loaded chunks/broken controls, bond consistent. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| P07 | P1 | NOT TESTED | Entity AI and render at 25+ horses in dev test don't massively degrade performance or spam log every tick. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| B01 | P0 | PASS | Project Gradle build exits 0 with existing target loader/Java; JAR produced. | build.log; production JAR hash |
| B02 | P0 | NOT TESTED | Dedicated server starts without client-only class loading errors; registered assets and packets valid. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| B03 | P0 | NOT TESTED | Actual RU and EN translations for all named items, states, controls, subtitles; no untranslated keys visible. | Minecraft launches: 0. Source/assets present; runtime evidence absent. |
| B04 | P0 | PASS | `07_delivery/DELIVERY_REPORT_TEMPLATE.md` filled with true file paths/test outcomes, no false PASS claims. | REPORT.md + this full matrix |
