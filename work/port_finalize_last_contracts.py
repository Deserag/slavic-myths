from pathlib import Path
import re,json
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');o=s
 s=s.replace('StairsBlock','StairBlock').replace('net.minecraft.tileentity.LockableLootTileEntity','net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity').replace('LockableLootTileEntity','net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity')
 s=s.replace('net.minecraftforge.event.furnace.','net.neoforged.neoforge.event.furnace.').replace('net.minecraftforge.event.ForgeEventFactory.onProjectileImpact','net.neoforged.neoforge.event.EventHooks.onProjectileImpact')
 s=s.replace('net.minecraftforge.registries.BuiltInRegistries','net.minecraft.core.registries.BuiltInRegistries').replace('net.minecraft.client.resources.I18n','net.minecraft.client.resources.language.I18n')
 s=s.replace('VanillaTypes.ITEM,','VanillaTypes.ITEM_STACK,').replace('VanillaTypes.ITEM)', 'VanillaTypes.ITEM_STACK)')
 s=s.replace('.getAdvancements().getAdvancement(','.getAdvancements().get(')
 if p.parent.name=='client':
  s=re.sub(r'(?<![\w.])Context (manager|m|c)',r'net.minecraft.client.renderer.entity.EntityRendererProvider.Context \1',s).replace('net.minecraft.client.renderer.entity.Context','net.minecraft.client.renderer.entity.EntityRendererProvider.Context')
 if p.name in ['CampStructure.java','LargeCampStructure.java','SwampStructure.java']:s=s.replace('BlockPos size=templates.getOrCreate(', 'net.minecraft.core.Vec3i size=templates.getOrCreate(')
 if p.name=='SwampStructure.java':s=s[:-2]+' private static final class Plan{final String name;final int x,z;final boolean wet;int y;Plan(String name,int x,int z,boolean wet){this.name=name;this.x=x;this.z=z;this.wet=wet;}}\n}\n'
 if p.name=='SwampStructures.java':s=s.replace('structure).anyMatch(', 'structure).stream().anyMatch(')
 if p.name=='BanditEntity.java':s=s.replace('.disableShield(true)', '.disableShield()').replace('new Arrow(level(),BanditEntity.this)', 'new Arrow(level(),BanditEntity.this,new ItemStack(Items.ARROW),getMainHandItem())')
 if p.name=='BanditRenderer.java':s=s.replace('new HeldItemLayer<>(this)', 'new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,manager.getItemInHandRenderer())')
 if p.name=='ArmorerScreen.java':s=s.replace('inventory.getDisplayName()', 'playerInventoryTitle')
 if p.name=='BannikEntity.java':s=s.replace('PotionUtils.getPotion(s)==Potions.WATER','s.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(net.minecraft.world.item.alchemy/Potions.WATER)'.replace('alchemy/','alchemy.')).replace('p.inventory','p.getInventory()')
 if p.name=='DomovoyEntity.java':s=s.replace('p.inventory','p.getInventory()')
 if p.name=='ElderVodyanoy.java':s=s.replace('yRot=(float)(Math.atan2(aim.z,aim.x)*180/Math.PI)-90;', 'setYRot((float)(Math.atan2(aim.z,aim.x)*180/Math.PI)-90);').replace('DamageSource.mobAttack(', 'damageSources().mobAttack(')
 if p.name=='BurialCoffinTile.java':s=s.replace('  @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(2);}\n','')
 if p.name=='BurialCoffinRenderer.java':pos=s.rfind('}');s=s[:pos]+' @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(BurialCoffinTile tile){return new net.minecraft.world.phys.AABB(tile.getBlockPos()).inflate(2);}\n'+s[pos:]
 if p.name=='ElementModel.java':s=s.replace('brightness,brightness,brightness,1)', 'net.minecraft.util.FastColor.ARGB32.color(255,(int)(brightness*255),(int)(brightness*255),(int)(brightness*255)))')
 if p.name=='HuntModel.java':s=s.replace('intensity,intensity,intensity,1)', 'net.minecraft.util.FastColor.ARGB32.color(255,(int)(intensity*255),(int)(intensity*255),(int)(intensity*255)))')
 if p.name=='KurganCreatureRenderer.java':s=s.replace('1,1,1,.75F)', '0xBFFFFFFF)')
 if p.name=='IgoshaEntity.java':s=s.replace('remove();','discard();')
 if p.name=='LeshyEntity.java':s=s.replace('new net.minecraft.core.BlockPos(x, getY(), z)', 'net.minecraft.core.BlockPos.containing(x, getY(), z)')
 if p.name=='PolevikEntity.java':s=s.replace('i.getThrower()!=null','i.getOwner() instanceof Player').replace('UUID id=drop.getThrower();Player p=level().getPlayerByUUID(id);','Player p=drop.getOwner() instanceof Player player?player:null;')
 if p.name=='RiverFish.java':s=s.replace('protected ItemStack getBucketItemStack()', 'public ItemStack getBucketItemStack()').replace('protected ResourceLocation getDefaultLootTable(){return ', 'protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,').replace('"entities/"+species);}', '"entities/"+species));}')
 if p.name=='WeaponTooltips.java':s=s.replace('stack.getItem().getRegistryName()', 'net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())')
 if p.name=='WorldBoss.java':s=s.replace('knockback(float power','knockback(double power').replace('remove();','discard();');s=re.sub(r'(?<!\.)\bonGround\b(?!\s*\()', 'onGround()',s)
 if p.name=='PoolSpear.java':s=s.replace('super(Tiers.IRON,4,-2.8F,p.durability(420))','super(Tiers.IRON,p.durability(420).attributes(SwordItem.createAttributes(Tiers.IRON,4,-2.8F)))')
 if p.name=='VodyanoyNetItem.java':s=s.replace('ThrownNet net=new ThrownNet(w,p);net.shoot','ThrownNet projectile=new ThrownNet(w,p);projectile.shoot').replace('w.addFreshEntity(net)','w.addFreshEntity(projectile)')
 if p.name in ['BossStone.java','EmberClump.java','KurganBolt.java']:s=s.replace('protected float getGravity()', 'protected double getDefaultGravity()')
 if p.name=='SerpentProjection.java':s=s.replace('new net.minecraft.core.BlockPos(position().add(drift))', 'net.minecraft.core.BlockPos.containing(position().add(drift))')
 if p.name=='ElementHuntMob.java':s=s.replace('calculateEntityAnimation(this,false)', 'calculateEntityAnimation(false)').replace('level().getBiome(blockPosition()).getPrecipitation()==net.minecraft.world.level.biome.Biome.RainType.SNOW', 'level().getBiome(blockPosition()).value().getPrecipitationAt(blockPosition())==net.minecraft.world.level.biome.Biome.Precipitation.SNOW')
 s=s.replace('cue(SoundEvents.GENERIC_EXPLODE)', 'cue(SoundEvents.GENERIC_EXPLODE.value())')
 if p.name=='KurganDisturbance.java':s=s.replace('SoundEvents.GENERIC_EXPLODE,','SoundEvents.GENERIC_EXPLODE.value(),')
 if s!=o:p.write_text(s,encoding='utf-8')
# Move damage classification for the existing fiery projectile into data.
p=Path('src/main/java/org/slavicmyths/combat/MythDamageSources.java');s=p.read_text(encoding='utf-8').replace('    private MythDamageSources(){}','    public static DamageSource ember(Entity projectile,net.minecraft.world.entity.LivingEntity owner){return new DamageSource(type(projectile,"ember_projectile"),projectile,owner);}\n    private MythDamageSources(){}');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/hunt/EmberClump.java');s=p.read_text(encoding='utf-8');s=re.sub(r'DamageSource.indirectMobAttack\((.*?)\).setProjectile\(\).setIsFire\(\)',r'org.slavicmyths.combat.MythDamageSources.ember(\1)',s);p.write_text(s,encoding='utf-8')
p=Path('src/main/resources/data/slavicmyths/damage_type/ember_projectile.json');p.write_text(json.dumps({'message_id':'mob','scaling':'when_caused_by_living_non_player','exhaustion':.1,'effects':'hurt'},indent=2)+'\n',encoding='utf-8')
for tag in ['is_fire','is_projectile']:
 p=Path('src/main/resources/data/minecraft/tags/damage_type')/(tag+'.json');p.parent.mkdir(parents=True,exist_ok=True);d=json.loads(p.read_text(encoding='utf-8'))if p.exists()else{'replace':False,'values':[]};d['values'].append('slavicmyths:ember_projectile');p.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
