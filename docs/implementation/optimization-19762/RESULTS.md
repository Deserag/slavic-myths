# #235 — IN_PROGRESS

Implemented allocation/render work reduction, indexed book data, session definition delivery, lifecycle invalidation and duplicate audit. Runtime performance/visual/save/MP regression will follow all three stages per owner instruction. No release/acceptance.

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Preserve main/user work | base SHA and work backup manifest | PASS
Client book layout cache + recipe parsing outside repeated frames | TaleScreen/UiText/FolkUi | PASS source/offline
Pixel-equivalent visible connections | 5000random clipped DDA cases | PASS offline
Native offline text/clip checks | 41271checks, ../book-performance-20261024/offline.json | PASS
Server cached catalog+recipe delivery | TaleService session revision, EntriesReset/AdvancementReset; defensive copies retained | PASS source; multiplayer NOT_RUN
Java21 clean build | work/book-stage1-build.log | PASS1m35s
Resources + packaged artifact | work/book-stage1-resources.log:7688resources/1843classes | PASS
Real frame time/MSPT/network/relog | scheduled disposable runtime campaign | NOT_RUN
Full1110-file post-line code review | inventory/targeted hotspots only | NOT_RUN
Issue acceptance | owner only | NOT_RUN

Offline 1080-character fixed-glyph native StringSplitter page: uncached46160–47193bytes/op; cache0–0.472bytes/op in3in-process batches. Not FPS, not actual game font/GPU. JAR and PR references follow in stage ledger.


## Integrated runtime follow-up

Actual A/B screenshots and frame metrics now exist in ../book237/client/ and ../book237/client-final/. They measure the combined #235/#237 implementation, with short fixed-order/JIT limitations documented in ../book237/REPORT.md. They are not standalone #235 attribution or a1%low.48 final RU/EN/resolution/GUI scenes rendered; vanilla-font blur not reproduced. JFR of the NPC planner/native travel is summarized in ../guides236/jfr-summary.json (3877 execution samples over52s; sample counts are not CPU percentages). Command fortress placement measured a111ms peak; do not claim all worldgen spikes are eliminated. First-open packet/CPU/alloc timings, full-relog packet budgets, two sockets and a broad whole-mod server performance campaign remain NOT_RUN. #235 remains IN_PROGRESS, Issue open, owner acceptance pending.
