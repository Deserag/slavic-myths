# ITEMS — geometry, art, behavior VFX, exact animation cues

## Visual specification
Do not recolor the same whistle icon for all heroes. Existing approved hero silhouettes are all unique and call items must likewise uniquely recognizable at 32×32. Create from source art in nearest-neighbor style at >=64×64 PNG, render inventory GUI and first-person hand. UV sprites map 0..1 with 2 px alpha border, nontransparent shapes use 4-shade ramps.

| Item | Icon silhouette | Position in 64×64 canvas | Dominant colors |
|---|---|---|---|
| Alyosha whistle | short copper whistle + red cord + blue gem | metal from (12,13) to (51,43); cord extends to (29,59) | #A77B3C #E8C66A #9C3A2D #2C83C9 |
| Ilya token | square steel seal with red shield/cross | x(12..51), y(9..51), strap y(51..58) | #60666D #AD3F32 #E9C970 |
| Dobrynya emblem | thin bright silver spearhead | from (18,50) to (46,7), blue ribbon (22,44) | #BBC9CE #7F93A2 #C1A05A |
| Beast horn | curved ivory+brown horn, red wrap | from (10,52) to (55,10), tip NE | #4B342B #E5CDA4 #A13335 |
| Skim egg | oval dark brown w red/ivory mottles | bounding (15,9)..(48,55) | #3B2A25 #9A3430 #D6C19D |
| Idolishe egg | warm dark mud + bone spots | same | #59413B #C1A48A |
| Marinka egg | near black violet + crimson spots | same | #2F222C #9B3351 |

## In-hand use animation
Hero summon item (whistle / token / emblem): t0 held near belt, t4 move to chest, t8 hand raise, t11 glint & localized particles, t12 server call/recall, t16 hand lowers, t22 done. If already called, use second t12 recall only out of fight. No overlarge colorful full-screen explosion. Alyosha whistle plays high-mid chirp unlike horse call, not horse-neigh reused sound. Ilya token clang, Dobrynya emblem spear shimmer.
Beast horn: t0 lift from waist, t9 raised to mouth, t12–28 low rumbling horn, t20 server validates one active encounter, t29 lower, t38 recovery. On failed location/timing no spawn, early hand drop by t10 and subtle negative sound. Boss egg standard right-click placement as vanilla.

## Registration notes
- Register both `items` and logical `sounds`, creative tab/order, food=false, max stack1 for summons, durability absent (`isDamageable=false`), cooldown tracked per item/player not persisted as permanently consumed.
- `UseOnContext`/interaction results should respect client/server to avoid double entity; don't claim `interactionResult` API exists unchanged across versions; inspect the current source just enough to use existing NeoForge conventions.
- Test item icons when GUI scaled 2x, when dropped in world and held in 1st/3rd person. The whistle and horn need distinguishable item transforms. Translation tooltip paragraphs short and accurate, no bogus damage bonus or 'reward' text.
