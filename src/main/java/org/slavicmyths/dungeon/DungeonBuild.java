package org.slavicmyths.dungeon;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Bounded server-thread placement. Shells first, interior carve second, fixtures last. */
@EventBusSubscriber(modid="slavicmyths")
public final class DungeonBuild {
    private record Palette(BlockState wall,BlockState floor,BlockState trim,BlockState column) {}
    private static final Palette DESERT=new Palette(Blocks.SANDSTONE.defaultBlockState(),Blocks.SMOOTH_SANDSTONE.defaultBlockState(),Blocks.CHISELED_SANDSTONE.defaultBlockState(),Blocks.CUT_SANDSTONE.defaultBlockState());
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static final int BUDGET=4096;
    private record Layer(DungeonPlan.Box box,int pass){}
    private static final class Job{
        final ServerLevel world;final BlockPos origin;final DungeonPlan plan;final List<Layer> layers=new ArrayList<>();
        int index,x,y,z;
        Job(ServerLevel world,BlockPos origin,DungeonPlan plan){
            this.world=world;this.origin=origin;this.plan=plan;
            for(var box:plan.boxes)if(box.kind()!=DungeonPlan.Kind.SHAFT)layers.add(new Layer(box,0));
            for(var box:plan.boxes)if(box.kind()!=DungeonPlan.Kind.SHAFT)layers.add(new Layer(box,1));
            for(var box:plan.boxes)if(box.kind()!=DungeonPlan.Kind.SHAFT&&box.kind()!=DungeonPlan.Kind.CORRIDOR)layers.add(new Layer(box,2));
            for(var box:plan.boxes)if(box.kind()==DungeonPlan.Kind.SHAFT)layers.add(new Layer(box,3));
            reset();
        }
        void reset(){if(index>=layers.size())return;var b=layers.get(index).box;x=b.x0();y=b.y0();z=b.z0();}
        boolean step(){
            if(index>=layers.size())return false;
            var l=layers.get(index);var b=l.box;
            boolean border=x==b.x0()||x==b.x1()||z==b.z0()||z==b.z1();
            boolean interior=!border&&y>b.y0()&&y<b.y1();
            BlockState state=null;
            if(l.pass==0){if(y==b.y0())state=DESERT.floor;else if(y==b.y1()||border)state=(y-b.y0())%5==0?DESERT.trim:DESERT.wall;}
            if(l.pass==1&&interior)state=Blocks.AIR.defaultBlockState();
            if(l.pass==2&&interior){
                int dx=x-b.x0(),dz=z-b.z0(),height=y-b.y0();
                int w=b.x1()-b.x0(),d=b.z1()-b.z0();
                boolean pillar=(dx==4||dx==w-4)&&(dz==4||dz==d-4);
                if(pillar&&height<=b.y1()-b.y0()-1&&b.kind()!=DungeonPlan.Kind.SECRET)state=height==1||y==b.y1()-1?DESERT.trim:DESERT.column;
                if(height==1&&b.kind()==DungeonPlan.Kind.FLOODED&&dx>=w/2-2&&dx<=w/2+2&&dz>=4&&dz<=d-4)state=Blocks.WATER.defaultBlockState();
                if(height==2&&((dx==3&&dz==3)||(dx==w-3&&dz==d-3)))state=Blocks.SOUL_LANTERN.defaultBlockState();
                if(height==1&&((dx==3&&dz==3)||(dx==w-3&&dz==d-3)))state=DESERT.trim;
            }
            if(l.pass==3){
                // Three-wide shaft, stone-lined on the north side; ladder faces south.
                if(z==b.z0())state=DESERT.wall;
                else if(z==b.z0()+1&&x==b.x0()+1)state=Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH);
                else state=Blocks.AIR.defaultBlockState();
            }
            if(state!=null)world.setBlock(origin.offset(x,y,z),state,Block.UPDATE_CLIENTS);
            if(++x>b.x1()){x=b.x0();if(++z>b.z1()){z=b.z0();if(++y>b.y1()){index++;reset();}}}
            return true;
        }
    }
    public static boolean start(ServerLevel world,BlockPos origin,DungeonPlan plan,int surface){
        if(JOBS.containsKey(world))return false;
        for(var b:plan.boxes){
            if(!world.getWorldBorder().isWithinBounds(origin.offset(b.x0(),b.y0(),b.z0()))||
               !world.getWorldBorder().isWithinBounds(origin.offset(b.x1(),b.y1(),b.z1())))return false;
            if(origin.getY()+b.y0()<world.getMinBuildHeight()||origin.getY()+b.y1()>=world.getMaxBuildHeight())return false;
        }
        if(!DungeonRecords.get(world).reserve(origin,plan))return false;
        JOBS.put(world,new Job(world,origin,plan));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        for(var it=JOBS.entrySet().iterator();it.hasNext();){
            var entry=it.next();if(entry.getKey().getServer()!=e.getServer())continue;
            Job job=entry.getValue();int n=0;while(n++<BUDGET&&job.step()){}
            if(job.index>=job.layers.size()){
                DungeonRecords.get(job.world).complete(job.origin);
                it.remove();
            }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){JOBS.entrySet().removeIf(x->x.getKey().getServer()==e.getServer());}
    private DungeonBuild(){}
}
