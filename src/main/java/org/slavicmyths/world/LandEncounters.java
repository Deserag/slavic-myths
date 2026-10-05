package org.slavicmyths.world;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.entity.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class LandEncounters {
    public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){
        event.register(ModEntities.KIKIMORA.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->!w.getLevel().isDay() && n.nextInt(12)==0 && w.getMaxLocalRawBrightness(p)<8 && allowed(w,p) && Mob.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(KikimoraEntity.class,new AABB(p).inflate(32)).isEmpty(),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.POLUDNITSA.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->w.getLevel().getDayTime()%24000>=5000 && w.getLevel().getDayTime()%24000<=7500 && !w.getLevel().isRaining() && w.canSeeSky(p) && n.nextInt(16)==0 && allowed(w,p) && Mob.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(PoludnitsaEntity.class,new AABB(p).inflate(48)).isEmpty(),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.POLEVIK.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->w.canSeeSky(p) && n.nextInt(16)==0 && allowed(w,p) && Mob.checkMobSpawnRules(t,w,r,p,n),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.IGOSHA.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->!w.getLevel().isDay() && n.nextInt(40)==0 && w.getMaxLocalRawBrightness(p)<5 && allowed(w,p) && Mob.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(IgoshaEntity.class,new AABB(p).inflate(48)).isEmpty(),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static boolean allowed(ServerLevelAccessor w,BlockPos p){return w.getLevel().dimension()==Level.OVERWORLD && w.getDifficulty()!=Difficulty.PEACEFUL;}
    private static java.util.List<LandSpiritEntity> guardians(Level w,BlockPos pos){return w.getEntitiesOfClass(LandSpiritEntity.class,new AABB(pos).inflate(12),e->e.home!=null && (e instanceof BannikEntity || e instanceof OvinnikEntity) && (pos.equals(e.home.offset(2,0,1)) || pos.equals(e.home.offset(-2,0,1))));}
    @SubscribeEvent public static void open(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide || e.getEntity().isCreative())return;
        for(LandSpiritEntity spirit:guardians(e.getLevel(),e.getPos()))if(e.getPos().equals(spirit.home.offset(2,0,1)) && !spirit.permits(e.getEntity())){spirit.provoke(e.getEntity());e.setCanceled(true);}
    }
    @SubscribeEvent public static void broken(BlockEvent.BreakEvent e){
        if(!(e.getLevel() instanceof Level) || e.getPlayer().isCreative())return;
        for(LandSpiritEntity spirit:guardians((Level)e.getLevel(),e.getPos()))spirit.provoke(e.getPlayer());
    }
    private LandEncounters(){}
}
