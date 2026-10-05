from pathlib import Path
import re
p=Path('src/main/java/org/slavicmyths/item/ItemState.java');s=p.read_text(encoding='utf-8');needle='    public static NonNullList<ItemStack> inventory(ItemStack stack, boolean flight, HolderLookup.Provider registries) {';s=s.replace(needle,'''    /** Read existing shield/bow snapshots in entity/player NBT, including pre-port nested stacks. */
    public static ItemStack snapshot(HolderLookup.Provider registries,CompoundTag input){
        CompoundTag item=input.copy();
        if(item.contains("Count"))item=(CompoundTag)DataFixers.getDataFixer().update(References.ITEM_STACK,
            new Dynamic<>(NbtOps.INSTANCE,item),2586,SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getValue();
        return ItemStack.parseOptional(registries,item);
    }
'''+needle);p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/rpg/RpgEvents.java');s=p.read_text(encoding='utf-8').replace('net.minecraftforge.event.entity.EntityJoinWorldEvent','net.neoforged.neoforge.event.entity.EntityJoinLevelEvent').replace('EntityJoinWorldEvent','EntityJoinLevelEvent').replace('e.getWorld()','e.getLevel()')
s=s.replace('// Forge 36 has no ShieldBlockEvent. Confirm vanilla\'s actual blocked-damage stat\n // after hurt() completes, instead of granting retaliation for merely holding a shield.','// Capture target shield blocking, then confirm the actual blocked-damage statistic after hurt().')
s=s.replace('attack(LivingAttackEvent e)','attack(LivingShieldBlockEvent e)').replace('if(!(e.getEntity() instanceof ServerPlayer)||e.getAmount()<=0)return;','if(!(e.getEntity() instanceof ServerPlayer)||!e.getBlocked()||e.getBlockedDamage()<=0)return;')
start=s.index(' public static void attack(');end=s.index(' @SubscribeEvent public static void blockConfirmation',start);s=s[:start]+s[start:end].replace('e.getSource()','e.getDamageSource()')+s[end:]
s=s.replace('p.getUseItem().save(new CompoundTag())','p.getUseItem().saveOptional(p.registryAccess())').replace('bow.save(new CompoundTag())','bow.saveOptional(p.registryAccess())').replace('ItemStack.of(n.getCompound("Shield"))','org.slavicmyths.item.ItemState.snapshot(p.registryAccess(),n.getCompound("Shield"))').replace('ItemStack.of(e.getSource().getDirectEntity().getPersistentData().getCompound("SlavicBow"))','org.slavicmyths.item.ItemState.snapshot(p.registryAccess(),e.getSource().getDirectEntity().getPersistentData().getCompound("SlavicBow"))')
s=s.replace('net.minecraft.world.damagesource.DamageSource.indirectMagic(p,p)','p.damageSources().indirectMagic(p,p)');p.write_text(s,encoding='utf-8')
for name in ['rpg/RuneEffects','rpg/Abilities']:
 p=Path('src/main/java/org/slavicmyths/'+name+'.java');s=p.read_text(encoding='utf-8').replace('DamageSource.indirectMagic(p,p)','p.damageSources().indirectMagic(p,p)')
 if name.endswith('RuneEffects'):s=s.replace('Biome.Category c=p.level().getBiome(p.blockPosition()).getBiomeCategory();return c==Biome.Category.FOREST||c==Biome.Category.TAIGA||c==Biome.Category.JUNGLE;','var biome=p.level().getBiome(p.blockPosition());return biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_TAIGA)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_JUNGLE);')
 p.write_text(s,encoding='utf-8')
# Modern finalizeSpawn does not carry the discarded optional CompoundTag argument.
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 s=re.sub(r'(SpawnGroupData finalizeSpawn\([^)]*SpawnGroupData \w+),\s*(?:net\.minecraft\.nbt\.)?CompoundTag \w+\)',r'\1)',s)
 s=re.sub(r'(super\.finalizeSpawn\([^,]+,[^,]+,[^,]+,[^,]+),\s*(?:tag|nbt|n)\)',r'\1)',s)
 s=s.replace('MobSpawnType.STRUCTURE,null,null)','MobSpawnType.STRUCTURE,null)')
 s=s.replace('MobEffectInstance(BossEffects.ILL_FATE.get(),','MobEffectInstance(BossEffects.ILL_FATE,')
 s=s.replace('net.minecraft.particles.RedstoneParticleData red=new net.minecraft.particles.RedstoneParticleData(.35F,.025F,.035F,1)','net.minecraft.core.particles.DustParticleOptions red=new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.35F,.025F,.035F),1)')
 if s!=old:p.write_text(s,encoding='utf-8')
