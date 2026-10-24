# Opt-in performance2.0 audit

Current owner instruction (2026-10-17): DO NOT LAUNCH MINECRAFT. Offline analysis/publication only until explicitly authorized again. Recorded launch switches below describe reproduction, not current authorization.

Never auto-launch from build tasks. Java21 /NeoForge21.1.255. Build and gate main before any client. Audit sourceSets are opt-in and excluded from production JAR.

On 2026-10-17 the user explicitly waived launch intervals for the remaining audit. Pass -SkipInterval only under that authorization; it records launch-policy.json and bypasses only the time gate. Concurrent Minecraft/evidence-overwrite checks and scene warmups remain in force. Previous runs followed the original 15-minute rule.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build compilePerf2CubeJava -I tools/performance-2.0/cube/init.gradle
python tools/performance-2.0/gates.py
python tools/check_event_subscribers.py build/libs/slavicmyths-1.9.7.5.jar
./tools/performance-2.0/run.ps1 -Label cube-A2 -Suite cube
# B uses the same audit sources in a checkout of actual PR37 production revision.
# Pass -EvidenceRoot <absolute report evidence folder> from that checkout.
# Reproduction only after new authorization. Prior Minecraft must have exited;
# interval>=15.5min unless explicitly waived with -SkipInterval.
python tools/performance-2.0/cube_summary.py
```

The fixed renderer scene appears static on screen by design. Do not move the mouse/camera or switch focus during sampled windows. Focus/iconified/visible/pause/camera checks are mandatory; typing in another app invalidates those frames. Actual140FPS cap, not the requested rounded144. Report protocol specifies eligibility, tolerances and uniform boundary trim. One documented camera/focus interruption is allowed one bounded replacement, not endless retries.

Normal overworld test:

```powershell
./gradlew.bat compilePerf2WorldJava -I tools/performance-2.0/world/init.gradle
./tools/performance-2.0/run.ps1 -Label world-mod -Suite world
# Run after it closes and interval has elapsed:
./tools/performance-2.0/run.ps1 -Label world-control -Suite world -Control
python tools/performance-2.0/world_summary.py
```

Control removes SlavicMyths registration after project configuration, leaves NeoForge/Curios/JEI and the standalone perf2audit sourceSet. A runtime assertion checks actual ModList before creating a world. Verify exact runtime mod list in events; config-only verification is not a vanilla gameplay test. Both normal-world modes use identical common source; mod-only bridge is excluded from control compilation.

Native keyboard movement/attacks are automated protected workloads, not manual Survival progression. Normal HP enemies, explicit equipment/ability prerequisites. Transition locate/teleport setup stays outside sampled ages>600; count actual chunks/path and actual generated structures, never label synthetic placement as natural generation. Five cycles plus idle/reopen observe lifecycle, not proof of absence of all leaks. Bounded city locate may fail; mark NOT_EXERCISED rather than create a city acceptance claim.

GPU sampler is optional Windows3D-engine utilization plus process memory/CPU, not GPU frame time. Run only against the observed benchmark PID after PROCESS_ID appears; keep its overhead consistent in control/mod. Raw JFR metadata/system/socket addresses stay local. JfrReport.java and ProfileSummary.java stream summaries; compressed CSV/sanitised logs and hashes are publishable.

Realistic normal-world/control settings are1600x900,render distance12/simulation8,cap140,VSync off,2–4GiB heap. Cube protocol stays1280x720/8/5 for comparability. Active display modes queried1920x1080 and2560x1440;1600x900 fits either visible window. These separate scene settings are not cross-comparable absolute FPS baselines.

Bounded follow-ups after observed coverage gaps (not replacement main criteria):

```powershell
# Compile only while benchmark clients are closed; city/init derives the common observer.
./gradlew.bat compilePerf2CityJava -I tools/performance-2.0/city/init.gradle
# Copy the owned failed test-save to work/performance-2-city/saves/Perf2-LegacyCopy first.
./tools/performance-2.0/run.ps1 -Label world-city -Suite city -OpenWorld Perf2-LegacyCopy -SkipInterval
# Separate fresh flat world; native Gorynych/Karachun/Morozko workload, normal HP/attacks.
./tools/performance-2.0/run.ps1 -Label world-bosses-flat -Suite city -BossesOnly -SkipInterval
```

City radius4 search occurs once; a miss stays NOT_EXERCISED. Never create a showcase city and label it natural. Existing-save mode logs pre-fixture player state, then native encounter cleanup before city sampling. BossesOnly and OpenWorld are mutually exclusive. Original common world sources remain intact; generated follow-up classes/config are excluded from production JAR.

Offline: `python tools/performance-2.0/world_summary.py`, `tables.py`, `signals.py`, `plots.py`, then `package.py`. Java21 source-file readers JfrReport.java/HeapTimeline.java consume saved JFR without launching Minecraft. Summaries accept published frames.csv.gz; raw JFR stays local.
