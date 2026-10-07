# Functional audit 1.1.8

Source/resource audit of the requested furniture only. Interaction, save fields, removal ownership and acquisition were inspected; no game, server, rejoin or actual block-break test was run.

| Existing system | Interaction / saved state / drops / acquisition |
| --- | --- |
| Chairs, stools, benches | Existing seating actor; temporary no-save seat, no stored inventory; ordinary block loot; crafts retained. |
| Pine/linden tables | Existing pairing and four real food stacks per half; `TableTile` saves inventory; removal returns food once; crafts retained. |
| Kitchen table | Two-part master, grouped menu, preparation/servings/returns saved; master owns block and contents; definitive craft repaired. |
| Produce crate | **Fixed:** actual 9-slot menu; server slot predicate retains produce tag; saved real stacks and existing 3D display; ordinary block plus inventory drops once; craft retained. |
| Household chest | 27-slot menu, actual inventory, viewer/open state; contents saved and returned on removal; craft retained. |
| Wooden barrel | Nine same-kind storage slots and existing fermentation transaction; saved batch age and inventory; only active fermentation ticks; contents returned, unfinished batch discarded by existing rule; craft retained. |
| Sacks / baskets | Existing direct insert/extract and real stacks; stored Items and sack fill derived from contents; removal returns contents; crafts retained. |
| Wall / household shelves | Existing direct 3/6-stack display storage; saved Items; multiblock master removal returns contents once; crafts retained. |
| Drying rack | Four independent existing recipe snapshots/timers; save/load preserves progress; actual contents returned once; craft and 2400-tick recipes retained. |
| Feeder | Direct feed insert/extract; 16 actual feed units saved, existing feeding goal consumes; contents returned; seven-plank craft retained. |
| Straw nest | Direct three egg positions, NestGoal lays real eggs; Items saved; removal returns eggs; craft retained. |
| Flax breaker / wheel / loom | Existing direct insertion/work/extraction; Input/Output/Progress saved and clamped; removal drops actual inputs/outputs; crafts/process ratios retained. |
| Linen bed | Existing vanilla sleep and completed-sleep Well Rested event; no invented inventory; bed craft/loot retained. |
| Fruit press | Existing three manual actions, inputs and output/pomace saved; removal returns actual stacks; definitive craft repaired. |
| Fermentation vat | Existing four ingredients/catalyst/returns/output menu and saved process snapshot; removal returns actual contents; screen registered once; definitive craft repaired. |
| Small keg | Existing beverage/quality/1-or-4 portion transfer and eight servings; saved data; removal returns filled servings plus block; definitive craft repaired. |
| Older decorative furniture | Old decorative shelf, cloth bag, wooden crate, bedside cabinet, firewood bundle and dummy stay decorative by design. Wardrobe, weapon rack and signal bell retain their existing distinct behavior. |

Manual acceptance must cover GUI click/Shift-click, actual save/rejoin, contents on break, completed sleep, seating, food pairing and processes. Static evidence does not establish successful runtime persistence.
