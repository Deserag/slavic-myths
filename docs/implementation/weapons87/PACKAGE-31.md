# #87 пакет31 — #163 / first-person recoil both crossbows

## Scope and proven cause
Actual baseline `src/main/java/org/slavicmyths/client/WeaponHands.java` (before Git blob `64107aba9e4b4538a67feff7798b703d6f23a901`) has charge wrist offset proportional to crossbow pull but **no shot-recoil interval whatsoever** after firing charged projectile. The item geometry updates to no-arrow model but held pose is static. This is independent from owner's earlier third-person vertical-stock #115 failure already fixed P28.

**After:** In the same client-only RenderHandEvent pathway, `crossbowKick(...)` remembers the exact rendered itemstack **per main/offhand** and `CrossbowItem.isCharged` state. Same stack charged→uncharged, not during active use: trigger lastShotClock on the client, animate 7 ticks of smooth recoil. Apply -9° × envelope X rotation; offset visual hand + model .13 blocks toward eye, .035 down, so arm and weapon remain linked. Reset on player/world change or new itemstack; after 7 ticks exactly zero. Do not modify player combat attributes or world client/server events. GUI, TP pose, 12 real model states, textures and shot/ammo/charge/burst logic remain unchanged.

The actual original Java is saved in `blueprints/p31-crossbow-recoil/BEFORE-WeaponHands.java.txt`. CI `tools/weapons87_recoil_storyboard.py` creates two four-phase PNG A/B boards from source model/UV at **same orthographic camera**, not player screenshot. `tools/verify_weapons87_crossbow_recoil.py` checks actual one source caller, reset guards, charge→fire, envelope and JAR class. Combined P28 and 68-ID resource source checks guard unrelated changes.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| No recoil in source baseline / authored plan | frozen before Java and 7-tick formula in same file | PASS source |
| One hand per actual main/offhand, only charged→shot | source guards + distinct slots, no item swap fake recoil | PASS source guard; runtime NOT_RUN |
| 4 actual model/UV positions, loaded/projectile art vs fired | CI before/after source four-phase preview | PENDING CI |
| Java21 clean JAR and legacy 12 state assets | branch `weapons87-p31-crossbow-recoil.yml` | PENDING CI |
| True 1P Steve/Alex, shot/refire/offhand, client+server timing | owner Minecraft test needed, PolyMC installation not done | NOT_RUN |
| Full #163 (TP shot/reload, 3 special modes, MP) | remaining work, not claimed complete | IN_PROGRESS |

Test manually: load each crossbow; shoot a single arrow and fireworks, visual arm and stock kick together and settle within7 ticks; switch slots while loaded; test reversed main arm, offhand and both hands; inspect TP idle (must not change); verify three-breath fan/burst damage counts unchanged. No automatic gameplay acceptance.

## 22.10.2026 — P31/32 final verified source and CI receipts
- **P31 #163:** crossbow FP visual recoil `WeaponHands` on same ItemStack charged→empty, 7-tick smooth client-only wrist+gun kick, per-hand state reset on switch/world, not ammo/server. [PR #202](https://github.com/Deserag/slavic-myths/pull/202) merged `4247c95ff8f8b82cb875161edf960686caaaa2af`. [Java21 CI 39775007057](https://github.com/Deserag/slavic-myths/actions/runs/39775007057) **PASS**: native 12 crossbow model states /48 handed pivot cases, 294 packaged resources, actual four-phase item-source/UV storyboards. JAR `slavicmyths-1.9.7.56.jar` SHA256 `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- **P32 #121:** isolated canonical source generator run **twice** in disposable temp sandbox; no actual project asset writes, 68 active weapon-ID models, 16 protected resources (#115/#163/#184/#185/#189). Byte differences **30** (12 item JSON +18 flail PNG), but JSON **semantics** and PNG **pixels** drift **0**, double-run **idempotence** drift **0**. Legacy `audit-68.json` SHA109 and old flail applied-manifest SHA51 still archived baseline differences (not silently overwritten). [PR #203](https://github.com/Deserag/slavic-myths/pull/203) merged `f1b9756454becb2cb424d6da272a68a9ab34ff6d`; [Java21 CI 39776454779](https://github.com/Deserag/slavic-myths/actions/runs/39776454779) **PASS** (68 ID,294 JAR resources, generator source gate), same JAR SHA `d920d52874db3d9d1d811756ff10177f0c9b94aec3db7c9edcf73eb34f86790f`.
- #121 remains **partial** (only canonical `weapons87_flails --apply` chain replayed, other historical standalone entrypoints and gameplay still pending); #163 remains partial (TP player animation/MP owner test pending).
- **Issues count:** 43 child issues #115–#121 + #157–#192, 42 OPEN /1 CLOSED (#184), plus parent #87 OPEN as of audit. No Issues closed automatically. Owner gameplay/PolyMC **NOT_RUN** in assistant session. Unapproved C/D/#190 untouched, #191 last.
