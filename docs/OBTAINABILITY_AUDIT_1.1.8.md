# Obtainability audit 1.1.8

165 existing item IDs were reviewed across farming, gardens/apple wood, husbandry, textiles, Kitchen II, household storage and brewing. Evidence inventory: `work/obtainability-1.1.8.json`; repeatable gate: `tools/verify_integration_118.py`. Classification distinguishes crafting, registered processing, harvesting/worldgen, carcass loot, spawn eggs and hidden process containers.

Missing acquisition routes found: 0; restored missing routes: 0; unresolved routes: 0. Five incorrect or superseded crafting recipes were corrected: kitchen table, dark bottle, small keg, fruit press and fermentation vat. All five have exactly one crafting recipe. Dark bottle uses four glass and one stick, producing two. Feeder retains the approved seven-plank pattern. Seed/produce and fruit/sapling shortcuts were not introduced.

Reviewed processes include 22 kitchen dishes, milling, flax → fibre → thread → cloth, three drying recipes, manual pressing, 15 vat recipes, eleven fermentation batches and serving. Hidden wort/must IDs and `filled_pitcher` have actual processing paths; the three spawn eggs remain Creative-only. JSON shaped patterns have defined keys, no unused keys and valid dimensions. This is source/resource evidence, not a survival playthrough or recipe-book/JEI acceptance.
