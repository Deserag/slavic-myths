package org.slavicmyths.wolverine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.trophy.TrophyHeadBlock;
import org.slavicmyths.trophy.TrophyHeads;

@GameTestHolder("slavicmyths_wolverine")
@PrefixGameTestTemplate(false)
public final class WolverineGameTests {
    private static NorthernWolverine create(GameTestHelper helper){
        NorthernWolverine beast=WolverineContent.BOSS.get().create(helper.getLevel());
        helper.assertTrue(beast!=null,"Wolverine entity could not be constructed");
        beast.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(8,3,8)));
        helper.getLevel().addFreshEntity(beast);
        return beast;
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void attributes(GameTestHelper helper){
        var beast=create(helper);
        helper.assertTrue(beast.getMaxHealth()==800&&beast.getHealth()==800,"Wolverine health must be 800");
        helper.assertTrue(beast.getArmorValue()==10,"Wolverine armor must be 10");
        helper.assertTrue(beast.getAttributeValue(Attributes.ATTACK_DAMAGE)==12,"Wolverine base damage mismatch");
        beast.discard();helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void firstPhase(GameTestHelper helper){
        var beast=create(helper);
        beast.setHealth(401);beast.tick();
        helper.assertTrue(!beast.phaseTwo(),"Second phase started too early");
        beast.discard();helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void secondPhase(GameTestHelper helper){
        var beast=create(helper);
        beast.setHealth(400);beast.tick();
        helper.assertTrue(beast.phaseTwo(),"Second phase did not activate at 50 percent");
        beast.discard();helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void saveAndRestore(GameTestHelper helper){
        var beast=create(helper);
        beast.setHealth(290);beast.tick();
        var data=new CompoundTag();beast.saveWithoutId(data);
        var restored=WolverineContent.BOSS.get().create(helper.getLevel());
        restored.load(data);
        helper.assertTrue(restored.phaseTwo()&&Math.abs(restored.getHealth()-290)<.01,"Saved phase or health was lost");
        restored.discard();beast.discard();helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void spawnEgg(GameTestHelper helper){
        var egg=WolverineContent.EGG.get();
        helper.assertTrue(egg instanceof SpawnEggItem,"Wolverine spawn egg missing");
        helper.assertTrue(((SpawnEggItem)egg).getType(new ItemStack(egg))==WolverineContent.BOSS.get(),"Spawn egg entity mismatch");
        helper.assertTrue(WolverineContent.BOSS.get().getCategory()==MobCategory.MONSTER,"Wolverine must be hostile");
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void trophy(GameTestHelper helper){
        var item=TrophyHeads.ITEMS.get(TrophyHeads.Kind.NORTH_WOLVERINE).get();
        var block=TrophyHeads.BLOCKS.get(TrophyHeads.Kind.NORTH_WOLVERINE).get();
        helper.assertTrue(item instanceof BlockItem,"Head must be placeable");
        helper.assertTrue(((BlockItem)item).getBlock()==block&&block instanceof TrophyHeadBlock,"Trophy head block mismatch");
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void dimensions(GameTestHelper helper){
        var beast=create(helper);
        helper.assertTrue(Math.abs(beast.getBbHeight()-2.8F)<.01,"Wolverine height mismatch");
        helper.assertTrue(beast.getBoundingBox().getXsize()>=2.3||beast.getBoundingBox().getZsize()>=2.3,"Wolverine hitbox too small");
        beast.discard();helper.succeed();
    }
}
