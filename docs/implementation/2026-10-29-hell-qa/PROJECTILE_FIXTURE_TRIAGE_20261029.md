# 29.10.2026 — Projectile GameTest fixture corrections (hell)

Scope: **read/repair the two failures in run11**, no gameplay retuning, no item/registry changes, no changes to `main`, no owner acceptance.

## Evidence from unchanged run11

`docs/implementation/2026-10-29-hell-qa/run11-console.txt` records **9/11**, with:
- `nativebowskillordinaryandpower`: `Expected one native arrow: 0`.
- `actualknifeprojectileoneitemandloyalty`: `Loyalty did not return exactly one knife`.

The underlying previously packaged production JAR has NOT been proven defective by either failure. Both assertions contained demonstrable fixture mistakes:

1. The bow test inserted 64 arrows into an initially empty inventory **before** assigning the bow to the selected main-hand slot. `Inventory.add` can place ammunition into that same slot; `setItemInHand` replaces it. The subsequent `getProjectile` therefore had no ammunition. Now the bow is equipped first and ammo is placed explicitly into inventory slot 1; the fixture asserts `getProjectile(bow).is(Items.ARROW)` before releasing. Each direct test release clears `usingItem` state, and an already inspected arrow is discarded before asserting another shot, so the test cannot accidentally count a previous shot.
2. The knife test inspected `knife.getItem()` **after** `throwOne` consumed its one-item source stack via `stack.shrink(1)`. The original `ItemStack` is now EMPTY, making its `getItem()` equal to AIR, not the originally thrown knife. The test now captures `Item knifeItem` **before** throwing and uses it for the exact-one inventory count and subsequent stack lookup.

Correction: [0132ad252726061b1ece8da054e38c5fd4df78df](https://github.com/Deserag/slavic-myths/commit/0132ad252726061b1ece8da054e38c5fd4df78df), `tools/hell-qa-runtime/HellProjectileQaTests.java`.

## Independent verification path

A scoped GitHub Actions Java 21 workflow was added in [c7034ea58220c28f13b5fe1467d7fb92ccfafe70](https://github.com/Deserag/slavic-myths/commit/c7034ea58220c28f13b5fe1467d7fb92ccfafe70): `.github/workflows/hell-headless-qa.yml`. It triggers on `hell` gameplay/test/build changes (not ordinary documentation edits), performs `clean build`, validates source/JAR resources, records the JAR SHA256 and executes the packaged-JAR headless GameTest in a disposable CI test world. Logs/JAR are uploaded even if the GameTest fails. First run: https://github.com/Deserag/slavic-myths/actions/runs/40832920954.

**Execution statuses must be read from the actual CI run**, not inferred from this handoff. No in-session local Minecraft/NeoForge runtime was available for an independently executed rerun when these changes were committed; no new `BUILD_PASS` or `RUNTIME_PASS` is asserted in this document.

## Remaining verification

- Read the exact CI result and the first new failing assertion, if any. If `nativebowskillordinaryandpower` progresses past ammo, check one-arrow count, direct 13/10/Power II expected damage, 160-tick fire, cooldown and ammunition after three shots.
- For `actualknifeprojectileoneitemandloyalty`, check hit/Weakness, exactly one recovered item, wear=3 and persisted skill cooldown; do not infer no-dupe from a fake-player-only test.
- Repeat actual-player, inventory-full, logout/relog, death, dimension-change, and two-client scenarios in an isolated environment. Resource packing and headless GameTest do not prove first-person animation, textures, original-art identity or owner acceptance.
- Respect `AGENTS.md`: no owner desktop/PolyMC/save takeover; no Issue close, `main` edit or force push.

Status of original **run11 remains FAIL** until a NEW run is recorded. Production logic is unchanged by this fixture-only repair.
