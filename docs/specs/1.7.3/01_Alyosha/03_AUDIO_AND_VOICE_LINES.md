# ALYOSHA audio events, voicing rules, subtitles

SFX are READY in `07_Audio/Alyosha/`; see `07_Audio/manifest.json` and `07_Audio/SOUND_EVENT_MAP.md`. They are ORIGINAL SYNTHESIZED GAME EFFECTS. They are NOT recorded human speech; do not label electronic synthesized grunts as actual spoken words.

## Voice direction if separately recorded later
Male youth, quick clever slightly playful mid-range; distinctly lighter/livelier timbre than Ilya's basso or Dobrynya's calm solid baritone. No celebrity voice imitation, no audio extraction from film without permission. All lines authored here as original. Future human actor assets optional; non-verbal sounds must work now.

| Event | Ru subtitle / line for future VO | When, limit |
|---|---|---|
| summon | «А вот и я! Кого тут проучить?» | owner calls, 1/activation |
| taunt_01 | «Эй, косматый! Попробуй догони!» | taunt triggered, max once /12s |
| taunt_02 | «Да сюда смотри, зверюга!» | alternate taunt, random 1 of 2 |
| dodge | «Не попал!» | successful dodges only, probability <=0.25 |
| feint | «А ну-ка, повернись!» | successful feint, max once /30s |
| counter | «Вот так-то!» | counter after clean dodge, chance <=0.25 |
| hurt | «Ух!» / wordless exhale | on hit, 1 every 1.5s |
| retreat | «Надо дух перевести!» | Last Stand once per battle |
| heal | quiet drinking / cloth rustle | recovery, every 2s, no speech loops |
| victory | «Не силой единой берут!» | victory once |

## OGG + mix targets
- 48kHz mono Vorbis, max individual effect <=4s except ambiance, 16-bit source, peak <= -1dBFS, no hard clipping, avoid loudness distortion. Prototype SFX gain in-game via normal sound registration.
- `alyosha.taunt`: audible 18 blocks; weapon/dodge 12–18; steps 10; idle 5. Random pitch 0.96–1.04, avoid constant repeat.
- Avoid high-pitched scream. His characteristic taunt effect is a quick breath + low wood/clack accent; actual spoken VO replaces *only* VO event later, not combat SFX.

## Events (must register each)
`alyosha.summon`, `.footstep_soft_1`, `.footstep_soft_2`, `.taunt_1`, `.taunt_2`, `.dodge_swish_1`, `.dodge_swish_2`, `.saber_swing_1`, `.saber_swing_2`, `.saber_hit_1`, `.bow_draw`, `.arrow_release`, `.feint`, `.counter`, `.hurt_1`, `.hurt_2`, `.drink_tonic`, `.recover_breath`, `.victory`. If the same OGG variation is used by multiple compatible events, document mapping precisely; do not point to missing files.

## Subtitles
Create `subtitles.slavicmyths.alyosha.<event>` for taunt, dodge, hit, summoning, drink, and victory. Use descriptive subtitles for effects where no real words exist: `«Алёша свистит и привлекает внимание»`, not false spoken lines. Speaker title `Алёша Попович` on named dialogue UI. No flickering every tick.
