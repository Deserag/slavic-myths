# #235 / #237 / #236 — IN_PROGRESS, 24.10.2026

This is the current consolidated evidence. Earlier reports below contain chronological failures and superseded pending results; they are not the final acceptance matrix. All three Issues remain OPEN, all three Project9 cards are «В работе», all PRs are drafts. Main and the initial local changes were preserved. No owner acceptance is claimed. The owner removed the launch pause after the final replay started.

## Delivery and source

Stage | Branch / commit | Draft PR
--- | --- | ---
#235 | codex/performance-20261024 / 6b2ecd2186338a107d09cff559c6c17d4abb37aa | https://github.com/Deserag/slavic-myths/pull/239
#237 | codex/book-redesign-237 / a9e3346c, integration 7027bb2e | https://github.com/Deserag/slavic-myths/pull/259
#236 + integrated fixes | codex/guides-236 / fd0fa630, 273bb77a, 43ebdae728508a1d0b5b25db5b3081b6aeac46cb | https://github.com/Deserag/slavic-myths/pull/260

PR259 builds on PR239; PR260 builds on PR259. The final production source is 43ebdae7. The subsequent evidence commit changes only documentation and the disposable client harness. No merge into main, reset, force push, Issue closure or new gameplay JAR installation occurred.

Java21 `clean build -I tools/book-offline.gradle checkBookOffline`: PASS, 1 minute, 50,289 offline checks. Production resource verification: PASS, 7,692 resources / 4,163 JSON / 1,373 PNG / 1,783 models / 1,851 Java21 classes. Test classes are excluded from the production JAR. Local raw logs: `work/resume-235-237-236/build-delivery.log`, `resources-delivery.log`.

Artifact: `D:/slavic-myths/build/libs/slavicmyths-1.9.7.62.jar`, SHA256 `bcb93c26d5383963eef7d5f19cde64676c20cd2a4f9f1b35b9c26f82b512b656`. This is a built candidate, NOT an installed update. The installed PolyMC JAR remains the earlier version/source; see [settings receipt](../guides236/skill-combos-installed.json).

## #235

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Preserve existing work/main | Initial patch and viewport backup in work/resume-235-237-236; separate stacked branches | PASS
Reduce repeated rendering/allocation | TaleScreen caches, UiText.Layout/FolkUi, indexed nodes, clipped UiLine, EarthCamera scratch reuse; offline checks | PASS
Avoid repeated server definitions/recipes | TaleService session cache, revisions/reset, reload/disconnect invalidation; defensive copies retained | PASS source; two-socket wire budget NOT_RUN
Duplicates and safe cleanup | [audit](../optimization-19762/DUPLICATES.md), cleanup manifest; legacy save bits/routes deliberately retained | PASS targeted audit; whole-mod exhaustive audit NOT_RUN
Measured book before/after | Matched real renderer [frames and screenshots](../book237/client/); results below | PASS bounded fixture; long-term 1% low NOT_RUN
First full snapshot | Native TaleService snapshot and CODEC on actual integrated server: 51.99ms / 8,374,664B, repeat 5.07ms / 802,320B; 245,167B full payload both | PASS measurement; old/new server comparison NOT_RUN
Eliminate all performance spikes | Full snapshot above; command placement peak101.39ms remains | FAIL unresolved performance scope
Broad server/network/multiplayer performance | One-player server sampling only | NOT_RUN

## #237

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Compact collapsible contents, shorter rows, selection | TaleScreen replaces eight giant tabs with 18px rows, category expansion/search/highlight | PASS real-client screenshots
Sidebar hide and wide reader | Same screen, persistent preferences, native buttons | PASS screenshots and actual screen callbacks
Readable achievements and tree | Default list with actual server descriptions/status/parent; TaleViewport fit/center/zoom/hit; icons scale, native text | PASS offline + actual screen callbacks for node click, drag threshold and pan
Different GUI scales/resolutions/languages | [48 real frames](../book237/client-final/): RU/EN, 1280x720/1920x1080/2560x1440, requested2/3/4/Auto; filenames record actual scale | PASS scripted coverage; owner visual acceptance NOT_RUN
Text blur diagnosis | No blur reproduced with vanilla font at native integer GUI coordinates | NOT_RUN original affected pack/driver configuration; no universal fix claimed
Keep recipes/map/rewards/quests | Existing TaleScreen/TaleNetwork/TaleService reused; guide screenshot shows same book | PASS source/smoke; exhaustive all rewards gameplay NOT_RUN
Real interaction | BOOK_INTERACTION_PASS in replay3: actual client screen callbacks; screenshot below | PASS callbacks; physical mouse/key manual acceptance NOT_RUN

The short fixed-order, uncapped A/B uses the old renderer from 1bed9897 and the integrated implementation. Tree at1280x720/requested2: **29.74→212.05 mean FPS**, p95 frame45.86→6.98ms. At1920x1080/requested3: **31.63→241.01 FPS**, p9550.47→5.77ms. This measures #235+#237 together. JIT/order/background load are confounders; some page p95 results regress. No measured 1% low or release-wide guarantee. Final48 scenes: mean152–829FPS, p951.92–9.05ms.

## #236 and keyboard addition

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Guide reputation/gates | Yaga native1/3 victories; Bayun per-cat25/60/85 and rumor2/3; Wolf fourth trust preserves original chain | PASS native server tests
All three target requests through both NPCs | Actual YagaMenu and authenticated BayunNetwork sessions in NORMAL-terrain world; all six accepted | PASS [actual replay](../guides236/normal-world/results-replay.txt)
Full native Nightingale fortress | Ordinary travel materialized17-piece native fortress,49 footprint chunks,25 containers; actual live boss; later historical run generated another18-piece camp before index fix | PASS native generation; prior bug retained in history
No repeated camp for known live boss | Fixed NavigationManager Nightingale type dispatch; final replay uses original saved UUID for both NPCs; no third camp | PASS final actual replay and native entity-join/shared-player test
Persistent world/camp/boss | Separate client process restart loads original17-piece camp, unchanged campUUID and living boss | PASS actual restart
Safe command generation | Actual /slavicmyths generate bandit_camp large2362026; diamond inside predicted footprint survives; complete fortress relocates;39,277 changed blocks | PASS native command test; arbitrary third-party claims NOT_RUN
Bounded NPC planning / no book-triggered placement | GuideDiscovery finite native metadata planning; max4 jobs, max64 anchor/24 structure candidates; no NPC tickets/manual placement; explicit Yaga search action | PASS source and normal-world route; exhaustive biome/worldborder matrix NOT_RUN
Book tracking and privacy | Existing TaleData + native NavigationRecords/SearchArea; public marker only, no targetUUID/anchor; scoped STOP and foreign TRACK test | PASS native tests + [real book](../guides236/normal-world/guide-tracking-replay.png)
Original giver and one reward | Native advancement completion, original cat, ancient sign once, duplicate reclaim denied | PASS native tests; actual full boss combat campaign NOT_RUN
Flight trading | All3 actual YagaMenu offers: stage, ingredients, one output, duplicate denial | PASS native tests
Two-seat mortar, pilot authority, legacy saves | Synced/saved pilotUUID; capacity2/1, no passenger promotion, owner-only input/cargo, descent at negativeY; legacy NBT test | PASS native tests; two real socket clients/visual flight NOT_RUN
Shortcut defaults/capture/persistence | Ctrl+1/2/3; Ctrl/Shift/Alt key or mouse capture, existing server checks; actual native Options save/reload | PASS client capture/save/reload; physical activation NOT_RUN
Requested PolyMC settings | Only three key lines edited, options backup and SHA in settings receipt; no gameplay JAR replacement | PASS disk change
Regression | Fourth isolated server:13/13 required PASS; earlier8/9,9/9,12/13 runs retained | PASS final native suite; complete mod-wide MP campaign NOT_RUN

Actual campaign process count: **5 dev clients + 4 isolated GameTest servers = 9 Minecraft processes**, including failed/incomplete attempts. No PolyMC process launched. The final client exited normally and saved all dimensions. Tests use disposable worlds, not user saves. Two native ServerPlayers do not equal two socket clients; native advancement mutation does not equal defeating bosses in combat.

Final normal-world last100 ticks: mean **14.43MSPT**, p95 **32.44ms**; single-player, near loaded NPC fortress, not before/after comparison. Command placement peak **101.39ms** is unresolved. JFR52s summary in [guides report](../guides236/jfr-summary.json) contains sampling counts, not CPU percentages.

The client harness initially mishandled world disconnect, creative-mode Bayun SAFE sessions and unloaded-entity replay checking. These harness failures and the genuine production Nightingale-index/STOP/negative-Y bugs are documented in [chronological report](../guides236/REPORT.md). The final harness first travels back normally before checking loaded entities. FAIL files remain historical evidence; results-replay.txt is the final successful route, not a claim that earlier attempts passed.

Remaining work before full technical/owner acceptance: old/new server opening and network budget, command-placement spike, two actual network clients including mortar flight, full boss combat/reward campaign, physical shortcuts, original blur reproduction and affected pack/driver, broader saves/biomes/claims and performance regression. Issues remain IN_PROGRESS; no transition to «Тестирование» while executor scope is incomplete.
