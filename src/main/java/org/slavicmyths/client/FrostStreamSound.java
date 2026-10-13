package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.slavicmyths.morozko.FrostContent;
/** A single accepted channel, stopped by the actual synchronized item-use state. */
public final class FrostStreamSound extends AbstractTickableSoundInstance {
 private final int entity;private int age;
 public FrostStreamSound(int entity){super(FrostContent.SOUNDS.get("staff_stream").get(),SoundSource.PLAYERS,RandomSource.create());this.entity=entity;looping=true;delay=0;volume=.45f;}
 @Override public void tick(){var mc=Minecraft.getInstance();if(mc.level==null){stop();return;}var e=mc.level.getEntity(entity);if(!(e instanceof net.minecraft.world.entity.LivingEntity l)||!l.isAlive()||++age>70||age>4&&(!l.isUsingItem()||!l.getUseItem().is(FrostContent.STAFF.get()))||l==mc.player&&mc.screen!=null){stop();return;}x=l.getX();y=l.getY()+1;z=l.getZ();}
}
