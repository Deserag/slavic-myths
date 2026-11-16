# Issue #507 — sails and rigging verification (2026-11-16)

Source: `origin/fix/ships-3-4-wip` at `59f470678012aa83fa19d0d49d24a7bd4999da26`.
Working branch: `codex/ships-3-4-sails-507-20261116`.
Issue remains OPEN. Owner acceptance has not been granted.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| III/IV sail tiers and mixed mast upgrades | `ShipSailRigProof`: 165 configurations, 1,307 surfaces, 6,035 physical anchors; GameTests include three-mast III and four-mast IV mixed-tier voyages | PASS (automated geometry and server behaviour) |
| Pinned cloth edges and three fill profiles | `ShipSailRigProof` and isolated client frames, with pressure forced by the test-only capture mod | PASS for geometry and visual preview; actual wind response NOT_RUN |
| Production build | Java 21 `gradlew.bat clean build`: successful, 6 tasks executed; production JAR SHA256 `5D8CE62DA54EBCE05E16498B99F2CE737E56051A201A07BE9B22DB228648B65B` | PASS at source commit `5b3927ef1` |
| Ship IV geometry | `verifyShipIVGeometry`: 576 configurations | PASS |
| Ship movement and save regression | `runOcean26Checks`: 33 GameTests, including III and IV mixed-tier voyage, falling crew collision on both moving decks and moving-palette NBT roundtrip; full suite exited 0 | PASS for tested server paths; real relog NOT_RUN |
| Packaged client render, all III/IV tiers | [Isolated GitHub client run](https://github.com/Deserag/slavic-myths/actions/runs/43906105617): 54 real framebuffer captures; seed `5072026`; packaged JAR SHA256 `1ac82e014738467e0b4e7d21a74bce64a29cbe45b60b7c59abf957528129d233` | PASS for stationary rendering only |
| Deck and full-view screenshot matrix | [Final 72-frame run](https://github.com/Deserag/slavic-myths/actions/runs/43913338217): six class/tier combinations × bow, starboard, stern, deck × weak/medium/strong preview. Inspected III and IV deck frames; seed `5072026`, packaged JAR SHA256 `1ac82e014738467e0b4e7d21a74bce64a29cbe45b60b7c59abf957528129d233` | PASS for stationary client rendering |
| Falling crew on III/IV moving deck | Direct `ServerPlayer.move` against detached moving hull in GameTests | PASS (server collision) |
| Player jumps, passage, mast/rope interaction, moving client render and relog | No isolated client evidence for these actions yet | NOT_RUN |
| Owner acceptance | Owner has not accepted the change | BLOCKED |
| Owner-approved original art in Issue | Issue #507 contains technical references but no reachable owner-approved original art | BLOCKED |

The screenshot fixture uses a spectator camera and overrides sail pressure on the client for the three visual profiles. It does not prove real wind, steering, player collision, save/relog, or multiplayer behaviour. GameTests exercise server movement and save paths separately.

No PolyMC installation was performed. No client was launched on the owner's desktop.
