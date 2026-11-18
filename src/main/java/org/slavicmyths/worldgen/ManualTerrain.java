package org.slavicmyths.worldgen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
/** Shared existing land/camp ground policy; never mistakes canopy or snow for ground. */
public final class ManualTerrain {
 public static BlockPos ground(CommandSourceStack source){
  var p=BlockPos.containing(source.getPosition());var w=source.getLevel();
  if(w.dimension().equals(net.minecraft.world.level.Level.OVERWORLD))return new BlockPos(p.getX(),LandTerrain.ground(w,p.getX(),p.getZ())+1,p.getZ());
  return p;
 }
 public static java.util.Map<BlockPos,net.minecraft.world.level.block.state.BlockState> foundation(net.minecraft.server.level.ServerLevel world,BlockPos min,BlockPos max){
  var changes=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
  var fill=(world.dimension().equals(net.minecraft.world.level.Level.NETHER)?net.minecraft.world.level.block.Blocks.NETHERRACK:net.minecraft.world.level.block.Blocks.STONE).defaultBlockState();
  for(int x=min.getX();x<=max.getX();x++)for(int z=min.getZ();z<=max.getZ();z++){
   boolean supported=false;
   for(int depth=1;depth<=org.slavicmyths.posad.PosadTerrain.MAX_SUPPORT_DEPTH;depth++){
    var at=new BlockPos(x,min.getY()-depth,z);
    if(at.getY()<world.getMinBuildHeight())break;
    var old=world.getBlockState(at);
    if(old.hasBlockEntity()||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(at)||!ManualStructureJobs.natural(old)&&old.getFluidState().isEmpty())throw new IllegalArgumentException("Защищённое основание: "+at.toShortString());
    if(!old.isAir()&&old.getFluidState().isEmpty()&&!LandTerrain.vegetation(old)){supported=true;break;}
    changes.put(at,fill);
   }
   if(!supported)throw new IllegalArgumentException("Не найдено основание площадки: "+x+","+z);
  }
  return changes;
 }
 private ManualTerrain(){}
}
