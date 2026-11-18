package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The first coherent city axis. The broad avenue begins at the existing palace
 * approach, opens into the marked Alatyr plaza and continues beyond its south
 * entrance. The plaza centre stays untouched for a later actual monument.
 *
 * A whole-plan collision preflight and rollback protect the existing districts.
 * Only new-world construction explicitly calls this class.
 */
public final class BuyanCityPlan {
    private static final String SAVE="slavicmyths_buyan_city_axis_2622";
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    private static final BlockState WHITE=Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState BORDER=Blocks.POLISHED_DIORITE.defaultBlockState();
    private static final BlockState ORNAMENT=Blocks.BLUE_TERRACOTTA.defaultBlockState();
    private static final BlockState FOUNDATION=Blocks.STONE_BRICKS.defaultBlockState();

    private BuyanCityPlan() {}

    private static final class Record extends SavedData {
        final Set<Long> completed=new HashSet<>();
        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new,Record::load),SAVE);
        }
        static Record load(CompoundTag tag,HolderLookup.Provider lookup) {
            Record state=new Record();
            for(long id:tag.getLongArray("Completed"))state.completed.add(id);
            return state;
        }
        @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup) {
            long[] ids=new long[completed.size()];
            int i=0;
            for(long id:completed) ids[i++]=id;
            tag.putLongArray("Completed",ids);
            return tag;
        }
        boolean contains(BuyanState.Site site) {
            return completed.contains(new BlockPos(site.x(),0,site.z()).asLong());
        }
        void mark(BuyanState.Site site) {
            completed.add(new BlockPos(site.x(),0,site.z()).asLong());
            setDirty();
        }
    }

    static boolean hasBuilt(ServerLevel world,BuyanState.Site site) {
        return Record.get(world).contains(site);
    }

    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final long seed;
        final Map<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        final Map<Integer,Integer> grade=new HashMap<>();

        Plan(ServerLevel world,BuyanState.Site site) {
            this.world=world;
            this.site=site;
            seed=world.getSeed();
            int prev=Integer.MIN_VALUE;
            for(int z=-100;z<=-14;z++) {
                int top=natural(0,z);
                if(top==Integer.MIN_VALUE)
                    throw new IllegalStateException("City axis has no land at z="+z);
                int wanted=top+1;
                if(prev!=Integer.MIN_VALUE)wanted=Math.clamp(wanted,prev-1,prev+1);
                grade.put(z,wanted);
                prev=wanted;
            }
        }

        int natural(int x,int z) {
            return BuyanNature.top(seed,site.x(),site.z(),site.radius(),
                site.x()+x,site.z()+z,site.waterLevel());
        }
        BlockPos pos(int x,int y,int z) {
            return new BlockPos(site.x()+x,y,site.z()+z);
        }
        int level(int z) {return grade.get(z);}
        void at(int x,int y,int z,BlockState state) {
            blocks.put(pos(x,y,z),state);
        }
        void pave(int x,int z,BlockState tile) {
            int natural=natural(x,z),height=level(z);
            if(natural==Integer.MIN_VALUE||natural<=site.waterLevel()+4
                ||Math.abs(natural+1-height)>10)
                throw new IllegalStateException("City street requires unsafe earthwork at "
                    +x+","+z);
            for(int y=natural+1;y<height;y++)at(x,y,z,FOUNDATION);
            at(x,height,z,tile);
            // The road replaces only the modest earthwork corridor and its
            // three-block-clear headroom, never foundations outside it.
            for(int y=height+1;y<=Math.max(height+3,natural+3);y++)
                at(x,y,z,AIR);
        }
        void mainStreet() {
            for(int z=-35;z<=-15;z++)
                for(int x=-5;x<=5;x++)
                    pave(x,z,Math.abs(x)==5?BORDER:x==0?ORNAMENT:WHITE);
            for(int z=-94;z<=-73;z++)
                for(int x=-5;x<=5;x++)
                    pave(x,z,Math.abs(x)==5?BORDER:x==0?ORNAMENT:WHITE);
        }
        void plaza() {
            // Twenty-five-plus-block reserved plaza with an open inner court.
            for(int z=-72;z<=-36;z++) {
                for(int x=-20;x<=-17;x++)
                    pave(x,z,x==-20?BORDER:WHITE);
                for(int x=17;x<=20;x++)
                    pave(x,z,x==20?BORDER:WHITE);
            }
            for(int x=-20;x<=20;x++)
                for(int z:new int[]{-72,-71,-70,-69,-39,-38,-37,-36})
                    pave(x,z,(z==-72||z==-36)?BORDER:WHITE);
            // Both future city approaches reach the sanctuary road at x +/-33.
            for(int x=-32;x<=-21;x++)
                for(int z=-55;z<=-53;z++)
                    pave(x,z,z==-54?ORNAMENT:WHITE);
            for(int x=21;x<=32;x++)
                for(int z=-55;z<=-53;z++)
                    pave(x,z,z==-54?ORNAMENT:WHITE);
        }
        void lamp(int x,int z) {
            pave(x,z,FOUNDATION);
            int h=level(z);
            for(int y=h+1;y<=h+4;y++)
                at(x,y,z,Blocks.QUARTZ_PILLAR.defaultBlockState());
            at(x,h+5,z,Blocks.LANTERN.defaultBlockState());
        }
        void build() {
            mainStreet();
            plaza();
            for(int z:new int[]{-18,-30,-78,-90})
                for(int x:new int[]{-7,7})lamp(x,z);
            for(int z:new int[]{-38,-70})
                for(int x:new int[]{-22,22})lamp(x,z);
        }
        boolean geometryCheck() {
            if(!blocks.getOrDefault(pos(0,level(-25),-25),AIR).is(Blocks.BLUE_TERRACOTTA)
                ||!blocks.getOrDefault(pos(0,level(-85),-85),AIR).is(Blocks.BLUE_TERRACOTTA)
                ||!blocks.getOrDefault(pos(19,level(-54),-54),AIR).is(Blocks.SMOOTH_QUARTZ))
                return false;
            for(BlockPos p:blocks.keySet()) {
                int x=p.getX()-site.x(),z=p.getZ()-site.z();
                if(Math.abs(x)<=8&&z>=-63&&z<=-45)
                    return false; // Reserve the Alatyr stone's future footprint.
            }
            return true;
        }
        boolean preflight() {
            Set<Long> chunks=new HashSet<>();
            for(var entry:blocks.entrySet()) {
                BlockPos p=entry.getKey();
                if(p.getY()<=world.getMinBuildHeight()
                    ||p.getY()>=world.getMaxBuildHeight()-1
                    ||!world.getWorldBorder().isWithinBounds(p))return false;
                if(chunks.add(ChunkPos.asLong(p.getX()>>4,p.getZ()>>4)))
                    world.getChunk(p.getX()>>4,p.getZ()>>4);
                BlockState previous=world.getBlockState(p);
                if(!previous.equals(entry.getValue())
                    &&!BuyanGenerator.canReplace(previous)) {
                    LogUtils.getLogger().warn(
                        "Buyan city street would overwrite a protected block at {}: {}",
                        p,previous);
                    return false;
                }
            }
            return true;
        }
        void commit() {
            List<Map.Entry<BlockPos,BlockState>> changed=new ArrayList<>();
            Map<BlockPos,BlockState> originals=new LinkedHashMap<>();
            for(var entry:blocks.entrySet()) {
                BlockState before=world.getBlockState(entry.getKey());
                if(!before.equals(entry.getValue())) {
                    changed.add(entry);
                    originals.put(entry.getKey(),before);
                }
            }
            try {
                for(var entry:changed)
                    if(!world.setBlock(entry.getKey(),entry.getValue(),2))
                        throw new IllegalStateException("City street block failed: "
                            +entry.getKey());
            } catch(RuntimeException problem) {
                for(int i=changed.size()-1;i>=0;i--) {
                    BlockPos p=changed.get(i).getKey();
                    world.setBlock(p,originals.get(p),2);
                }
                throw problem;
            }
        }
    }

    static boolean placeOnce(ServerLevel world,BuyanState.Site site) {
        Record record=Record.get(world);
        if(record.contains(site))return true;
        Plan plan=new Plan(world,site);
        plan.build();
        if(!plan.geometryCheck()||!plan.preflight())return false;
        plan.commit();
        record.mark(site);
        LogUtils.getLogger().info("Buyan central street and reserved Alatyr plaza created for {},{}",
            site.x(),site.z());
        return true;
    }

    public static void verifyBlueprintFixture(ServerLevel world) {
        BuyanState.Site site=new BuyanState.Site(0,0,BuyanTerrain.MAX_RADIUS,63);
        Plan plan=new Plan(world,site);
        plan.build();
        if(!plan.geometryCheck()||plan.blocks.size()<1800)
            throw new IllegalStateException("Buyan central street/plaza blueprint invalid");
    }
}
