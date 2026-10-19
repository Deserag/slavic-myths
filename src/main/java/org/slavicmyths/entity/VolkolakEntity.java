package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public final class VolkolakEntity extends org.slavicmyths.hunt.HuntMob{
 public VolkolakEntity(EntityType<? extends VolkolakEntity> t,Level w){super(t,w);}
 @Override public boolean oven(){return false;}
 @Override protected void huntVoice(boolean howl){playSound((howl?org.slavicmyths.registry.ModSounds.VOLKOLAK_HUNT_HOWL:org.slavicmyths.registry.ModSounds.VOLKOLAK_HUNT_RAGE).get(),.85F,1.0F);}
 public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(){return org.slavicmyths.hunt.HuntMob.attributes(false);}
}
