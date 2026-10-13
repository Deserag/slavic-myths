package org.slavicmyths.kalin;
import java.util.UUID;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;
/** One owned mount; no independent roaming or attacks. */
public final class WarHorse extends org.slavicmyths.finist.FalconActor {
 public UUID commander;public long exitAt;
 public WarHorse(EntityType<? extends WarHorse> t,Level w){super(t,w);}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;if(exitAt>0){if(w.getGameTime()>=exitAt){discard();return;}if(tickCount%5==0){Vec3 shift=new Vec3(.18,0,0);if(w.hasChunkAt(blockPosition().offset(1,-1,0))&&org.slavicmyths.sivka.SivkaWhistle.safeFloor(w,blockPosition().offset(1,-1,0))&&w.noCollision(this,getBoundingBox().move(shift)))move(MoverType.SELF,shift);}return;}if(commander!=null&&(!(w.getEntity(commander) instanceof KalinEntity k)||!k.valid()||!k.isPassengerOfSameVehicle(this))){discard();return;}if(tickCount%12==0&&getDeltaMovement().horizontalDistanceSqr()>.0001){playSound(KalinContent.sound(tickCount%24==0?"horse_step_01":"horse_step_02"),.4F,1);playSound(net.minecraft.sounds.SoundEvents.HORSE_ARMOR,.15F,1);}}
 @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return net.minecraft.sounds.SoundEvents.HORSE_BREATHE;}
 @Override public Vec3 getPassengerRidingPosition(Entity p){return position().add(0,1.4,0);}
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float d){if(commander!=null&&level() instanceof ServerLevel w&&w.getEntity(commander) instanceof KalinEntity k)return k.hurt(s,d);return super.hurt(s,d);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(commander!=null)n.putUUID("KalinCommander",commander);n.putLong("KalinExit",exitAt);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);commander=n.hasUUID("KalinCommander")?n.getUUID("KalinCommander"):null;exitAt=n.getLong("KalinExit");}
}
