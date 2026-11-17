package org.slavicmyths.buyan;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Dedicated, opt-in superflat ocean preset: build once at spawn without touching ordinary worlds. */
@EventBusSubscriber(modid="slavicmyths")
public final class BuyanIsland {
    private static final int RADIUS=45;
    private static final String SAVE_KEY="slavicmyths_buyan_ocean_generated_v1";
    private static final class WorldRecord extends SavedData {
        boolean generated;
        static WorldRecord load(CompoundTag tag,HolderLookup.Provider provider){
            WorldRecord record=new WorldRecord();
            record.generated=tag.getBoolean("Generated");
            return record;
        }
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){
            tag.putBoolean("Generated",generated);
            return tag;
        }
        void completed(){generated=true;setDirty();}
        static WorldRecord get(ServerLevel level){
            return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldRecord::new,WorldRecord::load),SAVE_KEY);
        }
    }

    /** Exact distinctive layer signature; NOT every flat world and NOT all ocean worlds. */
    private static boolean dedicated(ServerLevel world){
        if(!world.dimension().equals(Level.OVERWORLD))return false;
        if(!(world.getChunkSource().getGenerator() instanceof FlatLevelSource flat))return false;
        var layers=flat.settings().getLayersInfo();
        return layers.size()==4
            && layer(layers.get(0),Blocks.BEDROCK,1)
            && layer(layers.get(1),Blocks.STONE,16)
            && layer(layers.get(2),Blocks.SAND,2)
            && layer(layers.get(3),Blocks.WATER,12);
    }
    private static boolean layer(net.minecraft.world.level.levelgen.flat.FlatLayerInfo info,
                                 net.minecraft.world.level.block.Block block,int height){
        return info.getHeight()==height&&info.getBlockState().is(block);
    }

    @SubscribeEvent
    public static void firstVisit(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player) || player.tickCount%20!=1)return;
        ServerLevel level=player.serverLevel();
        if(!dedicated(level))return;
        WorldRecord state=WorldRecord.get(level);
        if(state.generated)return;
        int count=build(level);
        if(count<=0){
            player.sendSystemMessage(Component.literal("Buyan: automatic build refused; check spawn site or world height."));
            return;
        }
        state.completed();
        int sea=sea(level);
        level.setDefaultSpawnPos(new BlockPos(0,sea+4,8),0.0F);
        player.teleportTo(level,0.5,sea+5,8.5,player.getYRot(),player.getXRot());
        player.sendSystemMessage(Component.literal("Buyan test island created: house, market, warehouse, two piers and connected paths."));
    }

    /** For manual verification in the matching custom water world. Safe to rerun after auto creation. */
    public static int generate(CommandSourceStack source){
        ServerLevel level=source.getLevel();
        if(!dedicated(level)){
            source.sendFailure(Component.literal("Buyan island: create a new world with the 'Buyan - Ocean Test' world type."));
            return 0;
        }
        WorldRecord state=WorldRecord.get(level);
        if(state.generated){
            source.sendSuccess(()->Component.literal("Buyan island already exists at X=0 Z=0; no changes made."),false);
            return 1;
        }
        int count=build(level);
        if(count<=0){
            source.sendFailure(Component.literal("Buyan: insufficient space, height or protected blocks; no terrain was modified."));
            return 0;
        }
        state.completed();
        level.setDefaultSpawnPos(new BlockPos(0,sea(level)+4,8),0.0F);
        source.sendSuccess(()->Component.literal("Buyan island built at 0,0: "+count+" block edits."),true);
        return 1;
    }

    private static int sea(ServerLevel level){return level.getMinBuildHeight()+30;}
    private static final class Blueprint {
        final Map<BlockPos,BlockState> cells=new LinkedHashMap<>();
        void put(int x,int y,int z,BlockState block){cells.put(new BlockPos(x,y,z),block);}
        void importAt(BuyanPort.Plan plan,int x,int y,int z){
            for(var entry:plan.cells.entrySet())
                cells.put(entry.getKey().offset(x,y,z),entry.getValue());
        }
        void road(int x1,int z1,int x2,int z2,int top){
            int dx=x2-x1,dz=z2-z1,steps=Math.max(Math.abs(dx),Math.abs(dz));
            for(int i=0;i<=steps;i++){
                int x=x1+Math.round(dx*(float)i/steps),z=z1+Math.round(dz*(float)i/steps);
                for(int side=-1;side<=1;side++){
                    int xx=x+(Math.abs(dx)<Math.abs(dz)?side:0);
                    int zz=z+(Math.abs(dx)>=Math.abs(dz)?side:0);
                    if(Math.hypot(xx,zz)<42)
                        put(xx,top,zz,Blocks.COARSE_DIRT.defaultBlockState());
                }
            }
        }
    }

    private static Blueprint plan(int sea){
        Blueprint p=new Blueprint();
        for(int x=-RADIUS;x<=RADIUS;x++)for(int z=-RADIUS;z<=RADIUS;z++){
            double radius=Math.hypot(x,z);
            double edge=43+1.4*Math.sin(x*.21)+1.3*Math.cos(z*.16);
            if(radius>edge)continue;
            int top=radius<edge-6?sea+2:sea+1;
            boolean sand=radius>edge-7;
            for(int y=sea-11;y<=top;y++){
                BlockState state=y==top
                    ? (sand?Blocks.SAND:Blocks.GRASS_BLOCK).defaultBlockState()
                    : (y>=top-3?Blocks.DIRT:Blocks.STONE).defaultBlockState();
                p.put(x,y,z,state);
            }
        }
        // Level the occupied footprint only: solid foundations, not floating buildings.
        plot(p,-22,7,12,10,sea);
        plot(p,-25,-20,10,8,sea);
        plot(p,7,-21,16,11,sea);
        p.importAt(BuyanPort.building("house"),-22,sea+3,7);
        p.importAt(BuyanPort.building("market"),-25,sea+3,-20);
        p.importAt(BuyanPort.building("warehouse"),7,sea+3,-21);
        p.importAt(BuyanPort.pier(true),11,sea+3,-35);
        p.importAt(BuyanPort.pier(false),-22,sea+3,-32);
        // Connect house, market, warehouse, and their respective quays.
        p.road(-16,5,-17,-10,sea+2);
        p.road(-17,-10,-21,-21,sea+2);
        p.road(-20,-22,-21,-31,sea+2);
        p.road(-17,-10,-2,-8,sea+2);
        p.road(-2,-8,14,-22,sea+2);
        p.road(14,-23,13,-34,sea+2);
        // Spawn on the center plateau above any water.
        for(int x=-2;x<=2;x++)for(int z=5;z<=9;z++){
            p.put(x,sea+2,z,Blocks.GRASS_BLOCK.defaultBlockState());
            p.put(x,sea+3,z,Blocks.AIR.defaultBlockState());
            p.put(x,sea+4,z,Blocks.AIR.defaultBlockState());
        }
        return p;
    }
    private static void plot(Blueprint p,int x,int z,int w,int d,int sea){
        for(int xx=x-1;xx<x+w+1;xx++)for(int zz=z-2;zz<z+d+1;zz++){
            for(int y=sea-1;y<=sea+1;y++)p.put(xx,y,zz,Blocks.DIRT.defaultBlockState());
            p.put(xx,sea+2,zz,Blocks.GRASS_BLOCK.defaultBlockState());
        }
    }

    private static int build(ServerLevel level){
        int sea=sea(level);
        if(sea-18<level.getMinBuildHeight()||sea+20>=level.getMaxBuildHeight())return 0;
        Blueprint p=plan(sea);
        // Validate world bounds and *every* existing block entity before any edits.
        Set<Long> chunks=new HashSet<>();
        for(var entry:p.cells.entrySet()){
            BlockPos pos=entry.getKey();
            if(!level.getWorldBorder().isWithinBounds(pos)
                ||pos.getY()<level.getMinBuildHeight()+1
                ||pos.getY()>=level.getMaxBuildHeight()-1)return 0;
            long chunk=net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4);
            if(chunks.add(chunk))level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        }
        for(BlockPos pos:p.cells.keySet())
            if(level.getBlockState(pos).hasBlockEntity()||level.getBlockState(pos).is(Blocks.BEDROCK))
                return 0;
        for(var entry:p.cells.entrySet())
            level.setBlock(entry.getKey(),entry.getValue(),2);
        return p.cells.size();
    }
    private BuyanIsland(){}
}
