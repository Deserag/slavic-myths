package org.slavicmyths.posad.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;

public final class ShackledSickleContent {
    public static final DeferredHolder<EntityType<?>,EntityType<ShackledSickleProjectile>> PROJECTILE=
        ModEntities.ENTITIES.register("mytar_shackled_sickle_projectile",
            ()->EntityType.Builder.<ShackledSickleProjectile>of(ShackledSickleProjectile::new,MobCategory.MISC)
                .sized(.5F,.5F).clientTrackingRange(20).updateInterval(1).build("slavicmyths:mytar_shackled_sickle_projectile"));
    public static void init(){}
    private ShackledSickleContent(){}
}
