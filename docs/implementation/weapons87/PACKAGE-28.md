# #87 пакет 28 — #115/163 Crossbows TP stock

22.10.2026 source change after owner's actual Minecraft feedback. Working main base 64e89143e2d76d3e492bd1b5fa00462011ed718f, owner acceptance still OPEN.

**BEFORE / ACTUAL / AFTER:** Owner screenshot FAIL: crossbow stock/huge lever upright through chest. P14 centered grip but X=-90 rotated actual Z stock vertically. Now 12 model states (2 IDs×default/pull0,pull1,pull2/arrow/firework), both TP hands rotation X=0, anchored physical grip [8,5.5,12.5] translation [0,1.375,-2.475], scale .55. Geometry/UV/PNG/FP/GUI and weapon damage untouched.

**What to test in Minecraft:** Observe spectator front/side back, first-person and third-person, Steve/Alex, empty/charging/full arrow/firework. Confirm horizontal stock in hand and no chest intersection; owner game screenshot is evidence of BEFORE failure, AFTER runtime NOT_RUN.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Exact baseline and before-production plan | [PLAN.md](owner-qa-20261022/PLAN.md), original 12 JSON transforms in [before-12-crossbow-transforms.json](owner-qa-20261022/before-12-crossbow-transforms.json), actual changed sources in Git history | PASS source snapshot |
| Source regression | tools/weapons87_owner_qa.py, tools/verify_weapons87_crossbows.py, Java source guards | PASS on source build gate pending |
| Java21 clean build / JAR | .github/workflows/weapons87-owner-qa-java21.yml | PENDING CI |
| Same-scale true JSON/UV source before and after | CI source PNG sheet; not a Minecraft screenshot | PENDING CI |
| Actual 1P/3P, Alex/Steve, hit timing, sounds, rain/survival multiplayer | owner game retest not performed by assistant | NOT_RUN / OWNER_PENDING |

Do not close child issue #115 or master #87 without explicit owner sign-off. No PolyMC installation, no C/D/#190/#191 changes.
