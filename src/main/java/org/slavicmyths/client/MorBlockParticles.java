package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Short existing Mor particles, maximum 24 per emission (40 ticks); no persistent chunk state. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MorBlockParticles {
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.player.tickCount%40!=0)return;int emitted=0;for(var p:net.minecraft.core.BlockPos.betweenClosed(mc.player.blockPosition().offset(-2,-1,-2),mc.player.blockPosition().offset(2,1,2))){if(org.slavicmyths.mor.MorBlocks.index(mc.level.getBlockState(p))<0||!mc.level.getBlockState(p.above()).isAir())continue;for(int i=0;i<2;i++)mc.level.addParticle(org.slavicmyths.mor.MorContent.SPARK.get(),p.getX()+mc.level.random.nextDouble(),p.getY()+1.02,p.getZ()+mc.level.random.nextDouble(),0,.015,0);if((emitted+=2)>=24)break;}}
}
