package org.slavicmyths.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.slavicmyths.progression.Knowledge;
import org.slavicmyths.network.LoreNetwork;

public final class LoreBookItem extends Item {
    public LoreBookItem(Properties properties) { super(properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer) {
            Knowledge.award(player, "lore_book");
            LoreNetwork.open((ServerPlayer) player, Knowledge.mask((ServerPlayer) player));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }
}
