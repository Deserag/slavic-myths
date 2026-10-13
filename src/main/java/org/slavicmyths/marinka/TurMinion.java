package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
/** Local herd, 35HP, 22-second lifetime, no ecological spawning or loot. */
public final class TurMinion extends WitchActor {
 public UUID boss,owner,arena;public long expires;
 public TurMinion(EntityType<? extends TurMinion> t,Level w){super(t,w);}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;if(boss==null||!(w.getEntity(boss) instanceof MarinkaEntity m)||!m.active||w.getGameTime()>=expires){discard();return;}var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null||m.yard()==null||m.yard().radius(position())>27){discard();return;}if(action()==WitchAction.BULL_CHARGE){if(age()==14&&edge(p)<1.7&&hasLineOfSight(p))p.hurt(damageSources().mobAttack(this),5);if(age()>=28)begin(WitchAction.IDLE);return;}if(tickCount%4==0){face(p);setTarget(p);if(edge(p)<1.7&&ready("attack")){cooldown("attack",30);begin(WitchAction.BULL_CHARGE);}else if(tickCount%8==0)getNavigation().moveTo(p,1);}}
 @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(boss!=null)n.putUUID("Boss",boss);if(owner!=null)n.putUUID("Owner",owner);if(arena!=null)n.putUUID("Arena",arena);n.putLong("Expires",expires);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);boss=null;}
}
