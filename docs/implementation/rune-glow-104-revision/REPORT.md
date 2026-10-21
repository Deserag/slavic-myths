> Subsequent owner-authorized installation: version1.9.7.45 installed after stable clean build and JAR gates. See ../polymc-19745/REPORT.md and installed.json. Prior packaging block below is historical. Runtime/owner acceptance still NOT_RUN.

# #104 revision · 20.10.2026

IN_PROGRESS / OWNER_PENDING / OPEN. #105 NOT_STARTED.

Production changes: only shared rune_glint PNG, fragment/vertex shaders, RuneGlowStyle; generator and plain-JVM expectation updated. Armor geometry/textures/hooks, mechanics and IDs were not edited by this task. Existing uncommitted work preserved. No commit/push/install/issue changes.

Baseline hashes and exact files: baseline.json and before/. Two supplied GAME screenshots copied as GAME-owner-rejected-1/2.png; rejected build version is owner-reported, not independently verified from pixels. Local baseline differs from published .42 (alpha112/156/200 and sharpened strokes).

Diagnosis: old generator explicitly creates repeating diagonal distance bands, shader smoothstep hardens their edges, local alpha112/156/200 raises contrast. These are source-confirmed contributors, not a measured decomposition of the screenshots. UV repeats model-local projections; joints/custom multipass behavior still needs runtime review. Fixed buffer/armor depth guards preserved.

New seamless128px authored interference texture has no dominant broad diagonal carrier; continuous squared density instead of threshold, muted green/mint/turquoise; alpha24/40/58 with adapted palette thresholds. UV scale2.6; GameTime drift12/7 instead of48/24 (24000ticks/day implies x period100s, y171s at20TPS). Tint remains per-stack. No event wave.

OFFLINE candidates: CANDIDATE-weak.png, CANDIDATE-moderate.png (current production), CANDIDATE-upper.png; same four source meshes, camera and lighting at0/1/2/3. Weak/upper scale density red channel .75/1.2 before shader; reproducible via tools/rune104_revision_shader.py --preview --variant=weak|moderate|upper. BEFORE.png uses saved baseline PNG/GLSL/strengths on the same geometry. These are standalone GPU renders, NOT GAME captures. Current offline meshes use historical exported armor geometry; they do not validate current parallel armor changes. Shader compile/link PASS, monotonic RGB differences PASS. Visual inspection: no broad straps; very subtle differences, in-game readability remains unresolved. No actual vanilla-glint game comparison available.

Java21 clean build succeeded in1m8s. Plain JVM style/batch/UV/target ABI passed (jvm-check.log). Subsequently build/libs disappeared and additional DragonArmor/PeruniteArmor modifications appeared without this task writing them: concurrent writer/build activity. Resource verifier failed because expected production JAR was absent. Do not treat earlier build as current immutable artifact evidence. No new build started after detecting collision.

| REQUIREMENT | ITEM ID | rune count | viewing context | SOURCE BEFORE | SOURCE AFTER | GAME OWNER CAPTURE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|---|---|---|---|---|
| Soft pattern, three candidates | iron_sword/perunite_sword/iron_chestplate/perunite_chestplate |0–3|OFFLINE same camera day/shade|BEFORE.png|CANDIDATE-*.png|only rejected supplied captures|PASS offline only |
| Shader compile/link | shared |1–3|standalone OpenGL|baseline.json|shader-compile.json|absent|PASS |
| Zero, batch isolation, UV/ABI | shared |0–3|plain JVM|saved source|jvm-check.log|absent|PASS offline only |
| Java21 clean build | shared |all|build|local dirty baseline|successful command1m8s|n/a|PASS at time of build |
| Current resources / production JAR | all |all|packaging|baseline.json|resource-check.log: missing JAR|n/a|BLOCKED concurrent writer/build |
| Textile, actual current multilayer armor, front/back/side, joints | equipped slots |0–3|GAME|owner rejected captures|absent|absent|NOT_RUN |
| Vanilla glint visual reference, coexistence, GUI/hands/JEI/ground/frame | all |0–3|GAME day/night|absent|absent|absent|NOT_RUN |
| Insert/remove, legacy, MP/relog/dimension/Fabulous | all |0–3|GAME|absent|absent|absent|NOT_RUN |
| Performance | all |all|GAME|absent|absent|absent|NOT_RUN |
| PolyMC installation | all |all|installed artifact|prior installation unchanged by this task|POLYMC_NOT_INSTALLED|absent|NOT_RUN |
| Owner visual acceptance | all |0–3|GAME|rejected old effect|pending|absent|NOT_RUN |

Remaining: serialize writers; rebuild stable snapshot, verify resources and production JAR hashes; current armor/textile source coverage; real vanilla reference and all owner runtime views. Any installation requires separate instruction. Minecraft/PolyMC/GameTest/server launches0. #104 remains OPEN; #105 waits for owner acceptance.
