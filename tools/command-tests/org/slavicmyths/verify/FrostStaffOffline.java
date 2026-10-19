package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import org.slavicmyths.morozko.FrostStaffTargeting;

/** Unconstructed entity type probes exercise the actual targeting predicate only.
 * No world, hurt simulation or gameplay launch. Geometry uses native Vec3/AABB. */
public final class FrostStaffOffline {
 private static int checks;
 private static sun.misc.Unsafe unsafe;
 public static final class Peaceful extends Cow {
  boolean alive=true,spectator;
  Peaceful(){super(EntityType.COW,null);}
  @Override public boolean isAlive(){return alive;}
  @Override public boolean isSpectator(){return spectator;}
 }
 public static final class Hostile extends Zombie {
  Hostile(){super(EntityType.ZOMBIE,null);}
  @Override public boolean isAlive(){return true;}
  @Override public boolean isSpectator(){return false;}
 }
 public static final class Operator extends ServerPlayer {
  boolean friendlyFire;
  Operator(){super(null,null,null,null);}
  @Override public boolean isAlive(){return true;}
  @Override public boolean isSpectator(){return false;}
  @Override public boolean canHarmPlayer(Player other){return friendlyFire;}
 }
 private static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
 @SuppressWarnings("unchecked") private static <T>T probe(Class<T> type)throws Exception{return (T)unsafe.allocateInstance(type);}
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=(sun.misc.Unsafe)field.get(null);
  var caster=probe(Operator.class);var peaceful=probe(Peaceful.class);peaceful.alive=true;
  var hostile=probe(Hostile.class);var other=probe(Operator.class);
  check(FrostStaffTargeting.canHit(peaceful,caster),"Neutral cow accepted without Enemy/retaliation target");
  check(FrostStaffTargeting.canHit(hostile,caster),"Hostile still accepted");
  check(!FrostStaffTargeting.canHit(caster,caster),"Caster excluded");
  peaceful.alive=false;check(!FrostStaffTargeting.canHit(peaceful,caster),"Dead target excluded");peaceful.alive=true;
  peaceful.spectator=true;check(!FrostStaffTargeting.canHit(peaceful,caster),"Spectator excluded");peaceful.spectator=false;
  caster.friendlyFire=false;check(!FrostStaffTargeting.canHit(other,caster),"Player team protection retained");
  caster.friendlyFire=true;check(FrostStaffTargeting.canHit(other,caster),"Eligible PvP target accepted; actual server PvP remains in hurt");
  var eye=new Vec3(0,1.62,0);var look=new Vec3(0,0,1);var closeHead=new AABB(-.3,0,1.2,.3,1.8,1.8);
  check(look.dot(closeHead.getCenter().subtract(eye).normalize())<Math.cos(Math.toRadians(12.5)),"Reproduces former centre-only close-head miss");
  check(FrostStaffTargeting.intersectsStream(eye,look,closeHead,10,12.5),"Direct head aim now intersects living hitbox");
  check(FrostStaffTargeting.intersectsStream(eye,look,new AABB(-.2,1.55,3,.2,1.7,3.4),10,12.5),"Small target crossed by ray accepted");
  check(!FrostStaffTargeting.intersectsStream(eye,look,new AABB(3,0,2,3.5,1.8,2.5),10,12.5),"Outside cone excluded");
  check(!FrostStaffTargeting.intersectsStream(eye,look,new AABB(-.3,0,11,.3,1.8,12),10,12.5),"Out of range excluded");
  check(!FrostStaffTargeting.intersectsStream(eye,look,new AABB(-.3,0,-2,.3,1.8,-1),10,12.5),"Behind caster excluded");
  System.out.println("FROST_STAFF_OFFLINE PASS "+checks+" actual predicate/geometry checks; damage/runtime NOT_RUN");
 }
}
