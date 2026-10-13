# Full sound requirements — trigger mapping, authored OGG, acoustics and mix

Production audio files are shipped under `09_ASSETS/audio/`. All included assets are independently synthesized specifically for this TЗ and may be copied into the mod; they are usable OGG Vorbis, not web links. IMPORTANT: synthetic `purr`/`mew` are stylized placeholders for actual-cat realism; transparent classification in audio manifest. Enhance when possible with legitimately sourced recordings, but do NOT silently steal samples or skip sounds. Every audio file is connected to real `SoundEvent` and `sounds.json`; not just placed in folder.

Audio target mono 48 kHz (22.05/44.1 acceptable after conversion to OGG), peak ≤-1dBFS, average −18 to −24LUFS indicative; attenuate spatially, no clips. 3D spatial for all world sounds, UI page sound non-positional. Fade song loop 120–220ms, not abrupt.

| SoundEvent suffix | Ship file name | Clip and t | Gain target | Min audible space/description |
|---|---|---|---|---|
| `bayun.purr.soft` | `bayun_purr_soft.ogg` | sleep idle and calm, max 1/20s | 0.36 | low throaty pulsed purr, 4-6b |
| `bayun.purr.happy` | `bayun_purr_happy.ogg` | pet_rub t9, gift feedback | 0.55 | warmer purr 6b |
| `bayun.mew.soft` | `bayun_mew_soft.ogg` | wake t20, greeting | 0.35 | breathy short mew, 8b |
| `bayun.mew.annoyed` | `bayun_mew_annoyed.ogg` | invalid repeated gift, anger | 0.60 | growly downward voice, 10b |
| `bayun.breath` | `bayun_breath.ogg` | idle weighted | 0.28 | tiny breath, 3b |
| `bayun.settle` | `bayun_settle.ogg` | sleep/loaf t13/21 | 0.3 | dry furry shuffle, 5b |
| `bayun.stretch` | `bayun_stretch_exhale.ogg` | stretch_front t23 | 0.44 | soft yawn/exhale, 6b |
| `bayun.groom` | `bayun_groom.ogg` | groom t20,37,52 | 0.20 | 2 close tongue flicks, 3b |
| `bayun.milk` | `bayun_lap_milk.ogg` | eat_milk t22,33,44,55 | 0.50 | liquid lap, 5b |
| `bayun.fish` | `bayun_chew_fish.ogg` | eat_fish t26,35,45 | 0.43 | bite & swallow, 5b |
| `bayun.pawstep` | `bayun_pawstep.ogg` | walk foot contact | 0.25 | subtle paw on old stone, 5b |
| `bayun.warn` | `bayun_spell_warning.ogg` | threat_warn t13 | 0.82 | unsettling narrow chime+low warning, 16b |
| `bayun.song.intro` | `bayun_sleep_song.ogg` | hypnosis_song t4 | 0.72 | own 4-note hypnotic melody, 15b, localized subtitle |
| `bayun.song.loop` | `bayun_song_loop.ogg` | while CHARMED | 0.56 | seamless, lower in mix than warning, immediately stops when abort |
| `bayun.pounce` | `bayun_pounce.ogg` | pounce_kill t9 | 0.85 | powerful low spring and whoosh, 12b |
| `bayun.claw` | `bayun_claw_swoosh.ogg` | pounce_kill t18 | 0.9 | sharp scratch impact, 12b |
| `book.page` | `bayun_book_page.ogg` | next/prev page in GUI | 0.38 | warm dry parchment sound, UI only |
| `book.open` | `book_open.ogg` | open book | 0.45 | leather/hardcover slight creak UI |
| `book.discovery` | `book_discovery.ogg` | new real lore discovered | 0.45 | soft 2-note ink/wax seal, UI/notification |

### Mix policy
- NEVER loop intense song throughout world idle; only inside valid threat; song sound must carry safety information, audibly build at warning+windup.
- During charm attenuate non-critical ambience −4dB locally (do not change master volume) if engine provides safe per-instance mix; avoid overriding Discord/system audio.
- Ambient cat purr one sound at a time; warning overrides grooming and purr; playing 4 laps shouldn't stack to 4 simultaneous loud tracks.
- Add subtitle translations for each new SoundEvent, especially `«Кот Баюн начинает петь»`, `«Кот Баюн впивается когтями»`, `«Кот Баюн довольно мурлычет»`, `«Шелестят страницы Книги сказаний»`. Subtitle strings are Russian; no raw namespace.
- Add every final audio event file to `sounds.json` under mod namespace; validate actual decoder, number of channels and duration with ffprobe. If alternate source acquired, preserve proper SPDX/license attribution in `10_SOURCES/AUDIO_PROVENANCE.csv`; no copyrighted third-party samples.

### Real-field-recording options (not currently bundled)
Wikimedia Commons PD/CC0 (verify and fetch original when network is accessible): Whiskers' purr edit.ogg, Meow of a pleading cat.oga, Domestic cats eating.ogg; see RIGHTS.md. Our archive does NOT falsely claim those sounds included. These are optional aesthetic refinements, not an excuse to omit the shipped original functional audio.
