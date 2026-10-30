package org.slavicmyths.posad;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Explicit OP terrain adaptation; two bounded passes, shared native shell geometry. */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadCommands {
 private static final java.util.Map<ServerLevel,Job> JOBS=new java.util.IdentityHashMap<>();
 private static final class Job {final CommandSourceStack source;final BlockPos anchor;int column;boolean building;Job(CommandSourceStack s){source=s;anchor=BlockPos.containing(s.getPosition()).offset(-56,-1,12);}}
 public static void register(org.slavicmyths.commands.CommandTree tree){
  var command=Commands.literal("nether_posad").executes(c->start(c.getSource())).then(Commands.literal("shell").executes(c->start(c.getSource())));
  tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(Commands.literal("generate").then(command))));
 }
 public static int start(CommandSourceStack source){var w=source.getLevel();if(JOBS.containsKey(w)){source.sendFailure(Component.literal("Posad generation already active in this dimension."));return 0;}
  var job=new Job(source);if(job.anchor.getY()-2<w.getMinBuildHeight()||job.anchor.getY()+18>=w.getMaxBuildHeight()){source.sendFailure(Component.literal("Not enough vertical space."));return 0;}
  JOBS.put(w,job);source.sendSuccess(()->Component.literal("Posad: checking 113x113 terrain, then adapting foundation and clearing natural terrain. Shell only; district buildings are not connected yet."),false);return 1;
 }
 private static boolean natural(BlockState s){return s.isAir()||s.is(Blocks.LAVA)||s.is(Blocks.WATER)||org.slavicmyths.netherstructures.NetherBridgeTerrain.natural(s)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.DIRT)||s.is(Blocks.STONE)||s.is(Blocks.DEEPSLATE)||s.is(Blocks.SAND)||s.is(Blocks.GRAVEL);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){
  for(var entry:java.util.List.copyOf(JOBS.entrySet())){var w=entry.getKey();if(w.getServer()!=event.getServer())continue;var j=entry.getValue();
   for(int budget=0;budget<32&&JOBS.containsKey(w);budget++){
    int x=j.column%113,z=j.column/113;var base=j.anchor.offset(x,0,z);
    if(!w.hasChunkAt(base)){w.getChunk(base.getX()>>4,base.getZ()>>4);break;}
    if(!j.building){
     for(int y=-2;y<18;y++){var at=base.above(y);var s=w.getBlockState(at);if(!w.getWorldBorder().isWithinBounds(at)||s.hasBlockEntity()||!natural(s)){JOBS.remove(w);j.source.sendFailure(Component.literal("Posad rejected protected/non-natural terrain at "+at+"; no city blocks placed."));break;}}
     if(!JOBS.containsKey(w))break;
    }else{
     for(int y=-2;y<18;y++){var at=base.above(y);var old=w.getBlockState(at);if(!natural(old)||old.hasBlockEntity())continue;var state=PosadShell.voxel(x,y,z);if(state==null)state=Blocks.AIR.defaultBlockState();w.setBlock(at,state,2);}
     if(x%8==0&&z%8==0)for(int depth=3;depth<=48&&base.getY()-depth>=w.getMinBuildHeight();depth++){var at=base.below(depth);var old=w.getBlockState(at);if(!old.isAir()&&old.getFluidState().isEmpty())break;w.setBlock(at,Blocks.POLISHED_BASALT.defaultBlockState(),2);}
    }
    if(++j.column==113*113){if(!j.building){j.building=true;j.column=0;}else{JOBS.remove(w);j.source.sendSuccess(()->Component.literal("Posad shell generated at "+j.anchor+" (113x113). Buildings not yet integrated."),false);}}
   }
  }
 }
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent event){JOBS.keySet().removeIf(w->w.getServer()==event.getServer());}
 private PosadCommands(){}
}
