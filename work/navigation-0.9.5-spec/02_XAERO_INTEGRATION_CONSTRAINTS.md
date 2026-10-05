# XAERO INTEGRATION CONSTRAINTS

Known ecosystem facts:
- Xaero Minimap supports waypoints and off-screen waypoint display.
- Existing Waystones compatibility mods prove automatic waypoint creation is technically possible.
- Xaero has historically not exposed a stable public API that Slavic Myths should blindly rely on.

Therefore isolate Xaero-specific code.

Preferred dependency flow:

`NavigationManager`
→ generic Slavic marker/search-area model
→ `MapIntegrationBridge`
→ optional `XaeroIntegration`

Dedicated server/common code must never import Xaero client classes.

If true polygon/area overlay is not safely supported:
1. use deliberately-offset approximate search-center waypoint + radius text;
2. optionally use grouped boundary markers only if they can be managed cleanly;
3. never reveal exact boss waypoint as fallback.

Do not implement fragile map-rendering hacks just to draw a circle.
