package org.slavicmyths.entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
public final class OvinnikEntity extends org.slavicmyths.hunt.HuntMob {
 public OvinnikEntity(EntityType<? extends OvinnikEntity> t,World w){super(t,w);}
 @Override public boolean oven(){return true;}
 public static net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute attributes(){return org.slavicmyths.hunt.HuntMob.attributes(true);}
}
