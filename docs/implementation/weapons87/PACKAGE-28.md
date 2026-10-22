# #87 пакет 28 — #115/163 Crossbows TP stock

22.10.2026 source change after owner's actual Minecraft feedback. Working main base 64e89143e2d76d3e492bd1b5fa00462011ed718f, owner acceptance still OPEN.

**BEFORE / ACTUAL / AFTER:** Owner screenshot FAIL: crossbow stock/huge lever upright through chest. P14 centered grip but X=-90 rotated actual Z stock vertically. Now 12 model states (2 IDs×default/pull0,pull1,pull2/arrow/firework), both TP hands rotation X=0, anchored physical grip [8,5.5,12.5] translation [0,1.375,-2.475], scale .55. Geometry/UV/PNG/FP/GUI and weapon damage untouched.

**What to test in Minecraft:** Observe spectator front/side back, first-person and third-person, Steve/Alex, empty/charging/full arrow/firework. Confirm horizontal stock in hand and no chest intersection; owner game screenshot is evidence of BEFORE failure, AFTER runtime NOT_RUN.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Exact baseline and before-production plan | [PLAN.md](owner-qa-20261022/PLAN.md), original 12 JSON transforms in [before-12-crossbow-transforms.json](owner-qa-20261022/before-12-crossbow-transforms.json), actual changed sources in Git history | PASS source snapshot |
| Source regression | tools/weapons87_owner_qa.py, tools/verify_weapons87_crossbows.py, Java source guards | PASS on source build gate pending |
| Java21 clean build / JAR | .github/workflows/weapons87-owner-qa-java21.yml | PASS scoped Java21 CI #39770615398 |
| Same-scale true JSON/UV source before and after | CI source PNG sheet; not a Minecraft screenshot | PASS scoped Java21 CI #39770615398 |
| Actual 1P/3P, Alex/Steve, hit timing, sounds, rain/survival multiplayer | owner game retest not performed by assistant | NOT_RUN / OWNER_PENDING |

Do not close child issue #115 or master #87 without explicit owner sign-off. No PolyMC installation, no C/D/#190/#191 changes.

## Финальная публикация и CI 22.10.2026
- Production [PR #200](https://github.com/Deserag/slavic-myths/pull/200) merged into main `ac23497c1f745817c88faa48e4fdeab961722152` (source QA head `1f418cedd782eb290775db597b81a7b2c3353ea6`).
- [Java21 CI 39770615398](https://github.com/Deserag/slavic-myths/actions/runs/39770615398) **PASS**: clean build, all12 crossbow real stages /48 hand-pivot tests, 12 referenced textures, scoped gameplay-source guards, source UV A/B actual JSON PNG; production JAR bytes match all 12 model files.
- JAR: `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 `21b704ed7fddcbddac4e1bf161ae9e64b26193afc601ad48fa5224b0da87074b`. Minecraft/PolyMC CLIENT/SERVER/MP retest **NOT_RUN**; user supplied screenshot is **prior version FAIL**, not after proof. All Issues remain OPEN for owner acceptance.
