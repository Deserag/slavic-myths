package org.slavicmyths.equipment.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.slavicmyths.gorynych.CrossbowDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class GorynychArrowDamageMixin {
    @ModifyVariable(method="onHitEntity",at=@At(value="STORE",ordinal=0),ordinal=0)
    private double slavicmyths$base(double damage) {
        return CrossbowDamage.baseDamage((AbstractArrow)(Object)this,damage);
    }
    @Inject(method="onHitEntity",at=@At("HEAD"),cancellable=true)
    private void slavicmyths$duplicate(EntityHitResult hit,CallbackInfo ci) {
        if(hit.getEntity() instanceof LivingEntity target&&CrossbowDamage.repeated((AbstractArrow)(Object)this,target))ci.cancel();
    }
    @Redirect(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean slavicmyths$volley(Entity entity,DamageSource source,float amount) {
        var arrow=(AbstractArrow)(Object)this;
        return entity.hurt(entity instanceof LivingEntity target?CrossbowDamage.source(arrow,target,source):source,amount);
    }
}
