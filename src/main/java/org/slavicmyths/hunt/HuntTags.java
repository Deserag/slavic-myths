package org.slavicmyths.hunt;
public final class HuntTags {
 public static final net.minecraftforge.common.Tags.IOptionalNamedTag<net.minecraft.entity.EntityType<?>> TARGETS=net.minecraft.tags.EntityTypeTags.createOptional(new net.minecraft.util.ResourceLocation("slavicmyths","hunt_targets"));
 public static final net.minecraftforge.common.Tags.IOptionalNamedTag<net.minecraft.entity.EntityType<?>> BOSSES=net.minecraft.tags.EntityTypeTags.createOptional(new net.minecraft.util.ResourceLocation("slavicmyths","boss_targets"));
 public static boolean detects(net.minecraft.entity.LivingEntity e,double distance){return e.isAlive()&&(e.getType().is(BOSSES)?distance<=192*192:e.getType().is(TARGETS)&&distance<=128*128);}
 private HuntTags(){}
}
