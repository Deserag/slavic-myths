package org.slavicmyths.netherboss;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.worldgen.ManualStructureRecords;

/** Sparse, deterministic Nether encounters; only loaded terrain is inspected or modified. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class NetherEncounterSpawns {
    private static final int REGION=2000, ACTIVATE=128, MIN_DISTANCE=112;
    private record Definition(String key,String[] ids,Set<String> biomes) {}
    private static final Set<String> NETHER_BIOMES=Set.of("minecraft:nether_wastes","minecraft:soul_sand_valley",
        "minecraft:crimson_forest","minecraft:warped_forest","minecraft:basalt_deltas",
        "slavicmyths:navya_grove","slavicmyths:black_cinder","slavicmyths:lost_halls","slavicmyths:burning_islands");
    private static final Set<String> NEW_BIOMES=Set.of("slavicmyths:navya_grove","slavicmyths:black_cinder",
        "slavicmyths:lost_halls","slavicmyths:burning_islands");
    private static final Definition SISTERS=new Definition("gneteya_tryaseya",
        new String[]{"slavicmyths:gneteya","slavicmyths:tryaseya"},NETHER_BIOMES);
    private static final List<Definition> WILDERNESS=List.of(
        new Definition("bauk",new String[]{"slavicmyths:bauk"},NETHER_BIOMES),
        new Definition("cursed_mytar",new String[]{"slavicmyths:cursed_mytar"},NETHER_BIOMES),
        SISTERS);
    private static final List<Definition> POSAD_BOSSES=List.of(
        new Definition("bauk",new String[]{"slavicmyths:bauk"},Set.of("minecraft:crimson_forest","minecraft:warped_forest","minecraft:nether_wastes")),
        new Definition("cursed_mytar",new String[]{"slavicmyths:cursed_mytar"},Set.of("minecraft:nether_wastes","minecraft:soul_sand_valley","minecraft:basalt_deltas"))
    );
    private static long hash(long seed,long x,long z,long salt){long n=seed^x*0x9E3779B97F4A7C15L^z*0xC2B2AE3D27D4EB4FL^salt;n^=n>>>30;n*=0xBF58476D1CE4E5B9L;n^=n>>>27;n*=0x94D049BB133111EBL;return n^(n>>>31);}
    private static String biome(ServerLevel w,BlockPos pos){return w.getBiome(pos).unwrapKey().map(k->k.location().toString()).orElse("");}
    private static BlockPos safe(ServerLevel w,BlockPos xz,Definition d,int maxY,int minY){
        if(!w.hasChunkAt(xz))return null;
        for(int y=Math.min(maxY,w.getMaxBuildHeight()-10);y>=Math.max(34,minY);y--){
            BlockPos p=new BlockPos(xz.getX(),y,xz.getZ());
            if(!d.biomes.contains(biome(w,p))||!w.getFluidState(p.below()).isEmpty()||!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP))continue;
            boolean open=true;
            for(int dx=-2;dx<=2&&open;dx++)for(int dz=-2;dz<=2&&open;dz++)for(int dy=0;dy<6;dy++){
                BlockPos q=p.offset(dx,dy,dz);
                if(!w.hasChunkAt(q)||!w.getFluidState(q).isEmpty()||!w.getBlockState(q).getCollisionShape(w,q).isEmpty()){open=false;break;}
            }
            if(open)return p;
        }
        return null;
    }
    private static boolean supported(Definition d){for(String id:d.ids){ResourceLocation name=ResourceLocation.tryParse(id);if(name==null||!BuiltInRegistries.ENTITY_TYPE.containsKey(name))return false;}return true;}
    private static boolean spawn(ServerLevel w,Definition d,BlockPos at,NetherEncounterRecords records,NetherEncounterRecords.Encounter encounter){
        if(d==SISTERS){
            if(!org.slavicmyths.sisters.SistersContent.spawnPair(w,at))return false;
            records.markSpawned(encounter);
            return true;
        }
        List<Entity> created=new ArrayList<>();
        for(int i=0;i<d.ids.length;i++){
            EntityType<?> type=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(d.ids[i]));
            Entity entity=type.create(w);
            if(!(entity instanceof Mob)){created.forEach(Entity::discard);return false;}
            entity.moveTo(at.getX()+.5+i*3,at.getY(),at.getZ()+.5,0,0);
            if(!w.noCollision(entity)){created.forEach(Entity::discard);return false;}
            ((Mob)entity).setPersistenceRequired();
            entity.getPersistentData().putString("SlavicNetherEncounter",encounter.key);
            if(!w.addFreshEntity(entity)){created.forEach(Entity::discard);return false;}
            created.add(entity);
        }
        records.markSpawned(encounter);
        return true;
    }
    @SubscribeEvent public static void player(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)||player.tickCount%80!=0)return;
        ServerLevel w=player.serverLevel();
        if(w.dimension()!=Level.NETHER||w.getDifficulty()==Difficulty.PEACEFUL||player.isSpectator())return;
        NetherEncounterRecords records=NetherEncounterRecords.get(w);
        for(var site:ManualStructureRecords.get(w).entries()){
            if(!site.family().equals("nether_posad")||site.arrival().distSqr(player.blockPosition())>2400D*2400D)continue;
            for(int index=0;index<POSAD_BOSSES.size();index++){
                Definition d=POSAD_BOSSES.get(index);
                if(!supported(d))continue;
                String key="posad:"+site.id()+":"+d.key;
                long seed=hash(w.getSeed(),site.id().getMostSignificantBits(),site.id().getLeastSignificantBits(),index);
                int radius=500+(int)Math.floorMod(seed,1501);
                double angle=Math.floorMod(seed>>>19,1000000)/1000000D*Math.PI*2D;
                BlockPos candidate=site.arrival().offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
                var encounter=records.reserve(key,d.key,candidate);
                if(encounter.spawned||player.blockPosition().distSqr(candidate)>256D*256D)continue;
                for(int attempt=0;attempt<64;attempt++){
                    long h=hash(seed,attempt,0,313);
                    BlockPos xz=candidate.offset((int)Math.floorMod(h,193)-96,0,(int)Math.floorMod(h>>>24,193)-96);
                    BlockPos location=safe(w,xz,d,112,34);
                    if(location==null||location.distSqr(player.blockPosition())>ACTIVATE*ACTIVATE||location.distSqr(player.blockPosition())<32*32)continue;
                    double fromCity=Math.sqrt(Math.pow(location.getX()-site.arrival().getX(),2)+Math.pow(location.getZ()-site.arrival().getZ(),2));
                    if(fromCity<500||fromCity>2000)continue;
                    boolean tooClose=false;
                    for(var other:records.encounters.values())if(other!=encounter&&other.anchor.distSqr(location)<500D*500D){tooClose=true;break;}
                    if(tooClose)continue;
                    records.relocate(encounter,location);
                    spawn(w,d,location,records,encounter);
                    break;
                }
            }
        }

        int rx=Math.floorDiv(player.getBlockX(),REGION),rz=Math.floorDiv(player.getBlockZ(),REGION);
        tryEncounter(w,player,records,"wild:"+rx+":"+rz,rx,rz,hash(w.getSeed(),rx,rz,101),null);
        String currentBiome=biome(w,player.blockPosition());
        if(NEW_BIOMES.contains(currentBiome))
            tryEncounter(w,player,records,"biome:"+rx+":"+rz+":"+currentBiome,rx,rz,
                hash(w.getSeed(),rx,rz,currentBiome.hashCode()),currentBiome);
    }

    /** Every explored 2000x2000 region has one persistent encounter opportunity.
     * Each new Nether biome has an additional 100%-selected encounter per region.
     * Unsafe terrain delays placement; only already loaded chunks are inspected. */
    private static void tryEncounter(ServerLevel w,ServerPlayer player,NetherEncounterRecords records,
                                     String key,int rx,int rz,long seed,String requiredBiome){
        var previous=records.encounters.get(key);
        if(previous!=null&&previous.spawned)return;
        BlockPos origin=player.blockPosition();
        int top=Math.min(112,origin.getY()+16),bottom=Math.min(112,Math.max(34,origin.getY()-32));
        for(int choice=0;choice<WILDERNESS.size();choice++){
            Definition d=WILDERNESS.get((int)Math.floorMod(seed+choice,WILDERNESS.size()));
            if(previous!=null&&!previous.type.equals(d.key))continue;
            if(!supported(d)||requiredBiome!=null&&!d.biomes.contains(requiredBiome))continue;
            for(int attempt=0;attempt<8;attempt++){
                long roll=hash(seed,origin.getX()>>4,origin.getZ()>>4,
                    (player.tickCount/80L)*19+choice*73L+attempt);
                int radius=40+(int)Math.floorMod(roll,68);
                double angle=Math.floorMod(roll>>>17,65536)/65536.0D*Math.PI*2D;
                BlockPos xz=origin.offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
                if(Math.floorDiv(xz.getX(),REGION)!=rx||Math.floorDiv(xz.getZ(),REGION)!=rz)continue;
                BlockPos found=safe(w,xz,d,top,bottom);
                if(found==null||found.distSqr(origin)>ACTIVATE*ACTIVATE||found.distSqr(origin)<32*32)continue;
                if(requiredBiome!=null&&!requiredBiome.equals(biome(w,found)))continue;
                boolean nearby=false;
                for(var other:records.encounters.values())
                    if(other.spawned&&other.anchor.distSqr(found)<MIN_DISTANCE*MIN_DISTANCE){nearby=true;break;}
                if(nearby)continue;
                var encounter=records.reserve(key,d.key,found);
                if(spawn(w,d,found,records,encounter))return;
            }
        }
    }
    private NetherEncounterSpawns(){}
}
