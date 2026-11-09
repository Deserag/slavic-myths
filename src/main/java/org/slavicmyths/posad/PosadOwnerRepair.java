package org.slavicmyths.posad;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import org.slavicmyths.worldgen.ManualStructureRecords;
/** Explicit owner repair: only exact old generated cells, never player edits or containers. */
public final class PosadOwnerRepair {
 public static int repair(CommandSourceStack source){
  var w=source.getLevel();var here=BlockPos.containing(source.getPosition());
  var site=ManualStructureRecords.get(w).entries().stream().filter(e->e.family().equals("nether_posad")&&e.bounds().isInside(here)).findFirst();
  if(site.isEmpty()){source.sendFailure(Component.literal("Stand inside a recorded Posad to repair it."));return 0;}
  var e=site.get();var a=e.arrival();var old=PosadPiece.previousPlan(a,e.tier());var next=new PosadPiece(a,e.tier());int changed=0,preserved=0;
  var writes=new java.util.LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
  for(int x=0;x<113;x++)for(int z=0;z<113;z++)for(int y=0;y<18;y++){
   var before=old.voxel(x,y,z);var after=next.voxel(x,y,z);if(before==null)before=Blocks.AIR.defaultBlockState();if(after==null)after=Blocks.AIR.defaultBlockState();
   before=before.rotate(Rotation.values()[e.tier()]);after=after.rotate(Rotation.values()[e.tier()]);if(before.equals(after))continue;
   var cell=PosadGeometry.world(new PosadGeometry.Cell(x,z),a.getX(),a.getZ(),e.tier());var at=new BlockPos(cell.x(),a.getY()+y,cell.z());
   if(w.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null){source.sendFailure(Component.literal("Repair postponed: all affected chunks must already be loaded."));return 0;}
   var actual=w.getBlockState(at);if(actual.hasBlockEntity()||after.hasBlockEntity()||!actual.equals(before)){preserved++;continue;}writes.put(at,after);
  }
  for(var edit:writes.entrySet()){w.setBlock(edit.getKey(),edit.getValue(),2);changed++;}
  w.getServer().saveEverything(false,true,true);
  source.sendSuccess(()->Component.literal("Posad repair: "+writes.size()+" generated cells updated; player edits and containers preserved."),false);return changed;
 }
 private PosadOwnerRepair(){}
}
