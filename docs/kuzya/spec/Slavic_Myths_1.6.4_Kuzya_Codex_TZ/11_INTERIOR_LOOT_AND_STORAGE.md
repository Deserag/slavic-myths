# One lootable vanilla barrel in Baba Yaga hut

## Placement
One vanilla `minecraft:barrel` integrated in existing kitchen/storage zone, at safe waist height or floor position, **right-click opens vanilla 27-slot inventory**. Do not create GUI from scratch. Accessible even with Baba Yaga and Kuzya present. Spawn with loot table once on structure placement; never refill on ordinary chunk reload / player reentry / command to summon Kuzya.

## Proposed `baba_yaga_household_supplies` loot table balance
At structure generation roll **4–7 item groups total**, independently, with 1–3 stacks of food/herbs among them. Full manifest `manifests/HUT_BARREL_LOOT.csv`; actual item IDs MUST be dynamically verified against current game registry and replace nonexistent named resources with sensible vanilla fallback. Example content groups:
- common: `minecraft:glass_bottle` 1–3 75%, `minecraft:bowl` 1–2 55%, `minecraft:paper` 1–3 35%; `minecraft:brown_mushroom` 1–4 60%, `minecraft:red_mushroom` 1–3 50%, `minecraft:spider_eye` 1 18%;
- rare useful: `minecraft:honey_bottle` 1 18%, `minecraft:fermented_spider_eye` 1 12%, `minecraft:glow_berries` 1–4 25%; `minecraft:golden_apple` **forbidden**;
- mod-specific (if exists): existing relevant herb/root/berry 1–3 with 15–35%, existing simple ritual decoction/potion 1 with ≤12%; NEVER invent potion item ID without implementing it.

## Loot constraints
- Loot per barrel low to moderate, not a new shortcut to god-tier artifacts, no boss material, rare Firebird feathers, rune artifacts, netherite, etc.
- If existing Baba Yaga quest system reserves inventories/rewards, separate container ownership and loot. No loot table on Kuzya's decorative chest.
- In-world visually plausible: inside barrel jars/herbs/food; no weird redstone factory loot.
- Validate new generated hut always has exactly one barrel and it opens/rolls/stays persistent. Legacy hut content never silently overwritten.
