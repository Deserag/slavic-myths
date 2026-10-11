# Time-in-hut detection, escalation and voice selection

## Player time tracking (independent per UUID)
Track only **actual active server ticks while the player is inside hut's validated 3D interior bounding volume** (door threshold excluded; on roof excluded). Sleeping or unloading pauses. On exit reset escalation after 30s away (not instantly, to avoid door boundary jitter); full reset on return after 60s. If multiple players, each has own timer; never replay voice to entire dimension.

- **0–30s**: completely ignore unless player manually right-clicks; spontaneous Kuzya activity continues.
- **30–60s**: 15–20% chance of `notice_guest` glance, one glance per 15–25s, *no immediate spoken eviction*.
- **60–120s**: 35% chance of `mutter_small` or `voice_notice_01/02` no more once per 35–50s.
- **120–210s**: occasionally `grumble_medium`, select from `voice_ask_leave_01/02/04`; one line every 45–75s max.
- **210s+**: `grumble_large` or `shoo_guest` directing player toward door, choose `voice_ask_leave_03/05`; one line every 70–110s max, never spamming; at most 2 noticeable guest complaints per Minecraft minute per hut.

When Kuzya sleeps, guest timers still accrue but reactions wait until natural wake unless player interacts (then `wake_up`). High priority transitions finish safely before talking. If Baba Yaga's other scripted scenes involve player, Kuzya must not interrupt them in the same tick; queue reaction and lower priority.

## Phrases & real audio files
`manifests/dialogue_ru.json` is source of truth, ALL default utterances are **original**, not copied film transcription. Map one phrase ↔ one OGG file under `audio/ready/voice_draft/`. The 20 attached voices are distinct Russian synthesized demo dubs, playable. Sound length measured at runtime and the jaw/lip layer aligns to actual envelope or metadata, not a fixed 1 second.

Sample situational categories: greeting on deliberate interaction, food wishes, sleep interruptions, friendly but stern household comments, increasingly gruff questions after guest overstays, requests to Baba Yaga when she is near. Do NOT invent new dialogue text if manifest already covers case; use weighted options and cooldowns.

## Network & subtitles
One sound packet/source per authoritative Kuzya triggered state, clients render voice at NPC coordinates with `distance_delay` and proper attenuation; nearby party hears, far away not. Subtitle text localized (`ru_ru`, `en_us`) references actual selected dialogue ID. Even if speech volume 0 player can read subtitles. Disable voice option should not disable activity AI or lip syncing required for clips.
