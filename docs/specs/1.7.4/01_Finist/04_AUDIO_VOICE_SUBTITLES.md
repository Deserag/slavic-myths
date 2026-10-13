# Finist voice, SFX and sound event mapping

Use actual OGG provided in `07_Audio/Finist` and `07_Audio/Falcon`. Effects are ORIGINAL PROCEDURAL SYNTHESIS, not professional recorded vocals. Specific spoken Russian voice **scripts** below are not included as synthetic fake cartoon samples; record fresh with permission or use subtitle placeholders. Do not rip film soundtracks or samples.

Voice direction: young adult male, calm clear resonant middle/baritone, soft authority rather than Ilya's booming bass; slight breath at leap, clearly articulate vowels, no cartoon imitation. Spatial voice radius 18–24 blocks except taunt. Multiple alternates for hurt/footsteps.

| Event | Spoken script (original Russian) | Existing effect | Trigger/cooldown |
| --- | --- | --- | --- |
| hero summon | «Зову сокола — помощь близко!» | Finist/summon_chime.ogg | once spawn, sound radius 18 |
| scout | «Сокол, огляди округу!» | Falcon/scout_call.ogg | on Watch start, <= 1/8s |
| vulnerable target | «Вот слабое место!» | Finist/mark_cast.ogg | on successful mark, <= 1/12s |
| wing dash | «С неба — прямо в цель!» | Finist/wing_dash.ogg | once launch, not every attack |
| ally buff | «Держись! Вместе одолеем!» | Finist/inspire.ogg | every skill commit, max 1/18s |
| intercept | «Берегись!» | Falcon/intercept.ogg | only if a real attack cancelled |
| morph | «Крылья, несите!» | Finist/transform.ogg | every legit morph, <=1/25s |
| Kartaus falcon curse | «Он сковал сокола!» | Falcon/chained.ogg | 1 per curse |
| banner down | «Знамя пало!» | Finist/banner_break.ogg | each banner fall, >=6s gap |
| injury | short nonverbal gasp/grunt, 3 variations | Finist/hurt_1..3.ogg | hurt sound, >=8t gap |
| retreat | «Силы восстановлю — держись!» | Finist/retreat.ogg | once per encounter |
| healing | quiet bottle/cloth, breath | Finist/tonic.ogg | 1 per heal cycle |
| victory | «Небо снова чисто!» | Finist/victory.ogg | only boss completion |

`sounds.json` key prefix e.g. `entity.slavicmyths.finist.<event>` using project's real namespace. Events use 0.7–0.95 volume generally, falcon 0.7, effects 0.6–1.0, hurt pitch random 0.94–1.07, no on-every-tick loop. Long bird ambience once every 180–320 ticks when out of combat. Environmental muffling use engine default; no global music. Include RU/EN subtitles even if no recorded voice; users must not see unimplemented subtitle VO labels as if voice played. If recorded voice absent, use shorter subtitles corresponding to effect / action names; voice dialogue may appear as text in optional one-time chat/story encounter.
