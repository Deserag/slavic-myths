# Falcon companion, transformation avatar — original Minecraft models

FINIST IS NOT THE SAME THING AS A PERMANENT PET. In idle the falcon is a separate small companion as on approved sheet, perched on left arm; Finist can ALSO become a LARGE FALCON in a short transformation. Companion disappears into 'following/hidden' state while Finist is transformed so that only **one active bird attack silhouette** exists during morph; reappears on return. This resolves visual and mechanical confusion.

## Companion physical form
Distinct 128x128 RGBA hand-textured cuboid bird (no imported photorealistic mesh): body 5x8x5 model px; head 5x5x5; beak 2x2x3; two wings each with main segment 9x2x5 and two 4x1x3 feather tips; tail 5x1x8; legs 2x4x2; claws 1x2x2. Wingspan extended ~24 model px = 1.5 blocks. Idle perched whole bird ~0.65 blocks tall, render scale adjustable 0.85–1.0. Slate/warm beige and dark-brown folded wings, cream chest, alert gold/orange eyes with charcoal pupil, bright golden collar/ring; NOT blue feathers all over. Perching pivot to Finist left wrist, offsets approx x=-0.48, y=1.50,z=-0.20 world. Correct facing, distinct from his shoulder armor.

Atlas 128x128, reserved: head 0:32 x 0:32, wings left 34:78 x 0:40, wings right 80:126 x 0:40, body 0:48 x 42:90, tail/beak/claws 50:120 x 42:90. Separate feather strips silhouette important at 10–15 blocks; feather tips use layered crisp voxel polygons, not fluid mesh. Opaque alpha only; don't use blurred PNG.

## Transformed hero falcon
Larger wingspan **~2.25 blocks**, body length ~0.9, visible gold feather accents and blue-gold diagonal tail/streak matching Finist's cape, sky-blue glowing iris (subtle). Use child model or scale variant but unique markings: companion eye amber, transformed bird eye pale sky blue and a golden wing-collar. Transform avatar has combat collision shape shrink only server-safely: use height ~0.85,width 1.15 while airborne, ensure collision recovery to humanoid only in space >=2.25 blocks or auto-relocate to valid adjacent empty spot. No clipping into player blocks. The hero entity, ownership and active HP **never change** through transformation.

## Bird animation clips (20t/s)
| Clip | Timing / keyframes | Behavior |
| --- | --- | --- |
| perch_idle | 0 body upright wings folded; 12 head yaw+12°, eye blink; 24 tail twitch; 38 neutral | random head movement separated 60t. |
| takeoff | t0 crouch wing +0°, t3 wings -30°, t6 +65°, root Y+3px; t10 root Y+10px | detach from arm when attack actually authorized; don't leave phantom arm bird. |
| fly (16t loop) | t0 wings +55°, t4 0°, t8 -48°, t12 0°, t16 +55° | cycle 2.5–3.5 beats/s. |
| circle_watch (80–100t) | orbit yaw +360° over 80, bob Y+/-2px every 20; wings loop; at t12 gold marker pulse | movement server/path bounded to 10–16 block circle and not through terrain. |
| dive | 0 bank +15°, t6 pitch -34°, t12 pitch -58°, t16 strike, t20 pull-up, t26 return | hit once, one target; no clipping into player face. |
| intercept | t0 suddenly wings up, t8 cross line of attack, t12 collision flash, t18 bank away, t25 return | triggers only when guard cancels eligible attack; no endless barrage. |
| bound_by_curse | t0 purple chain begins, t12 wings restrained +18°, t24 barely flutter, hold max 240t | visible chain to real hittable anchor, eye dim, no bird scout while chained. |
| escape_curse | t0 chain breaks, t4 wings fully open, t12 rise, t24 return orbit/perch | hero core abilities resume after server state clear. |
| hero_morph_avatar | synced from Finist t12, wings wide, then 28t flight/dive | no independent summoned companion during state, restore after. |

Motion boundaries: when owner teleports / leaves dimension / disconnects safely hide and clean bird, never duplicate. Bird may render as a synced visual controlled by hero; if real entity, its collision must never push mobs/players, and it cannot be captured/tamed/killed permanently. Destroy curse anchor if boss dies or event quits.
