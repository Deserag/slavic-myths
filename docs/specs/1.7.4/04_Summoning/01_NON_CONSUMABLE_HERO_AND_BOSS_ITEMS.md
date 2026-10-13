# Summoning — names, items, implementation, fail behavior

This is a recurrent missing feature in previous versions: entities must be accessible in actual game, not just via `/summon`. REUSE exact Sivka/Burka style and hero registration infrastructure. New items have production-ready actual transparent PNG in `06_References/Technical/Item_Sprites` (64x64 nearest-neighbor no smoothing).

## Item A — Finist's Falcon Feather (Перо Финиста)
Registry candidate `finist_call_feather` unless conflicts with current code; 64x64 RGBA item view: long curved ivory wing feather 42px high tilted 23° right, navy blue rachis (central stem) 3–4 px, eight angular golden barbs on right side and four grey bars on left, gold ribbon near quill base, 2px deep-blue outline. Not a generic vanilla feather recolor. Inventory tooltip RU: `Призывает Финиста Ясного Сокола. Не расходуется. В сюжетном бою действуют ограничения.` English equivalent. Creative tab same as other hero summoning trinkets; normal survival obtainability: if an existing hero summon quest/knowledge condition exists, hook to it, do not invent new loot/rewards in 1.7.4. Temporary creative access is mandatory for testing if full progression not yet integrated.

Right-click air/solid face: if owner has <2 active heroes and no personal story encounter, spawn FINIST on safe ground <=3 blocks from owner with owner UUID. If already summoned, toggle recall/follow behavior through existing pattern; on repeated click DO NOT spawn duplicate. When user is in Kartaus personal encounter, allow Finist only if summon happens before boss challenge action, never during active battle. When boss already active, item displays actionbar `Нельзя призвать нового богатыря во время боя` and stays untouched. Item ALWAYS stays in inventory; no durability or stack shrink even with failed spawn. Item cooldown SUCCESS 40 ticks, FAIL 8 ticks client UI anti-spam, no per-player global high churn.

## Item B — Black Banner Seal (Печать чёрного знамени)
Registry `kartaus_black_seal` 64x64 RGBA item (actual sprite included). Black square angular talisman with red eye rune center and tiny rust-orange moustache curl emblems at bottom corners, iron rim and crimson string. Distinct from summon egg, frame icon, item no shiny animated shader. Survival re-usable chapter challenge item, NOT unique loot/reward. Obtained through existing story progression if present; if absent expose in creative plus document admin/dev acquisition rather than invent unrelated craft. Right-click safe *clear ground* proposes encounter start with owner, verifies prerequisites below. If valid, consume NO item, create lightweight temporary battle at player location (see `05_Encounter`). If invalid, localized reason and no entity placement (no digging new field).

## Item C — Kartaus Spawn Egg (creative/test)
`kartaus_spawn_egg`, base charcoal `#242127`, secondary rusty orange `#C65B31`, irregular crimson rune flecks; use normal NeoForge spawn egg pattern registry for entity as supported by project. Spawn boss for manual testing without narrative owner lock; never auto-award progress/unique items from creative/debug spawn, no spreading minions into villages; prevent creative spawning duplicates from incorrectly overwriting nearby encounter ownership.

## Actual visibility / recipes / easy testing
Put item(s) in same correct creative tab. Register model `item/generated` PNG layer0, translation EN/RU, tooltips at runtime, sound `items/finist_call.ogg` / `items/kartaus_seal.ogg`. No unused JSON model, no purple-black missing texture. Direct developer command test can use `/give <player> <modid>:finist_call_feather`, `/give ... kartaus_black_seal` and egg, replacing `<modid>` with project real one.

## Exact error matrix
| Condition | Expected action |
| --- | --- |
| Hero already summoned by player | recall/retask same entity, never duplicate |
| Two allied heroes already active in open world | refuse 3rd with message, item unchanged |
| Kartaus personal event active, hero already Finist | allow existing Finist to fight; no re-summon replacements |
| Kartaus event active, hero absent/retreated | deny new summon and preserve slot, no free reset |
| Other hero already summoned before Kartaus event | deny boss challenge with message `Для этого испытания нужен только Финист` rather than silently deleting ally |
| Location too cramped/unsafe/dimension invalid | refuse challenge, no item loss, no phantom boss |
| Owner offline or event abandoned | cleanup tagged enemies, leave hero inventory intact, keep durable progress already confirmed |
| Two players use seal same tick within same 32 radius | preserve separate owner if area allows; otherwise refuse second; no shared duplicate boss |
| Right-click client+server events | server handles exactly once, consumes zero even if prediction |
