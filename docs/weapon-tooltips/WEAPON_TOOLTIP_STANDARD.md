# Slavic Myths — unified weapon tooltip and title standard

Target: Minecraft 1.21.1 / NeoForge, branch `feature/weapon-skill-input-standard-20261122`.

## Scope

The source catalog in `docs/balance/weapon-audit-20261122/CATALOG.md` contains 102 weapon-related IDs, including shields, ammunition, bows, conventional melee weapons and skill-enabled boss rewards. The client-side `WeaponTooltips` handler explicitly enumerates those 102 IDs so unrelated items, accessories and armor do not inherit this style. Later unlisted weapons require review and registration.

No new game mechanics, attack strengths, attack speeds, cooldowns, drops, recipes or controls are defined by this style layer.

## Color palette

| Element | RGB | Minecraft analogue |
|---|---|---|
| Normal/standard weapon title | `#E4E4E7` | near-white |
| Rare title | `#55FFFF` | AQUA |
| Epic title | `#AA55FF` | LIGHT_PURPLE |
| Elder serpent weapon title | `#FFAA00` | GOLD |
| Elder-being subtitle | `#D4AF37` | muted gold |
| Active skill name / description | `#FFAA00` | GOLD |
| Damage and important offensive values | `#FF5555` | RED |
| Healing or positive effects | `#55FF55` | GREEN |
| Cooldowns, restrictions and supporting text | `#AAAAAA` | GRAY |
| Controls and key combinations | `#FFFF55` | YELLOW |

The semantic classes above apply to text only; the code does not replace lore or alter physical equipment rarity. For readable results avoid coloring an entire tooltip uniformly and do not use black text as a primary weapon name color.

## Exceptional origin: three elder serpents

Golden names are **provenance based**, not simply based on word matching or `Rarity.EPIC`:

| Elder being | Weapon IDs | Verified origin |
|---|---|---|
| Zmey Gorynych | `serpent_crossbow`, `three_breath_crossbow` | Both registered by `GorynychContent` and implemented by `SerpentCrossbow`; the second has additional elemental modes |
| Greater Morovoy Fire Zmey | `morovoy_staff` | The crafting recipe consumes the `morovoy_fire_zmey_core` and `morovoy_fire_zmey_bone` |
| Ocean Zmey (water serpent) | `storm_anchor`, `storm_harpoon` | Both recipes consume `ocean_zmey_storm_heart` |

Items used merely as ingredients, spawn eggs, decorative rewards and dragon armor are excluded. This establishes five weapon IDs, not all serpent-related items. A subdued gold `Elder being` status line is supplied in `ru_ru` and `en_us`. Other boss rewards retain rarity-based colors.

## Recommended layout

Only show blocks that actually exist on the item:

1. **Weapon name** — normal, rare or epic title color, or gold for verified elder-serpent weapons.
2. **Primary ability** — concise name, actual input, short action, relevant damage, reach, duration and cooldown.
3. **Secondary ability** — name, actual input, values and conditions, when implemented.
4. **Passives** — modifiers and activation conditions, when implemented.
5. **Restrictions** — water-only, charged release, target restrictions, resource use, cooldown, when implemented.

No fictional lore, blank ability headings, invented cooldowns or fabricated damage numbers.

## Controls

Localized Russian text: `ЛКМ`, `ПКМ`, `Shift + ЛКМ`, `Shift + ПКМ`. English text: `LMB`, `RMB`, `Shift + LMB`, `Shift + RMB`. Charged attacks must say to hold and release; only describe a combo if a corresponding handler is present. Text is a description of the existing implementation, never an input-remapping mechanism.

## Numeric truth

Read Java handlers and modifier sources *after all five balancing batches*. Damage should be labeled base, projectile, charged or multi-target as appropriate. Do not treat baseline default melee attributes of a staff/bow/shield as the damage dealt by its ability. For display, 20 ticks = one second; preserve actual runes, enchantment multipliers, conditional control resistance, target limits and modes. Dynamic damage should not be advertised as a guaranteed constant.

A tooltip may contain existing literal Java fragments and dynamically translated strings; the client presentation handler colors descriptions without changing game calculations. Numeric values that originate from localization should be synchronized with their source constants when they change.

## Custom names, enchanting and compatibility

`WeaponTooltips` changes only the first rendered title line and description line appearance for the catalog IDs. When a stack contains `DataComponents.CUSTOM_NAME`, its user-provided name is never forcibly recolored. No `ITEM_NAME` or other persistent item component is rewritten. Enchantments, rarity metadata, crafting, save data and server logic are untouched. The style event is restricted to the mod namespace and only 102 known IDs.

## Localization policy

Player-facing strings in Java should prefer `Component.translatable(...)`. Both `ru_ru.json` and `en_us.json` must define every new key, with matching key sets and valid JSON. Avoid duplicating translated text in literal Java components. No special formatting should cause a raw translation key to display.

## Validation

`tools/weapon_tooltip_style_check.py` checks the 102 catalog IDs against the client color-handler set, five elder-serpent IDs, color constants, anvil custom-name protection, duplicate localization keys and the translated ability keys added to the relevant Java files. `.github/workflows/weapon-skill-input-standard.yml` runs this validation and the existing weapon-input check before the Java 21 Gradle clean build.

Manual client verification remains necessary for layout, font legibility, long English phrases, custom name display, modifier interaction and multiplayer tooltips; a successful CI build does not prove visual appearance.

## Semantic token-color regression rule

When a positive-effect line contains numeric duration or amount tokens (for example localized healing or regeneration text), those numbers stay green `#55FF55`; the numeric-token highlighter must not turn them gray. Explicit damage values remain red and input combinations remain yellow. The client renderer reuses a compiled pattern for token matching, and `tools/weapon_tooltip_style_check.py` guards this presentation invariant. This rule does not add abilities or modify any underlying status effects.
