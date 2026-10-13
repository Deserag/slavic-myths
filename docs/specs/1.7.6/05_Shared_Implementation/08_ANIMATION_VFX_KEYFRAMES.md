# Precise 20-TPS VFX and action keyframes — rewards 1.7.6

This patch adds item and effect animations, not new complex skeleton rigs. Use existing client animation/event pipeline. All timings are relative to a server-authorized ability proc and non-blocking of gameplay; 1 game tick = 50 ms nominally.

| Effect | t0 | t3 | t6 | t10 | t16–20 | Special |
|---|---|---|---|---|---|---|
| Shield Opлот | shield raised, active-block confirmation | red face lights golden rim | 3-block outward golden arc | enemy receives exactly 1.5x reflect, short sparks | fade, lower arc | keep vanilla blocking pose; do not teleport player |
| Ring counter | ruby glints on block | small 1px wrist glow | ready cue icon stays | after successful next hit, curved red-gold trailing slash | fade after 12 ticks; ready cue until 4s | if no strike within 4s, fade quietly |
| Alyosha dodge | pre-hit safe-space ray validated | hips turn 30° and low lateral lean | body shifts 2.5 blocks sideways | scarf/tassel settles | regular locomotion resumes | no invincible teleport if unsafe |
| Finist mark | faint gold falcon at HUD edge | arrow points threat direction | outline pulse on correct hostile | brief ping | clear warning at 20t | supports low particles |
| Rune stomp | landed feet pose | concentric soil ring at 1.5 blocks | ring reaches 3 blocks | physical knockback impulse | dissipates at 3.5 blocks | never destroy world blocks |
| Rune dispel | harmful effect identified | broken purple chain fragments | violet/white spark ring | status disappears | fade | not a boss story curse bypass |
| Rune pursuit | 3rd direct hit confirmed | amber 3-claw slash overlay | narrow orange trail | HP settles, reset stack | fade | not constantly emitting idle particles |
| Rune suppression | hit target, marker applied | small dark chain glyph | purple fracture pulse | target debuff icon faint | fade | no full screen purple fog |
| Rune formation | ally-count verified at heavy hit | three shield marks near chest | gold arc pulse | 15% mitigation consumed | fade | don't add static huge protective dome |
| Marinka cloak | validated safe cell | old-position smoke cloud | 2–3 block blink | trailing torn cloth pose | settle at safe spot | cancel without sound if no safe position |
| Kartaus mantle | boss-owned summoned mob dies | crest flicker | red-black 1m aura around player | Resistance I tooltip present | aura fade; buff persists 5s | no extra minion spawned |
| Saber momentum | third kill stacks | blade gold edge illuminates | fully charged next swing | 95° 4.5 radius sweep | 1.5s residual sparks | <=3 additional targets and single proc |

Audio trigger exactly once on t0 or explicitly named event (Ilya reflect at t6). Never play every animation frame. Client playback must be synchronized to authoritative server action; move particle loops to client only with bounded entity count and lifetime. Low particle setting must still show a small 1-ring telegraph and captions.
