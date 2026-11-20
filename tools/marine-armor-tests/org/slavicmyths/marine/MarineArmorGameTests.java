package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.rpg.Runes;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineArmorGameTests {
    @GameTest(template="empty", timeoutTicks=50)
    public static void registrationDefenseAndFourPieces(GameTestHelper t) {
        var stand = EntityType.ARMOR_STAND.create(t.getLevel());
        t.assertTrue(stand != null, "Armor fixture missing");
        t.assertTrue(MarineArmor.HELMET.get() instanceof ArmorItem, "Helmet is not vanilla armor");
        t.assertTrue(MarineArmor.CHESTPLATE.get() instanceof ArmorItem, "Chest is not vanilla armor");
        t.assertTrue(MarineArmor.LEGGINGS.get() instanceof ArmorItem, "Leggings are not vanilla armor");
        t.assertTrue(MarineArmor.BOOTS.get() instanceof ArmorItem, "Boots are not vanilla armor");
        var stats = MarineArmor.MATERIAL.get().defense();
        t.assertTrue(stats.get(ArmorItem.Type.HELMET) == 3
            && stats.get(ArmorItem.Type.CHESTPLATE) == 8
            && stats.get(ArmorItem.Type.LEGGINGS) == 6
            && stats.get(ArmorItem.Type.BOOTS) == 3, "Armor points not 20");
        t.assertTrue(Runes.capacity(new ItemStack(MarineArmor.HELMET.get())) == 2,
            "Expected two ordinary rune slots");
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(MarineArmor.HELMET.get()));
        stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(MarineArmor.CHESTPLATE.get()));
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(MarineArmor.LEGGINGS.get()));
        t.assertTrue(!MarineArmor.full(stand), "Three pieces must not enable the suit");
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(MarineArmor.BOOTS.get()));
        t.assertTrue(MarineArmor.full(stand), "All four pieces must enable the suit");
        t.succeed();
    }
    @GameTest(template="empty", timeoutTicks=50)
    public static void waterSupportNotOnLandOrShift(GameTestHelper t) {
        t.assertTrue(MarineArmorEffects.canStand(true,false,false,false), "Support predicate failed");
        t.assertTrue(!MarineArmorEffects.canStand(true,true,false,false), "Shift must allow diving");
        t.assertTrue(!MarineArmorEffects.canStand(false,false,false,false), "3/4 set may not support");
        t.assertTrue(!MarineArmorEffects.canStand(true,false,true,false), "Swimming may not stand");
        t.assertTrue(!MarineArmorEffects.canStand(true,false,false,true), "Submerged may not stand");
        BlockPos pos=t.absolutePos(new BlockPos(3,1,3));
        t.assertTrue(Double.isNaN(MarineArmorEffects.surfaceHeight(t.getLevel(),
            pos.getX()+.5D,pos.getY()+.9D,pos.getZ()+.5D)), "Support in air");
        t.setBlock(new BlockPos(3,1,3), Blocks.WATER);
        t.assertTrue(Double.isFinite(MarineArmorEffects.surfaceHeight(t.getLevel(),
            pos.getX()+.5D,pos.getY()+.9D,pos.getZ()+.5D)), "Source water not detected");
        t.succeed();
    }
}
