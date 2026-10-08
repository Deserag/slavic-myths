package org.slavicmyths.rpg;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.particles.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slavicmyths.combat.*;

/** One server dispatcher; equipment-driven transient attributes, no global tick polling. */
@EventBusSubscriber(modid="slavicmyths")
public final class RuneRuntime {
    private static final ResourceLocation DAMAGE=id("damage"),ATTACK=id("attack"),MOVE=id("movement"),TOUGH=id("toughness"),KB=id("knockback"),JUMP=id("jump");
    private static ResourceLocation id(String key){return ResourceLocation.fromNamespaceAndPath("slavicmyths","rune2_"+key);}
    private static int count(ItemStack s,String id){return RuneRework.supports(s,id)?RuneRework.copies(s,id):0;}
    private static int armorCount(LivingEntity e,String id){int n=0;for(ItemStack s:e.getArmorSlots())n+=count(s,id);return n;}
    private static void upsert(AttributeInstance a,ResourceLocation id,double amount,AttributeModifier.Operation operation){a.removeModifier(id);if(amount!=0)a.addTransientModifier(new AttributeModifier(id,amount,operation));}
    private static void modifier(LivingEntity e,Holder<Attribute> attr,ResourceLocation id,double amount,AttributeModifier.Operation operation){var a=e.getAttribute(attr);if(a!=null)upsert(a,id,amount,operation);}
    public static void refresh(LivingEntity e){if(e.level().isClientSide)return;ItemStack weapon=e.getMainHandItem(),boots=e.getItemBySlot(EquipmentSlot.FEET);
        int strength=count(weapon,"strength"),speed=count(weapon,"speed"),crush=count(weapon,"crushing"),bootSpeed=count(boots,"speed"),wind=count(boots,"wind"),res=armorCount(e,"resilience"),fort=armorCount(e,"fortitude");
        modifier(e,Attributes.ATTACK_DAMAGE,DAMAGE,Math.min(RuneBalance.DAMAGE_CAP,RuneBalance.STRENGTH*RuneBalance.effective(strength)+RuneBalance.CRUSHING_DAMAGE*RuneBalance.effective(crush)),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        double attack=RuneBalance.attackFactor(speed,crush)-1;var a=e.getAttribute(Attributes.ATTACK_SPEED);if(a!=null){a.removeModifier(ATTACK);attack=RuneBalance.safeAttackFactor(a.getValue(),speed,crush)-1;}
        modifier(e,Attributes.ATTACK_SPEED,ATTACK,attack,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(e,Attributes.MOVEMENT_SPEED,MOVE,RuneBalance.movement(bootSpeed,wind,fort),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(e,Attributes.ARMOR_TOUGHNESS,TOUGH,RuneBalance.toughness(res,fort),AttributeModifier.Operation.ADD_VALUE);
        modifier(e,Attributes.KNOCKBACK_RESISTANCE,KB,RuneBalance.bonus(RuneBalance.FORT_KB,fort,RuneBalance.KB_CAP),AttributeModifier.Operation.ADD_VALUE);
        modifier(e,Attributes.JUMP_STRENGTH,JUMP,RuneBalance.bonus(RuneBalance.WIND_JUMP,wind,RuneBalance.JUMP_CAP),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof LivingEntity living)refresh(living);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void fireDefense(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide||!e.getSource().is(DamageTypeTags.IS_FIRE)||e.getNewDamage()<=0)return;
        double reduction=RuneBalance.bonus(RuneBalance.FIRE_REDUCTION,armorCount(e.getEntity(),"heat"),RuneBalance.FIRE_CAP);e.setNewDamage((float)(e.getNewDamage()*(1-reduction)));
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer p))return;
        LivingEntity target=e.getEntity();if(!org.slavicmyths.rpg.Abilities.canHit(p,target))return;ItemStack s=p.getMainHandItem();if(!RuneRework.melee(s))return;
        int fire=count(s,"heat"),blood=count(s,"blood"),crushing=count(s,"crushing");
        int legacyFire=RuneRework.legacyCopies(s,"heat");int fireSeconds=legacyFire>0&&RuneEffects.legacyFireReady(p)?RuneDefinition.i("heat","fire_seconds"):0;
        double chance=Math.min(RuneBalance.PROC_CAP,RuneBalance.FIRE_CHANCE*(RuneBalance.effective(fire)-legacyFire));
        if(fire>0&&chance>0&&p.getRandom().nextDouble()<chance)fireSeconds=RuneBalance.FIRE_SECONDS;
        if(fireSeconds>0){
            // Refresh a shorter fire only; never shorten existing Fire Aspect or accumulate duration.
            if(target.getRemainingFireTicks()<fireSeconds*20)target.igniteForSeconds(fireSeconds);
        }
        if(blood>0&&p.getRandom().nextDouble()<RuneBalance.bonus(RuneBalance.BLOOD_CHANCE,blood,RuneBalance.PROC_CAP)){
            RareBleedEffect.applyRune(p,target);if(target.level() instanceof ServerLevel w)w.sendParticles(new DustParticleOptions(new org.joml.Vector3f(.4F,.06F,.07F),1),target.getX(),target.getEyeY(),target.getZ(),3,.1,.1,.1,0);
        }
        if(crushing>0&&target.level() instanceof ServerLevel w&&PathData.ready(p,"rune2_impact",10)){w.sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+.7,target.getZ(),3,.1,.1,.1,.01);w.playSound(null,target.blockPosition(),net.minecraft.sounds.SoundEvents.ANVIL_HIT,net.minecraft.sounds.SoundSource.PLAYERS,.15F,1.6F);}
    }
    @SubscribeEvent public static void shield(LivingShieldBlockEvent e){if(e.getEntity().level().isClientSide||!e.getBlocked()||e.getBlockedDamage()<=0)return;ItemStack s=e.getEntity().getUseItem();if(!(s.getItem() instanceof ShieldItem))return;
        double reduction=Math.min(RuneBalance.SHIELD_CAP,RuneBalance.SHIELD_EFFICIENCY*(RuneBalance.effective(count(s,"resilience"))+RuneBalance.effective(count(s,"fortitude"))));e.setShieldDamage((float)(e.shieldDamage()*(1-reduction)));
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent e){LivingEntity entity=e.getEntity();if(entity.level().isClientSide)return;int wind=count(entity.getItemBySlot(EquipmentSlot.FEET),"wind");if(wind<=0)return;
        double bonus=RuneBalance.bonus(RuneBalance.WIND_MOMENTUM,wind,RuneBalance.MOMENTUM_CAP);var v=entity.getDeltaMovement();entity.setDeltaMovement(v.x*(1+bonus),v.y,v.z*(1+bonus));
        if(entity.level() instanceof ServerLevel world)world.sendParticles(ParticleTypes.CLOUD,entity.getX(),entity.getY(),entity.getZ(),3,.1,0,.1,0);
    }
    public static double projectileBonus(ItemStack bow){if(!RuneRework.ranged(bow))return 0;int copies=count(bow,"wind"),legacy=RuneRework.legacyCopies(bow,"wind");return Math.min(RuneBalance.PROJECTILE_CAP,legacy*RuneDefinition.n("wind","arrow_speed")+RuneBalance.WIND_PROJECTILE*(RuneBalance.effective(copies)-legacy));}
    public static java.util.List<net.minecraft.network.chat.Component> equippedTooltip(LivingEntity e){var out=new java.util.ArrayList<net.minecraft.network.chat.Component>();int res=armorCount(e,"resilience"),fort=armorCount(e,"fortitude"),fire=armorCount(e,"heat");
        if(res+fort+fire==0)return out;out.add(net.minecraft.network.chat.Component.translatable("rune2.slavicmyths.equipped"));
        if(res+fort>0)out.add(net.minecraft.network.chat.Component.translatable("rune2.slavicmyths.toughness",RuneBalance.format(RuneBalance.toughness(res,fort))));
        if(fort>0)out.add(net.minecraft.network.chat.Component.translatable("rune2.slavicmyths.kb",RuneBalance.format(100*RuneBalance.bonus(RuneBalance.FORT_KB,fort,RuneBalance.KB_CAP))));
        if(fire>0)out.add(net.minecraft.network.chat.Component.translatable("rune2.slavicmyths.fire_defense",RuneBalance.format(100*RuneBalance.bonus(RuneBalance.FIRE_REDUCTION,fire,RuneBalance.FIRE_CAP))));
        ItemStack boots=e.getItemBySlot(EquipmentSlot.FEET);double move=RuneBalance.movement(count(boots,"speed"),count(boots,"wind"),fort);if(move!=0)out.add(net.minecraft.network.chat.Component.translatable("rune2.slavicmyths.equipped_movement",RuneBalance.format(move*100)));return out;
    }
    private RuneRuntime(){}
}
