package org.slavicmyths.winter;

import java.util.HashSet;
import java.util.Set;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static org.slavicmyths.winter.WinterEncounterLayout.*;

/** Persistent encounter sites. No force-loaded chunks, no random mob waves, no respawn after defeat. */
@EventBusSubscriber(modid="slavicmyths")
public final class WinterEncounterSpawns {
    private static final TagKey<Biome> FOREST=tag("winter_forest"), LAND=tag("winter_land"), OCEAN=tag("winter_ocean");
    private static TagKey<Biome> tag(String name){return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("slavicmyths",name));}
    private static Habitat habitat(Holder<Biome> biome){
        if(biome.is(OCEAN))return Habitat.OCEAN;
        if(biome.is(FOREST))return Habitat.FOREST;
        if(biome.is(LAND))return Habitat.LAND;
        return Habitat.OTHER;
    }
    private static EntityType<?> gluheya(){
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths","gluheya")).orElse(null);
    }
    private static EntityType<?> type(Boss boss){
        return switch(boss){
            case CAPTAIN -> org.slavicmyths.winterboss.ShipCaptainContent.BOSS.get();
            case QUEEN -> org.slavicmyths.icequeen.QueenContent.BOSS.get();
            case AUKA -> org.slavicmyths.auka.AukaContent.AUKA.get();
            case OWL -> org.slavicmyths.owl.WinterOwlContent.BOSS.get();
            case WOLVERINE -> org.slavicmyths.wolverine.WolverineContent.BOSS.get();
            case GLUHEYA -> gluheya();
        };
    }
    public static final class Sites extends SavedData {
        final Set<Long> occupied=new HashSet<>();
        final Set<Long> ships=new HashSet<>(), awaitingCaptain=new HashSet<>();
        boolean naturalShip;
        BlockPos pendingShip;
        boolean pendingNatural;
        void beginShip(BlockPos origin,boolean natural){pendingShip=origin.immutable();pendingNatural=natural;setDirty();}
        void finishShip(BlockPos origin,boolean natural){
            ships.add(origin.asLong());awaitingCaptain.add(origin.asLong());
            if(natural)naturalShip=true;
            placed(key(cell(origin.getX()),cell(origin.getZ())));
            pendingShip=null;setDirty();
        }
        static Sites load(CompoundTag nbt,HolderLookup.Provider registries){
            Sites data=new Sites();for(long site:nbt.getLongArray("Occupied"))data.occupied.add(site);
            for(long site:nbt.getLongArray("Ships"))data.ships.add(site);
            for(long site:nbt.getLongArray("AwaitingCaptain"))data.awaitingCaptain.add(site);
            data.naturalShip=nbt.getBoolean("NaturalShip");
            if(nbt.contains("PendingShip")){data.pendingShip=BlockPos.of(nbt.getLong("PendingShip"));data.pendingNatural=nbt.getBoolean("PendingNatural");}
            return data;
        }
        static Sites get(ServerLevel world){
            return world.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Sites::new,Sites::load),"slavicmyths_winter_encounters_v1");
        }
        @Override public CompoundTag save(CompoundTag nbt,HolderLookup.Provider registries){
            nbt.putLongArray("Occupied",occupied.stream().mapToLong(Long::longValue).toArray());
            nbt.putLongArray("Ships",ships.stream().mapToLong(Long::longValue).toArray());
            nbt.putLongArray("AwaitingCaptain",awaitingCaptain.stream().mapToLong(Long::longValue).toArray());
            nbt.putBoolean("NaturalShip",naturalShip);
            if(pendingShip!=null){nbt.putLong("PendingShip",pendingShip.asLong());nbt.putBoolean("PendingNatural",pendingNatural);}
            return nbt;
        }
        void placed(long key){occupied.add(key);setDirty();}
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        FrozenShip.tick(event.getServer());
        ServerLevel world=event.getServer().overworld();
        if(!world.dimension().equals(Level.OVERWORLD) || !(world.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)
            || world.getGameTime()%100!=0 || world.getDifficulty()==Difficulty.PEACEFUL || world.players().isEmpty())return;
        Sites sites=Sites.get(world);boolean test=WinterWorld.isTest(world);
        if(FrozenShip.active(world))return;
        Set<Long> checked=new HashSet<>();
        for(ServerPlayer player:world.players()){
            if(player.isSpectator())continue;
            int cx=cell(player.getBlockX()),cz=cell(player.getBlockZ());
            for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++){
                int x=cx+dx,z=cz+dz;long key=key(x,z);
                if(sites.occupied.contains(key)||!checked.add(key))continue;
                if(Math.abs(player.getBlockX()-center(x))>160||Math.abs(player.getBlockZ()-center(z))>160)continue;
                if(trySite(world,sites,x,z,test))return; // max one encounter per server interval
            }
        }
    }
    private static boolean trySite(ServerLevel world,Sites sites,int cx,int cz,boolean test){
        // Search the stable center first, then a bounded grid. Every site stays within its cell.
        for(int radius=0;radius<=SEARCH;radius+=16)for(int dz=-radius;dz<=radius;dz+=16)for(int dx=-radius;dx<=radius;dx+=16){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
            BlockPos column=new BlockPos(center(cx)+dx,world.getSeaLevel(),center(cz)+dz);
            if(!loadedArea(world,column)||!world.getWorldBorder().isWithinBounds(column))continue;
            BlockPos pos=world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column);
            if(pos.getY()<world.getSeaLevel()-8||pos.getY()>world.getMaxBuildHeight()-8)continue;
            Habitat habitat=habitat(world.getBiome(pos));
            Boss boss=select(habitat,cx,cz,test,gluheya()!=null,world.getSeed());
            if(boss==null)continue;
            if(boss==Boss.CAPTAIN){
                if(FrozenShip.queueNatural(world,column))return true;
                continue;
            }
            if(!suitable(world,pos,boss))continue;
            boolean tooClose=false;
            for(ServerPlayer player:world.players())if(!player.isSpectator()&&player.blockPosition().distSqr(pos)<24*24){tooClose=true;break;}
            if(tooClose)continue;
            EntityType<?> type=type(boss);if(type==null)continue;
            if(!(type.create(world) instanceof Mob mob))continue;
            mob.moveTo(pos.getX()+.5,pos.getY()+(boss==Boss.OWL?3:0),pos.getZ()+.5,0,0);
            if(!world.noCollision(mob)||world.containsAnyLiquid(mob.getBoundingBox()))continue;
            mob.setPersistenceRequired();
            mob.restrictTo(pos,80);
            // Position and chosen boss are recorded only after a successful entity insertion.
            if(!world.addFreshEntity(mob))continue;
            sites.placed(key(cx,cz));
            LogUtils.getLogger().info("Winter encounter {} at {} (test={})",boss,pos,test);
            return true;
        }
        return false;
    }
    private static boolean loadedArea(ServerLevel world,BlockPos pos){
        for(int x=-4;x<=4;x+=8)for(int z=-4;z<=4;z+=8)
            if(!world.hasChunkAt(pos.offset(x,0,z)))return false;
        return true;
    }
    private static boolean ice(BlockState s){return s.is(Blocks.ICE)||s.is(Blocks.PACKED_ICE)||s.is(Blocks.BLUE_ICE);}
    private static boolean suitable(ServerLevel world,BlockPos pos,Boss boss){
        BlockPos ground=pos.below();BlockState state=world.getBlockState(ground);
        if(boss==Boss.CAPTAIN)return false;
        else{
            if(!state.getFluidState().isEmpty()||(boss!=Boss.OWL&&ice(state))||state.is(net.minecraft.tags.BlockTags.LOGS))return false;
            if((boss==Boss.QUEEN||boss==Boss.AUKA||boss==Boss.GLUHEYA)&&!world.getBiome(pos).is(FOREST))return false;
        }
        if(!state.isFaceSturdy(world,ground,net.minecraft.core.Direction.UP))return false;
        return world.canSeeSky(pos)&&world.getBlockState(pos).getFluidState().isEmpty();
    }
    private WinterEncounterSpawns(){}
}
