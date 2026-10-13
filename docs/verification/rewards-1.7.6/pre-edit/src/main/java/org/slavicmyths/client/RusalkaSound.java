package org.slavicmyths.client;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.water.RusalkaEntity;
import org.slavicmyths.registry.ModSounds;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RusalkaSound extends AbstractTickableSoundInstance {
 private static final WeakHashMap<RusalkaEntity,RusalkaSound> ACTIVE=new WeakHashMap<>();private final RusalkaEntity singer;
 private RusalkaSound(RusalkaEntity e){super(ModSounds.RUSALKA_SONG.get(),SoundSource.HOSTILE,net.minecraft.util.RandomSource.create());singer=e;looping=true;delay=0;volume=1.25F;pitch=1;x=e.getX();y=e.getY()+1;z=e.getZ();}
 @SubscribeEvent public static void living(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(!(e.getEntity() instanceof RusalkaEntity)||!e.getEntity().level().isClientSide)return;RusalkaEntity r=(RusalkaEntity)e.getEntity();if(r.singing()&&!ACTIVE.containsKey(r)){RusalkaSound s=new RusalkaSound(r);ACTIVE.put(r,s);Minecraft.getInstance().getSoundManager().play(s);}}
 public void tick(){if(!singer.isAlive()||singer.isRemoved()||!singer.singing()||Minecraft.getInstance().level!=singer.level()){stop();ACTIVE.remove(singer);return;}x=singer.getX();y=singer.getY()+1;z=singer.getZ();}
}
