package org.slavicmyths.marine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Six Curios-only rewards; inventory and hand alone never enable a passive effect. */
@EventBusSubscriber(modid = "slavicmyths")
public final class MarineAccessories {
    private static final Map<String, String> SLOTS = Map.of(
        "tide_ring", "ring",
        "abyss_necklace", "necklace",
        "storm_bracelet", "charm",
        "tailwind_feather", "charm",
        "tsar_pike_belt", "belt",
        "zhelva_amulet", "charm"
    );
    private static final Map<String, DeferredHolder<Item, Item>> ITEMS = new LinkedHashMap<>();
    public static final DeferredHolder<Item, Item> TIDE_RING = register("tide_ring");
    public static final DeferredHolder<Item, Item> ABYSS_NECKLACE = register("abyss_necklace");
    public static final DeferredHolder<Item, Item> STORM_BRACELET = register("storm_bracelet");
    public static final DeferredHolder<Item, Item> TAILWIND_FEATHER = register("tailwind_feather");
    public static final DeferredHolder<Item, Item> TSAR_PIKE_BELT = register("tsar_pike_belt");
    public static final DeferredHolder<Item, Item> ZHELVA_AMULET = register("zhelva_amulet");
    private static final ResourceLocation TIDE_MOVE = key("tide_ring_speed");
    private static final ResourceLocation PIKE_SWIM = key("tsar_pike_belt_swim");
    private static final ResourceLocation SHELL_ARMOR = key("zhelva_amulet_armor");
    private static final ResourceLocation SHELL_KNOCKBACK = key("zhelva_knockback");
    private static final String TAILWIND_READY = "SlavicMarineTailwindReady";

    private static ResourceLocation key(String name) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths", name);
    }
    private static DeferredHolder<Item, Item> register(String name) {
        DeferredHolder<Item, Item> item = ModItems.ITEMS.register(name,
            () -> new Wearable(name, SLOTS.get(name)));
        ITEMS.put(name, item);
        return item;
    }
    public static void init() {}
    public static boolean wears(LivingEntity entity, String name) {
        DeferredHolder<Item, Item> item = ITEMS.get(name);
        if (item == null) return false;
        return CuriosApi.getCuriosInventory(entity).map(handler ->
            handler.findCurios(stack -> stack.is(item.get())).stream()
                .anyMatch(found -> found.slotContext().identifier().equals(SLOTS.get(name))))
            .orElse(false);
    }

    private static final class Wearable extends Item implements ICurioItem {
        private final String slot;
        private Wearable(String name, String slot) {
            super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
            this.slot = slot;
        }
        @Override public boolean canEquip(SlotContext context, ItemStack stack) {
            return context.identifier().equals(slot);
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            lines.add(Component.translatable(stack.getDescriptionId() + ".desc")
                .withStyle(ChatFormatting.AQUA));
        }
    }

    private static void modifier(AttributeInstance attribute, ResourceLocation id,
            boolean enabled, double value) {
        if (attribute == null) return;
        if (!enabled) attribute.removeModifier(id);
        else if (attribute.getModifier(id) == null)
            attribute.addTransientModifier(new AttributeModifier(id, value,
                (id.equals(SHELL_ARMOR) || id.equals(SHELL_KNOCKBACK)) ? AttributeModifier.Operation.ADD_VALUE
                    : AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    static void apply(ServerPlayer player) {
        boolean active = player.isAlive() && !player.isSpectator();
        boolean wet = player.isInWaterOrBubble();
        modifier(player.getAttribute(Attributes.MOVEMENT_SPEED), TIDE_MOVE,
            active && wet && wears(player, "tide_ring"), .12);
        modifier(player.getAttribute(NeoForgeMod.SWIM_SPEED), PIKE_SWIM,
            active && wet && wears(player, "tsar_pike_belt"), .20);
        modifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), SHELL_KNOCKBACK,
            active && wears(player, "zhelva_amulet"), .2);
        modifier(player.getAttribute(Attributes.ARMOR), SHELL_ARMOR,
            active && wears(player, "zhelva_amulet"), 2.0);
    }

    private static void clear(ServerPlayer player) {
        modifier(player.getAttribute(Attributes.MOVEMENT_SPEED), TIDE_MOVE, false, 0);
        modifier(player.getAttribute(NeoForgeMod.SWIM_SPEED), PIKE_SWIM, false, 0);
        modifier(player.getAttribute(Attributes.ARMOR), SHELL_ARMOR, false, 0);
        modifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), SHELL_KNOCKBACK, false, 0);
    }

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        apply(player);
        long remaining = player.getPersistentData().getLong(TAILWIND_READY)
            - player.server.overworld().getGameTime();
        if (remaining > 0 && remaining <= 240
                && !player.getCooldowns().isOnCooldown(TAILWIND_FEATHER.get()))
            player.getCooldowns().addCooldown(TAILWIND_FEATHER.get(), (int) remaining);
    }

    public static void dash(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.onGround()
                || player.isPassenger() || player.getAbilities().flying || player.isFallFlying()
                || player.isInWaterOrBubble() || !wears(player, "tailwind_feather")) return;
        long now = player.server.overworld().getGameTime();
        if (now < player.getPersistentData().getLong(TAILWIND_READY)) return;
        Vec3 direction = player.getLookAngle().scale(.8);
        player.setDeltaMovement(direction);
        player.hurtMarked = true;
        player.getPersistentData().putLong(TAILWIND_READY, now + 240);
        player.getCooldowns().addCooldown(TAILWIND_FEATHER.get(), 240);
    }

    @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getNewDamage() <= 0
                || !player.isAlive()) return;
        float damage = event.getNewDamage();
        if (event.getSource().is(DamageTypes.FALL) && wears(player, "tailwind_feather")) damage *= .5F;
        if (wears(player, "abyss_necklace") && player.isInWaterOrBubble()
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && !(attacker instanceof Player) && attacker.isInWaterOrBubble())
            damage *= .8F;
        if (wears(player, "storm_bracelet")) {
            if (event.getSource().is(DamageTypes.LIGHTNING_BOLT)) damage *= .5F;
            else if (player.level().isThundering()
                    && event.getSource().is(DamageTypes.MAGIC)) damage *= .85F;
        }
        if (damage != event.getNewDamage()) event.setNewDamage(damage);
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }
    private MarineAccessories() {}
}
