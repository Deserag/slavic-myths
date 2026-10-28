package org.slavicmyths.trophy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Repairs only loaded legacy head sections; never loads chunks or searches every tick. */
@EventBusSubscriber(modid="slavicmyths")
public final class TrophyHeadMigration {
 @SubscribeEvent public static void loaded(ChunkEvent.Load event){
  if(!(event.getLevel() instanceof ServerLevel world)||!(event.getChunk() instanceof LevelChunk chunk))return;
  world.getServer().execute(()->{
   if(world.getChunkSource().getChunkNow(chunk.getPos().x,chunk.getPos().z)!=chunk)return;
   var head=TrophyHeads.BLOCKS.get(TrophyHeads.Kind.ZHARENITSA).get();
   var sections=chunk.getSections();
   for(int section=0;section<sections.length;section++){
    var cells=sections[section];if(cells.hasOnlyAir()||!cells.maybeHas(state->state.is(head)))continue;
    int bottom=world.getMinBuildHeight()+section*16;
    for(int y=0;y<16;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
     if(!cells.getBlockState(x,y,z).is(head))continue;
     BlockPos pos=new BlockPos(chunk.getPos().getMinBlockX()+x,bottom+y,chunk.getPos().getMinBlockZ()+z);
     var state=chunk.getBlockState(pos);
     if(state.getValue(TrophyHeadBlock.HALF)!=net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)continue;
     // getBlockEntities does not lazily create one: a missing legacy tile is explicit.
     if(chunk.getBlockEntities().containsKey(pos))continue;
     chunk.setBlockEntity(new TrophyHeadBlock.Tile(pos,state));chunk.setUnsaved(true);
     world.sendBlockUpdated(pos,state,state,3);
    }
   }
  });
 }
 private TrophyHeadMigration(){}
}
