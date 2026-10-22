# #87 пакет 29 — #160/161 Thunder Axe lightning

22.10.2026 source change after owner's actual Minecraft feedback. Working main base 64e89143e2d76d3e492bd1b5fa00462011ed718f, owner acceptance still OPEN.

**BEFORE / ACTUAL / AFTER:** Owner never saw storm: code had only 20% chance to add +1 final damage and 8 enchant particles, TRIDENT_THUNDER sound; no real LightningBolt spawn. Now one target-anchored vanilla LightningBolt with setVisualOnly(true) is spawned ONLY on accepted melee proc; original 20%, +1HP, 60ticks, particle+sound retained, no thunderstorm weather/fire/extra damage.

**What to test in Minecraft:** Hit armored/ordinary enemies across 40+ fully recovered swings with 3s CD; see clearly visible bolt on proc, confirm one bonus hit, no fires, grief, lightning-charged mobs or allied damage. Test rain/sun and multiplayer. Source QA/Java21 only; runtime NOT_RUN.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Exact baseline and before-production plan | [PLAN.md](owner-qa-20261022/PLAN.md), original 12 JSON transforms in [before-12-crossbow-transforms.json](owner-qa-20261022/before-12-crossbow-transforms.json), actual changed sources in Git history | PASS source snapshot |
| Source regression | tools/weapons87_owner_qa.py, tools/verify_weapons87_crossbows.py, Java source guards | PASS on source build gate pending |
| Java21 clean build / JAR | .github/workflows/weapons87-owner-qa-java21.yml | PENDING CI |
| Same-scale true JSON/UV source before and after | CI source PNG sheet; not a Minecraft screenshot | PENDING CI |
| Actual 1P/3P, Alex/Steve, hit timing, sounds, rain/survival multiplayer | owner game retest not performed by assistant | NOT_RUN / OWNER_PENDING |

Do not close child issue #160 or master #87 without explicit owner sign-off. No PolyMC installation, no C/D/#190/#191 changes.
