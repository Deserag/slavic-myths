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
import org.slavicmyths.water.RusalkaEncounter;
import org.slavicmyths.water.RusalkaActions;
import org.slavicmyths.registry.ModSounds;

/** One owned vocal at a time: recorded intro -> recorded calm/intense loop. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RusalkaSound extends AbstractTickableSoundInstance {
 private static final int MAX_LOOPS=4;
 // Owner must accept recorded replacements before this gate can be enabled.
 private static final boolean APPROVED_VOCALS_AVAILABLE=false;
 private static final Map<UUID,RusalkaSound> ACTIVE=new HashMap<>();
 private final RusalkaEntity singer;private final RusalkaEncounter.Vocal stage;private int playedTicks;
 private static RusalkaEncounter.Vocal stage(RusalkaEntity r){return RusalkaEncounter.vocal(r.songElapsed(),r.visualAction()==RusalkaActions.INTENSE||r.visualAction()==RusalkaActions.WARNING);}
 private RusalkaSound(RusalkaEntity e){
  super(switch(stage(e)){case INTRO->ModSounds.RUSALKA_SONG_INTRO.get();case LOOP->ModSounds.RUSALKA_SONG_LOOP.get();case INTENSE->ModSounds.RUSALKA_SONG_INTENSE.get();},SoundSource.HOSTILE,net.minecraft.util.RandomSource.create());
  singer=e;stage=stage(e);looping=stage!=RusalkaEncounter.Vocal.INTRO;delay=0;volume=.01F;pitch=1;updatePosition();
 }
 private boolean audible(){
  var mc=Minecraft.getInstance();return mc.level==singer.level()&&mc.player!=null&&singer.isAlive()&&!singer.isRemoved()
   &&singer.singing()&&singer.distanceToSqr(mc.player)<=400&&singer.hasLineOfSight(mc.player);
 }
 private void updatePosition(){x=singer.getX();y=singer.getY()+1;z=singer.getZ();}
 private void finish(){stop();Minecraft.getInstance().getSoundManager().stop(this);ACTIVE.remove(singer.getUUID(),this);}
 @SubscribeEvent public static void living(EntityTickEvent.Post event){
  if(!APPROVED_VOCALS_AVAILABLE||!(event.getEntity() instanceof RusalkaEntity r)||!r.level().isClientSide)return;
  var existing=ACTIVE.get(r.getUUID());
  if(existing!=null){if(existing.audible()&&existing.stage==stage(r))return;existing.finish();}
  if(ACTIVE.size()>=MAX_LOOPS)return;
  var sound=new RusalkaSound(r);if(!sound.audible())return;
  ACTIVE.put(r.getUUID(),sound);Minecraft.getInstance().getSoundManager().play(sound);
 }
 @SubscribeEvent public static void clientTick(ClientTickEvent.Post event){
  var manager=Minecraft.getInstance().getSoundManager();
  for(var sound:java.util.List.copyOf(ACTIVE.values()))if(!sound.audible()||sound.isStopped()||sound.playedTicks>2&&!manager.isActive(sound))sound.finish();
 }
 @Override public void tick(){
  if(!audible()){finish();return;}playedTicks++;updatePosition();
  float target=stage==RusalkaEncounter.Vocal.INTENSE?.70F:.65F;volume+=Math.clamp(target-volume,-.06F,.065F);
 }
}
