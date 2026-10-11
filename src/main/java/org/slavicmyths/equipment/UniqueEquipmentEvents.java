package org.slavicmyths.equipment;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.rpg.PathData;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class UniqueEquipmentEvents {
    private static final ResourceLocation STANCE=ResourceLocation.fromNamespaceAndPath("slavicmyths","bogatyr_stance"), KB=ResourceLocation.fromNamespaceAndPath("slavicmyths","indomitability");
    public static final TagKey<MobEffect> CONTROL=TagKey.create(Registries.MOB_EFFECT,ResourceLocation.fromNamespaceAndPath("slavicmyths","equipment_control"));
    public static boolean wears(LivingEntity e,EquipmentSlot slot,net.minecraft.world.item.Item item) { return e.getItemBySlot(slot).is(item); }
    public static long now(Player p) { return p.getServer().overworld().getGameTime(); }
    private static CompoundTag state(Player p) { var root=PathData.data(p);if(!root.contains("UniqueEquipment",10))root.put("UniqueEquipment",new CompoundTag());return root.getCompound("UniqueEquipment"); }
    public static CompoundTag saved(Player p) { return state(p); }
    private static void cleanup(Player p){if(p.level().isClientSide)return;p.getPersistentData().remove("UniqueKbUntil");modifier(p,KB,0,false);healthChanged(p);}
    @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){cleanup(event.getEntity());}
    @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){cleanup(event.getEntity());}
    @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event){cleanup(event.getEntity());}
    private static void modifier(LivingEntity e,ResourceLocation id,double amount,boolean active) {
        var a=e.getAttribute(Attributes.KNOCKBACK_RESISTANCE);if(a==null)return;
        if(!active){a.removeModifier(id);return;}if(a.getModifier(id)==null)a.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
    }
    /** Invoked after health changes as well as equipment changes; no health polling. */
    public static void healthChanged(LivingEntity e) {
        // LivingEntity constructor initializes health before Player constructs its inventory.
        if(e.level().isClientSide || !(e instanceof Player p) || p.getInventory()==null)return;
        modifier(e,STANCE,UniqueEquipmentRules.STANCE_KB,wears(e,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get()) && UniqueEquipmentRules.stance(e.getHealth(),e.getMaxHealth()));
    }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent event) {
        var e=event.getEntity();healthChanged(e);
        if(!e.level().isClientSide && e instanceof Player p && !wears(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get())) { modifier(p,KB,0,false);p.getPersistentData().remove("UniqueKbUntil"); }
    }
    @SubscribeEvent public static void fall(LivingFallEvent event) {
        if(wears(event.getEntity(),EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get()))event.setDamageMultiplier(event.getDamageMultiplier()*(float)UniqueEquipmentRules.FALL);
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event) {
        var e=event.getEntity();if(wears(e,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get()) && e.isSprinting()) {
            var v=e.getDeltaMovement();e.setDeltaMovement(v.x*1.10,v.y,v.z*1.10);
        }
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
        var e=event.getEntity();if(e.level().isClientSide)return;
        if(wears(e,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get()) && UniqueEquipmentRules.stance(e.getHealth(),e.getMaxHealth()) && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))event.setNewDamage(event.getNewDamage()*(float)(1-UniqueEquipmentRules.STANCE_REDUCTION));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void knock(LivingKnockBackEvent event) {
        if(!(event.getEntity() instanceof Player p) || p.level().isClientSide || event.isCanceled() || !wears(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get()) || event.getStrength()*(1-p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))<UniqueEquipmentRules.STRONG_KB)return;
        long now=now(p);var d=state(p);if(!UniqueEquipmentRules.ready(now,d.getLong("KbReady")))return;
        d.putLong("KbReady",now+UniqueEquipmentRules.KB_COOLDOWN);p.getPersistentData().putLong("UniqueKbUntil",now+UniqueEquipmentRules.KB_DURATION);
        modifier(p,KB,UniqueEquipmentRules.INDOMITABLE_KB,true);
    }
    /** Only a pending 5-second local timer is inspected; no entity/world scans. */
    @SubscribeEvent public static void timer(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        var p=event.getEntity();if(p.level().isClientSide || !p.getPersistentData().contains("UniqueKbUntil"))return;
        if(now(p)>=p.getPersistentData().getLong("UniqueKbUntil") || !wears(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get())){modifier(p,KB,0,false);p.getPersistentData().remove("UniqueKbUntil");}
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void control(MobEffectEvent.Applicable event) {
        var e=event.getEntity();var incoming=event.getEffectInstance();if(e.level().isClientSide || event.getResult()==MobEffectEvent.Applicable.Result.DO_NOT_APPLY || !wears(e,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get()) || !incoming.getEffect().is(CONTROL) || incoming.getDuration()<1 || e.getPersistentData().getBoolean("UniqueControlGuard"))return;
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);e.getPersistentData().putBoolean("UniqueControlGuard",true);
        try { e.addEffect(new MobEffectInstance(incoming.getEffect(),UniqueEquipmentRules.controlDuration(incoming.getDuration()),incoming.getAmplifier(),incoming.isAmbient(),incoming.isVisible(),incoming.showIcon()),event.getEffectSource()); }
        finally {e.getPersistentData().remove("UniqueControlGuard");}
    }
    /** Called after the final block event has been accepted by LivingEntity.hurt. */
    public static void reflected(LivingEntity defender,LivingShieldBlockEvent event) {
        if(defender.level().isClientSide || !defender.getUseItem().is(UniqueEquipment.WARD_SHIELD.get()))return;
        var source=event.getDamageSource();var attacker=source.getEntity();
        boolean attack=source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
            || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)
            || source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO)
            || source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
            || source.is(net.minecraft.world.damagesource.DamageTypes.STING);
        boolean valid=attack && attacker instanceof LivingEntity && attacker!=defender && attacker.isAlive();
        // NeoForge has already subtracted blocked damage: the event's clamped getter is now zero.
        float amount=UniqueEquipmentRules.reflected(event.getBlocked(),event.getDamageContainer().getBlockedDamage(),valid,"ward_reflection".equals(event.getDamageSource().getMsgId()),defender.getRandom().nextFloat());
        if(amount>0)attacker.hurt(org.slavicmyths.combat.MythDamageSources.periodic("ward_reflection",defender),amount);
    }
    private UniqueEquipmentEvents() { }
}
