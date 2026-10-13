# MEDIA PROVENANCE AND INTEGRATION HONESTY

## Approved visual references
- `05_Visual_References/Dobrynya/APPROVED_Dobrynya_white_hair_STUBBLE_spear_shield.png`: generated in our project conversation and APPROVED by the user; contains white short facial stubble, white hair, gray armor, spear, shield. This exact image is for coding direction, **not** a license to use commercially protected film stills.
- `05_Visual_References/Marinka/Marinka_character_and_abilities.png`: generated original interpretation in our project conversation as the current art reference; no third-party source image used in this ZIP.
- `05_Visual_References/Arena/Ruined_Witch_Yard_architecture_ritual_basement.png`: generated concept for user, architecture+underground reference. PNG diagrams are original computer-generated implementation diagrams, no external images.
- PNGs are *references*, not actual game-ready texture atlases nor actual Blockbench source files. Codex must construct valid textured voxel models/assets according to specs; don't embed the whole PNG as one texture in world.

## Audio files
All 39 `06_Audio/**/*ogg` produced procedurally with the bundled Python+NumPy+SciPy DSP script and FFmpeg encoding; **no samples from films, games or other artists; no voice recordings**. They are playable Ogg Vorbis mono 44.1kHz nonverbal procedural effects. They are prototypical synthesized game sounds, not professional human Foley/acting. `06_Audio/manifest.json` includes file path, length, SHA-256, codec and origin.

## Rights
The project's user may distribute sound files created as original output of this package in the context of the game/mod subject to ordinary platform terms and local legal assessment; we are not guaranteeing freedom from all possible legal claims. Do NOT take voice lines from the copyrighted animated films about the bogatyrs or imitate/synthesize a recognizable actor's voice. If later human acting is licensed and recorded, those lines may replace supplemental nonverbal effects in the same sound event map.

## How to integrate
Copy each `06_Audio/<group>/<file>.ogg` into `assets/<modid>/sounds/<group>/<file>.ogg`; register event in `sounds.json` with namespace and relative no-extension path; handle collisions, attenuation, pitch variance and subtitle. `ffprobe` should report codec_name=vorbis, sample_rate=44100, channels=1. Validate missing files programmatically. Ambient files should not start simultaneously on every mob tick.

## Rebuilding from sources
See `08_Legal_and_Resources/generate_audio_and_diagrams.py` (uses NumPy+SciPy+Pillow and ffmpeg executable), recreated from this package; defaults to the project root. Its reproducibility is *approximate* for procedural sounds/diagrams, source accepted concept art must be preserved. The generation script is provided for licensing traceability, not a requirement for the game's users at runtime.
