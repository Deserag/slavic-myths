package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.slavicmyths.bayun.BayunContent;
import org.slavicmyths.bayun.BayunEntity;

/** Positional, one source per loaded cat; cancellation fades over three ticks. */
final class BayunSong extends AbstractTickableSoundInstance {
    private final BayunEntity cat;
    private int fade;
    BayunSong(BayunEntity cat){super(BayunContent.sound("song.loop"),SoundSource.NEUTRAL,RandomSource.create());this.cat=cat;looping=true;delay=0;volume=.5F;position();}
    private void position(){x=cat.getX();y=cat.getY()+1;z=cat.getZ();}
    @Override public void tick(){var mc=Minecraft.getInstance();position();boolean audible=mc.level==cat.level()&&!cat.isRemoved()&&cat.clip().equals("hypnosis_song")&&mc.player!=null&&mc.player.isAlive()&&mc.player.distanceToSqr(cat)<=225&&mc.player.hasLineOfSight(cat);if(!audible||fade>0){volume=.5F*(3-++fade)/3;if(fade>=3)stop();}}
}
