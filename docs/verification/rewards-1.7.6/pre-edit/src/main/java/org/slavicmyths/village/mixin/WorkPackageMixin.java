package org.slavicmyths.village.mixin;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.npc.*;
import org.slavicmyths.village.VillageWork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Append to native WORK package, including every refreshBrain/conversion. No schedule replacement. */
@Mixin(value=VillagerGoalPackages.class,remap=false)
public abstract class WorkPackageMixin {
    @Inject(method="getCorePackage",at=@At("RETURN"),cancellable=true)
    private static void addGuard(VillagerProfession profession,float speed,CallbackInfoReturnable<ImmutableList<Pair<Integer,? extends BehaviorControl<? super Villager>>>> ci){var list=ImmutableList.<Pair<Integer,? extends BehaviorControl<? super Villager>>>builder();list.add(Pair.of(0,GoToWantedItem.create((Villager v)->!org.slavicmyths.military.Military.guard(v)&&VillageWork.eligible((net.minecraft.server.level.ServerLevel)v.level(),v)&&v.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).map(item->org.slavicmyths.village.WorkPolicy.input(v,item.getItem())).orElse(false),.5F,true,4)));list.addAll(ci.getReturnValue());list.add(Pair.of(0,new org.slavicmyths.military.GuardBehavior()));ci.setReturnValue(list.build());}
    @Inject(method="getWorkPackage",at=@At("RETURN"),cancellable=true)
    private static void addWork(VillagerProfession profession,float speed,CallbackInfoReturnable<ImmutableList<Pair<Integer,? extends BehaviorControl<? super Villager>>>> ci){
        var list=ImmutableList.<Pair<Integer,? extends BehaviorControl<? super Villager>>>builder();
        // Priority 1 sets WALK_TARGET before native workstation strolls, only when a task can run.
        list.add(Pair.of(1,new VillageWork()));list.addAll(ci.getReturnValue());ci.setReturnValue(list.build());
    }
}
