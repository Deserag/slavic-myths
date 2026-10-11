# Ready to install ORIGINAL audio pack — source paths, triggers, restrictions

## Included physical files
`manifests/AUDIO_FILES.json` enumerates **48 actual `.ogg` audio objects**, no placeholders and no remote dependencies. They are produced expressly for this task (deterministic procedural foley + espeak Russian draft speech). `audio/ready/sfx/*` comprises unique footsteps, fabric rustling, stove climb, dish clinks, chewing, sleeping breath, snoring, sigh, annoyance, broom. `audio/ready/voice_draft/*` comprises 20 original Russian phrases (audio + synchronized text).

## Required sound registry mapping
Example mod IDs should follow discovered `slavicmyths` resources, do not hardcode a nonexistent namespace. Copy each `.ogg` to `assets/<actualmodid>/sounds/kuzya/<subdir>/<filename>.ogg`, register appropriate `sounds.json` events: `kuzya.step_wood` variants 01..03, `kuzya.cloth` 01..02, `kuzya.stove_climb` 01..02, `kuzya.bowl` 01..02, `kuzya.chew` 01..03, `kuzya.sleep_breath` 01..02, `kuzya.snore` 01..02, `kuzya.huff` 01..02, etc. Register selected voice lines as either per-line sound IDs or a sound group whose triggered variant index is ALSO sent as subtitle ID. Avoid sound event group randomization where subtitles mismatch speech.

## Mixing (concrete starting volumes)
- Footsteps 0.38 gain, audible ≤6 b; pitch varied 0.94–1.06.
- Stove / bowl/chew 0.55–0.70, audible ≤8 b.
- Sleep breath .15–.22, only ≤3.5 b, not room-wide roar.
- Sigh and huff .35–.50 ≤6 b, 35–70s cooldown in ambient context.
- Voices gain .8–.95, audible 10–14 b through normal attenuator, quiet indoors not dimension-wide; prevent overlapping instances.
- Game volume sliders respected. All files mono 22050 Hz OGG Vorbis and valid for Java sound engine.

## Frame precise events
Use `manifests/animation_events.csv` for sound-to-frame association. No nonverbal sound every server tick. Return to ambient idle after voice clip finishes; if player moves away, let voice fade rather than restarting.

## Quality disclosure & legal source requirement
The supplied speech is **synthesized and deliberately NOT identical to the TV cartoon actor**. The original film's soundtracks are not included because there is no demonstrated redistribution license. Russian copyright/rightsholder analysis: https://base.garant.ru/480276408/ . Do not scrape YouTube, rip audio, or claim film recordings are public domain. Optional user-supplied, separately licensed replacement pack may override ID→OGG mapping with same durations/subtitle manifest; ship default with the 20 ORIGINAL dubs. If professional voice quality is needed for release, replace drafts with newly performed **licensed** voice work, not silent placeholders. Do not treat text in cartoon source as free dialogue.
