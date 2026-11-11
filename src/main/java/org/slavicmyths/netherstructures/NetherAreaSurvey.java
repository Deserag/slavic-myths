package org.slavicmyths.netherstructures;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.netherboss.NetherEncounterRecords;
import org.slavicmyths.worldgen.ManualStructureRecords;

/**
 * Read-only 1000x1000 Nether survey. Never creates or loads chunks.
 * Counts of structures and live mobs are a lower bound when coverage is incomplete.
 */
public final class NetherAreaSurvey {
    public static final int SIDE=1000, HALF=SIDE/2;
    private static final Set<String> STRUCTURES=Set.of(
        "nether_posad","nether_watchpost","broken_nether_bridge","wayfarer_camp",
        "forgotten_shrine","ancestor_shrine","trizna_pyre","navya_heart",
        "zharenitsa_arena","morovoy_raven_arena","morovoy_fire_zmey_arena"
    );
    private static final Set<String> BOSSES=Set.of(
        "bauk","cursed_mytar","gneteya","tryaseya","agneya","ledeya",
        "neveya","lomeya","viy","morovoy_raven","morovoy_fire_zmey",
        "zharenitsa","grove_guardian","posten","magma_lord"
    );
    private static final Set<String> BIOMES=Set.of(
        "navya_grove","black_cinder","lost_halls","burning_islands"
    );

    private static boolean inside(BlockPos at,BlockPos center){
        return Math.abs((long)at.getX()-center.getX())<=HALF&&Math.abs((long)at.getZ()-center.getZ())<=HALF;
    }
    private static String structureKey(ServerLevel world,net.minecraft.world.level.levelgen.structure.StructureStart start){
        var id=world.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        return id==null||!id.getNamespace().equals("slavicmyths")?"":id.getPath();
    }
    private static void message(CommandSourceStack source,String value){
        source.sendSuccess(()->Component.literal(value),false);
    }
    private static void entries(CommandSourceStack source,String title,Map<String,Integer> counts){
        message(source,title+": "+counts.values().stream().mapToInt(Integer::intValue).sum());
        counts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->message(source,"  "+e.getKey()+": "+e.getValue()));
    }

    public static int run(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player)){
            source.sendFailure(Component.literal("Команда требует игрока в Незере."));
            return 0;
        }
        ServerLevel world=player.serverLevel();
        if(world.dimension()!=Level.NETHER){
            source.sendFailure(Component.literal("Проверка работает только в Незере."));
            return 0;
        }

        BlockPos center=player.blockPosition();
        int minCX=Math.floorDiv(center.getX()-HALF,16),maxCX=Math.floorDiv(center.getX()+HALF,16);
        int minCZ=Math.floorDiv(center.getZ()-HALF,16),maxCZ=Math.floorDiv(center.getZ()+HALF,16);
        int expected=(maxCX-minCX+1)*(maxCZ-minCZ+1),loaded=0;
        var starts=new HashSet<String>();
        var natives=new TreeMap<String,Integer>();
        var biomeChunks=new TreeMap<String,Integer>();

        for(int cx=minCX;cx<=maxCX;cx++)for(int cz=minCZ;cz<=maxCZ;cz++){
            var chunk=world.getChunkSource().getChunkNow(cx,cz);
            if(chunk==null)continue;
            loaded++;
            for(var start:chunk.getAllStarts().values()){
                if(!start.isValid())continue;
                String type=structureKey(world,start);
                if(!STRUCTURES.contains(type)||!inside(start.getBoundingBox().getCenter(),center))continue;
                String key=type+":"+start.getChunkPos().toLong();
                if(starts.add(key))natives.merge(type,1,Integer::sum);
            }
            var foundBiomes=new HashSet<String>();
            for(int y:new int[]{48,80,112}){
                var id=chunk.getNoiseBiome(2,y>>2,2).unwrapKey().map(k->k.location()).orElse(null);
                if(id!=null&&id.getNamespace().equals("slavicmyths")&&BIOMES.contains(id.getPath()))foundBiomes.add(id.getPath());
            }
            for(String id:foundBiomes)biomeChunks.merge(id,1,Integer::sum);
        }

        var manual=new TreeMap<String,Integer>();
        for(var site:ManualStructureRecords.get(world).entries()){
            if(STRUCTURES.contains(site.family())&&inside(site.arrival(),center))manual.merge(site.family(),1,Integer::sum);
        }

        var live=new TreeMap<String,Integer>();
        AABB box=new AABB(center.getX()-HALF,world.getMinBuildHeight(),center.getZ()-HALF,
            center.getX()+HALF+1,world.getMaxBuildHeight(),center.getZ()+HALF+1);
        for(var entity:world.getEntities(null,box,e->e.isAlive())){
            var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if(id!=null&&id.getNamespace().equals("slavicmyths")&&BOSSES.contains(id.getPath()))live.merge(id.getPath(),1,Integer::sum);
        }

        int recorded=0,spawned=0,waiting=0;
        for(var encounter:NetherEncounterRecords.get(world).encounters.values()){
            if(!inside(encounter.anchor,center))continue;
            recorded++;
            if(encounter.spawned)spawned++;else waiting++;
        }
        message(source,"Незер: квадрат 1000x1000 вокруг X="+center.getX()+" Z="+center.getZ()+" (±500 по X/Z).");
        message(source,"Покрытие: загружено "+loaded+" из "+expected+" чанков. Неизведанные и выгруженные чанки НЕ проверены.");
        entries(source,"Нативные постройки в загруженных чанках",natives);
        entries(source,"Отдельно зарегистрированные ручные/индексированные постройки (возможны совпадения с нативными)",manual);
        entries(source,"Живые боссы в загруженной области",live);
        message(source,"Записи региональных встреч: "+recorded+" (когда-либо созданы: "+spawned+", ожидают: "+waiting+"). Это не число живых боссов.");
        entries(source,"Чанки с новыми биомами, выборка на Y=48/80/112",biomeChunks);
        if(loaded<expected)message(source,"ИТОГ НЕПОЛНЫЙ: для достоверного спаунрейта нужен автономный прогон генерации и учёт всех чанков.");
        return 1;
    }
    private NetherAreaSurvey(){}
}
