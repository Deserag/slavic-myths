package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public final class OvinnikEntity extends org.slavicmyths.hunt.HuntMob {
 public OvinnikEntity(EntityType<? extends OvinnikEntity> t,Level w){super(t,w);}
 @Override public boolean oven(){return true;}
 public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return org.slavicmyths.hunt.HuntMob.attributes(true);}
}
