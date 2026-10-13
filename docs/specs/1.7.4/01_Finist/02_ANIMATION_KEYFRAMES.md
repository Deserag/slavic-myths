# Finist keyframes (20 tps; Euler degrees; interpolate easing for continuity)

Use existing animation controller (GeckoLib only if already present). Each named animation specifies TOTAL length and exact key pose ticks. Rotation axes X=pitch forward positive, Y=yaw, Z=roll (renderer convention can vary; preserve visible pose). Animation root origin at floor; no teleport as mere rendering trick. For each entry, reset pelvis/head/arms/cape on end; upper-body additive poses may overlay locomotion only if bone masks exist.

| Animation | Ticks / channels (timeline) | Events and restrictions |
| --- | --- | --- |
| idle (loop) | 0 neutral; 18 torso pitch +2°, cape +3°, left hand +6°, head yaw -3°; 36 neutral; 54 torso -1°, cape -2°; 72 neutral | Bird perched on left forearm if idle; occasional look with random gap >=80t; no constant glowing particles. |
| walk (24 loop) | t0 right leg +24°/left -24°, arms opposite; t6 neutral; t12 invert; t18 neutral; t24 loop | Cape delayed +/- 5°; feet lock on steps; foot sounds at t3,t15. |
| run (16 loop) | t0 torso X+10°, legs +/-38°, cape X-19°; t4 passing; t8 invert; t12 passing; t16 loop | Actual movement capped 0.30 blocks/tick; speed buff changes stride timing not leg length. |
| sword slash (16) | 0 ready sword at -55° shoulder; 4 torso yaw -22°, arm X -75°; 7 strike across front yaw +45°, hand extends; 10 recoil; 16 neutral | Real hitbox active only t7–9, not throughout swing. No invisible 360° arc. |
| mark vulnerable (24) | 0 sword lowered; 6 left hand raises palm; 11 head tracks target, light feather crest above target; 16 hand closes; 24 neutral | Applying mark server t12, client gold outline/particles <=20-block perception, 0.5s emphasis then fades. |
| sword/sokol warning (20) | 0 alert head raises; 7 left arm points danger; 12 wing icon expands above danger ring; 20 return | Only danger already chosen by boss; no cheating by revealing unreachable mobs through walls. |
| leap / 'winged dash' (26) | 0 squat root y-2px, sword back; 4 launch root y+4px; 8 crest y+10px, cape spread 28°; 13 midair yaw toward target; 17 dive X+35°; 21 impact root y+1px; 26 recover | Server drives actual leap; hazard hit only around t19–21; max horizontal 6 blocks, altitude <=3.5 blocks, collision checks, no phasing or permanent flight. |
| falcon morph (20) | 0 sword down, companion flies off; 4 golden pixels around arms; 8 silhouette starts shrink, humanoid parts fade; 12 transformed bird replaces human; 16 wings flare; 20 falcon pose | Root/owner entity remains server-authoritative; hide vs swap only after synced `morphActive`. No simultaneous duplicate hero. |
| morph flight/dive (28) | bird t0 spread wings; t5 bank -24°, t9 high point; t13 dive pitch +42°, t18 near target; t22 recovery; t28 human returns via 12t de-morph | Damage-only during small server window on committed target, never pass through solid blocks. No rapid spam. |
| shield-like protection? | NOT APPLICABLE — no physical shield. Bird distraction/interception animations are in falcon file. | Do not import Ilya/Dobrynya shield animation. |
| stagger (12) | 0 torso backward 0; 3 X-19°, left arm flail; 8 centered; 12 reset | Limited interruption, exclude during retreat / knockback immune states. |
| last stand retreat (26) | 0 HP threshold; 4 sword lowered; 10 torso forward +18°, stumble; 18 turn 150°; 26 run away | At <=15% HP, leave active encounter, retain occupied party slot. No in-battle return. |
| healing tonic (60) | 0 seated/kneel; 14 bottle raised; 28 head pitch +19° bottle mouth; 38 tilted hold; 46 lowered; 60 sitting idle | Server heal outside battle in clear safe radius, 10–15s recover; no instant invulnerability exploit; if no safe position fallback noninteractive recovery marker. |
| victory (44) | 0 grounded; 12 blade raised 55°; 24 bird circles; 36 blade drops; 44 idle | Cannot trigger before server boss defeat. |

For every animated state specify visual+auditory telegraph threshold and event key. Time budgets matter more than rendering complexity. Tie movement and real damage to server state rather than performing hit via keyframe client event. Bird perched only in idle, off-arm as soon as scout/attack/morph begins. Client interpolation handles latency, explicit animation version/state field persists across tracking-start. On failure respawn model defaults idle; no invisible locked avatar.
