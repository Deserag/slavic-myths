package org.slavicmyths.pesigolovtsy;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
public final class DogheadArrow extends Arrow {
 private UUID pack;
 public DogheadArrow(EntityType<? extends Arrow> t,Level w){super(t,w);pickup=Pickup.DISALLOWED;}
 public DogheadArrow(Level w,DogheadEntity owner,UUID pack){this(DogheadContent.ARROW.get(),w);setOwner(owner);this.pack=pack;setBaseDamage(2.7);}
 @Override protected boolean canHitEntity(Entity e){return !(e instanceof DogheadEntity d&&pack!=null&&pack.equals(d.pack))&&super.canHitEntity(e);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(pack!=null)n.putUUID("DogheadPack",pack);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);pack=n.hasUUID("DogheadPack")?n.getUUID("DogheadPack"):null;pickup=Pickup.DISALLOWED;}
}
