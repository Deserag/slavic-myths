# MICRO RETROFIT 1.7.1 / 1.7.2, MINIMAL EDITS ONLY

Touch previous entity registry IDs and configs only where necessary for summoning accessibility. Do NOT reimplement or rebalance Ilya 280HP, Idolishe 900HP, Dobrynya 230HP and Marinka 600HP. Preserve final approved Dobrynya white hair + STUBBLE, long spear, gray wide armor, shield; no appearance edits.

Required checks in repo (bounded and stop after finding pattern):
1. Locate Sivka/Burka actual reusable summon item and registry path. Map hero call tools to same ownership/spawn/unload pattern. Be robust if implementation different from assumed behavior.
2. Check Ilya's call item exists and can be `/give`-n and used twice (second click dismiss) without shrink count. If yes preserve it; else introduce `Знак Ильи Муромца`. Repeat for Dobrynya call tool.
3. Check `EntityType` registrations for `idolishche_poganoe` and `marinka`; add spawn egg entries only if missing. Verify creative tab and translations. NO replacing entity IDs (world saves) or copying their classes to new entities.
4. For all three personal bosses (Idolishe, Marinka, Skim) enforce one matched story ally only during encounter, and broad party rules outside. To keep backward compatibility, add guard in shared summon dispatcher or existing arena gate rather than modifying all hero AIs. Existing 1.7.1/1.7.2 saves should load.
5. If old boss spawners already exist (e.g., quest item), retain them; eggs complement debugging/creative, do not accidentally let creative egg set quest rewards flags or disable original story encounter.

One file diff at a shared registry/controller can be acceptable. A repo-wide 'unified companion framework rewrite' is NOT. Log exact touched classes in final implementation report.
