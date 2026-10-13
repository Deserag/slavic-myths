package org.slavicmyths.finist;
import java.util.UUID;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;
/** Stationary, damageable temporary prop; never writes terrain. */
public final class BannerEntity extends FalconActor {
 public UUID boss,encounter;public long expiry;
 public BannerEntity(EntityType<? extends BannerEntity> t,Level w){super(t,w);setNoAi(true);}
 public boolean anchor(){return getType()==FinistContent.ANCHOR.get();}
 @Override public void tick(){super.tick();if(level() instanceof ServerLevel w&&tickCount%10==0){var e=boss==null?null:w.getEntity(boss);if(!(e instanceof KartausEntity k)||!k.isAlive()||!k.validEncounter()||(anchor()?k.anchor()!=this:!k.banners.contains(getUUID()))){discard();return;}if(expiry>0&&w.getGameTime()>=expiry){k.propGone(this,false);discard();}}}
 @Override public void die(DamageSource s){cue(anchor()?"event.encounter.curse_anchor_die":"event.encounter.black_banner_die");if(level() instanceof ServerLevel w&&boss!=null&&w.getEntity(boss) instanceof KartausEntity k)k.propGone(this,true);super.die(s);discard();}
 @Override public boolean isPushable(){return false;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(boss!=null)n.putUUID("KartausBoss",boss);if(encounter!=null)n.putUUID("KartausEncounter",encounter);n.putLong("Expires",expiry);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);boss=n.hasUUID("KartausBoss")?n.getUUID("KartausBoss"):null;encounter=n.hasUUID("KartausEncounter")?n.getUUID("KartausEncounter"):null;expiry=n.getLong("Expires");}
}
