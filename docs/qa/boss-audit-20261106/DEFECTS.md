# Confirmed resource defects and separate QA fixture failures

Baseline: release-2.5 `5738a4038f615dc292403cd6308d794cbc2915cc`. Production files are unchanged. These findings do not establish that boss combat/model animation is correct.

## BQA-001 — P2 — Boss trophy particle textures absent from block atlas

**Status:** FAIL, reproduced in all8 initial hosted Minecraft launches, run42166613024. This concerns trophy/item particle references, not missing boss entity skins.

**Files:** `src/main/resources/assets/minecraft/atlases/blocks.json`, `sources` (lines2–43); `src/main/resources/assets/slavicmyths/models/block/morovoy_raven_head.json:3` and corresponding `fire_serpent_head`, `mor_serpent_head`, `morovoy_fire_zmey_head`, `bauk_head_open/closed` models and item models.

**Cause:** Models reference `slavicmyths:entity/morovoy_raven`, `slavicmyths:entity/morovoy_fire_zmey_albedo`, and `slavicmyths:entity/bauk` as atlas sprites. The existing explicit atlas sources do not include these resources. The native resource reload reports the references missing.

**Reproduce:** Build unchanged JAR; launch Minecraft1.21.1/NeoForge21.1.255; reload resources or inspect startup log. Search `Missing textures in model slavicmyths:morovoy_raven_head`. Expected: all requested particle sprites resolve. Actual: `minecraft:textures/atlas/blocks.png:slavicmyths:entity/morovoy_raven` is missing (analogous warnings for the other heads). Actual block-breaking particle appearance has not been visually tested.

**Evidence:** All8 `work/boss-audit/artifact-*/work/boss-audit/hosted-client/logs/latest.log` files; preserved excerpts in `RESOURCE_RELOAD_EXCERPTS.txt`. Exact image/receipt manifest is separate.

**Proposed fix:** Add the missing explicit single sources to the block atlas or use valid dedicated block particle sprites. Keep registry IDs. Retest resource reload, inventory and placed trophies and breaking particles. Risk: atlas memory/layout changes; verify every affected head variant.

## BQA-002 — P2 — Mytar chained sickle recipe rejected during native reload

**Status:** FAIL, reproduced in all8 initial hosted clients. Existing baseline issue, not introduced by QA.

**Files/method:** `src/main/resources/data/slavicmyths/recipe/mytar_shackled_sickle.json:21`, recipe result ID; `src/main/java/org/slavicmyths/registry/ModItems.java:46` registers the chain and line47 the ordinary sickle; the chained result is absent from item registration.

**Reproduce:** Launch unchanged JAR or reload datapacks. Expected: recipe decodes against an existing registered output. Actual: `RecipeManager.apply` throws `JsonParseException: Unknown registry key ... slavicmyths:mytar_shackled_sickle`, and this recipe is excluded. Manual crafting has not been tested.

**Evidence:** Same8 native startup logs and excerpt file. Proposed fix: decide whether this result is an intended unregistered item or an obsolete recipe, then align registration/resources/recipe without renaming saved IDs. This requires a production task and design decision. Risk: adding a new item affects assets/loot/crafting; deleting an obsolete recipe changes availability.

## BQA-003 — P2 — Raven health differs from its declared attribute and regression contract

**Status:** FAIL on real NeoForge GameTest server after correcting the fixture startup. Run42168680626, job127731132861: `realravenworldtickandpersistence ... Raven max health incorrect`. The independent registry export returns1024HP for `slavicmyths:morovoy_raven`.

**Files/methods:** `src/main/java/org/slavicmyths/morovoy/MorovoyRaven.java:63`, `attributes(false)` requests1600; the class inherits the vanilla capped maximum. `tools/hell-qa-runtime/MorovoyRavenQaTests.java:33` asserts1600. Compare existing species-specific uncapped handling in `SisterBoss.getMaxHealth`; no production change was made here.

**Reproduce:** Build baseline JAR and run `./gradlew --no-daemon -I tools/hell-qa-runtime.gradle runHellQa -PhellQaRavenOnly -PhellQaDir=work/boss-audit/raven-regression`. Expected by the checked-in contract:1600. Actual constructed maximum:1024. The minion ownership/28HP/NBT test passes; the main lifecycle test stops at this assertion, so its later movement/persistence assertions are NOT RUN.

**Cause:** Consistent with Minecraft's1024 MAX_HEALTH attribute cap: the requested1600 is not exposed by this species as an uncapped maximum. Do not infer effective health for other species from this result; some implement their own health system.

**Proposed fix/risk:** First confirm the existing1600 contract remains intended. Then use the established species-local health mechanism and test actual damage/death, bossbar fraction and saved legacy health, without changing registry IDs or unrelated balance. Alternatively, a deliberate1024 target requires explicitly revising the design/test contract. Risk: migration can heal/damage existing bosses or desynchronize bossbars; do not silently alter attributes during this audit.

## QA-only failures (not production defect claims)

- Initial synchronous stress uses native `Player.attack` directly without a network reach gate; it does not establish legal-distance sword combat, and initially uses bare hands. Preserve scope limits.
- Tryaseya left forced-chunk coverage during the30second dedicated scenario:396actual entity ticks,610server ticks. Repeat with a valid loaded arena before diagnosing drift or attack starvation.
-56 initial captures have removed actors; session/owner/persistent-anchor setup was missing. Metadata retry captured all47 with no removed actors. Full views of those4 bosses still require retry.
-7 initial controlled attack captures waited25client ticks after injecting frame14; short actions could already end. Those captures are not attack animation evidence. Early-frame follow-up is separate.
- Raven-only initial startup failed because the fixture source set excluded its required SeedMixin. QA Gradle inclusion corrected; the subsequent run reached BQA-003. The initial run did not reach its health assertion.
- Native-arrow/restart QA compilation initially missed the ChunkPos import. Corrected in QA only. Successful rechecks produced21actual arrow collisions and7saved/restored entities. Initial arrow corridor loading was insufficient; only the3×3-loaded-corridor retry establishes projectile collisions.
- The188-case local performance collection wrote every sample, then Oracle21.0.12.1 crashed in native `jvm.dll` on `GC Thread#0` during shutdown. Run exit FAIL; collection completeness is separate. Cause is not established as a mod defect. Preserve `work/boss-audit/performance2/hs_err_pid4052.log` and reproduce before attribution.

## Fix sequence

1. Complete collision, active damage timing, natural phase/partner/target and repeated-hit scenarios before deciding combat/AI fixes. No confirmed combat fix is proposed from invalid fixtures.
2. Review early-frame captures and model hierarchy with actual model metadata; validate full animation cycles before a geometry change. Preserve weapon silhouettes and existing art intent.
3. Fix BQA-001 atlas entries; validate trophy variants and particles. Resolve BQA-002 result-item contract in a separate production task.
4. Evaluate valid1/5/10 samples against matching baselines, actor counts and scene visibility. High cuboid count alone is not a performance defect. Repeat measurements before optimization; preserve gameplay/art unless separately authorized.
