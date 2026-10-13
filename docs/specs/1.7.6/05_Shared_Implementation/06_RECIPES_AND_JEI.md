# Exact proposed 3x3 recipes; use existing recipe tech and JEI registration

Vanilla IDs are examples using 1.21.1 JSON semantics. Align actual recipe version and ingredient IDs with current project only; do NOT change loader / Minecraft version. All craftable trophy pieces must have registered recipes. Runes and hero gifts must not be freely craftable.

Legend: I = mod:mat_bone_plates; M = mod:mat_mirror_shards; F = mod:mat_skim_fang; B = mod:mat_banner_remnant; K = mod:mat_royal_iron. Use actual item registry paths `mat_*` above.

| Result | row1 | row2 | row3 | Additional ingredient mapping |
|---|---|---|---|---|
| Idol chest | `I I I` | `L D L` | `. I .` | L leather, D diamond chestplate; replace 2 spare lower positions with bone blocks if exact 9 cells allowed; preferred exact recipe `I I I / L D L / O I O`, O bone_block. Total I4 |
| Marinka cloak | `M M M` | `R C R` | `D M D` | R red_dye, C leather_chestplate, D black_dye, M4 |
| Skim necklace | `F S F` | `. N .` | `. L .` | S string, N iron_nugget, L leather, F2 |
| Kartaus mantle | `B B B` | `O C R` | `. B .` | O obsidian, C leather_chestplate, R red_dye, B4 |
| Kalin saber | `K K .` | `. D K` | `G K K` | D diamond_sword, G gold_ingot, K5 |

Recipe shape and orientation can be mirrored only if current recipe system already allows it. Never register recipes for boss-exclusive mats or runes that bypass bosses. First-clear quantities guarantee first associated trophy craft except when material costs 4/5 explicitly set. Show JEI category for new recipes and tooltips for how to obtain source boss materials, without adding duplicate JEI integrations.

Check recipe `Idol chest` result: four plates at row1 positions all three plus row3 middle. Bone blocks in row3 corners. Recipe count nine grid cells. Require actual chest armor item with no loss of enchantments unless the existing crafting behavior specifically carries them; if copy, use existing safe smithing adapter rather than invent JSON that discards NBT invisibly.
