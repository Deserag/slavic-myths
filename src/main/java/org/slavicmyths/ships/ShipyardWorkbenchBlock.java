package org.slavicmyths.ships;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Real shipbuilding table: conventional crafting view, plus server-side upgrades
 * while a packed raft is in hand. The vessel identity and cargo stay intact.
 */
public final class ShipyardWorkbenchBlock extends CraftingTableBlock {
    private static final MapCodec<ShipyardWorkbenchBlock> CODEC = simpleCodec(ShipyardWorkbenchBlock::new);

    public ShipyardWorkbenchBlock() { super(Properties.ofFullCopy(Blocks.CRAFTING_TABLE).strength(3F)); }
    private ShipyardWorkbenchBlock(Properties props) { super(props); }
    @Override protected MapCodec<? extends CraftingTableBlock> codec() { return CODEC; }

    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(held.getItem() instanceof RaftItem item)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        RaftSize next = item.size().next();
        if (next == null) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("Большой плот уже достиг предела расширений"), true);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        int required = switch (next) {
            case LONG -> 6;
            case WIDE -> 10;
            case CARGO -> 10;
            default -> 0;
        };
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!player.getAbilities().instabuild && player.getInventory().countItem(RaftContent.DECK_SECTION.get()) < required) {
            player.displayClientMessage(Component.literal("Для улучшения нужно палубных секций: " + required), true);
            return ItemInteractionResult.FAIL;
        }
        CompoundTag root = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag packed = root.contains("Raft2511Pack", Tag.TAG_COMPOUND)
                ? root.getCompound("Raft2511Pack").copy() : new CompoundTag();
        if (packed.contains("Raft2511Modules", Tag.TAG_LIST) && packed.getList("Raft2511Modules", Tag.TAG_COMPOUND).size() > 35)
            return ItemInteractionResult.FAIL;
        int oldWidth = item.size().width;
        ListTag modules = packed.getList("Raft2511Modules", Tag.TAG_COMPOUND);
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag module = modules.getCompound(i);
            int tile = module.getInt("Tile");
            int x = tile % oldWidth, z = tile / oldWidth;
            if (x < 0 || x >= oldWidth || z < 0 || z >= item.size().length) return ItemInteractionResult.FAIL;
            module.putInt("Tile", z * next.width + x);
        }
        packed.putInt("Version", 2);
        packed.putInt("RaftWidth", next.width);
        packed.putInt("RaftLength", next.length);
        packed.put("Raft2511Modules", modules);
        root.put("Raft2511Pack", packed);
        ItemStack upgraded = new ItemStack(RaftContent.raftItem(next));
        upgraded.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        // All validation happens before any inventory mutation. Never duplicate the source vessel.
        if (!player.getAbilities().instabuild) {
            int remaining = required;
            for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                ItemStack inSlot = player.getInventory().getItem(slot);
                if (inSlot.is(RaftContent.DECK_SECTION.get())) {
                    int amount = Math.min(remaining, inSlot.getCount());
                    inSlot.shrink(amount);
                    remaining -= amount;
                }
            }
        }
        player.setItemInHand(hand, upgraded);
        player.displayClientMessage(Component.literal("Судно расширено: " + next.width + "×" + next.length + " — груз сохранён"), true);
        return ItemInteractionResult.CONSUME;
    }
}
