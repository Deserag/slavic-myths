package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class MarineSpawnGameTests {
    @GameTest(template="empty",timeoutTicks=100)
    public static void naturalPlacementsInDeepOcean(GameTestHelper test) {
        var world=test.getLevel();
        var center=test.absolutePos(new BlockPos(1000,0,1000)).atY(40);
        for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++) {
            for(int y=39;y<=84;y++) {
                var p=new BlockPos(center.getX()+x,y,center.getZ()+z);
                world.setBlock(p,(y==39?Blocks.STONE:y<=63?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);
            }
        }
        String biome="fillbiome "+(center.getX()-4)+" 36 "+(center.getZ()-4)+" "+(center.getX()+4)+" 68 "+(center.getZ()+4)+" minecraft:deep_ocean";
        world.getServer().getCommands().performPrefixedCommand(world.getServer().createCommandSourceStack().withLevel(world).withSuppressedOutput(),biome);
        var types=java.util.List.of(DeepSeaSerpentContent.BOSS.get(),OceanZmeyContent.BOSS.get(),TsarPikeContent.BOSS.get(),ZhelvaContent.BOSS.get(),StratimBirdContent.BOSS.get());
        for(var type:types) {
            var pos=center.atY(type==StratimBirdContent.BOSS.get()?64:type==ZhelvaContent.BOSS.get()?40:52);
            int accepted=0;
            var random=RandomSource.create(29171);
            for(int i=0;i<2048;i++) if(SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,pos,random)) accepted++;
            test.assertTrue(accepted>0,type+" never passes natural placement: biome="+world.getBiome(pos)+", difficulty="+world.getDifficulty()+", water="+world.getFluidState(pos)+", nearby="+world.getEntitiesOfClass(OceanZmeyEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(300)).size());
            var mob=type.create(world);
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
            test.assertTrue(mob.checkSpawnObstruction(world),type+" blocked in valid ocean habitat");
            test.assertTrue(world.addFreshEntity(mob),type+" failed insertion");
            test.assertTrue(!MarineEncounterSpawns.attempt(world,pos,type),type+" scheduler duplicates nearby boss");
            int duplicates=0;
            for(int i=0;i<512;i++) if(SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,pos,random)) duplicates++;
            test.assertTrue(duplicates==0,type+" allows duplicate nearby natural boss");
            mob.discard();
        }
        test.succeed();
    }
}