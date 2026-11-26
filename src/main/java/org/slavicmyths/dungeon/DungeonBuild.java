package org.slavicmyths.dungeon;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
    private record Layer(DungeonPlan.Box box,int pass,boolean legacy){Layer(DungeonPlan.Box box,int pass){this(box,pass,false);}}
    private static boolean roomShell(DungeonPlan.Box box){return box.kind()!=DungeonPlan.Kind.SHAFT&&box.kind()!=DungeonPlan.Kind.STAIR_DESCENT&&box.kind()!=DungeonPlan.Kind.POOL;}
    private static List<Layer> layers(DungeonPlan plan,int wellY){
        List<Layer> layers=new ArrayList<>();
        for(var box:plan.boxes)if(roomShell(box))layers.add(new Layer(box,0));
        for(var box:plan.boxes)if(roomShell(box))layers.add(new Layer(box,1));
        for(var box:plan.boxes)if(roomShell(box)&&box.kind()!=DungeonPlan.Kind.CORRIDOR)layers.add(new Layer(box,2,plan.version==1));
        for(var box:plan.boxes)if(box.kind()==DungeonPlan.Kind.SHAFT)layers.add(new Layer(box,3));
        // Finish every basin liner before placing any water, including in superflat air.
        for(var box:plan.boxes)if(box.kind()==DungeonPlan.Kind.POOL)layers.add(new Layer(box,5));
        for(var box:plan.boxes)if(box.kind()==DungeonPlan.Kind.POOL)layers.add(new Layer(box,7));
        for(var box:plan.boxes)if(box.kind()==DungeonPlan.Kind.STAIR_DESCENT)layers.add(new Layer(box,6));
        var entry=plan.entrance();
        layers.add(new Layer(new DungeonPlan.Box(entry.x()-2,1,entry.z()-2,entry.x()+2,wellY-2,entry.z()+2,DungeonPlan.Kind.SHAFT,0),3));
        layers.add(new Layer(new DungeonPlan.Box(entry.x()-2,wellY-1,entry.z()-2,entry.x()+2,wellY+4,entry.z()+2,DungeonPlan.Kind.WELL,0),4));
        return layers;
    }
    static BoundingBox bounds(DungeonPlan plan,BlockPos origin,int wellY){
        List<Layer> layers=layers(plan,wellY);var b=layers.getFirst().box;
        BoundingBox result=new BoundingBox(origin.getX()+b.x0(),origin.getY()+b.y0(),origin.getZ()+b.z0(),origin.getX()+b.x1(),origin.getY()+b.y1(),origin.getZ()+b.z1());
        for(var layer:layers){b=layer.box;result.encapsulate(new BoundingBox(origin.getX()+b.x0(),origin.getY()+b.y0(),origin.getZ()+b.z0(),origin.getX()+b.x1(),origin.getY()+b.y1(),origin.getZ()+b.z1()));}
        return result;
    }
    static void render(WorldGenLevel world,BlockPos origin,DungeonPlan plan,int wellY,BoundingBox clip){
        for(var layer:layers(plan,wellY)){
            var b=layer.box;
            for(int y=Math.max(b.y0(),clip.minY()-origin.getY());y<=Math.min(b.y1(),clip.maxY()-origin.getY());y++)
                for(int z=Math.max(b.z0(),clip.minZ()-origin.getZ());z<=Math.min(b.z1(),clip.maxZ()-origin.getZ());z++)
                    for(int x=Math.max(b.x0(),clip.minX()-origin.getX());x<=Math.min(b.x1(),clip.maxX()-origin.getX());x++){
                        BlockState state=stateAt(layer,x,y,z);
                        if(state!=null)world.setBlock(origin.offset(x,y,z),state,Block.UPDATE_CLIENTS);
                    }
        }
    }
    private static final class Job{
        final ServerLevel world;final BlockPos origin;final DungeonPlan plan;final List<Layer> layers;final java.util.function.IntConsumer onComplete;
        int index,x,y,z;
        Job(ServerLevel world,BlockPos origin,DungeonPlan plan,int wellY,java.util.function.IntConsumer onComplete){
            this.world=world;this.origin=origin;this.plan=plan;this.layers=layers(plan,wellY);this.onComplete=onComplete;reset();
        }
        void reset(){if(index>=layers.size())return;var b=layers.get(index).box;x=b.x0();y=b.y0();z=b.z0();}
        boolean step(){
            if(index>=layers.size())return false;
            var l=layers.get(index);var b=l.box;
            BlockState state=stateAt(l,x,y,z);
            if(state!=null)world.setBlock(origin.offset(x,y,z),state,Block.UPDATE_CLIENTS);
            if(++x>b.x1()){x=b.x0();if(++z>b.z1()){z=b.z0();if(++y>b.y1()){index++;reset();}}}
            return true;
        }
    }
    private static BlockState stateAt(Layer l,int x,int y,int z){
        var b=l.box;
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
                if(l.legacy&&height==1&&b.kind()==DungeonPlan.Kind.FLOODED&&dx>=w/2-2&&dx<=w/2+2&&dz>=4&&dz<=d-4)state=Blocks.WATER.defaultBlockState();
                if(height==2&&((dx==3&&dz==3)||(dx==w-3&&dz==d-3)))state=Blocks.SOUL_LANTERN.defaultBlockState();
                if(height==1&&((dx==3&&dz==3)||(dx==w-3&&dz==d-3)))state=DESERT.trim;
            }
            if(l.pass==3){
                // Sealed shaft; open the lower landing, and seal the upper boss gate.
                boolean lowerDoor=z==b.z1()&&x>b.x0()&&x<b.x1()&&y<b.y0()+3;
                if(b.floor()>0&&y==b.y1())state=DESERT.floor;
                else if(border&&!lowerDoor)state=DESERT.wall;
                else if(z==b.z0()+1&&x==b.x0()+2)state=ladder();
                else state=Blocks.AIR.defaultBlockState();
            }

        if(l.pass==4){
            int dx=x-b.x0(),dz=z-b.z0(),h=y-b.y0();
            boolean edge=dx==0||dx==4||dz==0||dz==4;
            if(h==0)state=DESERT.floor;
            else if(h==1)state=edge?DESERT.trim:Blocks.WATER.defaultBlockState();
            else if(h==5)state=DESERT.floor;
            else if((dx==0||dx==4)&&(dz==0||dz==4))state=DESERT.column;
            else state=Blocks.AIR.defaultBlockState();
        }
        if(l.pass==5){
            boolean water=DungeonPlan.poolWater(b,x,z);
            boolean rim=!water&&(DungeonPlan.poolWater(b,x-1,z)||DungeonPlan.poolWater(b,x+1,z)||DungeonPlan.poolWater(b,x,z-1)||DungeonPlan.poolWater(b,x,z+1));
            if(water)state=y==b.y0()?DESERT.floor:Blocks.AIR.defaultBlockState();
            else if(rim)state=DESERT.column;
        }
        if(l.pass==7&&y>b.y0()&&DungeonPlan.poolWater(b,x,z)){
            state=Blocks.WATER.defaultBlockState();
        }
        if(l.pass==6){
            int u=DungeonPlan.stairU(b,x,z),v=DungeonPlan.stairV(b,x,z);
            int h=DungeonPlan.stairY(b,u);
            if(Math.abs(v)==2){if(y>=h-1&&y<=h+4)state=DESERT.wall;}
            else if(y==h-1)state=DESERT.floor;
            else if(y==h){
                if(u>=1&&u<=DungeonPlan.PITCH){
                    Direction facing=DungeonPlan.stairAlongX(b)?(DungeonPlan.stairSign(b)>0?Direction.EAST:Direction.WEST):(DungeonPlan.stairSign(b)>0?Direction.SOUTH:Direction.NORTH);
                    state=Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,facing);
                }else state=DESERT.floor;
            }else if(y==h+4)state=DESERT.trim;
            else if(y>h&&y<h+4)state=DungeonPlan.stairGate(b,x,y,z)?gateBars(b):Blocks.AIR.defaultBlockState();
        }
        return state;
    }
    private static BlockState gateBars(DungeonPlan.Box b){
        boolean alongX=DungeonPlan.stairAlongX(b);
        return Blocks.IRON_BARS.defaultBlockState().setValue(BlockStateProperties.NORTH,alongX).setValue(BlockStateProperties.SOUTH,alongX)
            .setValue(BlockStateProperties.EAST,!alongX).setValue(BlockStateProperties.WEST,!alongX);
    }
    static BlockState ladder(){return Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH);}
    public static boolean start(ServerLevel world,BlockPos origin,DungeonPlan plan,int surface,java.util.function.IntConsumer onComplete){
        if(JOBS.containsKey(world))return false;
        int wellY=Math.max(surface-origin.getY(),plan.top()+4);
        for(var layer:layers(plan,wellY)){var b=layer.box;
            if(!world.getWorldBorder().isWithinBounds(origin.offset(b.x0(),b.y0(),b.z0()))||
               !world.getWorldBorder().isWithinBounds(origin.offset(b.x1(),b.y1(),b.z1())))return false;
            if(origin.getY()+b.y0()<world.getMinBuildHeight()||origin.getY()+b.y1()>=world.getMaxBuildHeight())return false;
        }
        if(!DungeonRecords.get(world).reserve(origin,plan))return false;
        JOBS.put(world,new Job(world,origin,plan,wellY,onComplete));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        for(var it=JOBS.entrySet().iterator();it.hasNext();){
            var entry=it.next();if(entry.getKey().getServer()!=e.getServer())continue;
            Job job=entry.getValue();int n=0;while(n++<BUDGET&&job.step()){}
            if(job.index>=job.layers.size()){
                DungeonRecords records=DungeonRecords.get(job.world);
                records.complete(job.origin);
                it.remove();
                int spawned=records.spawnManualGuardians(job.world,job.origin);
                job.onComplete.accept(spawned);
            }
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){JOBS.entrySet().removeIf(x->x.getKey().getServer()==e.getServer());}
    private DungeonBuild(){}
}
