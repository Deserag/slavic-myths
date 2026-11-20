package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.Abilities;
import org.slavicmyths.rpg.RuneDefinition;
import org.slavicmyths.rpg.Runes;

/** Uses the existing ItemState rune sockets, installation and removal UI. */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineRunes {
    public static final DeferredHolder<Item, Item> TIDE = register("tide");
    public static final DeferredHolder<Item, Item> ABYSS = register("abyss");
    public static final DeferredHolder<Item, Item> STORM = register("storm");
    private static final ResourceLocation WALK = ResourceLocation.fromNamespaceAndPath("slavicmyths", "rune_tide_walk");
    private static final ResourceLocation SWIM = ResourceLocation.fromNamespaceAndPath("slavicmyths", "rune_tide_swim");
    private static final String VISION = "SlavicMarineRuneAbyssNightVision";
    private static final String STORM_CD = "SlavicMarineRuneStormReady";
    private static final int VISION_TICKS = 260;
    private static final int STORM_TICKS = 160;

    private static DeferredHolder<Item, Item> register(String id) {
        return ModItems.ITEMS.register("rune_" + id, () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
            @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                    List<Component> lines, TooltipFlag flags) {
                super.appendHoverText(stack, context, lines, flags);
                for (Component line : RuneDefinition.ALL.get(id).tooltip())
                    lines.add(line.copy().withStyle(ChatFormatting.AQUA));
            }
        });
    }
    public static void init() {}
    public static boolean marine(String id) {
        return id.equals("tide") || id.equals("abyss") || id.equals("storm");
    }
    public static boolean supports(ItemStack item, String id) {
        if (!marine(id) || item.isEmpty()) return false;
        if (item.getItem() instanceof MarineArmor.Piece) return !id.equals("storm");
        return item.getItem() instanceof TridentItem && !id.equals("abyss");
    }
    public static Component description(String id) {
        return Component.translatable("item.slavicmyths.rune_" + id + ".desc");
    }
    private static boolean installed(ServerPlayer player, String id) {
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack item = player.getItemBySlot(slot);
            if (supports(item, id) && Runes.has(item, id)) return true;
        }
        ItemStack hand = player.getMainHandItem();
        return supports(hand, id) && Runes.has(hand, id);
    }
    private static void modifier(AttributeInstance attr, ResourceLocation id, boolean active, double bonus) {
        if (attr == null) return;
        if (!active) {
            if (attr.getModifier(id) != null) attr.removeModifier(id);
        } else if (attr.getModifier(id) == null) {
            attr.addTransientModifier(new AttributeModifier(id, bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
    private static void removeVision(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(VISION)) return;
        var active = player.getEffect(MobEffects.NIGHT_VISION);
        if (active != null && active.getAmplifier() == 0
                && active.getDuration() <= VISION_TICKS && active.isAmbient() && !active.isVisible())
            player.removeEffect(MobEffects.NIGHT_VISION);
        player.getPersistentData().remove(VISION);
    }
    /** Deterministic gameplay update also used by isolated headless GameTests. */
    static void apply(ServerPlayer player, boolean wet, boolean underwater) {
        boolean tide = wet && installed(player, "tide");
        modifier(player.getAttribute(Attributes.MOVEMENT_SPEED), WALK, tide, .10D);
        modifier(player.getAttribute(NeoForgeMod.SWIM_SPEED), SWIM, tide, .15D);
        // The four-piece suit already owns underwater potions; don't override them.
        if (!underwater || MarineArmor.full(player) || !installed(player, "abyss")) {
            removeVision(player);
            return;
        }
        player.setAirSupply(player.getMaxAirSupply());
        var active = player.getEffect(MobEffects.NIGHT_VISION);
        if (active == null) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, VISION_TICKS, 0, true, false, true));
            player.getPersistentData().putBoolean(VISION, true);
        } else if (player.getPersistentData().getBoolean(VISION) && active.getAmplifier() == 0
                && active.getDuration() < 220)
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, VISION_TICKS, 0, true, false, true));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        boolean wet = player.isAlive() && !player.isSpectator() && player.isInWaterOrBubble();
        apply(player, wet, wet && player.isEyeInFluid(FluidTags.WATER));
    }
    private static void clear(ServerPlayer player) {
        modifier(player.getAttribute(Attributes.MOVEMENT_SPEED), WALK, false, 0);
        modifier(player.getAttribute(NeoForgeMod.SWIM_SPEED), SWIM, false, 0);
        removeVision(player);
        player.getPersistentData().remove(STORM_CD);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) clear(p);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) clear(p);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) clear(p);
    }
    /** Server-only shock wave: 2 magic damage + at most two nearby targets, every eight seconds. */
    static boolean discharge(ServerPlayer attacker, LivingEntity victim) {
        if (!attacker.isAlive() || !victim.isAlive() || !Abilities.canHit(attacker, victim)) return false;
        ItemStack hand = attacker.getMainHandItem();
        if (!supports(hand, "storm") || !Runes.has(hand, "storm")) return false;
        long now = attacker.serverLevel().getGameTime();
        if (attacker.getPersistentData().getLong(STORM_CD) > now) return false;
        attacker.getPersistentData().putLong(STORM_CD, now + STORM_TICKS);
        ServerLevel world = attacker.serverLevel();
        victim.hurt(attacker.damageSources().magic(), 2F);
        int jumped = 0;
        for (LivingEntity other : world.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(2D), e -> e != victim && e != attacker && e.isAlive())) {
            if (!Abilities.canHit(attacker, other)) continue;
            other.hurt(attacker.damageSources().magic(), 1F);
            if (++jumped >= 2) break;
        }
        world.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getEyeY(),
            victim.getZ(), 15, .45, .45, .45, .08);
        return true;
    }
    @SubscribeEvent public static void melee(LivingDamageEvent.Post e) {
        if (e.getNewDamage() <= 0 || !e.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(e.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        discharge(player, e.getEntity());
    }
    private MarineRunes() {}
}
