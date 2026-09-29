package org.slavicmyths.world;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.entity.*;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class LandEncounters {
    public static void placements(){
        EntitySpawnPlacementRegistry.register(ModEntities.KIKIMORA.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->!w.getLevel().isDay() && n.nextInt(12)==0 && w.getMaxLocalRawBrightness(p)<8 && allowed(w,p) && MobEntity.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(KikimoraEntity.class,new AxisAlignedBB(p).inflate(32)).isEmpty());
        EntitySpawnPlacementRegistry.register(ModEntities.POLUDNITSA.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->w.getLevel().getDayTime()%24000>=5000 && w.getLevel().getDayTime()%24000<=7500 && !w.getLevel().isRaining() && w.canSeeSky(p) && n.nextInt(16)==0 && allowed(w,p) && MobEntity.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(PoludnitsaEntity.class,new AxisAlignedBB(p).inflate(48)).isEmpty());
        EntitySpawnPlacementRegistry.register(ModEntities.POLEVIK.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->w.canSeeSky(p) && n.nextInt(16)==0 && allowed(w,p) && MobEntity.checkMobSpawnRules(t,w,r,p,n));
        EntitySpawnPlacementRegistry.register(ModEntities.IGOSHA.get(),EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            (t,w,r,p,n)->!w.getLevel().isDay() && n.nextInt(40)==0 && w.getMaxLocalRawBrightness(p)<5 && allowed(w,p) && MobEntity.checkMobSpawnRules(t,w,r,p,n) && w.getLevel().getEntitiesOfClass(IgoshaEntity.class,new AxisAlignedBB(p).inflate(48)).isEmpty());
    }
    private static boolean allowed(IServerWorld w,BlockPos p){return w.getLevel().dimension()==World.OVERWORLD && w.getDifficulty()!=Difficulty.PEACEFUL;}
    @SubscribeEvent public static void biomes(BiomeLoadingEvent e){
        if(e.getName()==null || !e.getName().getNamespace().equals("minecraft"))return;
        Biome.Category c=e.getCategory();
        if(c==Biome.Category.FOREST || c==Biome.Category.TAIGA){e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.KIKIMORA.get(),2,1,1));e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.IGOSHA.get(),1,1,1));}
        if(c==Biome.Category.PLAINS || c==Biome.Category.SAVANNA){e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.POLUDNITSA.get(),2,1,1));e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.POLEVIK.get(),2,1,1));}
    }
    private static java.util.List<LandSpiritEntity> guardians(World w,BlockPos pos){return w.getEntitiesOfClass(LandSpiritEntity.class,new AxisAlignedBB(pos).inflate(12),e->e.home!=null && (e instanceof BannikEntity || e instanceof OvinnikEntity) && (pos.equals(e.home.offset(2,0,1)) || pos.equals(e.home.offset(-2,0,1))));}
    @SubscribeEvent public static void open(PlayerInteractEvent.RightClickBlock e){
        if(e.getWorld().isClientSide || e.getPlayer().isCreative())return;
        for(LandSpiritEntity spirit:guardians(e.getWorld(),e.getPos()))if(e.getPos().equals(spirit.home.offset(2,0,1)) && !spirit.permits(e.getPlayer())){spirit.provoke(e.getPlayer());e.setCanceled(true);}
    }
    @SubscribeEvent public static void broken(BlockEvent.BreakEvent e){
        if(!(e.getWorld() instanceof World) || e.getPlayer().isCreative())return;
        for(LandSpiritEntity spirit:guardians((World)e.getWorld(),e.getPos()))spirit.provoke(e.getPlayer());
    }
    private LandEncounters(){}
}
