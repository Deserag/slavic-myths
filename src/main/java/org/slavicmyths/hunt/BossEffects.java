package org.slavicmyths.hunt;
import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.*;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class BossEffects {
 public static final RegistryObject<Effect> ILL_FATE=org.slavicmyths.kurgan.KurganCurse.EFFECTS.register("durnaya_dolya",()->new Fate());
 private static final class Fate extends Effect{Fate(){super(EffectType.HARMFUL,0x6C3233);}}
 public static void init(){}
 public static final UUID NAPOR=UUID.fromString("6d5945a5-a47e-42da-bb13-fd812d2c649f");
 public static void clearBelt(LivingEntity p){ModifiableAttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);if(a!=null)a.removeModifier(NAPOR);p.getPersistentData().remove("NaporTicks");p.getPersistentData().remove("NaporUntil");}
 public static boolean napor(PlayerEntity p){return FolkEquipmentEffects.wears(p,ModItems.POYAS_TUGARINA.get())&&p.getPersistentData().getLong("NaporUntil")>p.level.getGameTime();}
 @SubscribeEvent public static void healing(LivingHealEvent e){if(e.getEntityLiving().hasEffect(ILL_FATE.get()))e.setAmount(e.getAmount()*.8F);}
 @SubscribeEvent public static void knock(LivingKnockBackEvent e){LivingEntity victim=e.getEntityLiving();if(victim.level.isClientSide)return;if(victim.hasEffect(ILL_FATE.get()))e.setStrength(e.getStrength()*1.15F);net.minecraft.nbt.CompoundNBT n=victim.getPersistentData();if(n.getLong("NaporHitTick")==victim.level.getGameTime()&&n.hasUUID("NaporAttacker")){PlayerEntity player=victim.level.getPlayerByUUID(n.getUUID("NaporAttacker"));if(player!=null&&napor(player))e.setStrength(e.getStrength()*1.15F);}}
 @SubscribeEvent public static void melee(LivingHurtEvent e){if(e.getEntityLiving().level.isClientSide)return;if(e.getSource().getEntity() instanceof PlayerEntity&&e.getSource().getEntity()==e.getSource().getDirectEntity()){PlayerEntity p=(PlayerEntity)e.getSource().getEntity();if(napor(p)){e.getEntityLiving().getPersistentData().putLong("NaporHitTick",p.level.getGameTime());e.getEntityLiving().getPersistentData().putUUID("NaporAttacker",p.getUUID());}}}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||e.player.level.isClientSide)return;PlayerEntity p=e.player;net.minecraft.nbt.CompoundNBT n=p.getPersistentData();boolean equipped=FolkEquipmentEffects.wears(p,ModItems.POYAS_TUGARINA.get());if(!p.isAlive()||!equipped||p.isSwimming()||p.isInWater()||p.isFallFlying()||p.abilities.flying){clearBelt(p);return;}long now=p.level.getGameTime();int ticks=p.isSprinting()?n.getInt("NaporTicks")+1:0;n.putInt("NaporTicks",Math.min(40,ticks));if(ticks>=40)n.putLong("NaporUntil",now+20);ModifiableAttributeInstance a=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);boolean active=n.getLong("NaporUntil")>now;if(active&&a.getModifier(NAPOR)==null){a.addTransientModifier(new AttributeModifier(NAPOR,"Tugarin sustained momentum",.2,AttributeModifier.Operation.ADDITION));p.level.playSound(null,p.blockPosition(),SoundEvents.IRON_GOLEM_STEP,SoundCategory.PLAYERS,.6F,.7F);((ServerWorld)p.level).sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.1,p.getZ(),4,.2,.04,.2,.02);FolkAccessoryItem.award(p,"full_speed_ahead");}if(!active)a.removeModifier(NAPOR);}
 @SubscribeEvent public static void eye(PotionEvent.PotionApplicableEvent e){LivingEntity target=e.getEntityLiving();if(e.getResult()==Event.Result.DENY||!(target instanceof ServerPlayerEntity))return;ServerPlayerEntity p=(ServerPlayerEntity)target;EffectInstance incoming=e.getPotionEffect();Effect effect=incoming.getEffect();net.minecraft.nbt.CompoundNBT n=p.getPersistentData();long now=p.server.getLevel(World.OVERWORLD).getGameTime();if(n.getBoolean("EyeProcGuard")||now<n.getLong("EyeProcReady")||!FolkEquipmentEffects.wears(p,ModItems.ODNOGLAZYY_OBEREG.get())||effect.getCategory()!=EffectType.HARMFUL||effect.isInstantenous()||incoming.getDuration()<=1||incoming.getDuration()>72000||effect==Effects.BAD_OMEN||effect==org.slavicmyths.kurgan.KurganCurse.CURSE.get()&&(incoming.getAmplifier()>0||org.slavicmyths.kurgan.KurganCurse.persistent(p))||p.getRandom().nextFloat()>=.2F)return;
  e.setResult(Event.Result.DENY);n.putBoolean("EyeProcGuard",true);try{p.addEffect(new EffectInstance(effect,Math.max(1,incoming.getDuration()/2),incoming.getAmplifier(),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon()));}finally{n.remove("EyeProcGuard");}n.putLong("EyeProcReady",now+600);p.level.playSound(null,p.blockPosition(),SoundEvents.ENDER_EYE_DEATH,SoundCategory.PLAYERS,.35F,.65F);((ServerWorld)p.level).sendParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),3,.2,.3,.2,.01);
 }
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getPlayer().getPersistentData().putLong("EyeProcReady",e.getOriginal().getPersistentData().getLong("EyeProcReady"));clearBelt(e.getPlayer());}
}
