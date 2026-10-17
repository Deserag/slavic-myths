# Resumed PR37 repeat, 2026-10-17

Actual PR37 production in its separate worktree, same cube probe. User-confirmed dedicated PC and explicit interval waiver apply; no concurrent Minecraft. One early activation while loading, then screenshot capture and one refreshed-window retry both failed with `window capture timed out: timed out waiting on channel`; no click/key/typing. Native telemetry and boundary screenshots determine final eligibility after completion, not assumed from startup focus.

Final: COMPLETE, both sampled windows 100% eligible. Boundary screenshot has matching scene/camera and no F3 overlay. Geometry parity 1024 cases PASS; production micro 576 bytes/call vs frozen reference 1808. Scene JFR contains production_cube native Vector3f samples, no production_cube float-array samples. GC 43, sum pauses 74.1398 ms, max 2.3487 ms over 335.878 seconds; whole-profile totals include warmup and transitions.
