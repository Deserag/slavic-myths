package org.slavicmyths.ships.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slavicmyths.ships.FirstShipItem;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.RaftCharacteristics;
import org.slavicmyths.ships.RaftContent;
import org.slavicmyths.ships.RaftItem;
import org.slavicmyths.ships.RaftSize;
import org.slavicmyths.ships.ShipyardMenu;
import org.slavicmyths.ships.ShipyardUpgrades;
import org.slavicmyths.ships.ShipyardModular;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;

/**
 * Two-tab workshop:
 *  0 Создание корабля — choose a blueprint and preview exact inventory costs;
 *  1 Модификации — place a vessel in its slot and select an upgrade.
 * Only the server authorizes craft/install transactions; the GUI never consumes
 * materials or invents vessels on the client.
 */
public final class ShipyardScreen extends AbstractContainerScreen<ShipyardMenu> {
    private static final int FRAME = 0xffae824a, WOOD = 0xff302419;
    private static final int PAPER = 0xffe5d3aa, DARK = 0xff493323;
    private static final String[] VESSELS =
            {"Плот 3×3", "Плот 3×5", "Плот 5×5", "Плот 5×7", "Корабль 7×11"};
    private static final int[] CRAFT_ACTIONS = {2, 30, 31, 32, 11};
    private static final String[] SHIP_OPTIONS =
            {"Парус I", "Парус II", "Парус III", "Оснастка +", "Оснастка −", "Снять всё"};
    private static final int[] SHIP_ACTIONS = {20, 21, 22, 23, 24, 25};
    private static final String[] MODULAR_OPTIONS = {
            "Длина +", "Ширина +", "Парус +", "Мачта +",
            "Палуба +", "Мостик", "Каюта"
    };
    private static final int[] MODULAR_ACTIONS = {200, 201, 202, 203, 204, 205, 206};
    private static final String[] CLASS_NAMES = {
            "Малый", "Средний", "Большой", "Крупнейший"
    };
    private static final String[] RAFT_OPTIONS =
            {"Борта +", "Парус +", "Руль +", "Вёсла +", "Борта −",
             "Парус −", "Руль −", "Вёсла −", "Размер +"};
    private static final int[] RAFT_ACTIONS = {3, 4, 5, 6, 7, 8, 9, 10, 1};
    private record Cost(String title, ItemStack icon, int need, int have) {}

    private int tab;
    private int chosen;
    private boolean createRafts;
    private int selectedLength;
    private int selectedWidth;
    private int lastKind = -1;
    private Button primary;

    public ShipyardScreen(ShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 476;
        imageHeight = 299;
        inventoryLabelX = 198;
        inventoryLabelY = 195;
    }

    private int vesselKind() {
        ItemStack stack = menu.inspected();
        return stack.getItem() instanceof FirstShipItem ? 2
                : stack.getItem() instanceof RaftItem ? 1 : 0;
    }

    private ShipBuildConfiguration creationConfig() {
        if (createRafts || chosen < 0 || chosen >= 4) return null;
        try {
            return ShipBuildConfiguration.initial(
                    ShipSectionPlan.ShipClass.values()[chosen])
                    .resize(selectedLength, selectedWidth);
        } catch (RuntimeException invalid) { return null; }
    }

    private ShipBuildConfiguration nextConfig() {
        ShipBuildConfiguration current = ShipyardModular.configuration(menu.inspected());
        if (current == null || chosen < 0 || chosen >= MODULAR_ACTIONS.length) return null;
        try {
            return switch (MODULAR_ACTIONS[chosen]) {
                case 200 -> current.resize(current.extraLength() + 1, current.extraWidth());
                case 201 -> current.resize(current.extraLength(), current.extraWidth() + 1);
                case 202 -> current.withSail(current.sailTiers().size() - 1,
                        current.sailTiers().get(current.sailTiers().size() - 1) + 1);
                case 203 -> current.withSail(current.sailTiers().size(), 1);
                case 204 -> current.withDecks(current.decks() + 1);
                case 205 -> current.withBridge(true);
                case 206 -> current.withCabin(true);
                default -> null;
            };
        } catch (RuntimeException invalid) { return null; }
    }

    @Override protected void init() {
        super.init();
        lastKind = vesselKind();
        rebuild();
    }

    private void switchTab(int id) {
        tab = id;
        chosen = 0;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        String[] tabs = {"Создание корабля", "Модификации"};
        for (int i = 0; i < tabs.length; i++) {
            final int value = i;
            Button button = Button.builder(Component.literal(tabs[i]),
                    clicked -> switchTab(value))
                    .bounds(leftPos + 12 + 229 * i, topPos + 12, 222, 22).build();
            button.active = tab != i;
            addRenderableWidget(button);
        }
        if (tab == 0) {
            addRenderableWidget(Button.builder(
                    Component.literal(createRafts ? "Тип: плоты" : "Тип: корабли"),
                    clicked -> { createRafts = !createRafts; chosen = 0; rebuild(); })
                    .bounds(leftPos + 18, topPos + 64, 135, 18).build());
        }

        ShipBuildConfiguration current = ShipyardModular.configuration(menu.inspected());
        String[] options = tab == 0
                ? createRafts ? VESSELS : CLASS_NAMES
                : current != null ? MODULAR_OPTIONS
                : vesselKind() == 2 ? SHIP_OPTIONS
                : vesselKind() == 1 ? RAFT_OPTIONS : new String[0];

        chosen = Math.max(0, Math.min(chosen, Math.max(0, options.length - 1)));
        for (int i = 0; i < options.length; i++) {
            final int value = i;
            final int x = tab == 0 ? 18 : 16 + (i % 2) * 77;
            final int y = tab == 0 ? 85 + 18 * i : 99 + (i / 2) * 19;
            int width = tab == 0 ? 135 : 73;
            Button button = Button.builder(Component.literal(options[i]),
                    clicked -> {
                        chosen = value;
                        selectedLength = Math.min(selectedLength, ShipSectionPlan.ShipClass.values()[
                                Math.min(chosen, 3)].maxLength);
                        selectedWidth = Math.min(selectedWidth, ShipSectionPlan.ShipClass.values()[
                                Math.min(chosen, 3)].maxWidth);
                        rebuild();
                    }).bounds(leftPos + x, topPos + y, width, 17).build();
            button.active = chosen != i;
            addRenderableWidget(button);
        }

        if (tab == 0 && !createRafts) {
            addRenderableWidget(Button.builder(Component.literal("-"),
                    clicked -> { selectedLength = Math.max(0, selectedLength - 1); rebuild(); })
                    .bounds(leftPos + 94, topPos + 159, 24, 17).build());
            addRenderableWidget(Button.builder(Component.literal("+"),
                    clicked -> { selectedLength = Math.min(
                            ShipSectionPlan.ShipClass.values()[chosen].maxLength,
                            selectedLength + 1); rebuild(); })
                    .bounds(leftPos + 129, topPos + 159, 24, 17).build());
            addRenderableWidget(Button.builder(Component.literal("-"),
                    clicked -> { selectedWidth = Math.max(0, selectedWidth - 1); rebuild(); })
                    .bounds(leftPos + 94, topPos + 180, 24, 17).build());
            addRenderableWidget(Button.builder(Component.literal("+"),
                    clicked -> { selectedWidth = Math.min(
                            ShipSectionPlan.ShipClass.values()[chosen].maxWidth,
                            selectedWidth + 1); rebuild(); })
                    .bounds(leftPos + 129, topPos + 180, 24, 17).build());
        }
        primary = Button.builder(Component.literal(tab == 0 ? "Создать" : "Применить"),
                clicked -> execute()).bounds(leftPos + 18, topPos + 204, 135, 19).build();
        addRenderableWidget(primary);
        updateButton();
    }

    private void execute() {
        if (minecraft == null || minecraft.gameMode == null || !allowed()) return;
        int action;
        if (tab == 0) {
            if (createRafts) action = CRAFT_ACTIONS[chosen];
            else action = 100 + chosen * 16 + selectedLength * 4 + selectedWidth;
        } else {
            action = ShipyardModular.configuration(menu.inspected()) != null
                    ? MODULAR_ACTIONS[chosen]
                    : vesselKind() == 2 ? SHIP_ACTIONS[chosen] : RAFT_ACTIONS[chosen];
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
    }

    private int count(Item item) {
        return minecraft == null || minecraft.player == null ? 0
                : minecraft.player.getInventory().countItem(item);
    }

    private int countTag(TagKey<Item> tag) {
        if (minecraft == null || minecraft.player == null) return 0;
        int sum = 0;
        Inventory inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (inv.getItem(i).is(tag)) sum += inv.getItem(i).getCount();
        return sum;
    }

    private Cost itemCost(String label, Item item, int need) {
        return new Cost(label, new ItemStack(item), need, count(item));
    }

    private void addModularCosts(List<Cost> list, ShipyardModular.Materials price) {
        if (price.logs() > 0)
            list.add(new Cost("Брёвна", new ItemStack(Items.OAK_LOG),
                    price.logs(), countTag(ItemTags.LOGS)));
        if (price.planks() > 0)
            list.add(new Cost("Доски", new ItemStack(Items.OAK_PLANKS),
                    price.planks(), countTag(ItemTags.PLANKS)));
        if (price.iron() > 0) list.add(itemCost("Железо", Items.IRON_INGOT, price.iron()));
        if (price.string() > 0) list.add(itemCost("Нити", Items.STRING, price.string()));
        if (price.deckSections() > 0)
            list.add(itemCost("Секции", RaftContent.DECK_SECTION.get(), price.deckSections()));
        if (price.railKits() > 0)
            list.add(itemCost("Ограждения", RaftContent.RAIL_MODULE.get(), price.railKits()));
        if (price.helms() > 0)
            list.add(itemCost("Штурвал", RaftContent.HELM_MODULE.get(), price.helms()));
        if (price.basicSails() > 0)
            list.add(itemCost("Парус I", RaftContent.SAIL_MODULE.get(), price.basicSails()));
        if (price.linenSails() > 0)
            list.add(itemCost("Парус II", RaftContent.SAIL_LINEN_MODULE.get(), price.linenSails()));
        if (price.reinforcedSails() > 0)
            list.add(itemCost("Парус III", RaftContent.SAIL_REINFORCED_MODULE.get(), price.reinforcedSails()));
    }

    private List<Cost> costs() {
        List<Cost> list = new ArrayList<>();
        ShipBuildConfiguration built = ShipyardModular.configuration(menu.inspected());
        if (tab == 0 && !createRafts) {
            ShipBuildConfiguration selected = creationConfig();
            if (selected != null) addModularCosts(list, ShipyardModular.costs(null, selected));
        } else if (tab == 1 && built != null) {
            ShipBuildConfiguration next = nextConfig();
            if (next != null) addModularCosts(list, ShipyardModular.costs(built, next));
        } else if (tab == 0 && chosen < 4) {
            RaftSize size = switch (chosen) {
                case 0 -> RaftSize.SMALL;
                case 1 -> RaftSize.LONG;
                case 2 -> RaftSize.WIDE;
                default -> RaftSize.CARGO;
            };
            list.add(itemCost("Палубные секции", RaftContent.DECK_SECTION.get(),
                    ShipyardUpgrades.raftDeckCost(size)));
            list.add(itemCost("Вёсла", RaftContent.OAR_MODULE.get(), 1));
        } else if (tab == 0) {
            list.add(new Cost("Брёвна", new ItemStack(Items.OAK_LOG), 48, countTag(ItemTags.LOGS)));
            list.add(new Cost("Доски", new ItemStack(Items.OAK_PLANKS), 96, countTag(ItemTags.PLANKS)));
            list.add(itemCost("Железо", Items.IRON_INGOT, 32));
            list.add(itemCost("Нити", Items.STRING, 24));
            list.add(itemCost("Секции", RaftContent.DECK_SECTION.get(), 32));
            list.add(itemCost("Борта", RaftContent.RAIL_MODULE.get(), 4));
            list.add(itemCost("Руль", RaftContent.HELM_MODULE.get(), 1));
        } else if (tab == 1 && vesselKind() == 2) {
            int level = ShipyardUpgrades.shipRigLevel(menu.inspected());
            int fabric = ShipyardUpgrades.shipFabric(menu.inspected());
            if (chosen <= 2) {
                Item module = chosen == 0 ? RaftContent.SAIL_MODULE.get()
                        : chosen == 1 ? RaftContent.SAIL_LINEN_MODULE.get()
                        : RaftContent.SAIL_REINFORCED_MODULE.get();
                list.add(itemCost("Парусная ткань", module, Math.max(1, level)));
            } else if (chosen == 3) {
                list.add(itemCost("Оснастка", RaftContent.SAIL_RIGGING_KIT.get(), 1));
                if (fabric > 0)
                    list.add(itemCost("Парус", fabricItem(fabric), 1));
            }
        } else if (tab == 1 && vesselKind() == 1) {
            int action = RAFT_ACTIONS[chosen];
            if (action >= 3 && action <= 6) {
                Item required = switch (action) {
                    case 3 -> RaftContent.RAIL_MODULE.get();
                    case 4 -> RaftContent.SAIL_MODULE.get();
                    case 5 -> RaftContent.HELM_MODULE.get();
                    default -> RaftContent.OAR_MODULE.get();
                };
                list.add(itemCost("Деталь", required, 1));
            } else if (action == 1 && menu.inspected().getItem() instanceof RaftItem raft
                    && raft.size().next() != null)
                list.add(itemCost("Палубные секции", RaftContent.DECK_SECTION.get(),
                        ShipyardUpgrades.sectionCost(raft.size())));
        }
        return list;
    }

    private Item fabricItem(int quality) {
        return switch (quality) {
            case 1 -> RaftContent.SAIL_MODULE.get();
            case 2 -> RaftContent.SAIL_LINEN_MODULE.get();
            default -> RaftContent.SAIL_REINFORCED_MODULE.get();
        };
    }

    private boolean allowed() {
        if (minecraft == null || minecraft.player == null) return false;
        if (tab == 0 && !menu.inspected().isEmpty()) return false;
        if (tab == 1 && vesselKind() == 0) return false;
        if (!minecraft.player.getAbilities().instabuild)
            for (Cost cost : costs()) if (cost.have() < cost.need()) return false;

        if (tab == 0) return createRafts || creationConfig() != null;
        ShipBuildConfiguration modular = ShipyardModular.configuration(menu.inspected());
        if (modular != null) {
            ShipBuildConfiguration next = nextConfig();
            return next != null && !next.equals(modular);
        }
        ItemStack stack = menu.inspected();
        if (vesselKind() == 2) {
            int level = ShipyardUpgrades.shipRigLevel(stack);
            int fabric = ShipyardUpgrades.shipFabric(stack);
            if (level < 0 || fabric < 0) return false;
            int action = SHIP_ACTIONS[chosen];
            if (action >= 20 && action <= 22) return fabric != action - 19;
            if (action == 23) return level >= 1 && level < 4;
            if (action == 24) return level >= 2;
            return level >= 1;
        }
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(stack);
        if (info == null || info.invalid()) return false;
        RaftItem raft = (RaftItem)stack.getItem();
        return switch (RAFT_ACTIONS[chosen]) {
            case 3 -> !info.rails();
            case 4 -> info.sails() < raft.size().maxSails();
            case 5 -> !info.helm();
            case 6 -> !info.extraOars() && raft.size().length > 3;
            case 7 -> info.rails();
            case 8 -> info.sails() > 0;
            case 9 -> info.helm();
            case 10 -> info.extraOars();
            case 1 -> raft.size().next() != null;
            default -> false;
        };
    }

    private void updateButton() {
        if (primary != null) primary.active = allowed();
    }

    @Override protected void containerTick() {
        super.containerTick();
        int kind = vesselKind();
        if (lastKind != kind) { lastKind = kind; chosen = 0; rebuild(); }
        updateButton();
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, FRAME);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3,
                topPos + imageHeight - 3, WOOD);
        graphics.fill(leftPos + 10, topPos + 42, leftPos + 183, topPos + 192, 0xff694e36);
        graphics.fill(leftPos + 189, topPos + 42, leftPos + 466, topPos + 192, PAPER);
        graphics.fill(leftPos + 191, topPos + 44, leftPos + 464, topPos + 190, 0xfff0e4c4);
        graphics.fill(leftPos + 190, topPos + 197, leftPos + 464, topPos + 292, 0xff745335);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                graphics.fill(leftPos + 197 + col * 18, topPos + 210 + row * 18,
                        leftPos + 215 + col * 18, topPos + 228 + row * 18, 0xff231b14);
        for (int col = 0; col < 9; col++)
            graphics.fill(leftPos + 197 + col * 18, topPos + 267,
                    leftPos + 215 + col * 18, topPos + 285, 0xff231b14);
        graphics.fill(leftPos + 152, topPos + 64, leftPos + 171, topPos + 83, 0xff241a13);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Корабельная верфь", 14, 43, 0xfff8e1b5, false);
        graphics.drawString(font, tab == 0 ? "Чертежи судов" : "Установите судно", 18, 54,
                0xfff2dfbd, false);
        graphics.drawString(font, tab == 0 ? VESSELS[chosen]
                : vesselKind() == 0 ? "Сначала положите судно в ячейку"
                : vesselKind() == 2 ? SHIP_OPTIONS[chosen] : RAFT_OPTIONS[chosen],
                202, 52, DARK, false);
        if (tab == 0) drawBlueprint(graphics);
        if (tab == 1 && vesselKind() == 2) {
            int level = ShipyardUpgrades.shipRigLevel(menu.inspected());
            int fabric = ShipyardUpgrades.shipFabric(menu.inspected());
            graphics.drawString(font, "Оснастка: " + level + "/4     Парус: " + fabric + "/3",
                    202, 67, DARK, false);
        }
        if (tab == 1 && vesselKind() == 2) {
            int tier = ShipyardUpgrades.shipFabric(menu.inspected());
            double power = tier == 0 ? 0.16D : tier == 1 ? 0.40D : tier == 2 ? 0.68D : 1D;
            graphics.drawString(font, "Тяга текущего паруса: " + (int)Math.round(power * 100D) + "% (базовая оснастка)",
                    202, 77, DARK, false);
        }
        if (tab == 1 && vesselKind() == 0)
            graphics.drawString(font, "Выберите плот или корабль слева.", 202, 85, DARK, false);
        else if (costs().isEmpty()) {
            graphics.drawString(font, tab == 1 ? "Материалы не требуются" :
                    "Выберите чертёж", 202, 133, DARK, false);
        } else {
            List<Cost> req = costs();
            int baseY = tab == 0 ? 126 : 94;
            for (int i = 0; i < req.size(); i++) {
                Cost cost = req.get(i);
                int col = i % 2, row = i / 2, x = 201 + col * 132, y = baseY + row * 16;
                graphics.renderItem(cost.icon(), x, y);
                graphics.drawString(font, cost.title(), x + 19, y, DARK, false);
                graphics.drawString(font, cost.have() + "/" + cost.need(), x + 19, y + 8,
                        cost.have() >= cost.need() ? 0xff22612b : 0xffa13726, false);
            }
        }
        if (tab == 0 && !menu.inspected().isEmpty())
            graphics.drawString(font, "Заберите готовое судно из ячейки", 194, 176, 0xff942b25, false);
        graphics.drawString(font, "Инвентарь", 198, 199, 0xfff2d9aa, false);
    }

    /**
     * Orthographic technical views of the existing real voxel footprint.
     * These are intentionally not decorative models: each colored cell is a
     * distinct physical block column of the current buildable blueprint.
     */
    private void drawBlueprint(GuiGraphics graphics) {
        int width = chosen == 4 ? FirstShipHull.WIDTH :
                chosen == 0 || chosen == 1 ? 3 : 5;
        int length = chosen == 4 ? FirstShipHull.LENGTH :
                chosen == 0 || chosen == 2 ? 3 : chosen == 1 ? 5 : 7;
        final int blue = 0xff368fbd;
        final int edge = 0xff255572;
        final int grid = 0xffb6a57e;
        graphics.drawString(font, "СВЕРХУ", 204, 66, DARK, false);
        graphics.drawString(font, "СБОКУ", 296, 66, DARK, false);
        graphics.drawString(font, "СПЕРЕДИ", 394, 66, DARK, false);
        int cell = 4;
        int originX = 226 - width * cell / 2;
        int originY = 75;
        for (int z = 0; z < length; z++) {
            int halfBeam = chosen == 4 ? (int)Math.floor(FirstShipHull.halfBeam(z - length / 2)) : width / 2;
            for (int x = 0; x < width; x++) {
                if (chosen == 4 && Math.abs(x - width / 2) > halfBeam) continue;
                int bx = originX + x * cell, by = originY + z * cell;
                graphics.fill(bx, by, bx + 3, by + 3, blue);
            }
        }
        for (int z = 0; z < length; z++) {
            int bx = 297 + z * 4, by = 85;
            int height = chosen == 4 ? 5 : 1;
            graphics.fill(bx, by, bx + 3, by + height * 4, edge);
        }
        for (int x = 0; x < width; x++) {
            int bx = 405 + x * 4, by = 85;
            int height = chosen == 4 ? 5 : 1;
            graphics.fill(bx, by, bx + 3, by + height * 4, blue);
        }
        graphics.fill(194, 118, 461, 119, grid);
        graphics.drawString(font, "Корпус: " + width + " × " + length + " блоков",
                203, 110, DARK, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
