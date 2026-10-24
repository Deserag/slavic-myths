package org.slavicmyths.progression;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Bounded, entity-local damage credit for encounters that had no personal ledger.
 * Only damage callbacks, no tick polling, player/world scans or chunk requests.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class EncounterMilestoneCredit {
    private static final String KEY="slavicmyths:advancement2_contributors";
    @SubscribeEvent public static void damage(LivingDamageEvent.Post event){
        var mob=event.getEntity();
        if(!(mob.level() instanceof ServerLevel)||!(mob instanceof org.slavicmyths.depth.ElderVodyanoy||mob instanceof org.slavicmyths.kurgan.KurganCreature||mob instanceof org.slavicmyths.underground.RoomBoss))return;
        if(event.getNewDamage()<=0||!Float.isFinite(event.getNewDamage())||!AdvancementMilestones.combatDeath(event.getSource())||!(event.getSource().getEntity() instanceof ServerPlayer p)||!AdvancementMilestones.eligible(p))return;
        record(mob,p);
    }
    private static void record(LivingEntity mob,ServerPlayer p){
        var rows=mob.getPersistentData().getList(KEY,Tag.TAG_STRING);
        var id=StringTag.valueOf(p.getUUID().toString());
        if(!rows.contains(id)&&rows.size()<64){rows.add(id);mob.getPersistentData().put(KEY,rows);}
    }
    public static void victory(LivingEntity mob,DamageSource source,String id){
        if(!(mob.level() instanceof ServerLevel w)||!AdvancementMilestones.combatDeath(source)||mob.getPersistentData().getBoolean("RuneSummoned"))return;
        // A natural/admin death without a causal player is not an earned win.
        if(!(source.getEntity() instanceof ServerPlayer killer)||!AdvancementMilestones.eligible(killer))return;
        AdvancementMilestones.award(killer,id); // Lethal hit can precede Post, even if the tracker is full.
        for(var tag:mob.getPersistentData().getList(KEY,Tag.TAG_STRING)){
            UUID uuid;try{uuid=UUID.fromString(tag.getAsString());}catch(IllegalArgumentException malformed){continue;}
            var p=w.getServer().getPlayerList().getPlayer(uuid);
            if(p!=null&&p.serverLevel()==w&&p.distanceToSqr(mob)<=96*96)AdvancementMilestones.award(p,id);
        }
    }
    private EncounterMilestoneCredit(){}
}
