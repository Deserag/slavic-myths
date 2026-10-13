package org.slavicmyths.village.mixin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Skip native immediate reset only for guards; their bounded grace behavior owns demotion. */
@Mixin(value=ResetProfession.class,remap=false)
public abstract class GuardRoleMixin {
 @Inject(method="create",at=@At("RETURN"),cancellable=true)
 private static void grace(CallbackInfoReturnable<BehaviorControl<Villager>> ci){var nativeReset=ci.getReturnValue();ci.setReturnValue(BehaviorBuilder.create(p->p.point((level,v,time)->!org.slavicmyths.military.Military.guard(v)&&nativeReset.tryStart(level,v,time))));}
}
