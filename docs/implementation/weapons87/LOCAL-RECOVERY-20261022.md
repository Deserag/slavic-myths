# Local recovery audit — 2026-10-22

Scope inferred from latest active branch: P49–P52; not four completed Issues.
Source: 04158550, work/weapons87-continuous-20261022. main remains 61f604e2.
No commits, pushes, merges, issue closures or owner acceptance performed.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Synchronize local main | git fetch + pull --ff-only, clean initial tree, 61f604e2 | PASS |
| Recover latest development | remote continuous branch and cumulative worklog P49–P52 | PASS |
| Java21 clean build | build-audit-latest.log, BUILD SUCCESSFUL in 2m12s; test NO-SOURCE | PASS |
| P49–P52 source/JAR checks | four verify scripts --jar | PASS |
| Active catalog/resources in production JAR | weapons87_live_audit.py --jar: 68 IDs,302 resources | PASS |
| Actual production Finist steering | finist-latest-native.log; FinistChecks now directly invokes FinistArrow.steer for 3 axes and 10 angles, speed/turn/progress | PASS |
| Trophy animation fits block cells | heads-latest-native.log; actual native vertices: Tugarin minY=-0.06599287, Volkolak=-0.0022304133, Podzemnik=-0.064302765 | FAIL |
| Historical audit manifests | live audit: 76 old audit SHA differences,8 flail differences,0 missing | FAIL |
| P49–P52 full gameplay/visual acceptance | no game/client/server/GameTest/MP launch | NOT_RUN |
| PolyMC installation | incomplete chain and native geometry FAIL | BLOCKED / POLYMC_NOT_INSTALLED |

Built build/libs/slavicmyths-1.9.7.56.jar SHA256 bd869161bca4899ba366daf967bcdaeca540506bcea54392f880e5b4f8115815.
This is not an installed or accepted release. Offline bootstrap is not a game launch.

Next: fix trophy animated envelope with actual before/after source render, preserving strong owner-requested animation and existing neutral collision/art; then refresh obsolete FX timing fixtures only against current approved timing and rerun native checks. Do not weaken geometric assertions. P49–P52 remain IN_PROGRESS. Existing accepted crossbows/scythe/Morozko art must remain untouched.


Historical intermediate report; superseded for #140 by docs/implementation/trophy140-rework/REPORT.md (full six-head changes and installed test candidate 1.9.7.57).
