package org.slavicmyths.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
public final class VolkolakEntity extends org.slavicmyths.hunt.HuntMob{
 public VolkolakEntity(EntityType<? extends VolkolakEntity> t,World w){super(t,w);}
 @Override public boolean oven(){return false;}
 public static net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute attributes(){return org.slavicmyths.hunt.HuntMob.attributes(false);}
}
