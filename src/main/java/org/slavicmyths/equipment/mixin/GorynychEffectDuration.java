package org.slavicmyths.equipment.mixin;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MobEffectInstance.class)
public interface GorynychEffectDuration {
 @Accessor("duration") void slavicmyths$gorynychDuration(int ticks);
}
