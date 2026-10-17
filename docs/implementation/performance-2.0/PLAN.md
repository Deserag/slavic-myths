# Finite release performance plan

1. PR37:4 independent JVMs B/A/A/B, exact protocol CUBE_PROTOCOL.md; conclude READY_FOR_REVIEW, NEEDS_FIX or INCONCLUSIVE. No optimization rewrite without reproduced regression.
2. Normal overworld seed200015, structures=true, native walking input/new and return route; no synchronous getChunk loops or airborne teleport stress. Same common probe/settings for SlavicMyths vs NeoForge+Curios+JEI without SlavicMyths. Record actual path/chunks; no assumption that return generates0 new chunks.
3. Normal-HP groups6/16, native equipped weapons and AI, real packet attacks; mod-specific runes2/base class3 skills, native boss workloads with no100-damage fixture hits. ResistanceV/heal/fixture class/item grants mean automated protected workload, not Survival balance or full progression acceptance. No requirement to repeat old128-pillager stress.
4. Native village/city bounded locate during setup only; actual generated structures/population must be verified. No find => NOT_EXERCISED, not a city performance pass. Log setup costs separately; never use locate/getChunk setup as ordinary exploration latency.
5.5 spawn/combat/cleanup cycles,10min idle, actual disconnect/save/open same isolated world and marker/new-server verification. Live heap/GC floors/remaining entities can show accumulation but do not alone prove retained leak absence. Portal/dedicated/MP remain separate if unavailable.
6. Unified REPORT A–F, rank proven player-impact issues, only address critical reproducible faults separately. No graph/mechanics/content cuts. Production remains main for report branch; PR37 tested in its own worktree. Stop after finite scenarios; do not rerun stress just to fill report. Raw frames/CSV/JFR summaries/screenshots/hashes, no personal log metadata.

Baseline main remains1.9.7.5, not already version2.0. This is release-readiness evidence for planned2.0, not a version bump. Performance profiling and GPU sampler overhead disclosed; no unprofiled control promised. Main/PolyMC/user worlds/IDs untouched.

Observer retention guard: old integrated-server identity uses WeakReference, never a strong static server/world/entity cache. Spawned entity collections are cleared at scene cleanup; route stores only Vec3 coordinates. Old-server WeakReference not yet collected after young-GC is not itself a production leak proof.

Realistic normal-world/control settings are1600x900,render distance12/simulation8,cap140,VSync off,2–4GiB heap. Cube protocol stays1280x720/8/5 for comparability. Active display modes queried1920x1080 and2560x1440;1600x900 fits either visible window. These separate scene settings are not cross-comparable absolute FPS baselines.
