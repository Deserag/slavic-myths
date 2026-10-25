package org.slavicmyths.navya;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.level.levelgen.Heightmap;
public final class NavyaSpawns {
    public static void register(RegisterSpawnPlacementsEvent event){
        for(var kind:new NavyaContent.Kind[]{NavyaContent.Kind.NAVKA,NavyaContent.Kind.ROOTLING,NavyaContent.Kind.CRAWLER,NavyaContent.Kind.WISP})
            event.register(NavyaContent.MOBS.get(kind).get(),kind==NavyaContent.Kind.NAVKA||kind==NavyaContent.Kind.WISP?SpawnPlacementTypes.NO_RESTRICTIONS:SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->allowed(kind,type,world,reason,pos,random),RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static boolean allowed(NavyaContent.Kind kind,EntityType<NavyaMob> type,ServerLevelAccessor world,MobSpawnType reason,BlockPos pos,RandomSource random){
        if(!world.getBiome(pos).is(NavyaWorldgen.BIOME) || !world.isEmptyBlock(pos) || !world.isEmptyBlock(pos.above()) || pos.getY()<33 || pos.getY()>145)return false;
        if(kind!=NavyaContent.Kind.WISP && world.getDifficulty()==Difficulty.PEACEFUL)return false;
        var profile=NavyaProfile.at(world.getLevel().getSeed(),pos.getX(),pos.getZ());
        if(kind==NavyaContent.Kind.WISP)return random.nextInt(8)==0;
        if(kind==NavyaContent.Kind.NAVKA)return pos.getY()>profile.lower()+5 && pos.getY()<=profile.upper()+14;
        if(!Mob.checkMobSpawnRules(type,world,reason,pos,random))return false;
        return kind==NavyaContent.Kind.ROOTLING?pos.getY()>profile.lower()+5 && pos.getY()<=profile.middle()+6:pos.getY()<=profile.middle()-5;
    }
    private NavyaSpawns(){ }
}
