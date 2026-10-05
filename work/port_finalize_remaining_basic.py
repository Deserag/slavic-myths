from pathlib import Path
import re
# General names with exact target counterparts.
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');o=s
 for a,b in {'net.minecraftforge.client.event.':'net.neoforged.neoforge.client.event.','net.neoforged.neoforge.event.tick.ClientTickEvent':'net.neoforged.neoforge.client.event.ClientTickEvent','net.minecraft.world.chunk.Chunk':'net.minecraft.world.level.chunk.LevelChunk','net.minecraft.tileentity.CampfireTileEntity':'net.minecraft.world.level.block.entity.CampfireBlockEntity','net.minecraft.entity.passive.SheepEntity':'net.minecraft.world.entity.animal.Sheep','net.minecraftforge.event.entity.EntityJoinWorldEvent':'net.neoforged.neoforge.event.entity.EntityJoinLevelEvent'}.items():s=s.replace(a,b)
 s=s.replace('EntityJoinWorldEvent','EntityJoinLevelEvent')
 s=s.replace('public boolean canBeLeashed(Player player)','public boolean canBeLeashed()')
 if p.name=='ModItems.java':
  s=s.replace('RegistryObject<? extends net.minecraft.world.entity.EntityType<?>>','java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<?>>').replace('new Food.Builder()', 'new net.minecraft.world.food.FoodProperties.Builder()').replace('new SoupItem(', 'new Item(').replace('net.minecraft.world.item.LilyPadItem','net.minecraft.world.item.PlaceOnWaterBlockItem')
  s=s.replace('nutrition(nutrition).saturationMod(saturation).build())));}\n    private static DeferredHolder<Item, Item> egg', 'nutrition(nutrition).saturationMod(saturation).usingConvertsTo(Items.BOWL).build())));}\n    private static DeferredHolder<Item, Item> egg')
 if p.name=='SmokeAging.java':s=s.replace('s.getBlock().getRegistryName()', 'net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock())')
 if p.name=='PathData.java':s=s.replace('p.inventory','p.getInventory()')
 if p.name=='WildlifeEntity.java':
  s=s.replace('maxUpStep=kind==Kind.BEAR?.9F:.6F;', 'getAttribute(Attributes.STEP_HEIGHT).setBaseValue(kind==Kind.BEAR?.9F:.6F);').replace('animationSpeed','walkAnimation.speed()')
  s=s.replace('protected ResourceLocation getDefaultLootTable(){return ResourceLocation.fromNamespaceAndPath("slavicmyths","entities/"+kind.name().toLowerCase(java.util.Locale.ROOT));}', 'protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","entities/"+kind.name().toLowerCase(java.util.Locale.ROOT)));}')
 if p.name=='SerpentProjection.java':s=s.replace('protected net.minecraft.resources.ResourceLocation getDefaultLootTable(){return net.minecraft.loot.LootTables.EMPTY;}', 'protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;}')
 if p.name in ['NightingaleEntity.java','ElementHuntMob.java','HuntMob.java','WorldBoss.java']:
  s=re.sub(r'\byRot=(\(float\)Math.toDegrees\(Math.atan2\(-aim.x,aim.z\)\));',r'setYRot(\1);',s)
  s=s.replace('yBodyRot=yRot;', 'yBodyRot=getYRot();').replace('yHeadRot=yRot;', 'yHeadRot=getYRot();')
 if s!=o:p.write_text(s,encoding='utf-8')
