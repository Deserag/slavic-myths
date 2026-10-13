package org.slavicmyths.morozko;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.*;
public final class FrostVisuals {
 public static void burst(ServerLevel w,Vec3 p,int count){w.sendParticles(ParticleTypes.SNOWFLAKE,p.x,p.y,p.z,count,.6,.6,.6,.04);}
 public static void ring(ServerLevel w,Vec3 p,double radius,double height,int count){for(int i=0;i<count;i++){double a=Math.PI*2*i/count;w.sendParticles(new DustParticleOptions(new org.joml.Vector3f(.55f,.85f,1),.8f),p.x+Math.cos(a)*radius,p.y+height,p.z+Math.sin(a)*radius,1,0,0,0,0);}}
 public static void storm(ServerLevel w,Vec3 p,double radius){w.sendParticles(ParticleTypes.SNOWFLAKE,p.x,p.y+2,p.z,18,radius/2,2,radius/2,.06);}
 public static void cone(ServerLevel w,net.minecraft.world.entity.LivingEntity e,double radius){var p=e.getEyePosition();var d=e.getLookAngle();for(int i=1;i<radius;i++)burst(w,p.add(d.scale(i)),3);}
 private FrostVisuals(){}
}
