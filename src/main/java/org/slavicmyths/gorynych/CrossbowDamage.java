package org.slavicmyths.gorynych;

import java.util.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** A follow-up can bypass only the damage cooldown of its own successful volley. */
@EventBusSubscriber(modid="slavicmyths")
public final class CrossbowDamage {
    public static final float MIN_SPECIAL_DAMAGE=14;
    public static float impactDamage(AbstractArrow arrow,float vanilla) {
        var weapon=arrow.getWeaponItem();
        return weapon!=null&&weapon.getItem() instanceof SerpentCrossbow crossbow&&crossbow.special
            ?Math.max(MIN_SPECIAL_DAMAGE,vanilla):vanilla;
    }
    public static final String VOLLEY="GorynychVolley";
    private record Key(UUID owner, UUID volley) {}
    private static final class Receipt {
        long tick;
        final Set<UUID> arrows=new HashSet<>();
    }
    private static final Map<LivingEntity,Map<Key,Receipt>> HITS=new WeakHashMap<>();
    private static Key key(AbstractArrow arrow) {
        var tag=arrow.getPersistentData();
        var weapon=arrow.getWeaponItem();
        if(arrow.level().isClientSide||!(arrow.getOwner() instanceof LivingEntity owner)
            ||weapon==null||!(weapon.getItem() instanceof SerpentCrossbow)||!tag.hasUUID(VOLLEY))return null;
        return new Key(owner.getUUID(),tag.getUUID(VOLLEY));
    }
    private static Receipt receipt(LivingEntity target,Key key) {
        var map=HITS.get(target); if(map==null)return null;
        long now=target.level().getGameTime();
        map.values().removeIf(r->now-r.tick>40);
        return map.get(key);
    }
    public static boolean repeated(AbstractArrow arrow,LivingEntity target) {
        var key=key(arrow);var r=key==null?null:receipt(target,key);
        return r!=null&&r.arrows.contains(arrow.getUUID());
    }
    public static DamageSource source(AbstractArrow arrow,LivingEntity target,DamageSource original) {
        var key=key(arrow); var r=key==null?null:receipt(target,key);
        if(r==null||r.arrows.contains(arrow.getUUID()))return original;
        return new DamageSource(original.typeHolder(),arrow,arrow.getOwner()) {
            @Override public boolean is(TagKey<DamageType> tag) {
                return tag==DamageTypeTags.BYPASSES_COOLDOWN||super.is(tag);
            }
        };
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0||!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow))return;
        var key=key(arrow);if(key==null)return;
        var map=HITS.computeIfAbsent(event.getEntity(),unused->new HashMap<>());
        if(map.size()>=8&&!map.containsKey(key))map.clear();
        var r=map.computeIfAbsent(key,unused->new Receipt());
        r.tick=event.getEntity().level().getGameTime(); r.arrows.add(arrow.getUUID());
        var weapon=arrow.getWeaponItem();
        if(weapon!=null&&weapon.getItem() instanceof SerpentCrossbow crossbow&&crossbow.special){
            int mode=arrow.getPersistentData().getInt("GorynychImpactMode")-1;
            if(mode==0)event.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,40));
            if(mode==1)event.getEntity().igniteForSeconds(2);
        }
    }
    private CrossbowDamage() {}
}
