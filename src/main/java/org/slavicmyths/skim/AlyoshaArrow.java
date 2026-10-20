package org.slavicmyths.skim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/** Ordinary non-explosive bow shot, bounded to18b, never damages peaceful or player allies. */
public final class AlyoshaArrow extends Arrow {
 private Vec3 launch;private int life;
 public AlyoshaArrow(EntityType<? extends AlyoshaArrow> t,Level w){super(t,w);}
 public AlyoshaArrow(Level w,AlyoshaEntity owner){this(SkimContent.ARROW.get(),w);setOwner(owner);setPos(owner.getX(),owner.getEyeY()-.1,owner.getZ());launch=position();setBaseDamage(.8);pickup=Pickup.DISALLOWED;}
 @Override protected boolean canHitEntity(Entity e){if(!(e instanceof LivingEntity l)||!(getOwner() instanceof AlyoshaEntity a)||a.owner==null||!l.isAlive())return false;if(e instanceof SkimBeastEntity b)return b.valid(a);if(e instanceof org.slavicmyths.hunt.WorldBoss b&&b.kind()==org.slavicmyths.hunt.BossKind.TUGARIN&&level() instanceof net.minecraft.server.level.ServerLevel w){var player=w.getServer().getPlayerList().getPlayer(a.owner);return org.slavicmyths.bogatyr.HeroThreats.allowed(a,player,b)&&super.canHitEntity(e);}return e instanceof net.minecraft.world.entity.monster.Monster m&&!a.isAlliedTo(m)&&(m.getTarget()==a||(m.getTarget() instanceof net.minecraft.server.level.ServerPlayer p&&a.owner.equals(p.getUUID())))&&super.canHitEntity(e);}
 @Override public void tick(){super.tick();if(!level().isClientSide&&(++life>50||launch==null||position().distanceToSqr(launch)>18*18))discard();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(launch!=null){n.putDouble("LaunchX",launch.x);n.putDouble("LaunchY",launch.y);n.putDouble("LaunchZ",launch.z);}n.putInt("ArrowLife",life);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);launch=new Vec3(n.getDouble("LaunchX"),n.getDouble("LaunchY"),n.getDouble("LaunchZ"));life=n.getInt("ArrowLife");}
}
