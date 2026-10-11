package org.slavicmyths.underground;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.sounds.*;
public abstract class RoomBoss extends EncounterMob {
 private final ServerBossEvent bar=new ServerBossEvent(getDisplayName(),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
 public RoomBoss(EntityType<? extends Monster> t,Level l){super(t,l);}
 public void aiStep(){super.aiStep();if(!level().isClientSide){bar.setName(getDisplayName());bar.setProgress(getHealth()/getMaxHealth());bar.setVisible(isAlive()&&getTarget()!=null);}}
 public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 public void remove(RemovalReason r){bar.removeAllPlayers();super.remove(r);}
 public void die(net.minecraft.world.damagesource.DamageSource d){bar.removeAllPlayers();super.die(d);}
 protected void sound(String kind,float pitch){playSound(UndergroundSounds.sound(this instanceof Kladovik?"kladovik":"large_podzemnik",kind),.7F,pitch);}
 protected SoundEvent getAmbientSound(){return UndergroundSounds.sound(this instanceof Kladovik?"kladovik":"large_podzemnik","idle");}
 protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource d){return UndergroundSounds.sound(this instanceof Kladovik?"kladovik":"large_podzemnik","hurt");}
 protected SoundEvent getDeathSound(){return UndergroundSounds.sound(this instanceof Kladovik?"kladovik":"large_podzemnik","death");}
}
