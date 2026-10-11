# CODEX / IMPLEMENT EXACTLY — Slavic Myths 1.6.4

**This ZIP is the complete implementation specification. Do not rewrite it as another short prompt. Start coding.**

1. Inventory actual `main` branch first: MC/NeoForge version, Java toolchain, Baba Yaga entity, generated chicken-leg hut, structure template(s), command registration, Domovoy routines, current NPC anim/render and audio systems. Report exact repository paths/IDs discovered. Do not assume namespaces, class names or APIs from examples in documents.
2. Use `references/APPROVED_01_KUZYA_MODEL.png` as **authoritative** character design. `SOURCE_03_USER_CARTOON_SCREENSHOT.png` confirms dark face/hair puppet-like silhouette. Discard old blonde designs. See `01`, `02`, `03`.
3. Preserve existing worldgen and Baba Yaga. Modify or version its existing structure/interior and place exactly one Kuzya bound to that particular hut. See `10`, `11`, `12`, `13`, `16`.
4. Build the dedicated model, UV, 3D hair and facial rig. See `02`, `03` and palettes/crops. NO vanilla baby villager/domovoy reskin.
5. Implement locomotion/stove navigation and actions with explicit keyframes, visual phases and audio frame events. See `04` through `09`, `manifests/animation_*`.
6. Implement genuine sound playback from `audio/ready/*`. Copy relevant OGGs into resource paths at correct IDs; register `sounds.json` based on `manifests/AUDIO_FILES.json`. Draft Russian voice files are included; **do not falsely describe them as archival cartoon actors**. See `14`, `15`.
7. Implement no-damage contract, stay timers per player, dialogue/voice cooldown, hut ownership/anchor, local activity loops, multiplayer state, and unload/reload. See `12`, `16`, `17`, `18`.
8. Rework the hut interior with Russian stove/rest ledge, real physical access, food table, Baba Yaga alchemy station, Kuzya corner, storage barrel, clutter. No overlapping collisions. See `10`/`11`.
9. Check existing hut command. Reuse if present; otherwise add one test/admin command. **In final report print EXACT working command**, arguments, permission level, sample coordinates, and commands used for observing/retesting. See `19`.
10. Run Gradle build + resource validation + game/client tests + dedicated-server tests + screenshot/video checklist. Place deliverable `.jar` where user's existing launch workflow expects (PolyMC/instance mods only when path exists and target instance is identified; don't fabricate paths). See `20` and `21`.

### Quality gates
- Appearance: approved **dark puppet Kuzya**, ~0.64 block tall; no blonde hair, no bright-beige skin, no cute mascot.
- Animations: every clip has distinct body mechanics, phased anticipation/action/recovery, smooth transitions, synchronized 3D transforms and sound events; no hand waving accomplished by moving entity coordinates, no teleport between stove and table.
- Voice: **ogg files already exist**; real audible lines and foley play, correctly localized, with cooldowns and no chorus on servers.
- Security: player cannot damage, shove or leash Kuzya; Creative/admin `/kill` may remain privileged administrative tool. No interactions break Baba Yaga.
- Structure: existing outer shape and chicken legs remain intact; no unnecessary new worldgen variant.
- Report existing command or give implemented one, not a guessed string.

IMPORTANT: current design is NOT a license to distribute clips from Soviet animation. Source all media in this package from `references` and `audio`, do not scrape original film audio.
