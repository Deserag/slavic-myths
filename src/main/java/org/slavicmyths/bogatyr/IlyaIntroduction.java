package org.slavicmyths.bogatyr;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
/** Compatibility facade: Ilya uses the shared four-hero introduction service. */
public final class IlyaIntroduction {
 public static final String ENTRY="slavicmyths:ilya_introduction",OWNER="IlyaIntroductionOwner";
 public static CompoundTag state(ServerPlayer p){return HeroIntroductions.state(p,"ilya_muromets");}
 public static boolean unlocked(ServerPlayer p){return HeroIntroductions.unlocked(p,"ilya_muromets");}
 public static boolean mayCall(ServerPlayer p){return HeroIntroductions.mayCall(p,"ilya_muromets");}
 public static boolean offer(ServerPlayer p,String giver){return HeroIntroductions.offer(p,giver,"ilya_muromets");}
 public static InteractionResult interact(IlyaEntity npc,ServerPlayer p,InteractionHand hand){return HeroIntroductions.interact(npc,p,hand);}
 private IlyaIntroduction(){}
}
