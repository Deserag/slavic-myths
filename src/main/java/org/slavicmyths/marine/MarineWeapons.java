package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.rpg.Abilities;

/** Vanilla bobber and trident mechanics, bounded server-authoritative sea effects. */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineWeapons {
    public static final DeferredHolder<Item, Item> ANCHOR =
        ModItems.ITEMS.register("storm_anchor", Anchor::new);
    public static final DeferredHolder<Item, Item> STAFF =
        ModItems.ITEMS.register("abyss_staff", Staff::new);
    public static final DeferredHolder<Item, Item> HARPOON =
        ModItems.ITEMS.register("storm_harpoon", Harpoon::new);
    private static final String HARPOON_READY = "SlavicStormHarpoonReady";
    public static void init() { MarineHarpoons.init(); }

    private static void description(ItemStack stack, List<Component> lines) {
        lines.add(Component.translatable(stack.getDescriptionId() + ".desc")
            .withStyle(ChatFormatting.AQUA));
    }

    private static final class Anchor extends FishingRodItem {
        private Anchor() { super(new Item.Properties().durability(400).rarity(Rarity.RARE)); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            boolean retrieving = player.fishing != null;
            if (retrieving && player instanceof ServerPlayer owner) {
                Entity caught = player.fishing.getHookedIn();
                if (caught instanceof LivingEntity target && target.isAlive()
                        && target.isInWaterOrBubble() && Abilities.canHit(owner, target)) {
                    Vec3 toward = owner.position().subtract(target.position());
                    if (toward.lengthSqr() > .01) {
                        Vec3 impulse = toward.normalize().scale(.42);
                        target.push(impulse.x, .13, impulse.z);
                        target.hurt(owner.damageSources().playerAttack(owner), 2F);
                    }
                }
            }
            InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
            if (retrieving && !level.isClientSide) player.getCooldowns().addCooldown(this, 80);
            return result;
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            description(stack, lines);
        }
    }

    private static final class Staff extends Item implements WeaponInputHandler {
        private Staff() { super(new Item.Properties().durability(320).rarity(Rarity.RARE)); }
        // Keep vanilla left-click melee; only the other three combinations
        // are routed as marine staff abilities.
        @Override public boolean intercepts(Action action) {
            return action != Action.LEFT_CLICK;
        }
        @Override public void onWeaponAction(ServerPlayer player, ItemStack held, Action action) {
            if (held != player.getMainHandItem() || !player.isAlive() || player.isSpectator()) return;
            switch (action) {
                case RIGHT_CLICK -> MarineWeaponAbilities.staffShot(player);
                case SHIFT_LEFT_CLICK -> MarineWeaponAbilities.bubble(player);
                case SHIFT_RIGHT_CLICK -> MarineWeaponAbilities.staffVortex(player);
                case LEFT_CLICK -> {}
            }
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            description(stack, lines);
            lines.add(Component.literal("ЛКМ — удар  •  ПКМ — снаряд  •  Shift+ЛКМ — пузырь  •  Shift+ПКМ — вихрь")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static final class Harpoon extends MarineHarpoons.Ammo {
        private Harpoon() { super(); }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            description(stack, lines);
        }
    }

    @SubscribeEvent public static void harpoonHit(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0 || !event.getEntity().isAlive()) return;
        Entity direct = event.getSource().getDirectEntity();
        ServerPlayer owner;
        if (direct instanceof ThrownTrident thrown
                && thrown.getOwner() instanceof ServerPlayer serverPlayer
                && thrown.getWeaponItem() != null && thrown.getWeaponItem().is(HARPOON.get())) {
            owner = serverPlayer;
        } else if (direct instanceof ServerPlayer serverPlayer
                && serverPlayer.getMainHandItem().is(HARPOON.get())
                && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) {
            owner = serverPlayer;
        } else return;
        LivingEntity target = event.getEntity();
        if (!Abilities.canHit(owner, target)) return;
        if (target.isInWaterOrBubble()) event.setNewDamage(event.getNewDamage() + 3F);
        long now = owner.serverLevel().getGameTime();
        if (now >= owner.getPersistentData().getLong(HARPOON_READY)) {
            owner.getPersistentData().putLong(HARPOON_READY, now + 200);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0));
            owner.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getEyeY(), target.getZ(), 16, .35, .4, .35, .05);
        }
    }
    private MarineWeapons() {}
}
