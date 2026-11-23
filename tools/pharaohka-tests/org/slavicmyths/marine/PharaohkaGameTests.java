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
    @GameTest(template="empty",timeoutTicks=20)
    public static void aquaticBossesAcceptWaterButRejectStone(GameTestHelper test) {
        var world=test.getLevel();
        var types=java.util.List.of(DeepSeaSerpentContent.BOSS.get(),OceanZmeyContent.BOSS.get(),
            TsarPikeContent.BOSS.get(),StratimBirdContent.BOSS.get(),PharaohkaContent.LEADER.get());
        for(var type:types) {
            var mob=type.create(world);
            test.assertTrue(mob!=null,"Missing aquatic boss");
            var center=test.absolutePos(new BlockPos(10,40,10));
            mob.setPos(center.getX()+.5,center.getY(),center.getZ()+.5);
            var box=mob.getBoundingBox();
            var min=BlockPos.containing(box.minX-1,box.minY-1,box.minZ-1);
            var max=BlockPos.containing(box.maxX+1,box.maxY+1,box.maxZ+1);
            for(var pos:BlockPos.betweenClosed(min,max)) world.setBlock(pos,Blocks.WATER.defaultBlockState(),2);
            test.assertTrue(mob.checkSpawnObstruction(world),type+" rejects unobstructed water");
            world.setBlock(center,Blocks.STONE.defaultBlockState(),2);
            test.assertTrue(!mob.checkSpawnObstruction(world),type+" accepts solid obstruction");
            for(var pos:BlockPos.betweenClosed(min,max)) world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        }
        test.succeed();
    }
    private static void tank(GameTestHelper test) {
        for (int x=1;x<=19;x++) for (int z=1;z<=19;z++) {
            test.setBlock(new BlockPos(x,1,z),Blocks.PRISMARINE);
            for (int y=2;y<=8;y++) {
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
        test.assertTrue(Math.abs(boss.getBbHeight()-3.8F)<0.001F,"Boss must be 3.8 blocks tall");
        test.assertTrue(Math.abs(guard.getBbHeight()-2.15F)<0.001F,"Regular Pharaohka size");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==18.0,"Tier V basic attack");
        test.assertTrue(boss.getAttributeValue(Attributes.FOLLOW_RANGE)==40.0,"Boss pursuit range");
        test.assertTrue(PharaohkaEntity.DASH_DAMAGE>20F
            && PharaohkaEntity.SHIELD_BASH_DAMAGE>24F,"Boss special attacks use tier V multipliers");
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


    @GameTest(template="empty",batch="pharaohka_shipwreck_isolation",timeoutTicks=90)
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
                var area=new net.minecraft.world.phys.AABB(first.getX(),first.getY(),first.getZ(),last.getX()+1,last.getY()+1,last.getZ()+1).inflate(18);
                var found=world.getEntitiesOfClass(PharaohkaEntity.class,area,PharaohkaEntity::leader);
                test.assertTrue(found.size()==1,
                    "Shipwreck leaders="+found.size()
                    +" pending="+pending.containsKey(key)
                    +" pendingTotal="+pending.size()
                    +" marineBossesNearby="+world.getEntitiesOfClass(
                        net.minecraft.world.entity.Mob.class,
                        new net.minecraft.world.phys.AABB(box.getCenter()).inflate(512),
                        MarineEncounterSpawns::isMarineBoss).size()
                    +" centerChunkLoaded="+(world.getChunkSource().getChunkNow(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4)!=null));
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


    @GameTest(template="empty",timeoutTicks=135)
    public static void movingMeleeKeepsSwimming(GameTestHelper test) {
        tank(test);
        var attacker=spawn(test,true,5,3,8);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Missing swimming strike target");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        target.setHealth(500);
        target.moveTo(test.absoluteVec(new Vec3(5.5,3.2,12.5)));
        test.getLevel().addFreshEntity(target);
        attacker.setTarget(target);
        var tag=new CompoundTag();
        attacker.addAdditionalSaveData(tag);
        tag.putInt("DashCooldown",300);
        tag.putInt("SummonCooldown",300);
        tag.putInt("ShieldCooldown",300);
        tag.putInt("CurrentCooldown",300);
        attacker.readAdditionalSaveData(tag);
        final Vec3[] previous={attacker.position()};
        final boolean[] movedDuringStrike={false};
        final int[] strikeFrames={0};
        for(int tick=1;tick<=95;tick++){
            test.runAtTickTime(tick,()->{
                if(attacker.action()==PharaohkaEntity.STRIKE) {
                    strikeFrames[0]++;
                    if(attacker.position().distanceToSqr(previous[0])>.000001)
                        movedDuringStrike[0]=true;
                }
                previous[0]=attacker.position();
            });
        }
        test.runAfterDelay(100,()->{
            test.assertTrue(strikeFrames[0]>0,"No melee strike started");
            test.assertTrue(movedDuringStrike[0],"Boss froze throughout underwater spear sweep");
            test.assertTrue(target.getHealth()<500,"Moving spear missed stationary target");
            attacker.discard();target.discard();test.succeed();
        });
    }

    @GameTest(template="empty",timeoutTicks=130)
    public static void summonedGuardInheritsAggroAndMoves(GameTestHelper test) {
        tank(test);
        var leader=spawn(test,true,6,3,9);
        var guard=spawn(test,false,7,3,9);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Target for summoned guard missing");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300);
        target.setHealth(300);
        target.moveTo(test.absoluteVec(new Vec3(7.5,3.2,14.5)));
        test.assertTrue(test.getLevel().addFreshEntity(target),"Could not create combat target");
        leader.setTarget(target);
        var data=new CompoundTag();
        guard.addAdditionalSaveData(data);
        data.putUUID("Summoner",leader.getUUID());
        data.putInt("MinionLife",400);
        guard.readAdditionalSaveData(data);
        guard.setTarget(null);
        double startingDistance=guard.distanceToSqr(target);
        test.runAfterDelay(27,()->{
            test.assertTrue(guard.getTarget()==target,
                "Summoned Pharaohka did not inherit the boss target");
            test.assertTrue(guard.distanceToSqr(target)<startingDistance,
                "Summoned Pharaohka failed to pursue the target in water");
        });
        test.runAfterDelay(110,()->{
            test.assertTrue(target.getHealth()<300,"Summoned Pharaohka did not attack");
            guard.discard();leader.discard();target.discard();test.succeed();
        });
    }

    @GameTest(template="empty",timeoutTicks=115)
    public static void repeatedCloseRangeBossAttacks(GameTestHelper test) {
        tank(test);
        var boss=spawn(test,true,8,3,8);
        var target=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(target!=null,"Boss melee victim missing");
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        target.setHealth(500);
        target.moveTo(test.absoluteVec(new Vec3(8.5,3.2,10.5)));
        test.assertTrue(test.getLevel().addFreshEntity(target),"Missing boss test target");
        var data=new CompoundTag();
        boss.addAdditionalSaveData(data);
        data.putInt("DashCooldown",250);
        data.putInt("SummonCooldown",250);
        data.putInt("CurrentCooldown",250);
        data.putInt("ShieldCooldown",250);
        boss.readAdditionalSaveData(data);
        boss.setTarget(target);
        test.runAfterDelay(94,()->{
            test.assertTrue(target.getHealth()<=500-PharaohkaEntity.BOSS_BASE_DAMAGE*2,
                "Boss must land multiple basic attacks instead of waiting idle");
            boss.discard();target.discard();test.succeed();
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
        test.assertTrue(spawns==2048,"Shipwreck missing its resident encounter: "+spawns);
        test.succeed();
    }
}
