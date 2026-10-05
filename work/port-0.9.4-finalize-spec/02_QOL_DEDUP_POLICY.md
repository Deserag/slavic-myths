# QOL DE-DUPLICATION POLICY

Goal: stop spending project time recreating generic functionality already provided by mature ecosystem mods.

## JEI
Owns:
- generic item list;
- generic recipe lookup.

Slavic Myths keeps:
- JEI integrations/categories for custom processes;
- unique Slavic screens.

## Jade
Owns:
- generic entity/block identification;
- ordinary entity HP information.

Slavic Myths keeps:
- boss bars;
- boss phases;
- unique mechanic statuses;
- Yaga favor/UI;
- ritual-specific UI.

If a generic Slavic HP HUD exists, remove/disable it cleanly instead of porting old rendering code.

## Xaero Minimap + World Map
Own:
- minimap;
- full world map;
- generic user waypoints.

Slavic Myths keeps:
- path-stone travel mechanics;
- Putevodny Klubok;
- structure discovery gameplay.

Do not write a custom Slavic map.

## FallingTree
Owns:
- generic fast tree felling.

Slavic Myths keeps:
- its own tree blocks/worldgen.
- correct log/leaves tags so ecosystem mods can recognize trees.

Do not implement a new TreeCapitator in core.

## Do not over-apply this rule
Do NOT replace unique themed systems just because a generic mod has a superficially similar feature.
