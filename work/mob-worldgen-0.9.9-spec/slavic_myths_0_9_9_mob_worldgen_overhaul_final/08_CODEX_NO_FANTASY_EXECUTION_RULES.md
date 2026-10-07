# 08 — CODEX EXECUTION RULES
## THIS FILE EXISTS BECAUSE PREVIOUS IMPLEMENTATIONS INVENTED TOO MUCH

1. READ ALL SPEC FILES FIRST.

2. INVENTORY ACTUAL REGISTRY BEFORE EDITS.

3. DO NOT CREATE SUBSTITUTE DESIGNS.

4. DO NOT CHANGE:
- 1000 / 2000 / 4000 worldgen hard rules;
- Mother Bear 120 HP;
- Ataman 150 HP;
- Solovey 250 HP;
- one Bear egg;
- one Deer family egg.

5. IF A REQUIREMENT CANNOT BE IMPLEMENTED EXACTLY:
stop that sub-feature,
document technical reason,
implement closest non-destructive prerequisite,
DO NOT silently invent another mechanic.

6. NEVER "FIX" structure availability by:
- increasing locate radius;
- increasing random retries at distant coordinates;
- removing all biome rules;
- generating everything beside spawn;
- lying in the final report.

7. NEVER "FIX" natural spawn by:
- testing only `/summon`;
- testing only spawn egg;
- adding a command that spawns the mob.

8. NEVER "FIX" AI by:
- only increasing HP;
- only increasing damage;
- increasing movement speed until mob is unavoidable.

9. DO NOT ADD NEW ITEMS unless file 06 allows them AND current registry has no equivalent.

10. PRESERVE old IDs where possible.

11. NO CLIENT AUTO-LAUNCH.

12. FINAL RESPONSE MUST INCLUDE FACTS:
- changed files;
- actual registry IDs;
- actual HP values;
- actual spawn rules;
- exact automated test output/statistics;
- build result;
- remaining manual QA.

13. "BUILD SUCCESSFUL" DOES NOT MEAN GAMEPLAY DONE.

14. "No compile error" is not acceptance.

15. If automated coverage test fails, 0.9.9 is NOT DONE.
