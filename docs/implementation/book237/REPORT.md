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
