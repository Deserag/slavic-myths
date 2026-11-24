package org.slavicmyths.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

/**
 * One authoritative, four-action contract for weapons with active abilities.
 * A passive effect does not consume an input slot. An item implements only
 * the actions it supports; unsupported actions do nothing.
 */
public interface WeaponInputHandler {
    enum Action {
        LEFT_CLICK(0),
        RIGHT_CLICK(1),
        SHIFT_LEFT_CLICK(2),
        SHIFT_RIGHT_CLICK(3);

        private final int id;
        Action(int id) { this.id = id; }
        public int id() { return id; }

        public static Action fromId(int id) {
            for (Action action : values()) if (action.id == id) return action;
            return null;
        }
    }

    /**
     * Called exclusively on the logical server with the actual main-hand stack.
     * Validate target, cooldown and any item-specific resource costs here.
     */
    /**
     * Return false for a primary action that must remain vanilla (for example,
     * an ordinary melee hit or a held right-click charge/stream).
     */
    // Opt-in only: implementing the interface alone must never disable
    // vanilla LMB/RMB or consume an unsupported Shift action.
    default boolean intercepts(Action action) { return false; }

    /**
     * Some existing weapons preserve different LMB behavior in air and on a
     * target (e.g. spell in air but ordinary melee on an entity).
     */
    default boolean intercepts(Action action, HitResult.Type target) {
        return intercepts(action);
    }

    /** Stack-sensitive mode selection; older weapons keep their original behavior. */
    default boolean intercepts(ItemStack held, Action action, HitResult.Type target) {
        return intercepts(action, target);
    }

    void onWeaponAction(ServerPlayer player, ItemStack held, Action action);
}
