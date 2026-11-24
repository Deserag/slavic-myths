package org.slavicmyths.client;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slavicmyths.SlavicMyths;

/** Presentation only. Registered weapon IDs match the 102-entry source catalog. */
@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID, value = Dist.CLIENT)
public final class WeaponTooltips {
    private static final Set<String> WEAPONS = Set.of("ice_captain_axes","snow_wanderer_hammers","ice_queen_spear_staff","absolute_silence_sword","silence_blades","northern_beast_claws","ice_sword","ice_axe","ice_mace","ice_halberd","ice_scythe","winter_hunt_bow","ice_crossbow","gneteya_great_hammer","abyss_staff","ancient_carolingian_sword","ancient_chekan","ancient_spear","ashen_greatsword","ataman_flail","battle_axe","berdysh","bone_arrow","bone_knife","butchering_knife","carved_staff","chekan","chud_multitool_axe","club","diamond_flail","diamond_spear","diamond_sulitsa","diamond_throwing_knife","empowered_infernal_bow","finist_bow","flail","gift_ilya_shield","gold_flail","gold_spear","gold_sulitsa","gold_throwing_knife","heavy_bow","infernal_bow","iron_spear","iron_sulitsa","iron_throwing_knife","karachun_scythe","likho_staff","lomeya_hammer","lomeya_hammer_offhand","mace","morovoy_staff","morozko_staff","mytar_shackled_sickle","mytar_sickle","navya_ostroga","netherite_flail","netherite_spear","netherite_sulitsa","netherite_throwing_knife","neveya_scythe","nightingale_dagger","nine_mor_axe","nine_mor_shield","nine_mor_spear","noon_sickle","perunite_axe","perunite_flail","perunite_mace","perunite_shield","perunite_spear","perunite_sulitsa","perunite_sword","perunite_throwing_knife","pharaohka_trident","poloz_whip","pool_spear","restored_carolingian_sword","restored_spear","retainer_shield","ritual_knife","serpent_crossbow","shadow_throwing_knife","short_bow","silver_dagger","silver_flail","silver_mace","silver_spear","silver_sulitsa","silver_sword","silver_throwing_knife","spear","stone_flail","stone_mace","stone_sulitsa","stone_throwing_knife","storm_anchor","storm_harpoon","storm_staff","three_breath_crossbow","thunder_axe","thunder_spear","trophy_kalin_sabre","tugarin_sword","veles_staff","victor_spear","volcanic_heart_flail","ward_shield","wooden_mace","zhara_axe","zhara_flail","zhara_spear","zhara_sulitsa","zhara_sword","zhara_throwing_knife","zhelva_shield");
    /** Craft provenance: Gorynych, Greater Morovoy Fire Zmey and Ocean Zmey respectively. */
    private static final Set<String> ELDER_SERPENT_WEAPONS =
        Set.of("serpent_crossbow", "three_breath_crossbow", "morovoy_staff", "storm_anchor", "storm_harpoon");
    private static final int NORMAL = 0xE4E4E7;
    private static final int RARE = 0x55FFFF;
    private static final int EPIC = 0xAA55FF;
    private static final int ELDER = 0xFFAA00;
    private static final int SECONDARY_GOLD = 0xD4AF37;
    private static final int ABILITY = 0xFFAA00;
    private static final int DAMAGE = 0xFF5555;
    private static final int POSITIVE = 0x55FF55;
    private static final int MUTED = 0xAAAAAA;
    private static final int CONTROL = 0xFFFF55;
    private static final Pattern KEYS = Pattern.compile("(?i)(Shift\\s*\\+\\s*(?:ЛКМ|ПКМ|LMB|RMB|left.click|right.click)|(?:ЛКМ|ПКМ|LMB|RMB|right.click|left.click))");
    private static final Pattern VALUES = Pattern.compile("(?i)(\\d+(?:[.,]\\d+)?\\s*(?:урона|damage|блок(?:ов|а)?|blocks?|сек(?:унд(?:ы|у)?)?\\.?|seconds?|ticks?))");
    private static final Pattern DAMAGE_VALUES = Pattern.compile("(?i)(?:(?:урон|damage)\\s*[:：]?\\s*\\d+(?:[.,]\\d+)?|\\d+(?:[.,]\\d+)?\\s*(?:урона|damage))");
    private static final Pattern SKILL_NAMES = Pattern.compile("(?<=— )[A-Za-zА-Яа-яЁё][A-Za-zА-Яа-яЁё\\s-]{2,40}(?=:)");
    private static final Pattern TOKENS = Pattern.compile(KEYS.pattern() + "|" + SKILL_NAMES.pattern() + "|" + DAMAGE_VALUES.pattern() + "|" + VALUES.pattern());

    public static boolean isWeapon(String id) { return WEAPONS.contains(id); }
    public static boolean isElderSerpentWeapon(String id) { return ELDER_SERPENT_WEAPONS.contains(id); }

    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (location == null || !SlavicMyths.MOD_ID.equals(location.getNamespace())
                || !isWeapon(location.getPath()) || event.getToolTip().isEmpty()) return;

        String id = location.getPath();
        if (!stack.has(DataComponents.CUSTOM_NAME)) {
            int color = isElderSerpentWeapon(id) ? ELDER
                : stack.getRarity() == Rarity.EPIC ? EPIC
                : stack.getRarity() == Rarity.RARE ? RARE : NORMAL;
            Component title = event.getToolTip().get(0);
            event.getToolTip().set(0, title.copy().withStyle(s -> s.withColor(TextColor.fromRgb(color))));
        }

        // A restrained status line appears only for rewards traced to an elder serpent.
        if (isElderSerpentWeapon(id))
            event.getToolTip().add(1, Component.translatable("tooltip.slavicmyths.weapon.elder_serpent")
                .withStyle(s -> s.withColor(TextColor.fromRgb(SECONDARY_GOLD))));

        // Preserve translated source text and actual mechanics; standardize how it is presented.
        for (int i = isElderSerpentWeapon(id) ? 2 : 1; i < event.getToolTip().size(); i++) {
            Component original = event.getToolTip().get(i);
            if (isDescription(original.getString()))
                event.getToolTip().set(i, styledDescription(original));
        }

        String key = "tooltip.slavicmyths.weapon." + id;
        if (!id.equals("storm_staff") && I18n.exists(key)) {
            Component generic = Component.translatable(key).withStyle(s -> s.withColor(TextColor.fromRgb(MUTED)));
            event.getToolTip().add(isDescription(generic.getString()) ? styledDescription(generic) : generic);
        }
    }

    static Component styledDescription(Component original) {
        String value = original.getString();
        int color = categoryColor(value);
        MutableComponent line = Component.literal("");
        Matcher tokens = TOKENS.matcher(value);
        int from = 0;
        while (tokens.find()) {
            if (from < tokens.start()) line.append(colored(value.substring(from, tokens.start()), color));
            String token = tokens.group();
            Matcher named = SKILL_NAMES.matcher(value);
            boolean abilityName = named.find(tokens.start()) && named.start() == tokens.start() && named.end() == tokens.end();
            int tokenColor = KEYS.matcher(token).matches() ? CONTROL
                : abilityName ? ABILITY
                : DAMAGE_VALUES.matcher(token).matches() ? DAMAGE
                : color == POSITIVE ? POSITIVE : MUTED;
            line.append(colored(token, tokenColor));
            from = tokens.end();
        }
        if (from < value.length()) line.append(colored(value.substring(from), color));
        return line;
    }

    private static MutableComponent colored(String value, int rgb) {
        return Component.literal(value).withStyle(s -> s.withColor(TextColor.fromRgb(rgb)));
    }
    private static boolean isDescription(String text) {
        String s = text.toLowerCase(java.util.Locale.ROOT);
        return KEYS.matcher(text).find() || VALUES.matcher(text).find()
            || s.contains("навык") || s.contains("способност") || s.contains("ability")
            || s.contains("cooldown") || s.contains("перезаряд")
            || s.contains("пассив") || s.contains("passive")
            || s.contains("замедлен") || s.contains("slowness")
            || s.contains("лечение") || s.contains("healing");
    }
    private static int categoryColor(String text) {
        String s = text.toLowerCase(java.util.Locale.ROOT);
        if (s.contains("перезаряд") || s.contains("cooldown") || s.contains("ограничен") || s.contains("requires")) return MUTED;
        if (s.contains("лечение") || s.contains("исцел") || s.contains("healing") || s.contains("regeneration") || s.contains("восстанов")) return POSITIVE;
        if (s.contains("навык") || s.contains("способност") || s.contains("ability") || s.contains("пассив") || s.contains("passive")) return ABILITY;
        // Actual damage numbers are colored by the token renderer; keep explanatory prose neutral.
        return MUTED;
    }

    private WeaponTooltips() {}
}
