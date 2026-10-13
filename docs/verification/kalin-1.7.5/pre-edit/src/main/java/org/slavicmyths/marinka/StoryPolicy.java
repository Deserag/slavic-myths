package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
/** Matched hero metadata, used by every existing summon entry point. No future entities. */
public final class StoryPolicy {
    public static final Map<String,String> MATCH=Map.of("idolishche_poganoe","ilya_muromets","marinka","dobrynya_nikitich","skim_beast","alyosha_popovich","kartaus","finist");
    public static boolean canSummon(ServerPlayer p,String hero){
        if(org.slavicmyths.finist.KartausEncounter.get(p.serverLevel()).owner(p.getUUID())!=null||org.slavicmyths.finist.KartausEncounter.get(p.serverLevel()).near(p.serverLevel(),p.position())){org.slavicmyths.finist.KartausEncounter.msg(p,"event_no_midfight_summon");return false;}
        var valley=org.slavicmyths.skim.SkimValley.get(p.serverLevel());if(valley.lock(p.getUUID())!=null){org.slavicmyths.skim.HeroCalls.message(p,"summon.battle_locked");return false;}
        var nearby=valley.near(p.position());if(nearby!=null&&(nearby.status==org.slavicmyths.skim.SkimValley.Status.ACTIVE||nearby.status==org.slavicmyths.skim.SkimValley.Status.STARTING)){org.slavicmyths.skim.HeroCalls.message(p,"summon.battle_locked");return false;}if(nearby!=null&&(nearby.status==org.slavicmyths.skim.SkimValley.Status.READY)&&!hero.equals("alyosha_popovich")){org.slavicmyths.skim.HeroCalls.message(p,"summon.wrong_hero");return false;}
        if(!org.slavicmyths.skim.HeroCalls.map(p.serverLevel(),hero).containsKey(p.getUUID())&&org.slavicmyths.skim.HeroCalls.count(p)>=2){org.slavicmyths.skim.HeroCalls.message(p,"summon.party_full");return false;}
        var d=WitchYard.get(p.serverLevel().getServer().overworld());var lock=d.lock(p.getUUID());
        if(lock!=null){p.sendSystemMessage(Component.translatable("msg.slavicmyths.story_dobrynya_only"));return false;}
        if(org.slavicmyths.bogatyr.BogatyrLedger.active(p)){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.no_midbattle_summon"));return false;}return true;
    }
    public static boolean standby(UUID owner,net.minecraft.server.level.ServerLevel w){return owner!=null&&(org.slavicmyths.finist.KartausEncounter.get(w).owner(owner)!=null||WitchYard.get(w.getServer().overworld()).lock(owner)!=null||org.slavicmyths.skim.SkimValley.get(w).lock(owner)!=null);}
    private StoryPolicy(){}
}
