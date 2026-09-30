package org.slavicmyths.client;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.water.RusalkaEntity;
import org.slavicmyths.registry.ModSounds;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RusalkaSound extends TickableSound {
 private static final WeakHashMap<RusalkaEntity,RusalkaSound> ACTIVE=new WeakHashMap<>();private final RusalkaEntity singer;
 private RusalkaSound(RusalkaEntity e){super(ModSounds.RUSALKA_SONG.get(),SoundCategory.HOSTILE);singer=e;looping=true;delay=0;volume=1.25F;pitch=1;x=e.getX();y=e.getY()+1;z=e.getZ();}
 @SubscribeEvent public static void living(LivingEvent.LivingUpdateEvent e){if(!(e.getEntityLiving() instanceof RusalkaEntity)||!e.getEntityLiving().level.isClientSide)return;RusalkaEntity r=(RusalkaEntity)e.getEntityLiving();if(r.singing()&&!ACTIVE.containsKey(r)){RusalkaSound s=new RusalkaSound(r);ACTIVE.put(r,s);Minecraft.getInstance().getSoundManager().play(s);}}
 public void tick(){if(!singer.isAlive()||singer.removed||!singer.singing()||Minecraft.getInstance().level!=singer.level){stop();ACTIVE.remove(singer);return;}x=singer.getX();y=singer.getY()+1;z=singer.getZ();}
}
