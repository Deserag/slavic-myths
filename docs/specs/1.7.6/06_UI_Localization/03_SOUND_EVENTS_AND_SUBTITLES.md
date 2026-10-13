# Sound events, source OGG files and rights

Twenty synthetic OGG prototype cues are provided. They are original generated effects, *not* sourced from copyrighted films. Use as working audio or replace only with newly licensed/created equivalents. Sound registry IDs are derived from stem, e.g. `slavicmyths:reward/rune_stomp_proc` where code conventions dictate actual mapping; register under current sounds.json and use in server event only.

Events: stomp ground pulse, dispelling magical glass pop, pursuit 3-hit accent, suppression clink, battle line short horn, Ilya block/reflect bang, Dobrynya ring charge/chime, Alyosha dodge whoosh, Finist falcon detection chime, each trophy trigger, reward claim and loot acquisition, anvil insertion. 2–3 variations for common repeated sounds. Sound engine pitch variance +/- 3% okay; volume: combat cue -6dB relative to player hurt, passive -12dB; do not mask boss hazard audio. Include subtitle key text for each proc; hero gifts have one line of unique UI text, not reused character dialogue.

Source rights manifest: `08_Audio/MANIFEST_RIGHTS.json`. Every event maps to OGG inside `08_Audio/SOUND_MAP.json`, and a minimal `sounds.json` draft is provided as reference. If current code expects different JSON, adapt exactly, no parallel sound registry.
