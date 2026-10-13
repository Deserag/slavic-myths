# SKIM BEAST — sound design, exact audio triggers

Skim is not a talking human. Core acoustics: lion-like low chest resonance + wolf-like snarl + dry crackly ancient rasps, whispered whistle almost flute/serpent hiss, heavy granular paw impacts. The included `07_Audio/Skim/*.ogg` are self-created **synthetic provisional cues** (not sampled real lion/wolf, not copyrighted film), usable immediately. Do not pass them off as field recordings. Final audio may be replaced with legally clear professional recordings when available.

| Event ID | Sound and cue | Trigger | Limits |
|---|---|---|---|
| skim.ambient_1/2 | low throat rumble, far heavy breath | idle every 9–16s | min audible 22m, low volume |
| skim.step_1/2 | earth thud, grit, short bass | alternating front paws walking | not every simulation tick |
| skim.run_paws | fast double grit stomp | sprint gait foot contact | reduce spam at high speed |
| skim.claw_windup | dry fur/air claw drawing | claw t5 | 1/windup |
| skim.claw_hit | heavy claw snap/crunch | each actual contact | max 3/combo |
| skim.leap_charge | inhale + growl + spring | t5 | range 28m |
| skim.leap_land | strong deep grounded thud | actual landing t27–32 | range 34m |
| skim.whistle_charge | subtle rising pure sinister whistle | t9 | audible warning |
| skim.whistle_release | harmonic distorted whistle burst | t22 | range 36m |
| skim.roar | powerful rough roar + front impact | t26 | range 48m, no clipping |
| skim.grab | wet short jaw snap | grip t16 | 1 |
| skim.grip_hold | scraping, cloth/teeth pressure | grip t30 | 1 |
| skim.throw | air whoosh and ground burst | t45 | 1 |
| skim.rage | throaty extended roar | rage phase once | 1/phase |
| skim.exhausted | labored deep breathing | exhaustion at t15 and t44 | 2 max/cycle |
| skim.stone_collision | bone/stony head crash | collision with big natural rock | avoid ear-splitting peak |
| skim.hurt_1/2 | short restrained snarls | actual hit | cooldown >=0.7s |
| skim.death | fading guttural rasp | death once | 1 |

All game-ready cues mono OGG (Vorbis), 48kHz target, peak <= -1 dBFS, no overlap waves that constantly max out mix. `subtitles.slavicmyths.skim.*`: «Ским-зверь злобно рычит», «Ским-зверь издаёт змеиный свист», «Ским-зверь прыгает», «Удар огромных когтей», «Ским-зверь тяжело дышит», «Ским-зверь рухнул на землю». No fake speech subtitle for a nonverbal sound.

Spatial behavior: footfalls 25 blocks; roar 48; whistle 36, attack strikes 28; death 40. Prioritize warning signal over endless ambient. Whistle centered on head and can have light high-frequency layer that remains audible on consumer speakers; red visual rings also indicate ability for deaf players. Sound keys in manifest are illustrative; integrate actual registered names adapting modid.
