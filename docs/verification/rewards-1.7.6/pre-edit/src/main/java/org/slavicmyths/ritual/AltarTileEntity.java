package org.slavicmyths.ritual;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.progression.Knowledge;
import org.slavicmyths.registry.ModTiles;

/** Non-ticking, shared altar. Ingredients are committed as each step is accepted. */
public final class AltarTileEntity extends BlockEntity {
    private int step;
    private int rite;
    public AltarTileEntity(net.minecraft.core.BlockPos pos,BlockState state){super(ModTiles.ALTAR.get(),pos,state);}
    public int getStep() { return step; }
    public void offer(Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel)) return;
        Knowledge.award(player, "altar");
        ItemStack held = player.getItemInHand(hand);
        if (step == 0) {
            rite = Rituals.FIRST;
            for (int candidate = 1; candidate < Rituals.COUNT; candidate++) {
                if (held.getItem() == Rituals.ingredient(candidate, 0)) { rite = candidate; break; }
            }
        }
        if (held.getItem() != Rituals.ingredient(rite, step)) {
            player.displayClientMessage(Component.translatable("ritual.slavicmyths.next", new ItemStack(Rituals.ingredient(rite, step)).getHoverName()), true);
            return;
        }
        if (!player.getAbilities().instabuild) held.shrink(1);
        step++;
        boolean complete = step == Rituals.length(rite);
        if (complete) {
            step = 0;
            ItemStack result = Rituals.result(rite);
            if (!player.getInventory().add(result)) player.drop(result, false);
            if (rite == Rituals.FIRST) Knowledge.award(player, "first_ritual");
        }
        setChanged();
        level.playSound(null, worldPosition, complete ? SoundEvents.ENCHANTMENT_TABLE_USE : SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS, 0.6F, 1.0F);
        ((ServerLevel) level).sendParticles(complete ? ParticleTypes.ENCHANT : ParticleTypes.HAPPY_VILLAGER,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                complete ? 20 : 5, 0.3, 0.2, 0.3, 0.02);
        player.displayClientMessage(complete ? Component.translatable("ritual.slavicmyths.complete_item", Rituals.result(rite).getHoverName()) :
                Component.translatable("ritual.slavicmyths.accepted", new ItemStack(Rituals.ingredient(rite, step)).getHoverName()), true);
    }
    @Override protected void saveAdditional(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries) { super.saveAdditional(tag,registries); tag.putInt("RitualStep", step); tag.putInt("RitualKind", rite); }
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rite = Math.max(0, Math.min(Rituals.COUNT - 1, tag.getInt("RitualKind")));
        step = Math.max(0, Math.min(Rituals.length(rite) - 1, tag.getInt("RitualStep")));
    }
}
