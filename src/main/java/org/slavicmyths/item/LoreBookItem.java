package org.slavicmyths.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.slavicmyths.progression.Knowledge;
import org.slavicmyths.network.LoreNetwork;

public final class LoreBookItem extends Item {
    public LoreBookItem(Properties properties) { super(properties); }
    @Override public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            Knowledge.award(player, "lore_book");
            LoreNetwork.open((ServerPlayerEntity) player, Knowledge.mask((ServerPlayerEntity) player));
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }
}
