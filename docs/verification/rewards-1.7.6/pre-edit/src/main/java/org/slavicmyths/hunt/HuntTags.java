package org.slavicmyths.hunt;
public final class HuntTags {
 public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> TARGETS=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","hunt_targets"));
 public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> BOSSES=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","boss_targets"));
 public static boolean detects(net.minecraft.world.entity.LivingEntity e,double distance){return e.isAlive()&&(e.getType().is(BOSSES)?distance<=192*192:e.getType().is(TARGETS)&&distance<=128*128);}
 private HuntTags(){}
}
