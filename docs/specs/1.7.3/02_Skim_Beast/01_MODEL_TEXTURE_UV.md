# SKIM BEAST — unique four-legged boss model, UV, style

Approved reference: `06_References/Approved/APPROVED_Skim_Beast_4_views_and_attacks.png`. Lion/wolf/ancient predator MIX (not vanilla wolf scale-up). Charcoal shaggy segmented mane, dark brown body, rusty-red ribbon/scar motifs, **large pale tusks** from maw, yellow-red eyes, long articulated tail, four massive clawed paws. No anthropomorphic weapons, no wings.

## Scale and rig
- Ground quadruped standing: top mane ~3.0 blocks above ground, total shoulder 2.45, length snout to rump **4.9 blocks**, tail additional 1.8; total nose-to-tip ~6.7. Width through front shoulders ~2.2. Rear feet spread enough to show mass. AABB approx 2.1 W × 2.95 H × 4.8 length; tune nav to avoid clipping narrow den entrance. `reared` 4.5 block total *VISUAL* height on hind paws for ROAR; hitboxes should change only with suitable safety/physics checks, not clip low ceilings. Scale comparison reference may list 4.5 without pose qualifier; this design clarifies it means **reared**, not permanent quadruped stance.
- Anatomy: elongated head 1.2 L×0.9 W×0.83 H, jaw opening down ~45°, pale fangs 0.35–0.48 long, two tusks 0.44–0.58; tusks asymmetric but balanced. Neck mane segmented 16–28 angular plate tufts of size ~0.2–0.6 with varied color, cannot resemble solid helmet. Front heavy legs with forearm 0.62–0.80, articulated shoulder and digitigrade clawed paws (4 visible toe claws), hind legs 0.85–1.05 with knees/ankle; tail 4–6 chained cuboids with forked/tufted end and dark red streak.
- Root at center-of-mass projected to floor, skull pivot for head shake, jaw pivot near back of jaw, 4 leg chains at hip/shoulder, 3 sections spine (if renderer allows), 4–6 tail bones. Separate mane tufts groups follow neck motion ±6° delayed. No tail detached floating when animation interpolates.

## Suggested scaled rig boxes relative to floor center
`root` at (0,0,0), +Z is forward; numbers in block units:
| Named bone | Approximate center | box w×h×l | Pivot and detail |
|---|---|---|---|
| torso_front | 0,2.12,+0.88 | 2.0×1.62×2.25 | full muscled chest |
| torso_rear | 0,1.78,-0.80 | 1.60×1.26×2.06 | tapered hind hip |
| mane_neck | 0,2.39,+1.58 | 2.18×1.75×1.50 | interleaved chunky tufts |
| head | 0,2.65,+2.35 | 1.03×0.82×1.23 | head pitch pivot at neck |
| lower_jaw | 0,2.19,+2.60 | .78×.31×.89 | rotates down jaw 0–45° |
| left/right foreleg | ±.86,1.12,+1.1 | .43×1.68×.54 | articulate shoulder, wrist, claws |
| left/right hindleg | ±.64,.95,-1.0 | .44×1.44×.53 | rear hock joints |
| paws x4 | ±.8/.6,.19,+1.45/-1.25 | .52×.34×.78 | enlarged toe box, claws 0.16–0.23 |
| tail root | 0,1.80,-1.88 | .37×.34×.6 | sequence ~1.8 additional blocks |

Do not force this `center` sketch on entity's actual bounding origin; configure orientation relative to engine/renderer correctly. Testing must verify server hitboxes align with visual head/paws.

## Colors exact hex
Fur dark shadow #211E1C; main brown #4F3830; coat highlight #80614B; mane near-black #282122; mane warm dark #654234; scarring red #872C2B, ember bright #D35B38; tusk ivory #D5C29E, shadow #9B896A; eyes amber #F4B73D center red #B9492E; claws #A4A08C/edge #E0DBC0; mouth interior #5A222B. Small scratches remain dark and scarred red — NO glowing demonic lava over the entire body. Rage phase makes eyes and red marks glow briefly, not fully changes texture to bright red.

## Texture UV and model QA
- Base body atlas **256×256 PNG** (option 512×512 only where meaningful), nearest filtering, purposeful >=4 shade bands, alpha for sparse mane. Different textures/UV for torso vs head vs mane vs paws; no 16x16 placeholders.
- Logical atlas allocation (pixel rectangles, padding >=3): body front x0–95,y0–95; hind torso x96–191,y0–95; face+jaw x192–255,y0–95; front legs x0–95,y96–191; hind legs x96–191,y96–191; layered mane x192–255,y96–223; claws/fangs/tail x0–191,y192–255. Exact per-face islands can be packed inside these partitions by Blockbench.
- Texture accents must show scars along body visible from rear, striped tail, thick dark mane layered silhouette and teeth from front. Top-view has exposed central mane stripe. Every quadruped animation needs feet properly planted at selected keyframes; no skate in idle or roaring.
- At 15–20 blocks player must identify: VERY large mane, four thick legs, glowing amber eyes, pale fangs, red scar stripes. Avoid similar silhouette to large wolves or a generic modded lion.
