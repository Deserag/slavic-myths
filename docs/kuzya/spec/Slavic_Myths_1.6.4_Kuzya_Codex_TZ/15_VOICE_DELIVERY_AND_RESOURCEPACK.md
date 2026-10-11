# Voice and subtitles: implementation protocol

`manifests/dialogue_ru.json` includes state, line_id, original Russian text, `audio/ready/voice_draft` exact file, triggers and cooldown weight. No need for Codex to invent words or audio names. Dialogue lines were authored specifically for this mod. Direct lip sync via envelope extraction from sound OGG, or authored phoneme beats mapped by syllables; account for espeak voice duration.

## Accessibility
- `ru_ru.json`: every new voice subtitle ID and `entity.slavicmyths.kuzya`, home/hut messages.
- `en_us.json`: exact semantic (not word-by-word) translations for subtitles.
- If device can't play audio, still display Russian or English subtitle string for 3–4s / actual voice length whichever longer, not constantly on screen.

## Optional licensed archival pack (not in distribution)
Allow separate resource-pack mapping `assets/slavicmyths/sounds/kuzya/voice_override/...` supplied explicitly by user with proven permission. Packs MUST NOT be secretly downloaded. Resource pack can override `.ogg` at known paths, no Java code change, but line/subtitle pairing must be audited. Archive has `audio/README_RIGHTS.md` and `audio/INSTALL_SOUND_MAP.md`.

## Rig/mouth integration
At utterance start set `speakingUntilServerTick = start+ceil(audioLength*20)`, choose mouth openness based on envelope after attack (e.g. normalized RMS 20ms windows, floor .06px and peak .55px). Facial lip pose additive except sleep; silent random flap prohibited. Manual line overlay if exact voice playback missing. Hand/body gesture from 07 never restarts mid-phrase.
