# DOBRYNYA audio behavior / exact dialogue / sound event wiring

Use supplied original `06_Audio/dobrynya/*.ogg` files directly after placing them in mod namespace `sounds/`. All are generated for this package and must be processed as *nonverbal synthesized game effects*, NOT movie voice clips. Have `sounds.json` reference precisely correct relative filenames; stereo is not necessary and positional effects should be mono. Put player-readable subtitles in `ru_ru` and `en_us`. No actual voiced phrases are included: when a real recorded line is unavailable, play the mapped nonverbal sound plus subtitle, NOT machine text-to-speech that imitates a specific film actor.

| Sound event proposed | File | Trigger | Volume / pitch range | Cooldown |
|---|---|---|---|---|
| `dobrynya.ambient` | `breath_idle_01.ogg` | idle only, within 15b | .5 / .96–1.03 | 12–19s |
| `dobrynya.armor_step` | `armor_step_01.ogg` | actual grounded movement stride | .65 / .94–1.08 | each stride, dynamic |
| `dobrynya.parry` | `spear_parry_01.ogg` | exact parry success frame | 1 / .96–1.04 | 0.3s |
| `dobrynya.shield_raise` | `shield_ward_01.ogg` | spell ward initiation | .85 / .99–1.04 | 0.7s |
| `dobrynya.magic_deflect` | `magic_deflect_01.ogg` | reflected projectile success | 1 / .99–1.06 | 0.5s |
| `dobrynya.spear_whoosh` | `spear_swing_01.ogg` | each valid spear swing telegraph | .75 / .92–1.09 | 0.24s |
| `dobrynya.spear_hit` | `spear_impact_01.ogg` | hitbox contact ONLY | 1 / .96–1.06 | 0.18s |
| `dobrynya.sweep` | `spear_sweep_01.ogg` | circular sweep | 1 / .98–1.04 | per ability |
| `dobrynya.unravel` | `unravel_hex_01.ogg` | spellbreaker success | 1 / .97–1.03 | per ability |
| `dobrynya.hurt` | `grunt_hurt_01.ogg` | hit above flinch threshold | .85 / .95–1.03 | 1.4s |
| `dobrynya.transform` | `aurochs_transform_01.ogg` | one curse conversion | 1 / .97–1.03 | once per phase |
| `dobrynya.aurochs_charge` | `aurochs_charge_01.ogg` | telegraphed charge | 1 / .97–1.04 | per charge |
| `dobrynya.elixir` | `elixir_drink_01.ogg` | drink animation t=.8–1.6s | .85 / .98–1.05 | per retreat |
| `dobrynya.recover` | `hero_return_01.ogg` | hero visibly rallies | .8 / .97–1.03 | once per retreat |

## Original voice/subtitle scripts (DO NOT steal or sample film audio)
- summon/start: RU «Я рядом. Держись за мной!» EN “I'm here. Hold your ground!”
- entering witches' yard: RU «Чары тут крепки. Гляди под ноги.» EN “Strong witchcraft here. Watch your step.”
- parry: RU «Не пройдёшь!» EN “Not past me!”
- deflect: RU «Свои чары получай!» EN “Take your own spell!”
- ritual curse start: RU «След мой под властью чар?!» EN “My tracks are ensnared?!”
- transformed curse: RU «У-у-у!» (nonverbal), EN “[Aurochs bellow]”
- regained form: RU «Не удержать меня чарами!» EN “No spell can hold me!”
- protect partner: RU «За щитом укройся!» EN “Get behind the shield!”
- retreat: RU «Отойду, силы соберу!» EN “I'll recover my strength!”
- victory: RU «На сей раз зло отступило.» EN “The evil has withdrawn.”

At most one voiced/subtitle line every 7 sec; combat grunts may play without text at lower priority. Boss telegraphs must take sound priority over hero barks. The source package assets are audio prototypes; any future recorded human voice requires a license/voice actor's permission.
