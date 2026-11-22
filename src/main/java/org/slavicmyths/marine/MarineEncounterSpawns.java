package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Sparse water encounters have their own budget instead of competing with underground monsters. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarineEncounterSpawns {
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel world) || world.dimension()!=Level.OVERWORLD
            || world.getDifficulty()==Difficulty.PEACEFUL || world.getGameTime()%1200!=0) return;
        var types=java.util.List.of(TsarPikeContent.BOSS.get(),DeepSeaSerpentContent.BOSS.get(),
            OceanZmeyContent.BOSS.get(),ZhelvaContent.BOSS.get(),StratimBirdContent.BOSS.get());
        int index=(int)((world.getGameTime()/1200)%types.size());
        for(var player:world.players()) {
            if(player.isSpectator())continue;
            var biome=world.getBiome(player.blockPosition());
            if(!biome.is(BiomeTags.IS_OCEAN)&&!biome.is(BiomeTags.IS_RIVER))continue;
            attempt(world,player.blockPosition(),types.get(index));
        }
    }
    public static boolean isMarineBoss(Mob mob) {
        return mob instanceof OceanZmeyEntity||mob instanceof DeepSeaSerpentEntity||mob instanceof TsarPikeEntity
            ||mob instanceof ZhelvaEntity||mob instanceof StratimBirdEntity||mob instanceof PharaohkaEntity pharaohka&&pharaohka.leader();
    }
    @SubscribeEvent public static void naturalSpawn(net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent event) {
        if(event.getSpawnType()==MobSpawnType.SPAWN_EGG&&isMarineBoss(event.getEntity())&&!(event.getEntity() instanceof StratimBirdEntity)){
            var mob=event.getEntity();var world=event.getLevel().getLevel();
            if(!world.getFluidState(mob.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)){
                boolean found=false;
                var origin=mob.blockPosition();
                for(int radius=4;radius<=24&&!found;radius+=4)for(int i=0;i<16&&!found;i++){
                    double a=i*Math.PI/8;int x=origin.getX()+(int)(Math.cos(a)*radius),z=origin.getZ()+(int)(Math.sin(a)*radius);
                    var check=new BlockPos(x,origin.getY(),z);if(!world.hasChunkAt(check))continue;
                    int surface=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;
                    var water=new BlockPos(x,surface,z);
                    if(!world.getFluidState(water).is(net.minecraft.tags.FluidTags.WATER))continue;
                    var destination=net.minecraft.world.phys.Vec3.atBottomCenterOf(water);
                    var box=mob.getBoundingBox().move(destination.subtract(mob.position()));
                    if(!world.noCollision(mob,box))continue;
                    mob.moveTo(destination.x,destination.y,destination.z,mob.getYRot(),0);found=true;
                }
                if(!found){event.setSpawnCancelled(true);return;}
            }
        }
        if(event.getSpawnType()!=MobSpawnType.NATURAL||!isMarineBoss(event.getEntity()))return;
        var world=event.getLevel().getLevel();var mob=event.getEntity();
        if(!world.getEntitiesOfClass(Mob.class,mob.getBoundingBox().inflate(512),other->other!=mob&&isMarineBoss(other)).isEmpty())
            event.setSpawnCancelled(true);
    }
    static boolean attempt(ServerLevel world,BlockPos origin,EntityType<? extends Mob> type) {
        if(world.getDifficulty()==Difficulty.PEACEFUL)return false;
        if(!world.getEntitiesOfClass(Mob.class,new AABB(origin).inflate(512),MarineEncounterSpawns::isMarineBoss).isEmpty())return false;
        for(int i=0;i<96;i++) {
            double angle=world.random.nextDouble()*Math.PI*2;
            int radius=40+world.random.nextInt(57);
            int x=origin.getX()+(int)(Math.cos(angle)*radius),z=origin.getZ()+(int)(Math.sin(angle)*radius);
            if(!world.hasChunkAt(new BlockPos(x,63,z)))continue;
            int surface=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            int floor=world.getHeight(Heightmap.Types.OCEAN_FLOOR,x,z);
            int y=type==StratimBirdContent.BOSS.get()?surface:
                type==ZhelvaContent.BOSS.get()?floor:surface-4-world.random.nextInt(8);
            var pos=new BlockPos(x,y,z);
            var habitat=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "spawn_"+
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath()));
            if(!world.getBiome(pos).is(habitat))continue;
            if(origin.distSqr(pos)<32*32)continue;
            // The normal placement predicate still owns biome, depth, clearance and difficulty.
            // This scheduler supplies the rarity budget, rather than rolling it twice.
            if(!SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.CHUNK_GENERATION,pos,world.random))continue;
            var mob=type.create(world);
            if(mob==null)return false;
            mob.moveTo(x+.5,y,z+.5,world.random.nextFloat()*360,0);
            if(!mob.checkSpawnObstruction(world))continue;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),MobSpawnType.NATURAL,null);
            if(world.addFreshEntity(mob))return true;
        }
        return false;
    }
}