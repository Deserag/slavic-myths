package org.slavicmyths.bogatyr;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Explicit staging courtyard, only in empty loaded space. No terrain clearing or worldgen. */
@EventBusSubscriber(modid="slavicmyths")
public final class BogatyrArena {
    private record Placement(BlockPos pos,BlockState state){}
    private record Job(ServerLevel world,UUID player,Deque<Placement> pending){}
    private static final Map<ServerLevel,Job> jobs=new WeakHashMap<>();
    public static boolean create(ServerPlayer p){var w=p.serverLevel();if(jobs.containsKey(w)||BogatyrLedger.active(p))return false;var center=BlockPos.containing(p.position().add(p.getLookAngle().multiply(36,0,36)));var planned=new LinkedHashMap<BlockPos,BlockState>();
        for(int x=-30;x<=30;x++)for(int z=-30;z<=30;z++){double r=Math.hypot(x,z);if(r>30)continue;var at=center.offset(x,0,z);if(!w.hasChunkAt(at)||!w.getWorldBorder().isWithinBounds(at)){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.arena_blocked"));return false;}
            for(int y=0;y<=13;y++)if(!w.getBlockState(at.above(y)).isAir()||!w.getFluidState(at.above(y)).isEmpty()){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.arena_blocked"));return false;}
            planned.put(at,(Math.floorMod(x*3+z,9)==0?Blocks.ANDESITE:Blocks.STONE_BRICKS).defaultBlockState());
            if(r>=28&&!(Math.abs(x)<=3||Math.abs(z)<=3))for(int y=1;y<=3;y++)planned.put(at.above(y),(y==3?Blocks.ANDESITE:Blocks.STONE_BRICKS).defaultBlockState());
        }
        for(int i=0;i<8;i++){double a=(i+.5)*Math.PI/4;var at=center.offset((int)Math.round(Math.cos(a)*28),(int)0,(int)Math.round(Math.sin(a)*28));for(int y=4;y<=5;y++)planned.put(at.above(y),Blocks.OAK_LOG.defaultBlockState());planned.put(at.above(6),Blocks.IRON_BARS.defaultBlockState());}
        var queue=new ArrayDeque<Placement>();for(var e:planned.entrySet())queue.add(new Placement(e.getKey(),e.getValue()));jobs.put(w,new Job(w,p.getUUID(),queue));p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.arena_building",center.getX(),center.getY()+1,center.getZ()));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){for(var entry:new ArrayList<>(jobs.entrySet())){var job=entry.getValue();if(job.world.getServer()!=e.getServer())continue;int budget=256;boolean blocked=false;while(budget-->0&&!job.pending.isEmpty()){var a=job.pending.removeFirst();if(!job.world.hasChunkAt(a.pos)||!job.world.getBlockState(a.pos).isAir()||!job.world.getFluidState(a.pos).isEmpty()){blocked=true;break;}job.world.setBlock(a.pos,a.state,3);}if(blocked||job.pending.isEmpty()){jobs.remove(entry.getKey());var p=e.getServer().getPlayerList().getPlayer(job.player);if(p!=null)p.sendSystemMessage(Component.translatable(blocked?"gui.slavicmyths.171.arena_blocked":"gui.slavicmyths.171.arena_ready"));}}}
    private BogatyrArena(){}
}
