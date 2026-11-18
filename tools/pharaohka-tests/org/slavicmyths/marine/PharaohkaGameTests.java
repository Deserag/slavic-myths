package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Dedicated isolated GameTests; compiled only with -PpharaohkaCheck. */
@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class PharaohkaGameTests {
    private static void tank(GameTestHelper test) {
        for (int x=1;x<=19;x++) for (int z=1;z<=19;z++) {
            test.setBlock(new BlockPos(x,1,z),Blocks.PRISMARINE);
            for (int y=2;y<=5;y++) {
                boolean wall=x==1||z==1||x==19||z==19;
                test.setBlock(new BlockPos(x,y,z),wall?Blocks.PRISMARINE:Blocks.WATER);
            }
        }
    }
    private static PharaohkaEntity spawn(GameTestHelper test, boolean leader, int x,int y,int z) {
        var entity=(leader?PharaohkaContent.LEADER:PharaohkaContent.PHARAOHKA).get().create(test.getLevel());
        test.assertTrue(entity!=null,"Pharaohka entity not registered");
        entity.moveTo(test.absoluteVec(new Vec3(x+.5,y+.2,z+.5)));
        test.assertTrue(test.getLevel().addFreshEntity(entity),"Unable to spawn Pharaohka");
        return entity;
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void registryAndHealth(GameTestHelper test) {
        tank(test);
        var boss=spawn(test,true,8,3,8);
        var guard=spawn(test,false,11,3,8);
        test.assertTrue(boss.getMaxHealth()==550&&boss.getHealth()==550,"Leader must have 550 HP");
        test.assertTrue(guard.getMaxHealth()==32,"Ordinary Pharaohka health");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR)==12,"Leader armor missing");
        test.assertTrue(boss.inHabitat()&&guard.inHabitat(),"Entities cannot inhabit water");
        test.assertTrue(PharaohkaContent.EGG.get()!=PharaohkaContent.LEADER_EGG.get(),"Spawn eggs collided");
        test.assertTrue(PharaohkaContent.EGG.get().getDefaultInstance().getCount()==1,"Missing regular spawn egg");
        var nbt=new CompoundTag();
        boss.addAdditionalSaveData(nbt);
        var restored=PharaohkaContent.LEADER.get().create(test.getLevel());
        restored.readAdditionalSaveData(nbt);
        test.assertTrue(restored.getMaxHealth()==550,"Leader NBT read corrupted attributes");
        boss.discard();guard.discard();restored.discard();
        test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=165)
    public static void dryLandAndSurface(GameTestHelper test) {
        for(int x=3;x<15;x++)for(int z=3;z<15;z++)test.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var stranded=spawn(test,false,6,2,6);
        test.setBlock(new BlockPos(12,2,12),Blocks.WATER);
        var surface=spawn(test,true,12,3,12);
        test.assertTrue(surface.inHabitat(),"Water surface must count as habitat");
        test.runAfterDelay(35,()->test.assertTrue(stranded.isAlive(),"Dry land grace period absent"));
        test.runAfterDelay(132,()->{
            test.assertTrue(!stranded.isAlive(),"Pharaohka survived too long on dry ground");
            stranded.discard();surface.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=130)
    public static void synchronizedMeleeDamage(GameTestHelper test) {
        tank(test);
        var leader=spawn(test,true,8,3,8);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Missing zombie test target");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300);
        target.setHealth(300);
        target.moveTo(test.absoluteVec(new Vec3(8.5,3.2,10.5)));
        test.getLevel().addFreshEntity(target);
        leader.setTarget(target);
        test.runAfterDelay(5,()->test.assertTrue(target.getHealth()==300,"Impact occurred before animation windup"));
        test.runAfterDelay(85,()->{
            test.assertTrue(target.getHealth()<300,"Spear attack did not land in water");
            leader.discard();target.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void summoningLimitAndCooldown(GameTestHelper test) {
        tank(test);
        var boss=spawn(test,true,8,3,8);
        var dummy=EntityType.ZOMBIE.create(test.getLevel());
        dummy.setNoAi(true);
        dummy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(800);
        dummy.setHealth(800);
        dummy.moveTo(test.absoluteVec(new Vec3(8.5,3.2,13.5)));
        test.getLevel().addFreshEntity(dummy);
        boss.setTarget(dummy);
        var tag=new CompoundTag();
        boss.addAdditionalSaveData(tag);
        tag.putInt("SummonCooldown",1);
        tag.putInt("DashCooldown",200);
        tag.putInt("CurrentCooldown",200);
        boss.readAdditionalSaveData(tag);
        test.runAfterDelay(38,()->{
            var minions=test.getLevel().getEntitiesOfClass(PharaohkaEntity.class,boss.getBoundingBox().inflate(22),x->!x.leader());
            test.assertTrue(minions.size()>=1&&minions.size()<=4,"Summon count outside bounds");
            minions.forEach(PharaohkaEntity::discard);
            boss.discard();dummy.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=20)
    public static void shipwreckChance(GameTestHelper test) {
        int spawns=0;
        for(long i=0;i<2048;i++){
            boolean a=PharaohkaShipwreckSpawns.shouldSpawn(823347L,i);
            test.assertTrue(a==PharaohkaShipwreckSpawns.shouldSpawn(823347L,i),"Shipwreck chance not stable");
            if(a) spawns++;
        }
        test.assertTrue(spawns>850&&spawns<1200,"Shipwreck chance deviates from 50%: "+spawns);
        test.succeed();
    }
}
