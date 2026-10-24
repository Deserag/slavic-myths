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
