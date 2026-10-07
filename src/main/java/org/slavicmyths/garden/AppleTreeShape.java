package org.slavicmyths.garden;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** One canonical shape, shared by worldgen, saplings and anchor regrowth. No world shape search. */
public final class AppleTreeShape {
    public static final Map<BlockPos,Direction.Axis> LOGS;
    public static final List<BlockPos> CANOPY;
    public static final Set<BlockPos> FRUITABLE;
    static {
        var logs=new LinkedHashMap<BlockPos,Direction.Axis>();for(int y=0;y<=4;y++)logs.put(new BlockPos(0,y,0),Direction.Axis.Y);
        logs.put(new BlockPos(1,3,0),Direction.Axis.X);logs.put(new BlockPos(-1,3,0),Direction.Axis.X);
        logs.put(new BlockPos(0,3,1),Direction.Axis.Z);logs.put(new BlockPos(0,3,-1),Direction.Axis.Z);LOGS=Collections.unmodifiableMap(logs);
        var mask=new ArrayList<BlockPos>();
        for(int y=2;y<=6;y++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {
            boolean allowed=y==2?Math.abs(x)+Math.abs(z)<=1:y==6?(Math.abs(x)<=1&&Math.abs(z)<=1)||(Math.abs(x)+Math.abs(z)==2&&(x==0||z==0)):!(Math.abs(x)==2&&Math.abs(z)==2);
            var p=new BlockPos(x,y,z);if(allowed&&!logs.containsKey(p))mask.add(p);
        }
        CANOPY=List.copyOf(mask);
        var exterior=new ArrayList<>(mask);exterior.removeIf(p->Math.abs(p.getX())+Math.abs(p.getZ())<2);
        exterior.sort(Comparator.comparingInt(p->Math.floorMod(p.getX()*73+p.getY()*41+p.getZ()*29,211)));
        FRUITABLE=Set.copyOf(exterior.subList(0,Math.round(mask.size()*.35F)));
    }
    public static BlockState leaf(BlockPos offset,int stage) {
        int distance=LOGS.keySet().stream().mapToInt(p->Math.abs(p.getX()-offset.getX())+Math.abs(p.getY()-offset.getY())+Math.abs(p.getZ()-offset.getZ())).min().orElse(7);
        boolean fruit=FRUITABLE.contains(offset);
        return Gardens.block("apple_leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,Math.min(7,distance)).setValue(LeavesBlock.PERSISTENT,false).setValue(AppleLeaves.FRUITABLE,fruit).setValue(AppleLeaves.FRUIT_STAGE,fruit?stage:0);
    }
    public static boolean grow(LevelAccessor level,BlockPos base,RandomSource random,boolean natural) {
        for(var off:LOGS.keySet())if(!free(level,base.offset(off)))return false;
        for(var off:CANOPY)if(!free(level,base.offset(off)))return false;
        if(!level.getBlockState(base.below()).is(PerennialBush.SOIL))return false;
        for(var entry:LOGS.entrySet())level.setBlock(base.offset(entry.getKey()),Gardens.block("apple_log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,entry.getValue()).setValue(AppleLog.CROWN_ANCHOR,entry.getKey().equals(new BlockPos(0,4,0))),3);
        for(var off:CANOPY) {
            int roll=random.nextInt(100);int stage=natural?(roll<8?3:roll<23?1:0):0;
            level.setBlock(base.offset(off),leaf(off,stage),3);
        }
        return true;
    }
    private static boolean free(LevelAccessor level,BlockPos pos) {
        if(!level.hasChunkAt(pos)||pos.getY()<level.getMinBuildHeight()||pos.getY()>=level.getMaxBuildHeight())return false;
        var s=level.getBlockState(pos);return s.isAir()||s.is(Gardens.block("apple_sapling"));
    }
    private AppleTreeShape(){}
}
