# SEARCH AREA DESIGN NOTES

The most important gameplay rule:

**The displayed search area's center MUST NOT be the boss coordinate.**

The system should deliberately make the boss appear commonly in the middle-to-outer part of the area.

Conceptual example:

Actual boss:
X=1000, Z=2000

Quest search radius:
R=320

Deterministic target fraction:
f=0.82

Displayed search center may be around:
X=760, Z=1860

The boss is still inside the zone, but walking to the visible center does not solve the quest.

Good player experience:
- map tells player which REGION matters;
- travel time is reduced;
- actual search/exploration remains;
- terrain, sounds and visual spotting still matter.

Bad implementation:
- search circle centered exactly on boss;
- hidden exact waypoint sent to client;
- exact coordinates shown after entering region;
- arrow points from approximate zone directly to boss;
- zone follows boss every tick.

SearchArea is approximate information, not a scanner.
