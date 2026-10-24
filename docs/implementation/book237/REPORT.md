# #237 — IN_PROGRESS

Restored the interrupted deleted TaleScreen from the preserved #235 HEAD, then implemented the redesign in codex/book-redesign-237. Initial deletion patch and TaleViewport source are preserved in work/resume-235-237-236. Main remains unchanged.

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Compact collapsible contents, 18px rows, selected state | TaleScreen NavRow; replaces eight top tabs | PASS source
Hide sidebar / expand reader | contentX/contentWidth, persistent client preferences cleared on disconnect | PASS source
Achievement list/details and clickable tree | real server description/status/parent; drag threshold; TaleViewport shared transform | PASS source
Automatic fit/center, icon scale, clipped edges | TaleViewport + offline node hit/center checks including 1000 nodes | PASS offline; visual review in progress
Preserve map/recipes/rewards/quests/network IDs | same TaleNetwork and TaleService; rewards button retained | PASS source; gameplay NOT_RUN
Native font and sharper text | no scaling applied to text; first A/B fixture uses real old/new renderer | NOT_RUN final paired review
Java21 clean build | work/resume-235-237-236/build-book.log: 1m33s | PASS before final viewport refinements
Production resources/JAR | resources-book.log: 7688 assets, 1845 classes | PASS before final refinements
Real client captures | tools/book-client; disposable copy BookQA; campaign in progress | NOT_RUN final validation
RU/EN, scale Auto, 2560, reader interaction, two players | remaining campaign | NOT_RUN
Owner acceptance | reserved for owner; Issue remains OPEN | NOT_RUN

First client campaign exposed an irrelevant entry portrait while viewing achievements and expanded unrelated categories. Both corrected in source; a later client run is required. Owner requested a seven-minute pause before the next Minecraft launch on 24.10.2026. No installation in PolyMC.


## Integrated client evidence (24.10.2026)

The combined #235/#237 renderer campaign on fd0fa630 produced **48 real-client frames** in client-final/: RU/EN,1280x720/1920x1080/2560x1440, requested GUI2/3/4/Auto, native page/tree/list and sidebar/reader layouts. Filenames include actual native GUI scale; requested4 at720p is clamped by Minecraft. No image mockups or postprocessing. Native font runs at integer GUI coordinates; text blur was **not reproduced in this vanilla-font fixture**, so a blanket claim that all resource-pack/driver blur is fixed would be false.

The earlier matched renderer A/B in client/ uses source1bed9897 TaleScreenBefore compiled only in the test source set and the implemented screen in one real client. At1280x720/requested2, the achievement-tree mean frame rate was29.74→212.05FPS and p95 was45.86→6.98ms. At1920x1080/requested3,31.63→241.01FPS and p9550.47→5.77ms. This is the **combined optimization+redesign effect**, not attribution solely to #235. The fixture exposes the whole catalog, samples short scenes in fixed before/after order, and has JIT/order/background-load confounders. Some page p95 results regress. It is not a release-performance guarantee or a measured1%low.

Final48scenes have p951.92–9.05ms and mean152–829FPS in this uncapped scripted UI fixture. Full interaction, physical hotkeys, two real network clients and long-duration1%low remain NOT_RUN. The compact list uses real server descriptions/status/parent, not guessed completion conditions.

Actual modified-key capture/save/native Options reload passed in this client; physical keyboard dispatch was not emulated. The test harness initially omitted ClientLevel.disconnect before Minecraft.disconnect while changing worlds; the saved-world exit was repaired using the normal connection-disconnect operation via JDK attach, then fixed in tools/book-client/BookCapture.java. This was a harness error, not a production book fault. All48 UI measurements were completed before the repair.

Latest production clean build: work/resume-235-237-236/build-final.log SUCCESS1m; packaged resources7692/classes1851, SHA256b0714dbe8eb7df17e27ce8734d409c2a238616a9fefc37d523001ef1bcea4fa7. Test harnesses excluded. Following integration adds guide map/track/untrack controls to this same screen. No PolyMC gameplay JAR was installed.
