# TARGET PLATFORM / SOURCE SNAPSHOT

## Public project
https://github.com/Deserag/slavic-myths

Checked current master:
- project version shown in build.gradle: 0.9.3
- ForgeGradle 5.1.77
- Forge 1.16.5-36.2.42
- Java 8
- Curios Forge 1.16.5
- JEI 1.16.5

## NeoForge official docs
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/
- https://docs.neoforged.net/docs/1.21.1/items/
- https://docs.neoforged.net/docs/1.21.1/items/datacomponents/
- https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
- https://docs.neoforged.net/docs/1.21.1/networking/
- https://docs.neoforged.net/docs/1.21.1/gui/screens/
- https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
- https://docs.neoforged.net/primer/docs/

## Important 1.21 changes already identified
- Java 21 required for NeoForge 1.21.1.
- data/resource registry folders such as recipes/advancements/loot_tables and several tag folders were singularized.
- ResourceLocation constructors changed.
- ItemStack state is data-component oriented.
- NeoForge Data Attachments are available for persistent entity/player data.
- SavedData API is modernized.
- Networking uses typed payload registration.
- screens/menus registration/render APIs changed.
- biome modifiers provide data-driven feature/spawn injection.
- Minecraft 1.21 enchantments are datapack registry objects; full enchantment redesign is deliberately postponed.

## Existing ecosystem dependencies
Curios NeoForge 1.21.1 stable builds exist.
JEI NeoForge 1.21.1 stable builds exist.
Use actual stable versions available when Codex runs rather than assuming an outdated beta.
