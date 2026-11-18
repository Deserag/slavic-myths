package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Synchronous, before-login construction for the ONE Slavic Oceans flat preset.
 * This is intentionally separate from operator-driven, tick-budgeted Buyan creation.
 * The existing BuyanTerrain/BuyanNature functions are the single source of shape data.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class BuyanPreSpawn {
    private static final String KEY="slavicmyths_slavic_oceans_spawn_2622";
    private static final int R=BuyanTerrain.MAX_RADIUS;

    private static final class Record extends SavedData {
        boolean ready;
        static Record load(CompoundTag tag,HolderLookup.Provider registries){
            Record r=new Record();r.ready=tag.getBoolean("Ready");return r;
        }
        static Record get(ServerLevel world){
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new,Record::load),KEY);
        }
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
            tag.putBoolean("Ready",ready);return tag;
        }
        void finished(){ready=true;setDirty();}
    }

    private static boolean layer(FlatLayerInfo layer,int count,BlockState block){
        return layer.getHeight()==count&&layer.getBlockState().is(block.getBlock());
    }

    /** Only the existing shallow-ocean preset; not an ordinary superflat or another modded ocean. */
    private static boolean isPreset(ServerLevel world){
        if(world.dimension()!=Level.OVERWORLD
            ||!(world.getChunkSource().getGenerator() instanceof FlatLevelSource flat))return false;
        List<FlatLayerInfo> layers=flat.settings().getLayersInfo();
        return layers.size()==4
            &&layer(layers.get(0),1,Blocks.BEDROCK.defaultBlockState())
            &&layer(layers.get(1),122,Blocks.STONE.defaultBlockState())
            &&layer(layers.get(2),1,Blocks.GRAVEL.defaultBlockState())
            &&layer(layers.get(3),4,Blocks.WATER.defaultBlockState())
            &&world.getBiome(new BlockPos(0,63,0)).is(BiomeTags.IS_OCEAN);
    }

    @SubscribeEvent
    public static void beforePlayers(ServerStartedEvent event){
        ServerLevel world=event.getServer().overworld();
        if(!isPreset(world))return;
        Record state=Record.get(world);
        if(state.ready)return;
        if(!BuyanState.get(world).completedSites().isEmpty()){
            // Recover missing stages only; never repeat terrain or nature.
            try {
                constructOrResume(world);
                state.finished();
                world.setDefaultSpawnPos(BuyanCityPlan.spawn(world,
                    new BuyanState.Site(0,0,R,63)),0.0F);
            } catch(RuntimeException exception) {
                LogUtils.getLogger().error(
                    "Slavic Oceans: existing Buyan could not be completed safely",exception);
            }
            return;
        }
        long begin=System.nanoTime();
        try{
            constructOrResume(world);
            state.finished();
            world.setDefaultSpawnPos(BuyanCityPlan.spawn(world,new BuyanState.Site(0,0,R,63)),0.0F);
            LogUtils.getLogger().info("Slavic Oceans ready before player spawn; elapsed {} ms",
                (System.nanoTime()-begin)/1_000_000L);
        }catch(RuntimeException exception){
            // Never crash the integrated server because one optional district
            // cannot be safely placed. Ready remains false so recovery can
            // retry on the next world opening without rebuilding the terrain.
            LogUtils.getLogger().error(
                "Slavic Oceans masterplan is incomplete; placement was stopped safely. "
                    + "This save is NOT a completed Buyan.",exception);
            int ground=BuyanNature.top(world.getSeed(),0,0,R,0,-40,63);
            if(ground!=Integer.MIN_VALUE)
                world.setDefaultSpawnPos(new BlockPos(0,ground+2,-40),0.0F);
        }
    }

    /**
     * Reentrant construction shared by the actual spawn handler and GameTests.
     * The GameTest server reuses its save between Gradle invocations.
     * A previously completed nature stage must never be executed twice.
     */
    static void constructOrResume(ServerLevel world) {
        BuyanState terrain=BuyanState.get(world);
        BuyanNatureState nature=BuyanNatureState.get(world);
        for(BuyanState.Site site:terrain.completedSites()) {
            if(site.x()!=0||site.z()!=0||site.radius()!=R||site.waterLevel()!=63)
                continue;
            if(!nature.finishedAt(site))
                throw new IllegalStateException(
                    "Buyan foundation exists without completed nature; cannot regenerate it");
            // Old saves are not reworked automatically. Only an already saved
            // masterplan may be acknowledged after an interrupted startup.
            if(!BuyanCityPlan.hasBuilt(world,site))
                throw new IllegalStateException(
                    "Legacy Buyan detected; automatic city retrofitting is disabled");
            return;
        }
        if(!terrain.completedSites().isEmpty()||terrain.active||nature.active
            ||nature.hasCompletedSites())
            throw new IllegalStateException(
                "Incompatible or active Buyan generation exists; refusing overwrite");
        construct(world);
    }

    // Package access for the isolated integration GameTest: this executes
    // the REAL terrain, nature and all five districts in one disposable world.
    static void construct(ServerLevel world){
        final long seed=world.getSeed();
        final int sea=63;
        final int min=world.getMinBuildHeight(),max=world.getMaxBuildHeight();
        if(min!=-64 || max<sea+90)
            throw new IllegalStateException("Slavic Oceans needs the standard -64..320 Overworld build height");
        double forestCutoff=BuyanNature.forestCutoff(seed,0,0,R,sea);
        double[] coasts=BuyanNature.coastCutoffs(seed,0,0);

        // Bounded whole-island pre-generation. Start-up cannot finish until this completes.
        // This removes post-spawn queues, progress waits, and repeated landscape passes.
        for(int chunkZ=-22;chunkZ<=21;chunkZ++){
            for(int chunkX=-22;chunkX<=21;chunkX++){
                int bx=chunkX<<4,bz=chunkZ<<4;
                int nearX=Math.max(bx,Math.min(0,bx+15));
                int nearZ=Math.max(bz,Math.min(0,bz+15));
                if((long)nearX*nearX+(long)nearZ*nearZ>(long)(R+3)*(R+3))continue;
                // Loading one chunk at a time avoids keeping an extra 2000-chunk ticket set.
                world.getChunk(chunkX,chunkZ);
                for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){
                    int x=bx+dx,z=bz+dz;
                    int top=BuyanNature.top(seed,0,0,R,x,z,sea);
                    if(top==Integer.MIN_VALUE)continue;
                    BuyanTerrain.Surface kind=BuyanNature.surface(
                        seed,0,0,x,z,sea,top,coasts[0],coasts[1]);
                    double cover=BuyanTerrain.surfaceCover(seed,x,z);
                    int roll=BuyanTerrain.surfaceRockRoll(seed,x,z);
                    for(int y=sea+1;y<=top;y++){
                        BlockState block=BuyanNature.block(seed,0,0,R,x,y,z,top,sea,
                            forestCutoff,kind,cover,roll);
                        world.setBlock(new BlockPos(x,y,z),block,2);
                    }
                }
            }
        }

        // Trees are seeded on a sparse chunk-local grid after all ground columns exist.
        // Entire crowns stay inside the already generated island.
        for(int chunkZ=-22;chunkZ<=21;chunkZ++)for(int chunkX=-22;chunkX<=21;chunkX++){
            int bx=chunkX<<4,bz=chunkZ<<4;
            int nearX=Math.max(bx,Math.min(0,bx+15));
            int nearZ=Math.max(bz,Math.min(0,bz+15));
            if((long)nearX*nearX+(long)nearZ*nearZ>(long)(R+3)*(R+3))continue;
            for(int ox:new int[]{4,12})for(int oz:new int[]{4,12}){
                int x=bx+ox,z=bz+oz;
                if(!BuyanNature.forest(seed,0,0,R,x,z,sea,forestCutoff)
                    ||BuyanTerrain.value(seed ^ 0xB22A0603L,x,z,0)>0.55)continue;
                int y=BuyanNature.top(seed,0,0,R,x,z,sea);
                if(y==Integer.MIN_VALUE||y+18>=max)continue;
                BuyanNature.TreeKind type=BuyanNature.treeKind(seed,x,z,y,sea);
                int height=(type==BuyanNature.TreeKind.SPRUCE?9:6)
                    +(int)(4.0*BuyanTerrain.value(seed ^ 0xB22A0604L,x,z,0));
                if(type==BuyanNature.TreeKind.GOLD_BIRCH){
                    BuyanFlora.placeNaturalTree(world,seed,x,y,z,height);
                }else tree(world,x,y,z,height,type);
            }
        }

        // Deterministic flowers at creation time, with central/shore/route/highland exclusions.
        for(int chunkZ=-22;chunkZ<=21;chunkZ++)for(int chunkX=-22;chunkX<=21;chunkX++){
            int bx=chunkX<<4,bz=chunkZ<<4;
            int nearX=Math.max(bx,Math.min(0,bx+15));
            int nearZ=Math.max(bz,Math.min(0,bz+15));
            if((long)nearX*nearX+(long)nearZ*nearZ>(long)(R+3)*(R+3))continue;
            for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){
                int x=bx+dx,z=bz+dz;
                int ground=BuyanNature.top(seed,0,0,R,x,z,sea);
                if(ground==Integer.MIN_VALUE)continue;
                BlockState flower=BuyanFlora.flowerAt(seed,0,0,R,x,ground,z,sea);
                if(flower==null)continue;
                BlockPos pos=new BlockPos(x,ground+1,z);
                BlockState soil=world.getBlockState(pos.below());
                if((soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.PODZOL)
                    ||soil.is(Blocks.MOSS_BLOCK)||soil.is(Blocks.COARSE_DIRT))
                    &&world.getBlockState(pos).isAir())world.setBlock(pos,flower,2);
            }
        }

        // Preserve the existing large-island state machine for status and manual commands.
        BuyanState base=BuyanState.get(world);
        base.begin(0,0,R,sea);
        base.finish();
        BuyanState.Site site=new BuyanState.Site(0,0,R,sea);
        BuyanNatureState nature=BuyanNatureState.get(world);
        nature.begin(site,forestCutoff,coasts[0],coasts[1]);
        nature.nextPhase();
        nature.finish();

        // Only terrain and reserved city layout are generated in stage one.
        // Harbor, outpost and craft buildings are never placed automatically.
        if(!BuyanCityPlan.placeOnce(world,site))
            throw new IllegalStateException("White City masterplan could not be placed safely");
        LogUtils.getLogger().info("Buyan phase two: {}",
            BuyanDevelopment.develop(world,site));

    }

    private static BlockState wood(BuyanNature.TreeKind type){
        return switch(type){
            case OAK->Blocks.OAK_LOG.defaultBlockState();
            case BIRCH->Blocks.BIRCH_LOG.defaultBlockState();
            case GOLD_BIRCH->BuyanFlora.GOLD_BIRCH_LOG.get().defaultBlockState();
            case SPRUCE->Blocks.SPRUCE_LOG.defaultBlockState();
        };
    }
    private static BlockState leaf(BuyanNature.TreeKind type){
        return switch(type){
            case OAK->Blocks.OAK_LEAVES.defaultBlockState();
            case BIRCH->Blocks.BIRCH_LEAVES.defaultBlockState();
            case GOLD_BIRCH->BuyanFlora.GOLD_BIRCH_LEAVES.get().defaultBlockState();
            case SPRUCE->Blocks.SPRUCE_LEAVES.defaultBlockState();
        };
    }
    private static void tree(ServerLevel w,int x,int ground,int z,int height,BuyanNature.TreeKind type){
        for(int h=1;h<=height;h++)w.setBlock(new BlockPos(x,ground+h,z),wood(type),2);
        int top=ground+height;
        for(int yy=top-2;yy<=top+2;yy++){
            int radius=yy==top-2||yy==top+2?1:type==BuyanNature.TreeKind.SPRUCE?2:3;
            for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
                if(dx*dx+dz*dz>radius*radius+1)continue;
                BlockPos p=new BlockPos(x+dx,yy,z+dz);
                if(w.getBlockState(p).isAir())w.setBlock(p,leaf(type),2);
            }
        }
    }

    private BuyanPreSpawn(){}
}
