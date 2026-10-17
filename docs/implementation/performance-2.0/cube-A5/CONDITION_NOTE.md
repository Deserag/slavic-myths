# Final resumed reference, 2026-10-17

Actual main production, same compiled cube probe as the earlier A/B runs. Started after the user confirmed a dedicated active PC and explicitly waived launch intervals. One early activation during loading; screenshot capture failed twice with `window capture timed out: timed out waiting on channel`, then no UI input. Native boundary screenshots and every-frame telemetry provide the scene checks.

COMPLETE: both cap140 and uncapped sampled windows have 100% eligible frames (focus, visibility, no pause/screen, exact camera, dimensions and cap). Boundary screenshot visually matches B1's scene and has no F3 overlay. Geometry parity: 1024 bitwise cases PASS. Actual allocation micro batches remain 1808 bytes/call for main and frozen reference. The six batches are not independent JVM repetitions.

This run and A2 are the two eligible independent main JVMs. A1/A3/A4 remain retained and excluded; A5 does not retroactively validate them. Cross-day context and changed launch spacing are disclosed in the protocol.
