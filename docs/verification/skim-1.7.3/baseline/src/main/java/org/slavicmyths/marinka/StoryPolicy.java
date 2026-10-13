package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
/** Matched hero metadata, used by every existing summon entry point. No future entities. */
public final class StoryPolicy {
    public static final Map<String,String> MATCH=Map.of("idolishche_poganoe","ilya_muromets","marinka","dobrynya_nikitich");
    public static boolean canSummon(ServerPlayer p,String hero){
        var d=WitchYard.get(p.serverLevel().getServer().overworld());var lock=d.lock(p.getUUID());
        if(lock!=null){p.sendSystemMessage(Component.translatable("msg.slavicmyths.story_dobrynya_only"));return false;}
        if(org.slavicmyths.bogatyr.BogatyrLedger.active(p)){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.no_midbattle_summon"));return false;}return true;
    }
    public static boolean standby(UUID owner,net.minecraft.server.level.ServerLevel w){return owner!=null&&WitchYard.get(w.getServer().overworld()).lock(owner)!=null;}
    private StoryPolicy(){}
}
