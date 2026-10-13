package org.slavicmyths.mor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
/** Actual persisted greater-form arena state; legacy mobs stay normal in MorSnapshot. */
public record HigherSnapshot(boolean phase,int clock,int waitTicks,int beam,int cloud,int ground,int skill,Vec3 heading){
 private static final String P="slavicmyths:higher_";
 public HigherSnapshot{clock=Math.clamp(clock,0,48);waitTicks=Math.clamp(waitTicks,0,40);beam=Math.clamp(beam,0,190);cloud=Math.clamp(cloud,0,240);ground=Math.clamp(ground,0,60);if(skill!=10&&skill!=11&&skill!=12&&skill!=13&&skill!=15)skill=0;if(!Double.isFinite(heading.x)||!Double.isFinite(heading.y)||!Double.isFinite(heading.z)||heading.lengthSqr()>1.01)heading=Vec3.ZERO;}
 public void write(CompoundTag n){n.putBoolean(P+"phase",phase);n.putInt(P+"clock",clock);n.putInt(P+"wait",waitTicks);n.putInt(P+"beam",beam);n.putInt(P+"cloud",cloud);n.putInt(P+"ground",ground);n.putInt(P+"skill",skill);n.putDouble(P+"x",heading.x);n.putDouble(P+"y",heading.y);n.putDouble(P+"z",heading.z);}
 public static HigherSnapshot read(CompoundTag n){return new HigherSnapshot(n.getBoolean(P+"phase"),n.getInt(P+"clock"),n.getInt(P+"wait"),n.getInt(P+"beam"),n.getInt(P+"cloud"),n.getInt(P+"ground"),n.getInt(P+"skill"),new Vec3(n.getDouble(P+"x"),n.getDouble(P+"y"),n.getDouble(P+"z")));}
}
