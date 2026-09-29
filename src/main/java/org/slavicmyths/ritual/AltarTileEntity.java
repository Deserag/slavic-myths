package org.slavicmyths.ritual;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.progression.Knowledge;
import org.slavicmyths.registry.ModTiles;

/** Non-ticking, shared altar. Ingredients are committed as each step is accepted. */
public final class AltarTileEntity extends TileEntity {
    private int step;
    private int rite;
    public AltarTileEntity() { super(ModTiles.ALTAR.get()); }
    public int getStep() { return step; }
    public void offer(PlayerEntity player, Hand hand) {
        if (!(level instanceof ServerWorld)) return;
        Knowledge.award(player, "altar");
        ItemStack held = player.getItemInHand(hand);
        if (step == 0) {
            rite = Rituals.FIRST;
            for (int candidate = 1; candidate < Rituals.COUNT; candidate++) {
                if (held.getItem() == Rituals.ingredient(candidate, 0)) { rite = candidate; break; }
            }
        }
        if (held.getItem() != Rituals.ingredient(rite, step)) {
            player.displayClientMessage(new TranslationTextComponent("ritual.slavicmyths.next", new ItemStack(Rituals.ingredient(rite, step)).getHoverName()), true);
            return;
        }
        if (!player.abilities.instabuild) held.shrink(1);
        step++;
        boolean complete = step == Rituals.length(rite);
        if (complete) {
            step = 0;
            ItemStack result = Rituals.result(rite);
            if (!player.inventory.add(result)) player.drop(result, false);
            if (rite == Rituals.FIRST) Knowledge.award(player, "first_ritual");
        }
        setChanged();
        level.playSound(null, worldPosition, complete ? SoundEvents.ENCHANTMENT_TABLE_USE : SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundCategory.BLOCKS, 0.6F, 1.0F);
        ((ServerWorld) level).sendParticles(complete ? ParticleTypes.ENCHANT : ParticleTypes.HAPPY_VILLAGER,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5,
                complete ? 20 : 5, 0.3, 0.2, 0.3, 0.02);
        player.displayClientMessage(complete ? new TranslationTextComponent("ritual.slavicmyths.complete_item", Rituals.result(rite).getHoverName()) :
                new TranslationTextComponent("ritual.slavicmyths.accepted", new ItemStack(Rituals.ingredient(rite, step)).getHoverName()), true);
    }
    @Override public CompoundNBT save(CompoundNBT tag) { super.save(tag); tag.putInt("RitualStep", step); tag.putInt("RitualKind", rite); return tag; }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        rite = Math.max(0, Math.min(Rituals.COUNT - 1, tag.getInt("RitualKind")));
        step = Math.max(0, Math.min(Rituals.length(rite) - 1, tag.getInt("RitualStep")));
    }
}
