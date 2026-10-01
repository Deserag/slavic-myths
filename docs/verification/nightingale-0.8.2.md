# Verification 0.8.2 — 2026-10-01

- JDK 8u504, gradlew.bat --offline clean build: PASS (including reobfJar).
- verify_resources.py: PASS, 285 items / 122 blocks / 253 PNG /
  188 recipes / 76 visible advancements; packaged resources equal disk; class major 52.
- verify_stronghold_081.py: PASS, 23 templates, 26 defenders, two Atamans,
  five zones, furniture/loot/reference integrity and reproducibility.
- verify_nightingale_082.py: PASS, generator byte reproducibility,
  boss clearance and platform exit, loot probability, fragile tag, models, sound wiring.
- JDK8 WindGeometryTest: PASS, cone/radial bounds, forward/reverse/corner cover
  and 360 directions through open/enclosed space.
- Mono OGG decoded, finite samples and no clipping: PASS via common verifier.
- PolyMC: exact SHA256 and one JAR; install-0.8.2.json.
- git diff --check: PASS.

No Minecraft bootstrap, dev client, dedicated server or gameplay execution.
No listening/rendering QA, live save/rejoin test, multiplayer or MSPT measurement.
Runtime results remain TODO in CHECKLIST_0.8.2.md.
