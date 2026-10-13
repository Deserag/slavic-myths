# Gate-based acceptance matrix — THIS IS NOT OPTIONAL

Report PASS/FAIL/BLOCKED/NOT_RUN per case with evidence file/screenshot/log. A compilation pass alone is not game test. Automated harness integration encouraged, manually confirm visual cases.

## Book and GUI
| ID | Test | Expected |
|---|---|---|
| BK-01 | Fresh player joins brand new world | 1 book only, no duplicate on 2nd/3rd login |
| BK-02 | Full player inventory first join | safe one-time delivery/drop/pending, no dup/loss |
| BK-03 | Drop book in fire | not automatically reissued; progression persists |
| BK-04 | Die without keepInventory | no reissue, no progress reset |
| BK-05 | Die with keepInventory | no duplicate, original item persists |
| BK-06 | Move book chest, relog/dimension switch | no reissue, same catalog progress |
| BK-07 | Obtain second copy through creative | identical player-owned progress, not NBT-only |
| BK-08 | Open in singleplayer/multiplayer | all functional buttons, no GUI freezes |
| BK-09 | Each 8 book categories / 3 nested levels | correct navigation, back, forward, scroll |
| BK-10 | Russian at 1366×768 scale2,3,4 | no overlap, broken Cyrillic or clipped text |
| BK-11 | Search region for actual known anchor | actual target inside circle, no exposed XYZ |
| BK-12 | Invalid/moved target | stale marker/clear explanation, no fake location |
| BK-13 | Open early boss rumor then defeat | correct in-game stages, verified only earned |
| BK-14 | All current entity IDs enumerated | coverage manifest links each to book entry or explicit approved family grouping |
| BK-15 | Cross-client: player A discovers, B does not | per-player content separate |
| BK-16 | Missing client book textures/reload assets | recovery or useful error, no crash |

## Bayun
| ID | Test | Expected |
|---|---|---|
| BA-01 | Find natural Bayun area on generated seed | platform grounded, 2 exits, no clipping |
| BA-02 | Reload chunk / relog / restart | same persistent entity and UUID; no new spawn |
| BA-03 | Attempt sword/fire/lava/drown/explosion | Bayun lives, never natural drops |
| BA-04 | First notice from 12 blocks | genuine 8s grace, audible/visual prompt |
| BA-05 | Milk bucket gift before 8s | 1 bucket converted to empty, danger canceled |
| BA-06 | 1 raw fish gift before 8s | 1 fish consumed, proper chew animation |
| BA-07 | Wrong gift spam/double click | no item deletion, no extra trust |
| BA-08 | No gift, escape by >15 before 3.5s | no hypnosis, movement remains normal |
| BA-09 | No gift stay in radius | warning → song → charm → physical pounce kill |
| BA-10 | Run behind solid wall | attack canceled, no through-wall death |
| BA-11 | Teleport/dimension/disconnect mid charm | movement/controller immediately restored |
| BA-12 | Creative/spectator approach | no fatal capture |
| BA-13 | Player dies via another source during charm | zero stuck movement effect |
| BA-14 | Milk/fish gift cooldown rejoin | cannot grind bond 10× in 10sec |
| BA-15 | Pet with empty hand and right state | rub cheek animation and capped trust |
| BA-16 | Player B hasn't gifted, A has | A safe, B individually warned |
| BA-17 | Verify all 16+ clips played | actual joint motion, not root-only posing |
| BA-18 | Verify 19+ sound events in game | actual OGG played, spatial attenuation and subtitles |
| BA-19 | Client GUI and server logs under stress | no 100ms+ AI tick spikes, no memory leak |
| BA-20 | Spawned anchor unloaded for hours | no forced-chunk ticket, same anchor on return |
| BA-21 | Gift during warn | valid action cancels warning before fatal state if within reach |
| BA-22 | Physical leap misses because player dodged/cover | NO remote auto-kill |

## Achievements / Yaga / packaging
| ID | Test | Expected |
|---|---|---|
| AD-01 | Existing save with completed advancements | old progress preserved, book shows completed |
| AD-02 | Real SlavicMyths event triggers new advancement | toast + book update, no unearned XP |
| AD-03 | Noncyclic advancement graph | valid parent IDs, hidden nodes handled |
| AD-04 | Book achievements fit GUI at scale 4 | no collision/overflow |
| YG-01 | Existing Baba Yaga quests and rewards | identical before/after behavior, not reset |
| YG-02 | Interact Bayun and Yaga in same world | NO cross-quest completion or reward in this patch |
| RU-01 | Validate ru_ru and en_us keys | parse UTF-8, matching new keys, zero mojibake |
| RU-02 | GUI text on real screenshots | Russian displayed correctly (not English) |
| RE-01 | `gradlew.bat clean build` | successful production build, actual jar |
| RE-02 | JAR content extraction | textures/models/sounds/sounds.json/advancements/locales present |
| RE-03 | Existing 1.21.1 PolyMC staging | only when permitted, old 1.16.5 untouched |

## Deliverables to source repository
`docs/verification/bayun-book-final/AUDIT.md`, `CONTENT_COVERAGE.csv`, `ADVANCEMENTS_BEFORE_AFTER.csv`, `TEST_RESULTS.md`, `ASSET_MANIFEST.md`, `RUSSIAN_QA.md` (screenshots location), `PERFORMANCE.md` with actual measurements or NOT_MEASURED, changelog, build path to JAR and list of exact modified files. No fake PASS without testing.
