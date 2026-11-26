package org.slavicmyths.desert.oasis;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slavicmyths.desert.DesertBlocks;
import static org.slavicmyths.desert.oasis.LostOasisPlan.*;
@EventBusSubscriber(modid="slavicmyths")
public final class LostOasisBuild {
    private static final Map<ServerLevel,Job> JOBS=new IdentityHashMap<>();
    private static BlockState mod(String n){return DesertBlocks.ALL.get(n).get().defaultBlockState();}
    private static BlockState stair(Direction d){return Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,d);}
    private static BlockState leaves(Block b){return b.defaultBlockState().setValue(BlockStateProperties.PERSISTENT,true);}
    static BlockState[] palette(){
        return new BlockState[]{Blocks.AIR.defaultBlockState(),Blocks.SANDSTONE.defaultBlockState(),mod("ancient_sandstone"),Blocks.SMOOTH_SANDSTONE.defaultBlockState(),mod("cracked_sandstone_bricks"),mod("dark_sandstone"),mod("sandstone_bricks"),Blocks.GOLD_BLOCK.defaultBlockState(),Blocks.RED_TERRACOTTA.defaultBlockState(),Blocks.PURPLE_TERRACOTTA.defaultBlockState(),Blocks.PRISMARINE.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),leaves(Blocks.JUNGLE_LEAVES),leaves(Blocks.AZALEA_LEAVES),Blocks.JUNGLE_LOG.defaultBlockState(),Blocks.JUNGLE_PLANKS.defaultBlockState(),Blocks.CAMPFIRE.defaultBlockState(),Blocks.AIR.defaultBlockState(),Blocks.LANTERN.defaultBlockState(),Blocks.POLISHED_BLACKSTONE.defaultBlockState(),Blocks.RED_WOOL.defaultBlockState(),Blocks.BOOKSHELF.defaultBlockState(),Blocks.WHITE_WOOL.defaultBlockState(),stair(Direction.SOUTH),stair(Direction.NORTH),stair(Direction.EAST),stair(Direction.WEST),Blocks.CHEST.defaultBlockState(),Blocks.RED_BED.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH).setValue(BlockStateProperties.BED_PART,BedPart.FOOT),Blocks.RED_BED.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH).setValue(BlockStateProperties.BED_PART,BedPart.HEAD)};
    }
    static BoundingBox bounds(BlockPos o,int min,int max){return new BoundingBox(o.getX(),min,o.getZ(),o.getX()+W-1,max,o.getZ()+D-1);}
    static int anchor(int surface,int min,int max){return Math.max(min+2,Math.min(surface-FLOOR-1,max-H));}
    private static void column(WorldGenLevel w,BlockPos o,int x,int z,BlockState[] p,int minY,int maxY){
        if(!INSTANCE.active(x,z))return;
        int wx=o.getX()+x,wz=o.getZ()+z,base=o.getY();
        int surface=w.getHeight(w instanceof ServerLevel?Heightmap.Types.MOTION_BLOCKING_NO_LEAVES:Heightmap.Types.WORLD_SURFACE_WG,wx,wz);
        int ground=Math.max(w.getMinBuildHeight()+1,Math.min(base-1,surface-1));
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        for(int y=ground;y>=w.getMinBuildHeight()+1;y--){
            pos.set(wx,y,wz);if(w.getBlockState(pos).isSolidRender(w,pos))break;
            if(y>=minY&&y<=maxY)w.setBlock(pos,p[STONE],Block.UPDATE_CLIENTS);
        }
        for(int y=Math.max(ground+1,minY);y<base&&y<=maxY;y++)w.setBlock(pos.set(wx,y,wz),p[STONE],Block.UPDATE_CLIENTS);
        if(base-1>=minY&&base-1<=maxY)w.setBlock(pos.set(wx,base-1,wz),p[STONE],Block.UPDATE_CLIENTS);
        int ceiling=Math.min(w.getMaxBuildHeight()-1,Math.max(base+H-1,surface));
        for(int y=Math.max(base,minY);y<=Math.min(ceiling,maxY);y++){
            int m=INSTANCE.at(x,y-base,z);
            w.setBlock(pos.set(wx,y,wz),p[m],Block.UPDATE_CLIENTS);
        }
    }
    static void render(WorldGenLevel w,BlockPos o,BoundingBox clip){
        var p=palette();
        for(int x=Math.max(0,clip.minX()-o.getX());x<=Math.min(W-1,clip.maxX()-o.getX());x++)
            for(int z=Math.max(0,clip.minZ()-o.getZ());z<=Math.min(D-1,clip.maxZ()-o.getZ());z++)
                column(w,o,x,z,p,clip.minY(),clip.maxY());
    }
    private static final class Job {
        final ServerLevel w;final BlockPos o;final BlockState[] p=palette();final java.util.function.Consumer<Boolean> done;
        int x,z;
        Job(ServerLevel w,BlockPos o,java.util.function.Consumer<Boolean> done){this.w=w;this.o=o;this.done=done;}
        boolean step(){
            column(w,o,x,z,p,w.getMinBuildHeight(),w.getMaxBuildHeight()-1);
            if(++z==D){z=0;x++;}
            return x<W;
        }
    }
    static boolean start(ServerLevel w,BlockPos o,java.util.function.Consumer<Boolean> done){
        if(JOBS.containsKey(w)||o.getY()<w.getMinBuildHeight()+1||o.getY()+H>w.getMaxBuildHeight()
            ||!w.getWorldBorder().isWithinBounds(o)||!w.getWorldBorder().isWithinBounds(o.offset(W-1,0,D-1))
            ||org.slavicmyths.desert.temple.SunTempleRecords.get(w).overlaps(o,W,D)
            ||!LostOasisRecords.get(w).reserveManual(o))return false;
        JOBS.put(w,new Job(w,o,done));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        for(var it=JOBS.entrySet().iterator();it.hasNext();){
            var entry=it.next();if(entry.getKey().getServer()!=e.getServer())continue;
            Job j=entry.getValue();boolean more=true;
            for(int i=0;i<24&&more;i++)more=j.step();
            if(!more){it.remove();var r=LostOasisRecords.get(j.w);r.complete(j.o);j.done.accept(r.spawn(j.w,j.o));}
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){JOBS.keySet().removeIf(w->w.getServer()==e.getServer());}
    private LostOasisBuild(){}
}
