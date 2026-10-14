package org.slavicmyths.rpg.classes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassXpEvents {
    @SubscribeEvent public static void spawn(FinalizeSpawnEvent e){
        if(e.getSpawnType()==MobSpawnType.SPAWNER||e.getSpawnType()==MobSpawnType.TRIAL_SPAWNER||e.getSpawnType()==MobSpawnType.COMMAND||e.getSpawnType()==MobSpawnType.SPAWN_EGG||e.getSpawnType()==MobSpawnType.DISPENSER)
            e.getEntity().getPersistentData().putBoolean("ClassXpArtificial",true);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        if(e.isCanceled()||!(e.getSource().getEntity() instanceof ServerPlayer p)||!ClassXpService.eligible(p)||!ClassXpService.hostile(p,e.getEntity())||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        var mob=e.getEntity();if(mob.getPersistentData().getBoolean("ClassXpPaid"))return;mob.getPersistentData().putBoolean("ClassXpPaid",true);
        if(mob.getMaxHealth()>=100)ClassXpService.once(p,ClassXpService.AwardSource.COMBAT,net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString(),500);
        else ClassXpService.limited(p,ClassXpService.AwardSource.COMBAT,mob.getMaxHealth()>=40?40:12,240);
    }
    @SubscribeEvent public static void advancement(AdvancementEvent.AdvancementEarnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var id=e.getAdvancement().id();
        if(id.getNamespace().equals("slavicmyths")&&!id.getPath().equals("own_path")&&!id.getPath().equals("root")&&!id.getPath().startsWith("group_"))ClassXpService.once(p,ClassXpService.AwardSource.STORY,id.toString(),200);
        else if(id.getNamespace().equals("minecraft")&&id.getPath().startsWith("adventure/")&&!id.getPath().equals("adventure/root"))ClassXpService.once(p,ClassXpService.AwardSource.EXPLORATION,id.toString(),150);
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
        if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!ClassXpService.hostile(p,e.getEntity()))return;
        if(ClassState.data(p).getString("Base").equals("razboinik")&&e.getSource().getDirectEntity() instanceof AbstractArrow&&p.distanceToSqr(e.getEntity())>=256)ClassXpService.role(p,"ranged",e.getEntity());
    }
    private ClassXpEvents(){}
}
