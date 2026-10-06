Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

# 0.9.9 вЂ” worldgen work in progress

Status: COMPILES / IN PROGRESS. This is not release acceptance.

14 native structure sets now share explicit placement profiles. Registry IDs and native locate participation remain unchanged. Candidate spacing is not proof of accepted-placement coverage. Inventory: `docs/verification/mob-worldgen-0.9.9/worldgen-inventory.json` (14 structures, 48 configured features).

## Authorized thin-superflat adaptation

The user explicitly authorized raising the kurgan and creating a bounded soil mass. Natural and developer generation use `KurganEarthwork.plan` before adding a piece. The dungeon bottom stays at least two blocks above world minimum; the complete footprint receives a graded dirt/grass mass. The south entrance gets a three-block-wide approach with four blocks of headroom and steps at most one block high. Earthwork columns, raised origin, and completed earthwork chunks are persisted with the existing piece. Old saves without Earthwork keep their previous placement behavior. Registry IDs are unchanged.

Explicit maximum added-soil budgets: small 1,000,000; warrior 4,000,000; great 12,000,000 blocks. Footprint is limited to 262,144 columns. No partial placement is accepted when planning exceeds either limit. Developer command also preflights world border, existing burial instances, fluids, block entities, and non-natural blocks across the expanded earthwork bounds.

Headless evidence: 30 plans / three tiers / ten seeds on four-layer superflat, saved column roundtrip, largest added fill 1,685,122 blocks (current grading). Separate actual block-placement fixture checks the entrance approach's four-block headroom and maximum one-block step. The complete small raised dungeon has also been placed through chunk clips, serialized/reloaded as a piece and placed again: three modified reward containers and the accessible entrance survived. This is a piece-NBT regression, not an actual player logout/login. This does not prove a complete generated dungeon in every natural terrain scenario. Height extrema initialized to Integer.MAX_VALUE/MIN_VALUE in kurgan and bandit structure planners, fixing negative-Y terrain sampling.

## Remaining release blockers

BLOCKED: the first complete natural-start measurement failed: small kurgan maximum gaps 1362.46 / 1232.67 / 1066.88 blocks on seeds 0 / 17 / 731269. Seed 0 produced no accepted underwater ruins. Those reports describe the previous planner, and remain evidence of failures rather than current acceptance.

COMPILES / validation in progress: all 14 native sets use bounded lazy local retries, cached terrain columns, world-height-relative checks and the native eight-chunk reference window. A new flat regression exposed a real Great Kurgan refusal caused by exclusions of unaccepted neighbor plans; kurgans now resolve deterministic accepted neighbor plans before footprint collision decisions. Existing neighbor protections are retained. This is planned-start protection, not a check of arbitrary existing player buildings or block entities.

COMPILES / runtime regression passed: vanilla TemplateStructurePiece resets its bounds on every postProcess; camp/stronghold/swamp pieces retain expanded foundations/approaches, synchronize placement and save hooks. Three camp fixtures placed twice through chunk clips preserve three modified loot containers. A structure-spawned Ataman has 150 HP, with role equipment retained. Camp/stronghold ledgers are prepared on server LevelEvent.Load; worker lookups use the prepared cache. Read-only missing-record queries no longer create or dirty saved records. A worker-thread regression confirms both ledger reads preserve their serialized state.

NOT STARTED / IN PROGRESS: rejected-site compensation, biome-region boundaries, complete coverage of 48 configured features/POIs, full rejection diagnostics and actual placement/protection across natural worlds. The 1000/2000/4000 limits have NOT been established for the final implementation. The expensive natural-noise measurement is a separate mandatory gate: `runRuntimeChecks -PportRuntimeCheck -PwithoutJei -PnaturalCoverageCheck`. Core checks do not replace it. Test templates exist only in the test source set.

The installed PolyMC profile remains the accepted 0.9.7 artifact. No 0.9.9 deployment or client launch has occurred.

## Continuation: raised approach regression

The expanded fixture places the actual architecture before the approach, for all three tiers. It exposed authored cobblestones left at the old fixed entrance elevation above the new descending bank. Architecture now places those details at the persisted entrance surface; old pieces without earthworks retain their previous elevation. The approach replaces soil/vegetation only and preserves the fixture's construction blocks. The latest individual fixture passes three tiers, continuous one-block steps and four air blocks above the supporting floor. Collision shapes, rather than isSolid, identify supporting dirt paths. Full companion suite passed 27 required tests in work/mob-worldgen-099-graded-approach-companions.log. The subsequent strengthened fixture passes all three lanes per tier; the full latest suite also passes all 27 tests in work/mob-worldgen-099-three-lane-companions.log.

Coverage measurement now tests the bounded local candidate set instead of excluding a cell solely by its center biome. Reports distinguish inapplicable tiers, absent eligible candidates, missing measurements and unmatched accepted starts; empty applicable networks are no longer reported as successful coverage. Full natural measurement of this implementation is pending.

## Latest gate statuses (2026-10-06)

- DONE: raised-approach regression, three sizes / all three lanes, construction preservation; 27 required companion headless tests. Native flat protection checks 1,991,475 accepted kurgan/camp pairs without intersection.
- COMPILES: clean build and test-source compilation with `-Pmod_version=0.9.9-dev`; production JAR passes resource/Java-21/test-exclusion checks. Static data: 2676 references, zero errors. Existing 16 unrelated door-parent issues remain reported.
- BLOCKED: complete release acceptance, pending coverage compensation and remaining architecture/mob requirements. Earlier failed natural reports are not current acceptance.
- NOT STARTED for this implementation: final full natural-coverage rerun and client/manual QA. Client launch is not authorized.

Next implementation block: bounded deterministic compensation for rejected native cells and eligible connected-region validation, then the separate full natural-coverage gate; continue architectural feature/POI migration rather than marking native-only coverage complete. Installed stable PolyMC JAR is unchanged, confirmed by SHA-256. Machine-readable continuation evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json`.
