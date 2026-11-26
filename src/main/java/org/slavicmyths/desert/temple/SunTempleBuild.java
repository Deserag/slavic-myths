package org.slavicmyths.desert.temple;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slavicmyths.desert.DesertBlocks;

@EventBusSubscriber(modid="slavicmyths")
public final class SunTempleBuild {
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static BlockState mod(String name){return DesertBlocks.ALL.get(name).get().defaultBlockState();}
    private static BlockState[] palette(){
        return new BlockState[]{Blocks.AIR.defaultBlockState(),mod("ancient_sandstone"),mod("sandstone_bricks"),mod("cracked_sandstone_bricks"),mod("dark_sandstone"),
            Blocks.SMOOTH_SANDSTONE.defaultBlockState(),Blocks.CHISELED_SANDSTONE.defaultBlockState(),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
            Blocks.RED_SANDSTONE.defaultBlockState(),Blocks.GOLD_BLOCK.defaultBlockState(),mod("carved_solar_sandstone"),Blocks.RED_WOOL.defaultBlockState(),
            Blocks.YELLOW_WOOL.defaultBlockState(),Blocks.GLOWSTONE.defaultBlockState(),mod("solar_seal"),Blocks.WATER.defaultBlockState(),Blocks.CAMPFIRE.defaultBlockState(),
            Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),
            Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.WEST),Blocks.RED_CARPET.defaultBlockState()};
    }
    static BoundingBox bounds(BlockPos o,int bottom,int top){return bounds(o,bottom,top,SunTemplePlan.VERSION);}
    static BoundingBox bounds(BlockPos o,int bottom,int top,int version){
        return new BoundingBox(o.getX(),bottom,o.getZ(),o.getX()+SunTemplePlan.width(version)-1,top,o.getZ()+SunTemplePlan.depth(version)-1);
    }
    static int anchor(int surface,int min,int max){return Math.max(min+4,Math.min(surface,max-SunTemplePlan.H));}
    /** Each column owns its terrain shaping, and writes only within the chunk clip. */
    private static void column(WorldGenLevel w,BlockPos o,int x,int z,BlockState[] p,int minY,int maxY,int version){
        int wx=o.getX()+x,wz=o.getZ()+z,base=o.getY();
        int surface=w.getHeight(w instanceof ServerLevel?Heightmap.Types.MOTION_BLOCKING_NO_LEAVES:Heightmap.Types.WORLD_SURFACE_WG,wx,wz);
        int ground=Math.max(w.getMinBuildHeight()+1,Math.min(base-1,surface-1));
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        // Fill voids and depressions below the platform, retaining bedrock.
        for(int y=ground;y>=w.getMinBuildHeight()+1;y--){
            pos.set(wx,y,wz);
            if(w.getBlockState(pos).isSolidRender(w,pos))break;
            if(y>=minY&&y<=maxY)w.setBlock(pos,p[SunTemplePlan.STONE],Block.UPDATE_CLIENTS);
        }
        for(int y=Math.max(ground+1,minY);y<base&&y<=maxY;y++)w.setBlock(pos.set(wx,y,wz),p[SunTemplePlan.STONE],Block.UPDATE_CLIENTS);
        if(base-1>=minY&&base-1<=maxY)w.setBlock(pos.set(wx,base-1,wz),p[SunTemplePlan.STONE],Block.UPDATE_CLIENTS);
        // Clear terrain, trees, liquids and obstructions inside the envelope. The
        // upper bound includes existing terrain even when the temple is shorter.
        int ceiling=Math.min(w.getMaxBuildHeight()-1,Math.max(base+SunTemplePlan.height(version)-1,surface));
        for(int y=Math.max(base,minY);y<=Math.min(ceiling,maxY);y++){
            int m=SunTemplePlan.at(version,x,y-base,z);
            w.setBlock(pos.set(wx,y,wz),p[m],Block.UPDATE_CLIENTS);
        }
    }
    static void render(WorldGenLevel w,BlockPos o,BoundingBox clip,int version){
        BlockState[] p=palette();
        for(int x=Math.max(0,clip.minX()-o.getX());x<=Math.min(SunTemplePlan.width(version)-1,clip.maxX()-o.getX());x++)
            for(int z=Math.max(0,clip.minZ()-o.getZ());z<=Math.min(SunTemplePlan.depth(version)-1,clip.maxZ()-o.getZ());z++)
                column(w,o,x,z,p,clip.minY(),clip.maxY(),version);
    }
    private static final class Job {
        final ServerLevel w;final BlockPos o;final BlockState[] p=palette();final java.util.function.Consumer<Boolean> done;
        int x,z;
        Job(ServerLevel w,BlockPos o,java.util.function.Consumer<Boolean> done){this.w=w;this.o=o;this.done=done;}
        boolean step(){
            column(w,o,x,z,p,w.getMinBuildHeight(),w.getMaxBuildHeight()-1,SunTemplePlan.VERSION);
            if(++z==SunTemplePlan.D){z=0;x++;}
            return x<SunTemplePlan.W;
        }
    }
    static boolean start(ServerLevel w,BlockPos o,java.util.function.Consumer<Boolean> done){
        if(JOBS.containsKey(w)||o.getY()<w.getMinBuildHeight()+1||o.getY()+SunTemplePlan.H>w.getMaxBuildHeight()
            ||!w.getWorldBorder().isWithinBounds(o)||!w.getWorldBorder().isWithinBounds(o.offset(SunTemplePlan.W-1,0,SunTemplePlan.D-1))
            ||!SunTempleRecords.get(w).reserve(o))return false;
        JOBS.put(w,new Job(w,o,done));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        for(var it=JOBS.entrySet().iterator();it.hasNext();){
            var e=it.next();if(e.getKey().getServer()!=event.getServer())continue;
            Job job=e.getValue();boolean more=true;
            // bounded columns per tick; at most 24*world-height block writes
            for(int i=0;i<24&&more;i++)more=job.step();
            if(!more){it.remove();var records=SunTempleRecords.get(job.w);records.complete(job.o);job.done.accept(records.spawn(job.w,job.o));}
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){JOBS.keySet().removeIf(w->w.getServer()==e.getServer());}
    private SunTempleBuild(){}
}
