package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DoorBlock;

/** Explicit administrator audit, bounded by a finite city and 100000 nodes; never loads chunks. */
public final class ChudNavigation {
    public record Result(String status,int visited,List<String> unreachable){}
    public static Result inspect(ServerLevel world,ChudPlan plan){
        var targets=new LinkedHashMap<String,BlockPos>();
        plan.points.forEach((role,pos)->targets.put(role.name(),plan.world(pos)));
        for(var r:plan.homes)targets.put(r.id(),plan.world(new BlockPos(r.x()+3+(r.variant()%3),r.y()+1,r.z()+r.depth()-2)));
        for(var r:plan.homes)if(r.variant()==2)targets.put(r.id()+"_upper",plan.world(new BlockPos(r.x()+3,r.y()+6,r.z()+4)));
        int number=0;for(var p:List.of(new BlockPos(-20,8,8),new BlockPos(19,8,8),new BlockPos(0,8,17),new BlockPos(-70,-3,21),new BlockPos(-109,-27,-106),new BlockPos(-109,-17,-101)))targets.put("gallery_"+(number++),plan.world(p));
        return inspect(world,plan.bounds(),plan.world(plan.points.get(ChudPlan.Role.ENTRANCE)),targets);
    }
    public static Result inspect(ServerLevel world,net.minecraft.world.level.levelgen.structure.BoundingBox bounds,BlockPos start,Map<String,BlockPos> targets){
        var missing=new HashSet<>(targets.keySet());var seen=new HashSet<Long>();var queue=new ArrayDeque<BlockPos>();boolean unloaded=false;
        if(walk(world,start)){queue.add(start);seen.add(start.asLong());}
        while(!queue.isEmpty()&&seen.size()<100000&&!missing.isEmpty()){
            var p=queue.removeFirst();missing.removeIf(name->targets.get(name).equals(p));
            for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST))for(int dy:new int[]{0,1,-1}){
                var q=p.relative(dir).offset(0,dy,0);if(!bounds.isInside(q)||seen.contains(q.asLong()))continue;
                if(!world.hasChunkAt(q)){unloaded=true;continue;}
                if(!walk(world,q)||dy==1&&!empty(world,p.above(2)))continue;
                seen.add(q.asLong());queue.add(q);
            }
        }
        return new Result(missing.isEmpty()?"PASS":seen.size()>=100000?"BUDGET_LIMIT":unloaded?"UNKNOWN_UNLOADED_CHUNKS":"UNREACHABLE",seen.size(),missing.stream().sorted().toList());
    }
    private static boolean empty(ServerLevel world,BlockPos p){if(!world.hasChunkAt(p))return false;var s=world.getBlockState(p);return s.getFluidState().isEmpty()&&(s.getBlock() instanceof DoorBlock||s.getCollisionShape(world,p).isEmpty());}
    private static boolean walk(ServerLevel world,BlockPos p){if(!empty(world,p)||!empty(world,p.above()))return false;var s=world.getBlockState(p.below());var shape=s.getCollisionShape(world,p.below());return s.getFluidState().isEmpty()&&!shape.isEmpty()&&shape.max(Direction.Axis.Y)<=1.01;}
    private ChudNavigation(){}
}
