package org.slavicmyths.morovoy;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Reusable three-tone magical fire, scoped to callers only; vanilla fire remains unchanged. */
public final class PurpleFlameVisuals {
 public static final DustParticleOptions DARK=new DustParticleOptions(new Vector3f(.15f,.02f,.28f),1.2f);
 public static final DustParticleOptions BRIGHT=new DustParticleOptions(new Vector3f(.65f,.07f,.98f),1.05f);
 public static final DustParticleOptions HEART=new DustParticleOptions(new Vector3f(1f,1f,1f),.52f);
 public static void burst(ServerLevel level,Vec3 at,int amount){
  int n=Math.clamp(amount,1,80);level.sendParticles(DARK,at.x,at.y,at.z,n,.9,.55,.9,.03);
  level.sendParticles(BRIGHT,at.x,at.y,at.z,n/2,.6,.35,.6,.035);
  level.sendParticles(HEART,at.x,at.y,at.z,Math.max(1,n/6),.2,.17,.2,.016);
 }
 public static void trail(Level level,Vec3 at,int age){level.addParticle(age%4==0?HEART:age%2==0?BRIGHT:DARK,at.x,at.y,at.z,0,.014,0);}
 private PurpleFlameVisuals(){}
}
