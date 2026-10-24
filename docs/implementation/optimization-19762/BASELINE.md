# Baseline #235

Base main/origin/main: 1bed9897edb7497e4cf1d77b1a0c8baf8a297700; fetched24.10.2026, no new remote commits. Branch codex/performance-20261024 preserves earlier uncommitted work. Original files additionally backed up in work/three-stage-backup/manifest.json. Stack Minecraft1.21.1 NeoForge21.1.255 Java21.0.12. Installed1.9.7.62 receipt unchanged.

Owner authorized all three implementation stages and disposable test worlds on24.10.2026. No PolyMC/user-world overwrite. Game baselines and final comparisons scheduled after implementation; no game performance claim from offline fixtures. Commands: gradlew.bat clean build -I tools/book-offline.gradle checkBookOffline; python tools/verify_resources.py. Set JAVA_HOME only for each shell process. Baseline screenshots must be captured from actual old1.9.7.62 renderer at matching resolution/language/gui scale, not a mockup.

Old #33 GuardianClient.cube directly emits24vertices without per-face arrays in current main; preserved. #28 historical audit is not rerun as a replacement for this work. Existing open PRs32/39 contain historic perf audits; #211 crossbow, #94 bogatyr, #40 advancement unrelated and not overwritten.
