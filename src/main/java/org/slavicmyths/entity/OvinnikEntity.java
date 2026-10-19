package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public final class OvinnikEntity extends org.slavicmyths.hunt.HuntMob {
 public OvinnikEntity(EntityType<? extends OvinnikEntity> t,Level w){super(t,w);}
 @Override protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose){return morState()!=0?net.minecraft.world.entity.EntityDimensions.scalable(1.65F,3.1F).withEyeHeight(2.65F):super.getDefaultDimensions(pose);}
 @Override public boolean oven(){return true;}
 @Override public float getVoicePitch(){return morState()==0?1:super.getVoicePitch();}
 public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return org.slavicmyths.hunt.HuntMob.attributes(true);}
}
