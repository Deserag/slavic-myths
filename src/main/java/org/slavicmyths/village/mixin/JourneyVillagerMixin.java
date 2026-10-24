package org.slavicmyths.village.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observe accepted native trading interactions, never change their result. */
@Mixin(Villager.class)
public abstract class JourneyVillagerMixin {
    @Inject(method="mobInteract",at=@At("RETURN"))
    private void slavicmyths$neighbor(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> ci){
        var villager=(Villager)(Object)this;
        if(player instanceof ServerPlayer p&&ci.getReturnValue().consumesAction()&&!villager.isBaby()&&villager.getTradingPlayer()==p&&BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession()).getNamespace().equals("slavicmyths"))
            org.slavicmyths.progression.AdvancementJourney.award(p,"first_neighbors");
    }
}
