from pathlib import Path
import re
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 for a,b in [('PlayerTickEvent','PlayerTickEvent.Post'),('WorldTickEvent','LevelTickEvent.Post'),('ClientTickEvent','ClientTickEvent.Post')]:
  if 'TickEvent.'+a in s:s=s.replace('TickEvent.'+a,'net.neoforged.neoforge.event.tick.'+b)
 if 'TickEvent.Phase' in s:
  s=re.sub(r'\b\w+\.phase\s*!=\s*TickEvent\.Phase\.END\s*\|\|','',s)
  s=re.sub(r'if\(\w+\.phase\s*!=\s*TickEvent\.Phase\.END\)return;','',s)
 # Each old field was part of a tick event variable with public player/world fields.
 if 'net.neoforged.neoforge.event.tick.' in s:
  for var in re.findall(r'net\.neoforged\.neoforge\.event\.tick\.\w+\.Post\s+(\w+)\)',s):
   s=re.sub(r'\b'+var+r'\.player\b',var+'.getEntity()',s);s=re.sub(r'\b'+var+r'\.world\b',var+'.getLevel()',s)
 if 'TickEvent.' not in s:s=s.replace('import net.minecraftforge.event.TickEvent;','')
 s=s.replace('import net.minecraftforge.event.entity.living.LivingHurtEvent;','import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;').replace('LivingHurtEvent','LivingIncomingDamageEvent')
 if 'LivingDamageEvent event)' in s:s=s.replace('LivingDamageEvent event)','LivingDamageEvent.Pre event)').replace('event.setAmount(event.getAmount()*1.25F)','event.setNewDamage(event.getNewDamage()*1.25F)')
 s=s.replace('.isFire()','.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)').replace('.isBypassInvul()','.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)').replace('.isProjectile()','.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)')
 if s!=old:p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/hunt/BossEffects.java');s=p.read_text(encoding='utf-8').replace('public static final UUID NAPOR=UUID.fromString(', 'public static final ResourceLocation NAPOR=ResourceLocation.fromNamespaceAndPath("slavicmyths",').replace('new AttributeModifier(NAPOR,"Tugarin sustained momentum",.2,AttributeModifier.Operation.ADDITION)','new AttributeModifier(NAPOR,.2,AttributeModifier.Operation.ADD_VALUE)').replace('hasEffect(ILL_FATE.get())','hasEffect(ILL_FATE)').replace('PotionEvent.PotionApplicableEvent','MobEffectEvent.Applicable').replace('Event.Result.DENY','MobEffectEvent.Applicable.Result.DO_NOT_APPLY').replace('getPotionEffect()','getEffectInstance()').replace('MobEffect effect=incoming.getEffect()','net.minecraft.core.Holder<MobEffect> effect=incoming.getEffect()').replace('effect.getCategory()','effect.value().getCategory()').replace('effect.isInstantenous()','effect.value().isInstantenous()').replace('effect==org.slavicmyths.kurgan.KurganCurse.CURSE.get()','effect.equals(org.slavicmyths.kurgan.KurganCurse.CURSE)').replace('e.getPlayer()','e.getEntity()');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/hunt/HuntEffects.java');s=p.read_text(encoding='utf-8').replace('public static final UUID BELT=UUID.fromString(','public static final net.minecraft.resources.ResourceLocation BELT=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",').replace('old.getAmount()','old.amount()').replace('new AttributeModifier(BELT,"Wolf belt night stride",value,AttributeModifier.Operation.MULTIPLY_TOTAL)','new AttributeModifier(BELT,value,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)');p.write_text(s,encoding='utf-8')
