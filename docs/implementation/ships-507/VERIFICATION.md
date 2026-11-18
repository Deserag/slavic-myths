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
| Moving IV client turn, idle and restart | [Isolated client run](https://github.com/Deserag/slavic-myths/actions/runs/44037687715) at `5da13b9e`: actual client W/D, server speeds 0.372 / 0 / 0.709 blocks/tick, three moving frames, seed `5072026`, packaged JAR SHA256 `1ac82e014738467e0b4e7d21a74bce64a29cbe45b60b7c59abf957528129d233` | PASS for this IV route |
| III/IV moving-deck jump, main passage and mast collision | Local headless 33-GameTest suite with both hulls' falling crew, upward jump, landing and lateral passage, plus physical mast collision on upgraded hulls | PASS headless; real player client NOT_RUN |
| Damaged III/IV hull regression | Local `-PshipIVDamageCheck runOcean26Checks`: five tests | PASS |
| III/IV saved world restart | Local `-PshipPersistenceStage=write` followed by `read`: two tests each stage, actual server process restart | PASS headless; client relog NOT_RUN |
| Rope interaction, real client relog and multiplayer | No isolated client evidence for these actions yet; rope collision/climbing is outside #507 scope | NOT_RUN |
| Owner acceptance | Owner has not accepted the change | BLOCKED |
| Owner-approved original art in Issue | An original six-configuration concept is proposed at `docs/art/ships-507-proposed-original.png`. It is not a source render of the Java model and has not been accepted by the owner | BLOCKED |

The screenshot fixture uses a spectator camera and overrides sail pressure on the client for the three stationary visual profiles. The moving IV portion uses the actual client helm keys. It does not prove real wind, player collision, client relog or multiplayer behaviour. GameTests exercise server movement and save paths separately. A local two-stage headless world write/restart/read check passed for III and IV; this is a real server world restart, not a client relog.

The previous isolated moving-client FAIL at [run 43930729750](https://github.com/Deserag/slavic-myths/actions/runs/43930729750) came from the fixture's bounded water basin and simultaneous direct server helm commands competing with the normal client `HELM_IDLE` packets. In the corrected isolated run the ship traveled beyond the old basin boundary; the final fixture uses actual client keys and checks the server speed at turn, idle and restart. Production ship code was not changed in this continuation.

No PolyMC installation was performed. No client was launched on the owner's desktop.
