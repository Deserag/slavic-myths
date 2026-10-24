# #241 — IN_PROGRESS, 24.10.2026

Implemented in85c03cd8. Integrated production source is **3b00acaf194aec4059b5b51d0b0b991614f1a740**. Main is untouched; latest origin/main documentation/workflow changes were merged into the execution branch. #109 stays unchanged/closed; no geometry/art/population changes. All Nether and release/cleanup tasks excluded. Direct owner execution authorization supersedes the old HOLD heading.

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Assigned natural upyr55%, no summoned/outside farm | RuneFoundationLoot; actual native LivingDropsEvent2000 draws, summoned100 and unassigned100 draws | PASS native suite
Voevoda/volkhv70%, prince1–2; preserve other loot | Original pools retained; only the three soul pools changed | PASS source/resource verification; boss combat owner NOT_RUN
All coffin routes valid and five-slot compatible |27 production table routes including18 missing V2 aliases;1000 native rolls per table, max5 stacks; actual BurialCoffinTile unpack | PASS native suite
Important reward cannot be empty | Guaranteed practical base; final-room coffin routes use important;1000 native important draws per tier, no empty | PASS native suite; full natural dungeon combat/exploration NOT_RUN
Retain artifacts/weapons/currency | Original two pools unchanged, appended independent pools, no coin changes | PASS source
Whole-kurgan before/after balance |192 real native layout/roster exports,20,000 trials/tier/version; baseline74a17a29, missing old V2 tables counted empty; [values](balance-simulation.json), [policy](POLICY.md) | PASS model; not gameplay income measurement
No reroll / shared inventory / save data | Actual tile resolves seeded pending table once; second native server player gets same five slots; NBT retains contents | PASS native suite; real process restart of these new coffins and two sockets NOT_RUN
Build/resources | Java21 clean build47s;7732 assets/1863 Java21 classes; test harness excluded | PASS
Install | [1.9.7.64 receipt](../polymc-19764/installed.json): source3b00acaf, SHA verified, backup, duplicate0 | PASS installed test candidate; PolyMC launch0
Owner acceptance | All Issues OPEN, Project «В работе» | NOT_RUN

Mean soul-equivalent per entire modeled dungeon (four fragments per soul, not automatic conversion): small0.350→0.773; warrior0.933→2.780; great2.606→8.300. Mean/probability/minimum for each soul/fragment/blank rune/ready rune/diamond/emerald are in the JSON. Observed minima and mathematical minima are identified separately. No extra enemies were introduced. Great-kurgan high reward is distributed over the entire long dungeon, not one coffin.

Final combined native suite8/8 in ninth isolated server process; the first two surface tests assumed a fixture ceiling was the true surface and were corrected after heightmap diagnostics. Later tree tests needed the fake actor's head orientation and explicit player-queue ticks (fake connections do not run normal connection/player ticks). Earlier failures are retained in [runtime-history](../chud258/runtime-history.json) and local raw logs; no earlier failed run is renamed PASS. Third server passed6/6 before the later tests were added; eighth7/7; ninth8/8. Full [final log](../chud258/runtime-server-final.txt).

Scope files: RuneFoundationLoot.java, KurganDungeonPiece.java (only final loot table routing),27 kurgan chest table files,3 entity soul tables, reproducible tools/kurgan_rewards_241.py and tools/simulate_kurgan241.py. No registry ID rename or existing save migration.

Owner test: explore a newly generated or previously unopened small/warrior/great kurgan, compare ordinary/secret/final coffins and combat rewards, take loot, reopen with another player and after saving/rejoining. Already opened old coffins must remain unchanged; this patch does not refill them. Use disposable test worlds/copies.
