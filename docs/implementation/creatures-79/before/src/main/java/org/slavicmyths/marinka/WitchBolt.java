package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
/** Local segment collision, bounded flight and one reflection; not a homing invisible hit. */
public final class WitchBolt extends Projectile {
 public UUID boss,arena,encounter;public int armingTicks=9;public float raw=17.5F;public boolean reflectable=true,reflected;private Vec3 launch=Vec3.ZERO;
 public WitchBolt(EntityType<? extends WitchBolt> t,Level w){super(t,w);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 public MarinkaEntity activeBoss(){return level() instanceof ServerLevel w&&boss!=null&&w.getEntity(boss) instanceof MarinkaEntity m&&m.active?m:null;}
 public void launch(Vec3 velocity){launch=velocity;setDeltaMovement(velocity);}
 public boolean reflect(DobrynyaEntity h){var b=activeBoss();if(reflected||!reflectable||b==null)return false;reflected=true;setOwner(h);launch=b.position().add(0,1,0).subtract(position()).normalize().scale(.8);setDeltaMovement(launch);b.deflected(h);return true;}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;var b=activeBoss();if(b==null||tickCount>80){discard();return;}if(tickCount<armingTicks&&!reflected)return;Vec3 from=position(),to=from.add(launch);if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(to))){discard();return;}var wall=w.clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));double first=wall.getType()==HitResult.Type.MISS?2:from.distanceToSqr(wall.getLocation());LivingEntity hit=null;for(var e:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().expandTowards(launch).inflate(.25),e->e.isAlive()&&e!=getOwner()&&!(e instanceof HexAnchor)&&!(e instanceof TurMinion)&&(!(e instanceof MarinkaEntity)||reflected))){var contact=e.getBoundingBox().inflate(.15).clip(from,to);if(contact.isPresent()){double d=from.distanceToSqr(contact.get());if(d<first){first=d;hit=e;}}}
  if(hit!=null){if(reflected&&hit instanceof MarinkaEntity m){hit.hurt(hSource(),13);if(getOwner() instanceof DobrynyaEntity h)m.deflected(h);}else hit.hurt(MarinkaEntity.source(b,"fire"),raw);discard();return;}
  if(wall.getType()!=HitResult.Type.MISS){if(!reflected){var rune=MarinkaContent.RUNE.get().create(w);rune.boss=boss;rune.arena=arena;rune.kind(2);rune.waitTicks=0;var pos=wall.getLocation();rune.moveTo(pos.x,pos.y+.02,pos.z);w.addFreshEntity(rune);}discard();return;}setPos(to);}
 private net.minecraft.world.damagesource.DamageSource hSource(){return getOwner() instanceof LivingEntity h?h.damageSources().mobAttack(h):damageSources().magic();}
 @Override protected void addAdditionalSaveData(CompoundTag n){if(boss!=null)n.putUUID("Boss",boss);if(arena!=null)n.putUUID("Arena",arena);}
 @Override protected void readAdditionalSaveData(CompoundTag n){boss=null;}
}
