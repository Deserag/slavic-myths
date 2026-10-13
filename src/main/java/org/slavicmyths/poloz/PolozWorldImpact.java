package org.slavicmyths.poloz;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.mor.MorBlocks;
/** One bounded mutation queue per root; no terrain restoration and no unloaded writes. */
public final class PolozWorldImpact {
 private final LinkedHashSet<BlockPos> breaking=new LinkedHashSet<>(),conversion=new LinkedHashSet<>();
 private void add(Set<BlockPos> queue,BlockPos p){if(queue.size()<2048)queue.add(p.immutable());}
 public void sample(GreatPoloz boss) {
  for(int i=-1;i<24;i++) {
   var entity=i<0?boss:boss.parts[i];AABB box=entity.getBoundingBox().inflate(.5,0,.5);
   for(BlockPos p:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY+.15,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))add(breaking,p);
   double r=boss.phase()==1?2.5:boss.phase()==2?3.5:4.5;
   for(int x=-(int)Math.ceil(r);x<=r;x++)for(int z=-(int)Math.ceil(r);z<=r;z++)if(x*x+z*z<=r*r)for(int y=-1;y<=0;y++)add(conversion,entity.blockPosition().offset(x,y,z));
  }
 }
 public void patch(BlockPos p){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)add(conversion,p.offset(x,0,z));}
 public static boolean breakable(ServerLevel w,BlockPos p) {
  if(!w.hasChunkAt(p)||!w.getWorldBorder().isWithinBounds(p))return false;
  var s=w.getBlockState(p);String id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
  return !s.isAir()&&s.getFluidState().isEmpty()&&s.getDestroySpeed(w,p)>=0&&!s.is(Blocks.OBSIDIAN)&&!id.contains("portal")&&!id.contains("command_block")&&!id.contains("structure")&&!id.contains("jigsaw")&&!id.contains("spawner")&&!id.equals("moving_piston");
 }
 public void tick(GreatPoloz boss) {
  var w=(ServerLevel)boss.level();if(!net.neoforged.neoforge.event.EventHooks.canEntityGrief(w,boss)){clear();return;}
  int budget=32,containers=0;var it=breaking.iterator();while(budget-->0&&it.hasNext()){var p=it.next();if(w.hasChunkAt(p)&&w.getBlockEntity(p)!=null&&containers>=1)continue;it.remove();if(!breakable(w,p))continue;
   if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent(boss,p,w.getBlockState(p))).isCanceled())continue;
   if(w.getBlockEntity(p)!=null&&!w.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS))continue;
   if(w.getBlockEntity(p)!=null)containers++;w.destroyBlock(p,w.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS),boss);
  }
  budget=48;it=conversion.iterator();while(budget-->0&&it.hasNext()){var p=it.next();it.remove();MorBlocks.convert(w,p,boss);}
 }
 public void clear(){breaking.clear();conversion.clear();}
 public int breakSize(){return breaking.size();}public int conversionSize(){return conversion.size();}
}
