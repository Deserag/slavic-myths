package org.slavicmyths.rpg.tools.mixin;

import java.util.ArrayDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.rpg.tools.ToolRuneRuntime;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Return value alone is insufficient: destroyBlock may return true when removeBlock returned false. */
@Mixin(ServerPlayerGameMode.class)
public abstract class ToolRuneBreakMixin {
 @Shadow @Final protected ServerPlayer player;
 @Unique private final ArrayDeque<ToolRuneRuntime.BreakFrame> slavicmyths$frames=new ArrayDeque<>();
 @Inject(method="destroyBlock",at=@At("HEAD")) private void slavicmyths$begin(BlockPos pos,CallbackInfoReturnable<Boolean> ci){slavicmyths$frames.push(ToolRuneRuntime.beginBreak(player,pos));}
 @Inject(method="removeBlock",at=@At("RETURN")) private void slavicmyths$removed(BlockPos pos,BlockState state,boolean harvest,CallbackInfoReturnable<Boolean> ci){var frame=slavicmyths$frames.peek();if(frame!=null&&frame.origin.equals(pos))frame.removed=ci.getReturnValue();org.slavicmyths.progression.AdvancementJourney.harvested(player,pos,state,ci.getReturnValue());}
 @Inject(method="destroyBlock",at=@At("RETURN")) private void slavicmyths$end(BlockPos pos,CallbackInfoReturnable<Boolean> ci){ToolRuneRuntime.finishBreak(player,slavicmyths$frames.pop(),ci.getReturnValue());}
}
