package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.industry.ChudTools;

/** Client-only propagated light. No block state, packets, server light or saved chunk mutations. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ChudLanternLight {
 private static volatile Set<Long> sources=Set.of();private static volatile ClientLevel world;private static int tick;
 public static int emission(BlockGetter level,long pos){return level==world&&sources.contains(pos)?14:0;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();var level=mc.level;
  if(level!=world){sources=Set.of();world=level;tick=0;}if(level==null||++tick%4!=0)return;
  Set<Long> next=new HashSet<>();
  // Only already tracked, nearby client players; bounded to eight sources. Co-located lamps merge.
  level.players().stream().filter(p->!p.isSpectator()&&mc.player!=null&&p.distanceToSqr(mc.player)<48*48)
   .sorted(Comparator.comparingDouble(p->p.distanceToSqr(mc.player)))
   .filter(p->p.getMainHandItem().is(ChudTools.LANTERN.get())||p.getOffhandItem().is(ChudTools.LANTERN.get())).limit(8)
   .forEach(p->{var pos=BlockPos.containing(p.getX(),p.getY()+1,p.getZ());if(level.hasChunk(pos.getX()>>4,pos.getZ()>>4)&&level.getBlockState(pos).isAir())next.add(pos.asLong());});
  var old=sources;if(next.equals(old))return;sources=Set.copyOf(next);
  var changed=new HashSet<>(old);changed.addAll(next);for(long pos:changed)level.getChunkSource().getLightEngine().checkBlock(BlockPos.of(pos));
 }
 private ChudLanternLight(){}
}
