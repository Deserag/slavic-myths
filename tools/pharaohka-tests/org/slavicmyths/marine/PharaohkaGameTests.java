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
            boss.discard();
        });
        test.runAfterDelay(101,()->{
            var minions=test.getLevel().getEntitiesOfClass(PharaohkaEntity.class,boss.getBoundingBox().inflate(22),x->!x.leader());
            test.assertTrue(minions.isEmpty(),"Summoned minions survived their commander");
            dummy.discard();test.succeed();
        });
    }

    @GameTest(template="empty",timeoutTicks=115)
    public static void shieldBashDamagesAfterWindup(GameTestHelper test) {
        tank(test);
        var boss=spawn(test,true,8,3,8);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Bash victim missing");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300);
        target.setHealth(300);
        target.moveTo(test.absoluteVec(new Vec3(8.5,3.2,10.5)));
        test.getLevel().addFreshEntity(target);
        boss.setTarget(target);
        var data=new CompoundTag();
        boss.addAdditionalSaveData(data);
        data.putInt("ShieldCooldown",1);
        data.putInt("MeleeCooldown",300);
        data.putInt("DashCooldown",300);
        data.putInt("SummonCooldown",300);
        data.putInt("CurrentCooldown",300);
        boss.readAdditionalSaveData(data);
        test.runAfterDelay(4,()->test.assertTrue(target.getHealth()==300,"Shield damaged before windup"));
        test.runAfterDelay(35,()->{
            test.assertTrue(target.getHealth()<300,"Shield bash did no physical damage");
            boss.discard();target.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=120)
    public static void swimmingDashApproachesTarget(GameTestHelper test) {
        tank(test);
        var boss=spawn(test,true,5,3,6);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Dash victim missing");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300);
        target.setHealth(300);
        target.moveTo(test.absoluteVec(new Vec3(5.5,3.2,13.5)));
        test.getLevel().addFreshEntity(target);
        boss.setTarget(target);
        var data=new CompoundTag();
        boss.addAdditionalSaveData(data);
        data.putInt("DashCooldown",1);
        data.putInt("ShieldCooldown",300);
        data.putInt("MeleeCooldown",300);
        data.putInt("SummonCooldown",300);
        data.putInt("CurrentCooldown",300);
        boss.readAdditionalSaveData(data);
        double start=boss.distanceToSqr(target);
        test.runAfterDelay(5,()->test.assertTrue(boss.action()==PharaohkaEntity.DASH,"Dash not entered"));
        test.runAfterDelay(60,()->{
            test.assertTrue(boss.distanceToSqr(target)<start || target.getHealth()<300,
                "Dash did not approach or hit its underwater target");
            boss.discard();target.discard();test.succeed();
        });
    }


    @GameTest(template="empty",timeoutTicks=90)
    @SuppressWarnings("unchecked")
    public static void shipwreckCreatesExactlyOneLeader(GameTestHelper test) {
        tank(test);
        test.runAfterDelay(5,()->{
            var world=test.getLevel();
            var first=test.absolutePos(new BlockPos(7,2,7));
            var last=test.absolutePos(new BlockPos(10,4,10));
            var box=net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(first,last);
            var saved=PharaohkaShipwreckSpawns.get(world);
            try {
                var pendingField=PharaohkaShipwreckSpawns.class.getDeclaredField("pending");
                pendingField.setAccessible(true);
                var pending=(java.util.Map<Long,net.minecraft.world.level.levelgen.structure.BoundingBox>)
                    pendingField.get(saved);
                long key=first.asLong();
                pending.put(key,box);
                saved.setDirty();
                var process=PharaohkaShipwreckSpawns.class.getDeclaredMethod("process",
                    net.minecraft.server.level.ServerLevel.class);
                process.setAccessible(true);
                process.invoke(saved,world);
                var area=new net.minecraft.world.phys.AABB(first,last).inflate(18);
                var found=world.getEntitiesOfClass(PharaohkaEntity.class,area,PharaohkaEntity::leader);
                test.assertTrue(found.size()==1,"Shipwreck did not produce exactly one leader");
                test.assertTrue(!pending.containsKey(key),"Spawned shipwreck remained pending");
                found.getFirst().discard();
                process.invoke(saved,world);
                var repeated=world.getEntitiesOfClass(PharaohkaEntity.class,area,PharaohkaEntity::leader);
                test.assertTrue(repeated.isEmpty(),"Leader respawned after encounter consumed");
                test.succeed();
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Shipwreck server encounter fixture failed",ex);
            }
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
