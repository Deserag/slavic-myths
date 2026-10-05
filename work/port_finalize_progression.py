from pathlib import Path
import re
p=Path('src/main/java/org/slavicmyths/progression/Knowledge.java');s=p.read_text(encoding='utf-8').replace('import net.minecraft.advancements.Advancement;','import net.minecraft.advancements.AdvancementHolder;').replace('Advancement advancement','AdvancementHolder advancement').replace('Advancement a','AdvancementHolder a').replace('.getAdvancement(','.get(');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/yaga/YagaPlacement.java');s=p.read_text(encoding='utf-8').replace('s.getBlock().getRegistryName()!=null&&s.getBlock().getRegistryName().getNamespace().equals("slavicmyths")','net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getNamespace().equals("slavicmyths")');s=re.sub(r'\.isAir\(w,p\)','.isAir()',s);p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/yaga/YagaUtilityItem.java');s=p.read_text(encoding='utf-8').replace('for(MobEffect bad:new MobEffect[]{','for(net.minecraft.core.Holder<MobEffect> bad:java.util.List.of(').replace('MobEffects.CONFUSION})','MobEffects.CONFUSION))').replace('p.inventory.add(bottle)','p.getInventory().add(bottle)');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/item/SilverCombat.java');s=p.read_text(encoding='utf-8').replace('.getMainHandItem().getItem().is(SILVER)','.getMainHandItem().is(SILVER)');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/bandit/WindAttack.java');s=p.read_text(encoding='utf-8').replace('DamageSource.playerAttack((Player)source)','source.damageSources().playerAttack((Player)source)').replace('DamageSource.mobAttack(source)','source.damageSources().mobAttack(source)');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kitchen/KitchenMenu.java');s=p.read_text(encoding='utf-8').replace('if(tool.hurt(1,p.getRandom(),p instanceof ServerPlayer?(ServerPlayer)p:null)){tool.shrink(1);tool.setDamageValue(0);}', '''// The old low-level hurt() consumed internal tools even in creative mode.
  int before=tool.getDamageValue();ItemStack previous=tool.copy();
  tool.hurtAndBreak(1,(net.minecraft.server.level.ServerLevel)p.level(),(net.minecraft.world.entity.LivingEntity)null,broken->{});
  if(p instanceof ServerPlayer sp&&(tool.isEmpty()||tool.getDamageValue()!=before)){
   net.minecraft.advancements.CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(sp,previous,tool.isEmpty()?previous.getMaxDamage():tool.getDamageValue());
  }''');p.write_text(s,encoding='utf-8')
