package org.slavicmyths.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.MythDamageSources;

/** Bounded per-owner timers. Tick work is restricted to active effects or a held Berserker rune. */
@EventBusSubscriber(modid="slavicmyths")
public final class RareRuneRuntime {
    public static final String KEY="RareRunes";
    public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,id("rune_death_immune"));
    private static final ResourceLocation DAMAGE=id("rare_damage"),SPEED=id("rare_speed");
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    public static long now(Player p){return org.slavicmyths.rpg.classes.ClassState.now(p);}
    public static CompoundTag data(Player p){var root=PathData.data(p);if(!root.contains(KEY,10))root.put(KEY,new CompoundTag());return root.getCompound(KEY);}
    private static CompoundTag temp(Player p){return p.getPersistentData();}
    private static boolean ready(Player p,String id){return RareRuneRules.ready(now(p),data(p).getLong("CD_"+id));}
    private static void cooldown(Player p,String id){data(p).putLong("CD_"+id,now(p)+RareRuneRules.cooldown(id));}
    public static int tier(Player p){return RareRunes.equipped(p,"berserker")?RareRuneRules.tier(p.getHealth(),p.getMaxHealth()):0;}
    public static double meleeMultiplier(Player p){return 1+RareRuneRules.damage(tier(p))+(temp(p).getLong("RuneSacrificeUntil")>now(p)&&RareRunes.equipped(p,"sacrifice")?.25:0);}
    private static void modifier(Player p,net.minecraft.core.Holder<Attribute> type,ResourceLocation id,double value){var a=p.getAttribute(type);if(a==null)return;a.removeModifier(id);if(value!=0)a.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
    public static void refresh(ServerPlayer p){int tier=tier(p);boolean sacrifice=temp(p).getLong("RuneSacrificeUntil")>now(p)&&RareRunes.equipped(p,"sacrifice");
        modifier(p,Attributes.ATTACK_SPEED,SPEED,RareRuneRules.speed(tier)+(sacrifice?.15:0));
        // Damage is applied only to direct melee in incoming(), not to projectiles or empty hands.
        modifier(p,Attributes.ATTACK_DAMAGE,DAMAGE,0);
        temp(p).putInt("RuneBerserkTier",tier);temp(p).putFloat("RuneLastHP",p.getHealth());temp(p).putFloat("RuneLastMaxHP",p.getMaxHealth());
        data(p).putInt("Tier",tier);data(p).putLong("ServerTick",now(p));data(p).putLong("SacrificeUntil",temp(p).getLong("RuneSacrificeUntil"));RpgNetwork.sync(p);
    }
    public static void intent(ServerPlayer p,boolean cycle){if(!p.isAlive()||p.isSpectator())return;var t=temp(p);if(t.getLong("RuneRequestUntil")>now(p))return;t.putLong("RuneRequestUntil",now(p)+4);
        var active=RareRunes.active(p);if(active.isEmpty())return;int selected=RareRuneRules.selected(data(p).getInt("Selected"),active.size());
        if(cycle){data(p).putInt("Selected",(selected+1)%active.size());data(p).putLong("ServerTick",now(p));RpgNetwork.sync(p);return;}
        String id=active.get(selected);if(!ready(p,id)){p.displayClientMessage(RareRunes.text("cooldown",(data(p).getLong("CD_"+id)-now(p)+19)/20),true);return;}
        switch(id){
            case "floating_weapon"->{var visual=RuneVisual.weapon(p);if(!p.serverLevel().addFreshEntity(visual))return;t.putInt("RuneWeaponVisual",visual.getId());}
            case "flight"->{if(p.isCreative()||p.getAbilities().mayfly)return;var a=p.getAbilities();data(p).putBoolean("FlightLease",true);data(p).putFloat("FlightSpeedBefore",a.getFlyingSpeed());t.putLong("RuneFlightUntil",now(p)+100);a.mayfly=true;a.flying=true;a.setFlyingSpeed(.05F*1.35F);p.onUpdateAbilities();}
            case "sacrifice"->{if(!pay(p,6))return;t.putLong("RuneSacrificeUntil",now(p)+160);data(p).putLong("PulseUntil",now(p)+12);p.serverLevel().sendParticles(ParticleTypes.WITCH,p.getX(),p.getY()+1,p.getZ(),5,.3,.3,.3,.01);}
            default->{return;}
        }
        cooldown(p,id);refresh(p);
    }
    /** Exact health debit, explicit marker, no hostile hurt event: armor, absorption and procs cannot pay the price. */
    private static boolean pay(ServerPlayer p,float cost){if(!RareRuneRules.canPay(p.getHealth(),cost))return false;temp(p).putBoolean("RuneSelfCost",true);try{p.setHealth(p.getHealth()-cost);}finally{temp(p).remove("RuneSelfCost");}return true;}
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){if(e.getEntity() instanceof ServerPlayer p){if(!RareRunes.equipped(p,"flight"))endFlight(p);if(!RareRunes.equipped(p,"rare_protection"))endProtection(p);if(!RareRunes.equipped(p,"floating_weapon"))removeVisual(p,"RuneWeaponVisual");if(!RareRunes.equipped(p,"sacrifice"))temp(p).remove("RuneSacrificeUntil");refresh(p);}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity().level().isClientSide||e.getAmount()<=0)return;DamageSource s=e.getSource();
        if(e.getEntity() instanceof ServerPlayer p){if(s.is(DamageTypeTags.IS_FALL)&&temp(p).getLong("RuneFallGrace")>now(p)){e.setCanceled(true);return;}
            if(direct(s)&&RareRunes.equipped(p,"rare_protection")){
                if(temp(p).getInt("RuneShields")==0&&ready(p,"rare_protection")){temp(p).putInt("RuneShields",3);temp(p).putLong("RuneShieldUntil",now(p)+400);data(p).putLong("CD_rare_protection",now(p)+400+1200);var visual=RuneVisual.shields(p);if(p.serverLevel().addFreshEntity(visual))temp(p).putInt("RuneShieldVisual",visual.getId());}
                int shields=temp(p).getInt("RuneShields");if(shields>0){temp(p).putInt("RuneShields",RareRuneRules.remainingShields(shields));var visual=p.level().getEntity(temp(p).getInt("RuneShieldVisual"));if(visual instanceof RuneVisual v&&v.ownedBy(p))v.crack(shields-1,s.getSourcePosition());if(shields==1)endProtection(p);e.setCanceled(true);RpgNetwork.sync(p);return;}
            }
        }
        if(s.is(DamageTypes.PLAYER_ATTACK)&&s.getDirectEntity() instanceof ServerPlayer p&&RuneRework.melee(p.getMainHandItem()))e.setAmount((float)(e.getAmount()*meleeMultiplier(p)));
    }
    public static boolean direct(DamageSource s){if(s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||s.is(DamageTypes.THORNS)||s.getEntity()==null||s.getDirectEntity()==null||s.getMsgId().equals("rune_execute")||s.getMsgId().equals("rune_cost"))return false;return s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile||s.is(DamageTypeTags.IS_PROJECTILE)||s.getDirectEntity() instanceof RuneVisual||s.getDirectEntity() instanceof LivingEntity&&!s.is(DamageTypeTags.IS_FIRE)&&!s.is(DamageTypeTags.IS_FALL);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void defense(LivingDamageEvent.Pre e){if(e.getEntity() instanceof ServerPlayer p&&e.getNewDamage()>0&&!e.getSource().getMsgId().equals("rune_cost"))e.setNewDamage((float)(e.getNewDamage()*RareRuneRules.defenseMultiplier(tier(p))));}
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;
        if(e.getEntity() instanceof ServerPlayer p){if(direct(e.getSource())){data(p).putLong("HitUntil",now(p)+8);}refresh(p);}
        if(!e.getSource().is(DamageTypes.PLAYER_ATTACK)||!(e.getSource().getDirectEntity() instanceof ServerPlayer p))return;data(p).putLong("HitUntil",now(p)+8);if(tier(p)>0)RpgNetwork.sync(p);
        var target=e.getEntity();if(!RareRunes.equipped(p,"death")||!RuneRework.melee(p.getMainHandItem())||!ready(p,"death")||!target.isAlive()||!Abilities.canHit(p,target)||!RareRuneRules.execute(target.getHealth(),p.getHealth(),target.getType().is(IMMUNE)))return;
        var source=MythDamageSources.periodic("rune_execute",p);if(target.isInvulnerableTo(source))return;
        if(target.hurt(source,Math.max(100,target.getMaxHealth()*4))&&!target.isAlive()){pay(p,12);cooldown(p,"death");p.serverLevel().sendParticles(ParticleTypes.SOUL,target.getX(),target.getY()+.7,target.getZ(),4,.2,.3,.2,.01);refresh(p);}
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var t=temp(p);long now=now(p);
        if(t.getLong("RuneFlightUntil")>0){if(!p.isAlive()||p.isSpectator()||!RareRunes.equipped(p,"flight")||t.getLong("RuneFlightUntil")<=now)endFlight(p);else if(p.tickCount%20==0)p.serverLevel().sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY(),p.getZ(),3,.1,0,.1,0);}
        if(t.getInt("RuneShields")>0&&(!p.isAlive()||t.getLong("RuneShieldUntil")<=now))endProtection(p);
        if(t.getLong("RuneSacrificeUntil")>0&&(!p.isAlive()||t.getLong("RuneSacrificeUntil")<=now)){t.remove("RuneSacrificeUntil");refresh(p);}
        if(RareRunes.equipped(p,"berserker")&&(t.getFloat("RuneLastHP")!=p.getHealth()||t.getFloat("RuneLastMaxHP")!=p.getMaxHealth()))refresh(p);
    }
    private static void endFlight(ServerPlayer p){if(!data(p).getBoolean("FlightLease"))return;data(p).putBoolean("FlightLease",false);var a=p.getAbilities();if(!p.isCreative()&&!p.isSpectator()){a.mayfly=false;a.flying=false;}a.setFlyingSpeed(data(p).getFloat("FlightSpeedBefore"));p.onUpdateAbilities();temp(p).remove("RuneFlightUntil");temp(p).putLong("RuneFallGrace",now(p)+60);p.fallDistance=0;}
    private static void endProtection(ServerPlayer p){if(temp(p).getInt("RuneShields")<=0&&temp(p).getLong("RuneShieldUntil")==0)return;temp(p).remove("RuneShields");temp(p).remove("RuneShieldUntil");cooldown(p,"rare_protection");var v=p.level().getEntity(temp(p).getInt("RuneShieldVisual"));if(v instanceof RuneVisual visual&&visual.ownedBy(p))visual.dissolve();temp(p).remove("RuneShieldVisual");}
    private static void removeVisual(ServerPlayer p,String key){var v=p.level().getEntity(temp(p).getInt(key));if(v instanceof RuneVisual visual&&visual.ownedBy(p))visual.dissolve();temp(p).remove(key);}
    private static void cleanup(ServerPlayer p){endFlight(p);endProtection(p);removeVisual(p,"RuneWeaponVisual");temp(p).remove("RuneSacrificeUntil");refresh(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    private RareRuneRuntime(){}
}
