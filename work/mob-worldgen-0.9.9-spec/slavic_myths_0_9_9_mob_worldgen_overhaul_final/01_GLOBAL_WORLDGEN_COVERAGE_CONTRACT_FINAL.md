# 01 — GLOBAL WORLDGEN COVERAGE CONTRACT
## THIS OVERRIDES ALL PREVIOUS LOOSE SPACING GUIDANCE

The problem is global, not Kurgan-only.

The same availability failure has been observed for Kurgans and Bandit Camps.
Assume shared worldgen is defective until proven otherwise.

---

# 1. CORE UX RULE

A normal player must be able to encounter mod content by exploring.

They must NOT be expected to:
- travel 7000–10000 blocks for ordinary progression content;
- use dev commands;
- repeatedly teleport 1000 blocks and test `/locate`;
- know source code to find structures.

"Rare" is allowed.
"Effectively absent" is not.

---

# 2. DISTANCE METRIC

All hard distances use true horizontal Euclidean X/Z distance:

`distance = sqrt(dx*dx + dz*dz)`

This includes diagonal travel.

Example:
dx=3000, dz=3000 => 4242.64.

That FAILS a 4000 limit.

Never use a square search radius as proof of compliance.

---

# 3. GLOBAL TIER MODEL

Every normal exploration structure belongs to a family and tier.

Tier SMALL:
- local/common entry structure;
- max network gap: 1000.

Tier MEDIUM:
- uncommon/stronger structure;
- must stay within 2000 of its relevant SMALL network.

Tier LARGE:
- major/rare structure;
- must stay within 4000 of its relevant SMALL/MEDIUM network.

ABSOLUTE RULE:
No ordinary exploration family may require a >4000 jump from its local small network to its largest content.

---

# 4. FAMILY EXAMPLES

KURGAN:
Small Kurgan = SMALL
Warrior/Medium Kurgan = MEDIUM
Great Kurgan = LARGE

BANDITS:
Small Camp = SMALL
Fortified Camp = MEDIUM
Large Settlement / Solovey regional stronghold relationship = LARGE/SPECIAL according to actual registry

SWAMP:
micro/small hut/boardwalk = SMALL
watchtower/shrine/medium site = MEDIUM
rare major shrine cluster = LARGE

WATER:
small shore/water POI = SMALL
larger flooded/underwater location = MEDIUM/LARGE according to actual registry

Other registered structure families:
audit and assign equivalent tier.

---

# 5. BIOME-RESTRICTED CONTENT

A structure cannot be forced into an invalid biome merely to satisfy geometry.

Therefore coverage is measured over the eligible-region network.

However biome restriction must NOT be abused to explain missing content.

For each family:
- map eligible biomes;
- identify valid regions;
- within a sufficiently large eligible region, apply the hard coverage rules;
- if a valid region is narrow/fragmented, use nearest valid placement and record it.

If current allowed-biome list makes content practically absent, fix the allowed-biome policy.

---

# 6. DO NOT USE PURE RNG EXISTENCE

Forbidden:

`roll spawn chance -> fail -> structure does not exist in this macro-region`

when repeated failure can create multi-kilometer gaps.

Use deterministic coverage cells / regions.

RNG may choose:
- jitter;
- rotation;
- template variant;
- local decoration.

RNG must not decide whether basic content disappears for 8000 blocks.

---

# 7. COVERAGE ARCHITECTURE

Implement a shared service such as:

`StructureCoverageService`
`StructurePlacementProfile`
`CoverageCell`
`PlacementCandidate`
`TerrainScan`
`TerrainAdaptationPlan`
`PlacementDecision`

Names may follow repository conventions.

The concept is mandatory.

Each profile contains:
- family;
- tier;
- eligible biome/tag;
- footprint;
- water tolerance;
- slope tolerance;
- tree handling;
- replaceable decoration handling;
- terraform cut/fill budget;
- collision rules;
- hard coverage relationship;
- local candidate radius;
- unique-structure constraints.

---

# 8. LOCAL SEARCH, NOT DISTANT ESCAPE

Candidate search offsets stay local.

Recommended rings:
0
16
32
48
64
96
128
160
192
256

Search multiple directions.

Do NOT react to a bad candidate by jumping 1000/2000/4000 more blocks looking for perfect flat terrain.

---

# 9. TERRAIN ADAPTATION

Ordinary terrain is NOT a rejection.

Replaceable:
- grass;
- tall grass;
- flowers;
- fern;
- snow layer;
- small mushrooms;
- similar decorative plants.

Moderate terrain:
- cut small rises;
- fill shallow depressions;
- produce natural foundation;
- clear only footprint trees;
- use supports/terraces when profile allows.

Land structures reject only serious conflicts:
- deep water beyond profile;
- lava;
- protected structures;
- block-entity conflicts;
- extreme cliff/ravine;
- impossible world bounds;
- excessive terraform budget.

---

# 10. SUPERFLAT REGRESSION

A clear Superflat area is an ideal placement test.

If:
- land is level;
- no lava;
- no deep water;
- no protected structure;
- no relevant block entities;

then candidate MUST be ACCEPTED.

Snow layers do not change this.

If Superflat returns `NO_SAFE_LOCATION`, test fails.

---

# 11. COMPENSATION

If a coverage cell cannot place its intended structure:
- do not silently skip forever;
- test alternate local candidates;
- test neighboring coverage candidates;
- verify resulting network gap.

If the skip causes hard gap violation:
create a compensating candidate in nearest eligible area.

---

# 12. LOCATE

`/slavicmyths locate ...` must query deterministic placement/coverage information or proper Minecraft structure placement.

It must not only inspect already-loaded chunks.

Registered structures should also work with vanilla:
`/locate structure slavicmyths:<real_id>`

where architecture permits.

Increasing locate radius is NOT a worldgen fix.

---

# 13. HARD COVERAGE TESTS

Run several seeds.

Generate/simulate at least a region large enough to expose multi-km gaps.

For each family output:
- count by tier;
- min nearest distance;
- mean;
- median;
- p95;
- maximum;
- rejected candidates by reason;
- compensating placements.

PASS:

SMALL relevant network max gap <= 1000.

MEDIUM distance to nearest relevant SMALL network <= 2000.

LARGE distance to nearest SMALL/MEDIUM network <= 4000.

Diagonal metric must be Euclidean.

---

# 14. EXACT REJECTION REASONS

No generic:
"height, water, buildings or intersection".

Use:
INVALID_BIOME
DEEP_WATER
LAVA
WORLD_BOUNDS
PROTECTED_STRUCTURE
BLOCK_ENTITY_COLLISION
EXTREME_TERRAIN
TERRAFORM_BUDGET_EXCEEDED
UNDERGROUND_COLLISION
SPACING_CONFLICT
UNIQUE_STRUCTURE_RULE

Dev diagnostics print actual measured values.

---

# 15. REQUIRED AUDIT

Audit EVERY registered Slavic Myths worldgen structure:
- Kurgans;
- bandit camps/settlements/Solovey location;
- Yaga hut;
- bathhouse;
- swamp/water POI;
- hunting/creature locations;
- world boss anchors/locations;
- all other structures discovered in actual registry/data.

Special unique structures may use custom bands, but they still require explicit hard maximum and deterministic availability.

Do not leave `UNKNOWN` entries in final report.
