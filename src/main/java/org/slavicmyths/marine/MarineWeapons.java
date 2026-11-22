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

    private static final class Staff extends Item {
        private Staff() { super(new Item.Properties().durability(320).rarity(Rarity.RARE)); }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            if (player instanceof ServerPlayer owner) {
                ServerLevel world = owner.serverLevel();
                Vec3 eye = owner.getEyePosition();
                Vec3 direction = owner.getLookAngle();
                int hits = 0;
                for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class,
                        new AABB(eye.subtract(5, 5, 5), eye.add(5, 5, 5)),
                        target -> target != owner && target.isAlive() && Abilities.canHit(owner, target))) {
                    Vec3 offset = target.getEyePosition().subtract(eye);
                    if (offset.lengthSqr() > 25 || offset.lengthSqr() < .01
                            || offset.normalize().dot(direction) < .55
                            || !owner.hasLineOfSight(target)) continue;
                    if (target.hurt(owner.damageSources().magic(), 4F)) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                        if (++hits >= 4) break;
                    }
                }
                Vec3 splash = eye.add(direction.scale(3));
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    splash.x, splash.y, splash.z, 25, .7, .7, .7, .04);
                stack.hurtAndBreak(1, owner, LivingEntity.getSlotForHand(hand));
                owner.getCooldowns().addCooldown(this, 200);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            description(stack, lines);
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
