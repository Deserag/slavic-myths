package org.slavicmyths.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slavicmyths.water.RusalkaEntity;
import org.slavicmyths.registry.ModSounds;

/** Bounded ownership; legacy recording pending a licensed vocal source (#49). */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RusalkaSound extends AbstractTickableSoundInstance {
 private static final int MAX_LOOPS=4;
 // Re-enable ONLY after a real original singing source is supplied and accepted (#59).
 private static final boolean APPROVED_VOCALS_AVAILABLE=false;
 private static final Map<UUID,RusalkaSound> ACTIVE=new HashMap<>();
 private final RusalkaEntity singer;private int playedTicks;
 private RusalkaSound(RusalkaEntity e){
  super(ModSounds.RUSALKA_SONG.get(),SoundSource.HOSTILE,net.minecraft.util.RandomSource.create());
  singer=e;looping=true;delay=0;volume=.01F;pitch=1;updatePosition();
 }
 private boolean audible(){
  var mc=Minecraft.getInstance();return mc.level==singer.level()&&mc.player!=null&&singer.isAlive()&&!singer.isRemoved()
   &&singer.singing()&&singer.distanceToSqr(mc.player)<=400&&singer.hasLineOfSight(mc.player);
 }
 private void updatePosition(){x=singer.getX();y=singer.getY()+1;z=singer.getZ();}
 private void finish(){stop();ACTIVE.remove(singer.getUUID(),this);}
 @SubscribeEvent public static void living(EntityTickEvent.Post event){
  if(!APPROVED_VOCALS_AVAILABLE||!(event.getEntity() instanceof RusalkaEntity r)||!r.level().isClientSide||ACTIVE.containsKey(r.getUUID())||ACTIVE.size()>=MAX_LOOPS)return;
  var sound=new RusalkaSound(r);if(!sound.audible())return;
  ACTIVE.put(r.getUUID(),sound);Minecraft.getInstance().getSoundManager().play(sound);
 }
 @SubscribeEvent public static void clientTick(ClientTickEvent.Post event){
  var manager=Minecraft.getInstance().getSoundManager();
  // Also handles stopped sounds/resource reload, level replacement and disconnect with no entity ticks.
  for(var sound:java.util.List.copyOf(ACTIVE.values()))if(!sound.audible()||sound.isStopped()||sound.playedTicks>2&&!manager.isActive(sound))sound.finish();
 }
 @Override public void tick(){
  if(!audible()){finish();return;}playedTicks++;updatePosition();
  float target=singer.state()==4?.30F:.22F;volume+=Math.clamp(target-volume,-.03F,.02F);
 }
}
