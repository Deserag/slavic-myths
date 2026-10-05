# PHASE 3 — ENTITIES + CLIENT + NETWORK + UI
## Use together with MASTER prompt

Goal:
Port all runtime entity/client/UI behavior.

Do:
- entity types;
- attributes;
- AI/goals;
- boss bars;
- spawn eggs;
- renderers/models/layers;
- effects/damage;
- typed payload networking;
- screens/menus;
- commands;
- key/input packets;
- Curios rendering/inventory;
- JEI integration.

Preserve:
- attack numbers;
- AI state timing;
- boss phases;
- Hunt behavior;
- Yaga UI rules.

No GeckoLib migration in this phase.

Exit criteria:
- common + client source compiles;
- dedicated server common code has no client references;
- network packet inventory fully mapped to payloads.
