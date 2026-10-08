# Cleanup — 1.2.5

Before inventory: 5,559,312,107 logical bytes (5.559 GB). The sum of 25 `run-*` QA directories was 3,823,721,458 bytes; `src` was 14,086,232 bytes. This is project/runtime size, not mod JAR size.

## Preserved outside the working checkout

25 old isolated QA runtime directories were moved on the same volume into:

`D:\slavic-myths-runtime-backup\maintenance-1.2.5-20261008`

Exact source/destination pairs: [runtime-backup-1.2.5.json](runtime-backup-1.2.5.json). Includes worlds/regions/playerdata, logs, server settings, reports and configs. No worlds or screenshots deleted. This relocation reduces checkout size but does **not** reclaim disk space. Restoring a directory requires moving it back when no server is using it; reject existing destinations rather than overwrite them.

## Git index and history

4,798 tracked runtime files were removed from the index only, not destroyed. Another 188 generated/binary scratch artifacts were untracked with local copies retained. Lists: [runtime](runtime-untrack-1.2.5.txt), [generated](generated-untrack-1.2.5.txt). These are staged cleanup deletions for review; no commit has been made. Source, unique art, reference documents and reusable scripts were retained. Legacy tracked scratch/reference source remains where documentation/tool migration is not proven safe.

`.gitignore` now covers `/run-*/`, `/logs/`, crash reports, output/cache directories and `/work/`. `src/generated/resources` and `docs/media/readme` are not ignored.

`git -c gc.reflogExpire=never -c gc.reflogExpireUnreachable=never gc --no-prune` repacked Git safely. No history rewrite, force push, reflog expiry or explicit object pruning. Before: 73.00 MiB loose + 721.16 MiB packs; after: 0 loose + 424.77 MiB packs. Historical worlds and old artifacts remain reachable in normal history. Before/after `git fsck --full` succeeded; orphan temporary object files remain, under 1 MiB. LFS listing returned no files.

Largest reachable historical blobs include a 6,972,010-byte NeoForge installer, 6,340,754-byte RC2 JAR, 6,330,602-byte RC1 JAR and many ~4 MiB `.mca` test regions. Complete read-only object inventory: [git-objects-before-1.2.5.txt](git-objects-before-1.2.5.txt). No backup refs needed because no history transformation was attempted.

## Regeneration and preserved content

`gradlew.bat clean build` regenerated build outputs. Toolchains in `.tools`, local historical release archives, source art, reference material and screenshots were retained. Assets were not downscaled, recompressed lossily, merged across saved IDs or deleted as speculative orphans. All 127 byte-identical groups are documented.

New test output remains ignored and intentionally retained as evidence; current headless logs are copied under `docs/maintenance`. Final audit includes regenerated build outputs and new QA worlds. Fresh-cache source verification uses separate directories outside this checkout.
