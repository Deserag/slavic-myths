from pathlib import Path
import re
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 s=re.sub(r'appendHoverText\((ItemStack \w+),\s*(?:net\.minecraft\.world\.level\.)?Level (\w+),',r'appendHoverText(\1,net.minecraft.world.item.Item.TooltipContext \2,',s)
 s=re.sub(r'getUseDuration\(ItemStack (\w+)\)',r'getUseDuration(ItemStack \1,net.minecraft.world.entity.LivingEntity user)',s)
 s=re.sub(r'\.hurtAndBreak\((\d+),(\w+),\w+->\w+\.broadcastBreakEvent\(([^;]+?)\)\)',r'.hurtAndBreak(\1,\2,net.minecraft.world.entity.LivingEntity.getSlotForHand(\3))',s)
 if p.name=='GusliItem.java':s=s.replace('getUseDuration(s)-','getUseDuration(s,e)-')
 if p.name=='PoolSpear.java':s=s.replace('getUseDuration(s)-','getUseDuration(s,user)-').replace('DamageSource.playerAttack(p)','p.damageSources().playerAttack(p)')
 if p.name=='NightingaleDagger.java':s=s.replace('super(Tiers.IRON,4,-2F,p.durability(700))','super(Tiers.IRON,p.durability(700).attributes(SwordItem.createAttributes(Tiers.IRON,4,-2F)))').replace('getUseDuration(stack)-','getUseDuration(stack,e)-')
 if p.name=='BurialWeapon.java':s=s.replace('super(ancient?Tiers.STONE:Tiers.IRON,ancient?2:3,spear?-2.8F:-2.4F,p.durability(ancient?90:spear?380:450))','super(ancient?Tiers.STONE:Tiers.IRON,p.durability(ancient?90:spear?380:450).attributes(SwordItem.createAttributes(ancient?Tiers.STONE:Tiers.IRON,ancient?2:3,spear?-2.8F:-2.4F)))')
 if p.name=='YagaSpawnEgg.java':s=s.replace('net.minecraftforge.common.ForgeSpawnEggItem','net.neoforged.neoforge.common.DeferredSpawnEggItem')
 if s!=old:p.write_text(s,encoding='utf-8')
