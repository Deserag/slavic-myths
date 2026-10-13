package org.slavicmyths.equipment.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Includes physical destruction and block changes outside player events. */
@Mixin(LevelChunk.class)
public abstract class Quest185BlockChangeMixin {
 @Inject(method="setBlockState",at=@At("HEAD"),cancellable=true)
 private void quest185$immunity(BlockPos pos,BlockState state,boolean moving,CallbackInfoReturnable<BlockState> ci){var c=(LevelChunk)(Object)this;if(c.getLevel() instanceof ServerLevel w&&org.slavicmyths.mor.MorBlocks.index(state)>=0&&org.slavicmyths.quest185.CleansedLand.protectedAt(w,pos))ci.setReturnValue(null);}
 @Inject(method="setBlockState",at=@At("RETURN"))
 private void quest185$changed(BlockPos pos,BlockState state,boolean moving,CallbackInfoReturnable<BlockState> ci){var c=(LevelChunk)(Object)this;if(ci.getReturnValue()!=null&&c.getLevel() instanceof ServerLevel w){var old=ci.getReturnValue();boolean transition=org.slavicmyths.quest185.Quest185Origins.grassTransition(w,pos);boolean natural=old.is(org.slavicmyths.mor.MorBlocks.BLOCKS.get("grass_block").get())&&state.is(org.slavicmyths.mor.MorBlocks.BLOCKS.get("dirt").get());if(natural||transition){if(org.slavicmyths.mor.MorBlocks.index(state)>=0)org.slavicmyths.quest185.CleansingLessons.naturalChange(w,pos,state);}else{org.slavicmyths.quest185.CleansingLessons.changed(w,pos);org.slavicmyths.quest185.Quest185Origins.remove(w,pos);}org.slavicmyths.quest185.CleansedLand.remove(w,pos);if(!transition&&org.slavicmyths.mor.MorBlocks.index(state)>=0&&org.slavicmyths.mor.MorBlocks.index(old)<0)for(var base:org.slavicmyths.mor.MorBlocks.BASE)if(old.is(base)){org.slavicmyths.quest185.Quest185Origins.remember(w,pos,old,state);break;}}}
}
