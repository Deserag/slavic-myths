# MARINKA — original SFX, voice direction, triggers, subtitles

Sound palette: a witch, hushed delivery in dialogue (human VO optional), nonverbal sonic texture of ash/wood hiss, dark glass/metal resonance and wing flutter. `06_Audio/marinka/*.ogg` are synthesized ORIGINAL nonverbal sound effects, **not** copyrighted film audio and **not** recorded acted speech. Use Minecraft sound registry with positional mono Vorbis and subtitles. If VO is not recorded and properly licensed, show original subtitled lines + nonverbal FX; never use missing .ogg paths or imitate specific known film actor.

| Event ID | OGG filename | Sound/action trigger | Volume / pitch / cooldown |
|---|---|---|---|
| marinka.idle | witch_whisper_01.ogg | idle nearby | .45 / .95–1.04 / 12–18s |
| marinka.walk | cloth_step_01.ogg | every actual grounded 2nd step | .45 / .94–1.05 / stride |
| marinka.trace | trace_hex_01.ogg | footprint marks appear | .85 / .98–1.05 / cast |
| marinka.fire_charge | fire_charge_01.ogg | palm tell t=0.22 | .95 / .94–1.05 / cast |
| marinka.fire_release | fire_release_01.ogg | projectile server cast tick | 1.0 / .98–1.03 / cast |
| marinka.shadow | shadow_dissolve_01.ogg | start of three-form summon | 1.0 / .97–1.03 / once |
| marinka.clone_break | shadow_clone_break_01.ogg | illusion loses 24 HP | .85 / .96–1.04 / per false |
| marinka.barrier | barrier_raise_01.ogg | veil applies at t45 | .80 / .97–1.03 / per activation |
| marinka.barrier_crack | barrier_break_01.ogg | ward becomes broken | 1.0 / .97–1.04 / success |
| marinka.blink | witch_blink_01.ogg | fade/arrival | .85 / .96–1.03 / blink |
| marinka.magpie | magpie_change_01.ogg | body into bird | .90 / .95–1.03 / transition |
| marinka.magpie_wing | magpie_wings_01.ogg | bird flaps, max 2/s | .65 / .95–1.08 / flap |
| marinka.curse_aurochs | aurochs_hex_01.ogg | anchor + gold runes | 1 / .98–1.04 / ritual |
| marinka.herd | enchanted_herd_01.ogg | first summoned tur steps | .9 / .94–1.04 / cast |
| marinka.hurt | witch_hurt_01.ogg | hit, including false forms | .75 / .94–1.07 / 1.1s |
| marinka.rage | witch_rage_01.ogg | phase 3 only | 1.0 / .98–1.03 / once |
| marinka.defeat | witch_defeat_01.ogg | real Marinka 0 HP only | 1.0 / 1 / once |

## ORIGINAL spoken dialogue/subtitles (not ripped from any media)
- Intro RU: «Добрыня Никитич... След твой мне знаком.» EN “Dobrynya Nikitich... I know your footsteps.”
- Clone taunt RU: «А где же настоящая я?» EN “Which one of us is real?”
- Barrier RU: «Не достать меня!» EN “You cannot reach me!”
- Witchfire RU: «Огонь, пляши!» EN “Fire, dance!”
- Footprint curse RU: «Чужие следы — моя воля!» EN “Your tracks obey my will!”
- Aurochs spell RU: «Стань туром золоторогим!» EN “Become the golden-horned aurochs!”
- Anchor broken RU: «Нет! Печать моя!» EN “No! My seal!”
- Rage RU: «Не уйти вам отсюда!» EN “You won't leave here!”
- Defeat RU: «И мои чары... рассыпались...» EN “My spells... are undone...”

One subtitle VO line <= every 7s; urgent attack warnings take precedence over ambient lines. Shared neutral scene subtitle in clone fight (do not attach text only to real actor). Hurt FX must be the same among the three visual bodies. Damage warnings always audible independently from client graphics settings if sound is enabled.
