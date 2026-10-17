package org.slavicmyths.progression;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;

/** Advancement-only callbacks. Never rolls loot, changes AI, or imports at login. */
public final class AdvancementMilestones {
    private static final String PENDING="slavicmyths:advancement2_pending";
    private static final Map<String,String> SUBJECTS=Map.ofEntries(
        Map.entry("defeat_gorynych","zmey_gorynych"),
        Map.entry("curse_resolved","cursed_volkolak"),Map.entry("night_watch","nochnitsa"));

    public static boolean eligible(ServerPlayer p){return p!=null&&!p.isCreative()&&!p.isSpectator();}
    public static boolean combatDeath(DamageSource source){return source!=null&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&!source.is(DamageTypes.GENERIC_KILL)&&!(source.getEntity() instanceof ServerPlayer p&&!eligible(p));}
    public static void award(ServerPlayer p,String id){
        if(!eligible(p)||Knowledge.knows(p,id))return;
        Knowledge.award(p,id);
        String subject=SUBJECTS.get(id);
        if(subject!=null)org.slavicmyths.tales.TaleService.subject(p,"entity/slavicmyths:"+subject,4,true);
    }
    /** Native ledgers may commit before super.die; queue ONLY their accepted recipients. */
    public static void beginDeath(LivingEntity boss){boss.getPersistentData().remove(PENDING);}
    public static void nativeVictory(ServerPlayer p,LivingEntity boss,String id){
        if(!eligible(p)||boss==null||boss.level()!=p.level())return;
        var data=boss.getPersistentData();
        var pending=data.getList(PENDING,Tag.TAG_COMPOUND);
        for(var tag:pending){var row=(CompoundTag)tag;if(row.hasUUID("Player")&&row.getUUID("Player").equals(p.getUUID())&&row.getString("Id").equals(id))return;}
        // This batch is bounded by the native callback's accepted online recipients;
        // do not impose a new co-op player cap on those existing policies.
        var row=new CompoundTag();row.putUUID("Player",p.getUUID());row.putString("Id",id);pending.add(row);data.put(PENDING,pending);
    }
    /** Caller passes the actual !wasDead && dead flag AFTER native super.die cancellation. */
    public static void endDeath(LivingEntity boss,boolean accepted,DamageSource source){
        var pending=boss.getPersistentData().getList(PENDING,Tag.TAG_COMPOUND).copy();
        boss.getPersistentData().remove(PENDING);
        if(!accepted||!combatDeath(source)||!(boss.level() instanceof ServerLevel w))return;
        for(var tag:pending){var row=(CompoundTag)tag;if(row.hasUUID("Player")){
            var p=w.getServer().getPlayerList().getPlayer(row.getUUID("Player"));
            if(p!=null&&p.serverLevel()==w)award(p,row.getString("Id"));
        }}
    }
    private AdvancementMilestones(){}
}
