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
    private static final int REGION=2048, ACTIVATE=112, MIN_DISTANCE=800;
    private record Definition(String key,String[] ids,Set<String> biomes) {}
    private static final List<Definition> WILDERNESS=List.of(
        new Definition("morovoy_raven",new String[]{"slavicmyths:morovoy_raven"},Set.of("minecraft:crimson_forest","minecraft:warped_forest")),
        new Definition("morovoy_fire_zmey",new String[]{"slavicmyths:morovoy_fire_zmey"},Set.of("minecraft:nether_wastes","minecraft:basalt_deltas")),
        new Definition("mor_serpent",new String[]{"slavicmyths:mor_serpent"},Set.of("minecraft:soul_sand_valley","minecraft:nether_wastes"))
    );
    private static long hash(long seed,long x,long z,long salt){long n=seed^x*0x9E3779B97F4A7C15L^z*0xC2B2AE3D27D4EB4FL^salt;n^=n>>>30;n*=0xBF58476D1CE4E5B9L;n^=n>>>27;n*=0x94D049BB133111EBL;return n^(n>>>31);}
    private static String biome(ServerLevel w,BlockPos pos){return w.getBiome(pos).unwrapKey().map(k->k.location().toString()).orElse("");}
    private static BlockPos safe(ServerLevel w,BlockPos xz,Definition d){
        if(!w.hasChunkAt(xz))return null;
        for(int y=Math.min(112,w.getMaxBuildHeight()-10);y>=34;y--){
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
        int rx=Math.floorDiv(player.getBlockX(),REGION),rz=Math.floorDiv(player.getBlockZ(),REGION);
        for(int x=rx-1;x<=rx+1;x++)for(int z=rz-1;z<=rz+1;z++){
            long roll=hash(w.getSeed(),x,z,101);
            if(Math.floorMod(roll,3)!=0)continue;
            Definition d=WILDERNESS.get((int)Math.floorMod(roll>>>8,WILDERNESS.size()));
            if(!supported(d))continue;
            String key="wild:"+x+":"+z;
            int cx=x*REGION+256+(int)Math.floorMod(roll>>>16,REGION-512);
            int cz=z*REGION+256+(int)Math.floorMod(roll>>>36,REGION-512);
            BlockPos candidate=new BlockPos(cx,64,cz);
            if(player.blockPosition().distSqr(candidate)>256*256)continue;
            var encounter=records.reserve(key,d.key,candidate);
            if(encounter.spawned)continue;
            if(player.blockPosition().distSqr(encounter.anchor)>256*256)continue;
            BlockPos location=null;
            for(int attempt=0;attempt<48;attempt++){
                long h=hash(roll,attempt,0,117);
                BlockPos xz=encounter.anchor.offset((int)Math.floorMod(h,193)-96,0,(int)Math.floorMod(h>>>24,193)-96);
                if(!w.hasChunkAt(xz))continue;
                BlockPos found=safe(w,xz,d);
                if(found==null||found.distSqr(player.blockPosition())>ACTIVATE*ACTIVATE||found.distSqr(player.blockPosition())<32*32)continue;
                boolean close=false;
                for(var other:records.encounters.values())if(other!=encounter&&other.spawned&&other.anchor.distSqr(found)<MIN_DISTANCE*MIN_DISTANCE){close=true;break;}
                if(!close){location=found;break;}
            }
            if(location!=null){records.relocate(encounter,location);spawn(w,d,location,records,encounter);}
        }
    }
    private NetherEncounterSpawns(){}
}
