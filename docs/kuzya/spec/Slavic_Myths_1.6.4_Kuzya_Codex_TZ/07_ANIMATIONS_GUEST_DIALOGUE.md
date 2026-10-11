# Guest attention, annoyed acting, speech and shooing

## `notice_guest` 1.15s (not immediately on entry)
0 current activity continues, eyes glance towards player by 12°; .30 lift head 6°, eyebrows asymmetrical ±4°; .55 rotate torso ~8°; .82 hold suspicious gaze .15s, eyelid droop; 1.15 resume previous action. For player below/above, head yaw target eased over 0.25–0.50s, not hard snap.

## `mutter_small` 1.6s
0/0.28 head down 8°, arms folded closer; .45–.88 jaw opens/closes aligned to short voice, slight shoulder bounce; 1.20 head looks away; 1.60 neutral. Can play if player <8 blocks but should not interrupt climbing.

## `grumble_medium` 2.2s
0–.3 stop (only if standing safely); .30 head angles ~−8° pitch, brows negative; .55 left hand rises chest 20°, right hand reaches 28° outward; 0.75–1.65 new line plays with two discrete jaw syllable pulses, body 8° rock, right hand minor flick; 1.65–2.2 lower hands and turn away. Use `kuzya.voice_ask_leave_01/02` with 45s per-listener timeout.

## `grumble_large` 2.45s
0.0–.35 head turns to guest (≤38°), .35–.60 dramatic left foot half-step 0.1b, shoulders lift +9°, .60–1.15 right arm extends toward door 45–65°, fingers open; 1.15–1.65 second palm sweeping toward door, head tilt +10°, jaw speech; 1.65–2.05 sharp dismissive hand flick; 2.05–2.45 sigh and arms fall, eyes roll upward. Must feel small but fed-up, never physical shove or attack.

## `shoo_guest` 1.8s
0.0 arms folded; .28 right arm up 45° toward doorway, .52 palm outward, .70–1.1 two short waves ±12° wrist, 1.35 torso rotates away 12°, 1.8 return. No hostile AI state, damage/knockback or navigation forcing player to exit.

## `respond_right_click` 1.45s
During normal idle: attention face yaw to player, .30 eyebrows move, voice plays once at 0.36, mouth follows voice duration length (1.4s nominal clip can be extended to actual audio end, upper body overlay), then return. During `sleep`: instead play `wake_up` short then response; during `eat`: finish next bite cycle first; while climbing or falling: queue a response after reaching a safe anchor, cooldown 6s.

## Voice facial sync
- Speech start event client sound at server-authoritative timestamp, jaw movement uses amplitude envelope from resource (or authored vowel pulse curve) with ±60ms tolerance; blink at consonant pauses.
- Mouth pose ~0.2–0.7px vertical range; brows and cheek move subtly. Never wave hands continuously during whole audio clip; one or two expressive accents max.
- Each new ambient grumble line has cooldown 45–90s, never >1 audio line simultaneously per Kuzya; 3D sound max audible 10–14 blocks and must not be audible through entire underground map.
