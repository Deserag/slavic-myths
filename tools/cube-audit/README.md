# Opt-in native cube audit

Minecraft1.21.1 /NeoForge21.1.255 /Java21. Build main first. Tools/reference fixtures are never production sourceSets by default; `-I tools/cube-audit/init.gradle` enables the extra sourceSet only for this run.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build compileCubeAuditJava -I tools/cube-audit/init.gradle
python tools/cube-audit/gates.py
./tools/cube-audit/run.ps1 -Phase before
# Wait for the client to finish and >=15min from launch; apply only the reviewed cube patch.
./tools/cube-audit/run.ps1 -Phase after
python tools/cube-audit/summarize.py
& "$env:JAVA_HOME/bin/java.exe" tools/cube-audit/ProfileSummary.java docs/implementation/cube-allocation/after/profile.jfr
& "$env:JAVA_HOME/bin/java.exe" tools/cube-audit/ProfileSummary.java docs/implementation/cube-allocation/after/micro.jfr
```

Run the baseline on main and the changed version on the feature branch using the same probe/reference sources. `before` is a label, not an automatic code checkout. The launcher checks a shared timestamp and other Minecraft processes, creates a disposable test game directory and bypasses accessibility onboarding in that directory. It never edits PolyMC or user saves. Do not run a resource gate concurrently with clean: the open JAR is locked on Windows.

Native client-only render staging: one LOCKDOWN FireGuardian, one FLOOD SpringKeeper, one WHIRL SpringKeeper, one WaterPrison and one WaterEcho; the real production renderers run against fixed state/age. No claim of native guardian AI, ritual, damage or multiplayer acceptance. World seed330015, flat ground,1280×720, rd8/sim5, VSync off/uncapped,2–4GiB heap. All code paths/geometry remain production.

VertexAudit compares1024 cases,24 vertices each, using the frozen baseline implementation and native VertexConsumer default pose/normal transformations. It checks float bits and all emitted attributes including order, UV, color, overlay/light and normals under identity and translated/rotated/nonuniform negative-scale poses. The Sink consumes values, not GL. This is separate from native screenshots/JFR.

Micro measurements use ThreadMXBean allocated bytes,100000 warmup calls to each function and six alternating250000-call batches each. These batches are **not independent JVM repeats**; class loading/JIT and sink behavior limit extrapolation. The scene recording is restarted after the micro record, so the frozen reference is not part of scene rendering.

The published comparison takes ages2401–3900 on both sides:120s scene warmup +75s sample, retaining all earlier raw data. Earlier before data overlapped fixture compilation. One JVM per side cannot establish a tight statistical noise band or prove a small FPS improvement. Screenshot differences due to texture animation, partial ticks, client clock/HUD are not pixel equivalence; vertex attributes are tested strictly.

## Historical crossover reproduction

The shipped implementation is the small helper already exercised by `after` and the helper blocks of `crossover`. The constant candidate is diagnostic history, not production. To reproduce the historical crossover, start a separate disposable checkout of main@fb2994e9, copy these audit tools, and apply `constant-candidate.patch` there (the patch is against main, not against this helper branch). Run `run.ps1 -Phase crossover`; modes0/1/2 select frozen reference/frozen helper/the then-production constant candidate. Do not run crossover directly on this final helper and label mode2 as constant. Compile-only validation does not launch Minecraft. Respect the shared15-minute guard and all external Minecraft processes.

Crossover block1 is invalid after an integrated pause/minimized window and is excluded entirely by crossover_summary.py. Block0 is retained but not a post-resume control. Blocks2–5 are short sequential controls, not independent repeats. Original before/after probes did not log continuous visibility/camera, so the independent FPS drop is unresolved and no FPS improvement/no-regression guarantee is claimed. See REPORT.md. The reference/helper equivalence tests use default native transforms, while scene screenshots exercise actual GL renderers.
