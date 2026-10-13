package org.slavicmyths.underground.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Also releases an already acquired target after equipping the second rune, without scanning mobs. */
@Mixin(LivingEntity.class)
public abstract class OminousContinueMixin {
 @Inject(method="canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At("HEAD"),cancellable=true)
 private void slavicmyths$continue(LivingEntity target,CallbackInfoReturnable<Boolean> cir){if((Object)this instanceof Mob mob&&target instanceof Player p&&org.slavicmyths.underground.OminousAura.ignores(mob,p))cir.setReturnValue(false);}
}
