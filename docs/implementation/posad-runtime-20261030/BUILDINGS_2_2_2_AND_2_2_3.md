# Пекельный посад — checkpoint 2.2.2 + 2.2.3, 30.10.2026

Scope: #347 Вечевая палата + #348 Общинный жилой корпус; parent #358. Branch hell only.

## Source of truth
- Hall: exact 3598 non-air positions from 2.2_VECHE_HALL_VOXELS_R1.py, packed SHA256 8813d5c44e144ba5e6f087584f7acbf8dfa4cf25c0e31f6f9193f0b933035cda.
- House: exact 1992 positions from 2.2_COMMUNAL_HOUSE_MODEL_R1.json, 4 approved chest markers and their original loot drafts.
- Slot anchors: hall city origin 43/13 (models include -1 overhang); communal city origin 14/16 (models include -1 overhang).
- Only new native PosadPiece schema 2 invokes models; previously persisted schema 0/1 never silently adds these buildings. Full server-owned retrofit is not implemented.
- No independent structures or spawns. The structure_set for nether_posad has NOT been activated here.

## Requirement | Evidence | Status
| Requirement | Evidence | Status |
| --- | --- | --- |
| Exact hall/house voxels | Runtime parser + checksum/count checks, original model resources | CODE / CI NOT_RUN |
| Four fixed loot tables | JSON drafts copied byte-exactly; lazy vanilla loot assigned to newly placed chest once | CODE / GAME NOT_RUN |
| 4 city rotations and nonoverlap | PosadPiece existing city-local rotate; offline Python QA script | TEST NOT_RUN |
| Save/reload and no destructive retrofit | Persisted schema gate; old stages omitted, no automatic retrofit | PARTIAL / RUNTIME NOT_RUN |
| Passage / headroom / stairs / bed safety | Original source model only; no vanilla bed | GAME QA NOT_RUN |
| 5+ seeds, biome/terrain, chunk borders, MP, real client screenshots, performance | Not run here | NOT_RUN |
| Java21 compile + NeoForge GameTest / dedicated server | Not run here | NOT_RUN |
| Original owner-approved PNG visual reference | Not in GitHub | BLOCKED ART |

No Issue is closed or marked owner accepted. This is an integration checkpoint, not a completed 2.2 release. Preserve isolated PolyMC and main branch.
