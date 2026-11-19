package org.slavicmyths.marine;

import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Isolated aquatic-boss GameTests, excluded from the production JAR. */
@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class DeepSeaSerpentGameTests {
    private static void tank(GameTestHelper test) {
        for(int x=1;x<=19;x++)for(int z=1;z<=19;z++){
            test.setBlock(new BlockPos(x,1,z),Blocks.PRISMARINE);
            for(int y=2;y<=8;y++)
                test.setBlock(new BlockPos(x,y,z),
                    x==1||z==1||x==19||z==19?Blocks.PRISMARINE:Blocks.WATER);
        }
    }
    private static DeepSeaSerpentEntity spawn(GameTestHelper test,int x,int y,int z){
        DeepSeaSerpentEntity boss=DeepSeaSerpentContent.BOSS.get().create(test.getLevel());
        test.assertTrue(boss!=null,"Serpent registry missing");
        boss.moveTo(test.absoluteVec(new Vec3(x+.5,y+.25,z+.5)));
        test.assertTrue(test.getLevel().addFreshEntity(boss),"Unable to spawn serpent");
        return boss;
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void registrationBalanceAndReload(GameTestHelper test){
        tank(test);
        DeepSeaSerpentEntity boss=spawn(test,9,4,9);
        test.assertTrue(boss.getMaxHealth()==1000,"Tier VI HP incorrect");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==20,"Tier VI A4 not applied");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR)==9,"Missing armor");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==2,"Missing toughness");
        test.assertTrue(DeepSeaSerpentEntity.VORTEX_DAMAGE==14F,"A3 coefficient incorrect");
        test.assertTrue(DeepSeaSerpentContent.EGG.get().getDefaultInstance().getCount()==1,
            "Spawn egg missing");
        test.assertTrue(DeepSeaSerpentContent.HEAD_ITEM.get().getDefaultInstance().getCount()==1,
            "Mandatory 2x3 skull missing");
        test.assertTrue(DeepSeaSerpentContent.CREST_ITEM.get().getDefaultInstance().getCount()==1,
            "Secondary 1x2 crest missing");
        CompoundTag tag=new CompoundTag();
        boss.addAdditionalSaveData(tag);
        DeepSeaSerpentEntity reloaded=DeepSeaSerpentContent.BOSS.get().create(test.getLevel());
        reloaded.readAdditionalSaveData(tag);
        test.assertTrue(reloaded.action()==DeepSeaSerpentEntity.IDLE,
            "Hold must not persist across reload");
        boss.discard();reloaded.discard();test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=90)
    public static void discoversAquaticPrey(GameTestHelper test){
        tank(test);
        DeepSeaSerpentEntity boss=spawn(test,8,3,8);
        var cod=EntityType.COD.create(test.getLevel());
        test.assertTrue(cod!=null,"Missing fish fixture");
        cod.setNoAi(true);
        cod.moveTo(test.absoluteVec(new Vec3(8.5,3.5,12.5)));
        test.assertTrue(test.getLevel().addFreshEntity(cod),"Could not add prey");
        test.runAfterDelay(40,()->{
            test.assertTrue(boss.getTarget()==cod || !cod.isAlive(),
                "Serpent must detect living aquatic mobs");
            boss.discard();cod.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void headSixCellsAndNoOrphans(GameTestHelper test){
        BlockPos anchor=new BlockPos(8,2,9);
        var head=(DeepSeaSerpentTrophyBlock)DeepSeaSerpentContent.HEAD.get();
        Direction facing=Direction.NORTH;
        var base=head.defaultBlockState().setValue(DeepSeaSerpentTrophyBlock.FACING,facing);
        for(int i=0;i<6;i++)
            test.setBlock(head.at(anchor,facing,i),base.setValue(DeepSeaSerpentTrophyBlock.PART,i));
        test.assertTrue(test.getLevel().getBlockEntity(test.absolutePos(anchor))
            instanceof DeepSeaSerpentTrophyBlock.Tile,"Trophy owner tile missing");
        BlockPos frontRight=head.at(anchor,facing,5);
        test.assertTrue(test.getBlockState(frontRight).is(head),"Head front-right cell missing");
        test.getLevel().destroyBlock(test.absolutePos(frontRight),true);
        test.runAfterDelay(3,()->{
            for(int i=0;i<6;i++)
                test.assertTrue(test.getBlockState(head.at(anchor,facing,i)).isAir(),
                    "Head was not cleaned up across all 2x3 cells at "+i);
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void crestOccupiesBothCells(GameTestHelper test){
        BlockPos anchor=new BlockPos(9,2,9);
        var crest=(DeepSeaSerpentTrophyBlock)DeepSeaSerpentContent.CREST.get();
        Direction facing=Direction.WEST;
        var base=crest.defaultBlockState().setValue(DeepSeaSerpentTrophyBlock.FACING,facing);
        for(int i=0;i<2;i++)
            test.setBlock(crest.at(anchor,facing,i),base.setValue(DeepSeaSerpentTrophyBlock.PART,i));
        test.getLevel().destroyBlock(test.absolutePos(anchor),true);
        test.runAfterDelay(3,()->{
            test.assertTrue(test.getBlockState(anchor).isAir()
                && test.getBlockState(crest.at(anchor,facing,1)).isAir(),
                "Crest left orphaned blocks");
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void damageReleasesBodyGrab(GameTestHelper test){
        tank(test);
        DeepSeaSerpentEntity boss=spawn(test,8,3,8);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        var savior=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null && savior!=null,"Missing grab fixture");
        victim.setNoAi(true);savior.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(8.5,3.2,10.5)));
        savior.moveTo(test.absoluteVec(new Vec3(11.5,3.2,8.5)));
        test.getLevel().addFreshEntity(victim);
        test.getLevel().addFreshEntity(savior);
        try {
            Field field=DeepSeaSerpentEntity.class.getDeclaredField("victimId");
            field.setAccessible(true);
            field.set(boss,victim.getUUID());
            test.assertTrue(boss.hurt(savior.damageSources().mobAttack(savior),40F),
                "Legitimate rescue damage rejected");
            test.assertTrue(field.get(boss)==null,
                "At least 24 HP of real damage must break the coil");
        }catch(ReflectiveOperationException error){
            throw new IllegalStateException("Rescue fixture failed",error);
        }
        boss.discard();victim.discard();savior.discard();test.succeed();
    }
}
