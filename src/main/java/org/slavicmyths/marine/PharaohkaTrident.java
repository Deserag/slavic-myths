package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.Abilities;

/** Vanilla trident projectile carries the original ItemStack's data components. */
@EventBusSubscriber(modid = "slavicmyths")
public final class PharaohkaTrident extends TridentItem {
    public static final DeferredHolder<Item, Item> ITEM = ModItems.ITEMS.register(
        "pharaohka_trident", () -> new PharaohkaTrident());
    static final int COOLDOWN = 240;
    static final double RADIUS = 3.5D;
    private static final String READY = "SlavicPharaohkaTidalRiftReady";
    private static final String SPENT = "SlavicPharaohkaTidalRiftSpent";

    private PharaohkaTrident() {
        super(new Item.Properties().durability(900).rarity(Rarity.EPIC).fireResistant()
            .attributes(TridentItem.createAttributes()));
    }
    public static void init() {}

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, context, lines, flags);
        lines.add(Component.translatable("item.slavicmyths.pharaohka_trident.desc")
            .withStyle(ChatFormatting.AQUA));
    }

    static boolean ready(ServerPlayer owner) {
        return owner.serverLevel().getGameTime() >= owner.getPersistentData().getLong(READY);
    }

    static boolean wave(ServerPlayer owner, Vec3 center, LivingEntity direct) {
        if (!ready(owner) || !owner.isAlive()) return false;
        ServerLevel level = owner.serverLevel();
        owner.getPersistentData().putLong(READY, level.getGameTime() + COOLDOWN);
        int affected = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.subtract(RADIUS, RADIUS, RADIUS),
                    center.add(RADIUS, RADIUS, RADIUS)),
                entity -> entity != direct && entity != owner && entity.isAlive()
                    && entity.distanceToSqr(center) <= RADIUS * RADIUS
                    && Abilities.canHit(owner, entity))) {
            target.hurt(owner.damageSources().trident(null, owner), 4.0F);
            if (++affected >= 5) break;
        }
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + .4, center.z,
            48, RADIUS * .65, .45, RADIUS * .65, .08);
        return true;
    }

    @SubscribeEvent public static void impact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownTrident projectile)
                || !(projectile.getOwner() instanceof ServerPlayer owner)
                || !(projectile.getWeaponItem() instanceof ItemStack stack)
                || !stack.is(ITEM.get())
                || projectile.getPersistentData().getBoolean(SPENT)) return;
        projectile.getPersistentData().putBoolean(SPENT, true);
        LivingEntity direct = event.getRayTraceResult() instanceof EntityHitResult hit
            && hit.getEntity() instanceof LivingEntity living ? living : null;
        wave(owner, event.getRayTraceResult().getLocation(), direct);
    }
}
