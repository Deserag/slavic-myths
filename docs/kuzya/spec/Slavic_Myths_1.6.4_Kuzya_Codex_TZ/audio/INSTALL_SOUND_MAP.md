# Install audio in the game
1. Read `manifests/AUDIO_FILES.json` and `manifests/dialogue_ru.json`, count all 48 `.ogg` files.
2. For each item copy from `audio/ready/{category}/{stem}.ogg` into actual modid asset audio path `assets/<actual_mod_id>/sounds/kuzya/{category}/{stem}.ogg` preserving filenames. Use Minecraft `SoundEvent` IDs in `sound_events.json` (generate if project uses registry wrappers) and `sounds.json` entries with `name: <modid>:kuzya/...`, volume/pitch from `14`.
3. Wire events exactly according to `manifests/animation_events.csv`, and use dialogue ID to preserve caption-to-audio matching.
4. Validate actual files with ffprobe, registered lookup and in-game `playsound` tests, plus caption synchronization.
5. Original TV cartoon audio: not part of archive; optional user-provided licensed audio packs may replace sound resources in a safe local-only flow.

**Preassembled assets:** `resources_overlay/assets/slavicmyths/sounds/kuzya/...` contains the same 48 OGG files already laid out for the existing `slavicmyths` mod ID, with a syntactically valid sample `resources_overlay/assets/slavicmyths/sounds.json`. Merge `sounds.json` with existing one rather than overwrite it. Per-line event key `slavicmyths:kuzya.voice_*` is distinct for subtitle accuracy. `resources_overlay/assets/slavicmyths/lang/*_kuzya_fragment.json` are fragments: merge their keys into actual `ru_ru.json` and translate en_us phrases in final stage; never create unrecognized `_fragment.json` as the only locale source.
