package org.slavicmyths.village.mixin;

import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.slavicmyths.hunt.WorldBoss;
import org.slavicmyths.hunt.BossKind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tugarin shares a class with Likho: target only Tugarin without changing Likho. */
@Mixin(value=IronGolem.class,remap=false)
public abstract class BossGolemTargetMixin {
    @Inject(method="registerGoals",at=@At("TAIL"))
    private void slavicBossThreat(CallbackInfo callback){
        IronGolem golem=(IronGolem)(Object)this;
        golem.targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(golem,WorldBoss.class,5,false,false,
            entity->entity instanceof WorldBoss boss&&boss.kind()==BossKind.TUGARIN));
    }
}
