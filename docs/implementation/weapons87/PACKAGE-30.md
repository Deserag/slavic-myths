# #87 пакет 30 — #160/184 Kalin sabre momentum

22.10.2026 source change after owner's actual Minecraft feedback. Working main base 64e89143e2d76d3e492bd1b5fa00462011ed718f, owner acceptance still OPEN.

**BEFORE / ACTUAL / AFTER:** Original 3 ordinary hostile kills in 30s silently armed another grounded non-sprint fully charged strike inside one-tick AttackEntityEvent window, then a 95°/4.5 radius sweep against up to3 hostile victims for 60% damage. Owner's 3 slime kills alone never showed a payoff. Now 3 actual accepted melee hits within 30s (kills not required), HUD Натиск воеводы 1/3,2/3, third hit immediately spends charges on front 95°/4.5 wave (max 3 additional ordinary hostile/non-allied, not player/boss, 60% damage) with gold arc, actionbar wave and existing sound. Other loot/gift code and base stats untouched.

**What to test in Minecraft:** Use 3 successive hits on 3 slimes; see 1/3,2/3, then wave + 0 targets when no others around. Put 3 more slimes 2–4 blocks ahead: wave damages up to3 extras; behind player/out of sight/team allies never hurt. Test 30s expiry, one-hit kills, weapon swap, multiplayer and boss exclusion. Runtime NOT_RUN.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Exact baseline and before-production plan | [PLAN.md](owner-qa-20261022/PLAN.md), original 12 JSON transforms in [before-12-crossbow-transforms.json](owner-qa-20261022/before-12-crossbow-transforms.json), actual changed sources in Git history | PASS source snapshot |
| Source regression | tools/weapons87_owner_qa.py, tools/verify_weapons87_crossbows.py, Java source guards | PASS on source build gate pending |
| Java21 clean build / JAR | .github/workflows/weapons87-owner-qa-java21.yml | PASS scoped Java21 CI #39770615398 |
| Same-scale true JSON/UV source before and after | CI source PNG sheet; not a Minecraft screenshot | PASS scoped Java21 CI #39770615398 |
| Actual 1P/3P, Alex/Steve, hit timing, sounds, rain/survival multiplayer | owner game retest not performed by assistant | NOT_RUN / OWNER_PENDING |

Do not close child issue #160 or master #87 without explicit owner sign-off. No PolyMC installation, no C/D/#190/#191 changes.

## Финальная публикация и CI 22.10.2026
- Production [PR #200](https://github.com/Deserag/slavic-myths/pull/200) merged into main `ac23497c1f745817c88faa48e4fdeab961722152` (source QA head `1f418cedd782eb290775db597b81a7b2c3353ea6`).
- [Java21 CI 39770615398](https://github.com/Deserag/slavic-myths/actions/runs/39770615398) **PASS**: clean build, all12 crossbow real stages /48 hand-pivot tests, 12 referenced textures, scoped gameplay-source guards, source UV A/B actual JSON PNG; production JAR bytes match all 12 model files.
- JAR: `build/libs/slavicmyths-1.9.7.56.jar`, SHA256 `21b704ed7fddcbddac4e1bf161ae9e64b26193afc601ad48fa5224b0da87074b`. Minecraft/PolyMC CLIENT/SERVER/MP retest **NOT_RUN**; user supplied screenshot is **prior version FAIL**, not after proof. All Issues remain OPEN for owner acceptance.
