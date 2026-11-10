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
import org.slavicmyths.ships.FirstShipEntity;
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
            "Длина +", "Ширина +", "Парус 1 +", "Парус 2 +",
            "Парус 3 +", "Парус 4 +", "Мачта +", "Палуба +",
            "Мостик", "Каюта"
    };
    private static final int[] MODULAR_ACTIONS = {200, 201, 207, 208, 209, 210, 203, 204, 205, 206};
    private static final String[] MODULAR_CATEGORIES = {"Корпус", "Паруса и мачты", "Палубы", "Капитанские помещения"};
    private static final int[][] MODULAR_GROUPS = {{0, 1}, {6, 2, 3, 4, 5}, {7}, {8, 9}};
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
    private int modCategory;
    private boolean createRafts;
    private int selectedLength;
    private int selectedWidth;
    private int lastKind = -1;
    private String lastSelectedShip = "";
    private Button primary;

    public ShipyardScreen(ShipyardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 476;
        imageHeight = 282;
        inventoryLabelX = 198;
        inventoryLabelY = 180;
    }

    private int vesselKind() {
        ItemStack stack = menu.inspected();
        return stack.getItem() instanceof FirstShipItem ? 2
                : stack.getItem() instanceof RaftItem ? 1 : 0;
    }

    private ShipBuildConfiguration currentConfig() {
        ShipBuildConfiguration packed = ShipyardModular.configuration(menu.inspected());
        if (packed != null || !menu.inspected().isEmpty()
                || minecraft == null || minecraft.player == null) return packed;
        var candidates = minecraft.player.level().getEntitiesOfClass(
                FirstShipEntity.class,
                new net.minecraft.world.phys.AABB(menu.shipyardPosition()).inflate(30D, 18D, 30D),
                ship -> ship.isNativeHull() && !ship.isRemoved() && !ship.isMoving()
                        && !ship.isVehicle() && ship.modularConfiguration() != null);
        return candidates.size() == 1 ? candidates.get(0).modularConfiguration() : null;
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
        ShipBuildConfiguration current = currentConfig();
        if (current == null || chosen < 0 || chosen >= MODULAR_ACTIONS.length) return null;
        try {
            return switch (MODULAR_ACTIONS[chosen]) {
                case 200 -> current.resize(current.extraLength() + 1, current.extraWidth());
                case 201 -> current.resize(current.extraLength(), current.extraWidth() + 1);
                case 202 -> current.withSail(current.sailTiers().size() - 1,
                        current.sailTiers().get(current.sailTiers().size() - 1) + 1);
                case 207, 208, 209, 210 -> current.withSail(
                        MODULAR_ACTIONS[chosen] - 207,
                        current.sailTiers().get(MODULAR_ACTIONS[chosen] - 207) + 1);
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
        modCategory = 0;
        rebuild();
    }

    private void selectClass(int value) {
        int maximum = createRafts ? VESSELS.length - 1 : CLASS_NAMES.length - 1;
        chosen = Math.max(0, Math.min(maximum, value));
        if (!createRafts) {
            ShipSectionPlan.ShipClass kind = ShipSectionPlan.ShipClass.values()[chosen];
            selectedLength = Math.min(selectedLength, kind.maxLength);
            selectedWidth = Math.min(selectedWidth, kind.maxWidth);
        }
        rebuild();
    }

    private void selectModificationCategory(int category) {
        modCategory = category;
        chosen = MODULAR_GROUPS[category][0];
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        String[] tabs = {"Создание корабля", "Модификации"};
        for (int i = 0; i < tabs.length; i++) {
            final int selectedTab = i;
            Button button = Button.builder(Component.literal(tabs[i]),
                    clicked -> switchTab(selectedTab))
                    .bounds(leftPos + 12 + i * 230, topPos + 9, 222, 22).build();
            button.active = tab != i;
            addRenderableWidget(button);
        }

        if (tab == 0) {
            addRenderableWidget(Button.builder(
                    Component.literal(createRafts ? "Тип: плоты и 7×11" : "Тип: корабли"),
                    clicked -> { createRafts = !createRafts; chosen = 0;
                        selectedLength = 0; selectedWidth = 0; rebuild(); })
                    .bounds(leftPos + 16, topPos + 63, 154, 19).build());
            int maximum = createRafts ? VESSELS.length - 1 : CLASS_NAMES.length - 1;
            Button previous = Button.builder(Component.literal("◀"),
                    clicked -> selectClass(chosen - 1))
                    .bounds(leftPos + 17, topPos + 92, 28, 19).build();
            previous.active = chosen > 0;
            addRenderableWidget(previous);
            Button next = Button.builder(Component.literal("▶"),
                    clicked -> selectClass(chosen + 1))
                    .bounds(leftPos + 142, topPos + 92, 28, 19).build();
            next.active = chosen < maximum;
            addRenderableWidget(next);
            if (!createRafts) {
                ShipSectionPlan.ShipClass kind = ShipSectionPlan.ShipClass.values()[chosen];
                Button lengthMinus = Button.builder(Component.literal("−"),
                        clicked -> { selectedLength--; rebuild(); })
                        .bounds(leftPos + 110, topPos + 121, 27, 18).build();
                lengthMinus.active = selectedLength > 0;
                addRenderableWidget(lengthMinus);
                Button lengthPlus = Button.builder(Component.literal("+"),
                        clicked -> { selectedLength++; rebuild(); })
                        .bounds(leftPos + 143, topPos + 121, 27, 18).build();
                lengthPlus.active = selectedLength < kind.maxLength;
                addRenderableWidget(lengthPlus);
                Button widthMinus = Button.builder(Component.literal("−"),
                        clicked -> { selectedWidth--; rebuild(); })
                        .bounds(leftPos + 110, topPos + 147, 27, 18).build();
                widthMinus.active = selectedWidth > 0;
                addRenderableWidget(widthMinus);
                Button widthPlus = Button.builder(Component.literal("+"),
                        clicked -> { selectedWidth++; rebuild(); })
                        .bounds(leftPos + 143, topPos + 147, 27, 18).build();
                widthPlus.active = selectedWidth < kind.maxWidth;
                addRenderableWidget(widthPlus);
            }
        } else {
            ShipBuildConfiguration current = currentConfig();
            if (current != null) {
                modCategory = Math.max(0, Math.min(modCategory, MODULAR_CATEGORIES.length - 1));
                for (int i = 0; i < MODULAR_CATEGORIES.length; i++) {
                    final int category = i;
                    int x = 16 + (i % 2) * 80, y = 63 + (i / 2) * 24;
                    String title = switch (i) {
                        case 0 -> "Корпус";
                        case 1 -> "Мачты";
                        case 2 -> "Палубы";
                        default -> "Помещения";
                    };
                    Button button = Button.builder(Component.literal(modCategory == i ? "» " + title : title),
                            clicked -> selectModificationCategory(category))
                            .bounds(leftPos + x, topPos + y, 75, 20).build();
                    button.active = true;
                    addRenderableWidget(button);
                }
                int[] options = MODULAR_GROUPS[modCategory];
                boolean found = false;
                for (int option : options) if (option == chosen) found = true;
                if (!found) chosen = options[0];
                for (int index = 0; index < options.length; index++) {
                    final int option = options[index];
                    int x = 16 + (index % 2) * 80, y = 118 + (index / 2) * 23;
                    Button button = Button.builder(Component.literal(chosen == option ? "» " + MODULAR_OPTIONS[option] : MODULAR_OPTIONS[option]),
                            clicked -> { chosen = option; rebuild(); })
                            .bounds(leftPos + x, topPos + y, 75, 20).build();
                    button.active = true;
                    addRenderableWidget(button);
                }
            } else {
                String[] options = vesselKind() == 2 ? SHIP_OPTIONS
                        : vesselKind() == 1 ? RAFT_OPTIONS : new String[0];
                chosen = Math.max(0, Math.min(chosen, Math.max(0, options.length - 1)));
                for (int i = 0; i < options.length; i++) {
                    final int selected = i;
                    int x = 16 + (i % 2) * 80, y = 93 + (i / 2) * 22;
                    Button button = Button.builder(Component.literal(options[i]),
                            clicked -> { chosen = selected; rebuild(); })
                            .bounds(leftPos + x, topPos + y, 75, 19).build();
                    button.active = chosen != i;
                    addRenderableWidget(button);
                }
            }
        }
        primary = Button.builder(Component.literal(tab == 0
                ? createRafts && chosen < 4 ? "Создать плот" : "Создать корабль"
                : "Применить улучшение"),
                clicked -> execute())
                .bounds(leftPos + 17, topPos + 245, 153, 22).build();
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
            action = currentConfig() != null
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
        ShipBuildConfiguration built = currentConfig();
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
        if (tab == 1 && vesselKind() == 0 && currentConfig() == null) return false;
        if (!minecraft.player.getAbilities().instabuild)
            for (Cost cost : costs()) if (cost.have() < cost.need()) return false;

        if (tab == 0) return createRafts || creationConfig() != null;
        ShipBuildConfiguration modular = currentConfig();
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
        ShipBuildConfiguration shown = currentConfig();
        String key = shown == null ? "" : shown.wire();
        if (lastKind != kind || !key.equals(lastSelectedShip)) {
            lastKind = kind;
            lastSelectedShip = key;
            chosen = 0;
            rebuild();
        }
        updateButton();
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, FRAME);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3,
                topPos + imageHeight - 3, WOOD);
        graphics.fill(leftPos + 10, topPos + 39, leftPos + 184, topPos + 274, 0xff694e36);
        graphics.fill(leftPos + 190, topPos + 39, leftPos + 466, topPos + 191, PAPER);
        graphics.fill(leftPos + 192, topPos + 41, leftPos + 464, topPos + 189, 0xfff0e4c4);
        graphics.fill(leftPos + 190, topPos + 193, leftPos + 466, topPos + 274, 0xff745335);
        graphics.fill(leftPos + 14, topPos + 238, leftPos + 174, topPos + 271, 0xff38281d);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                graphics.fill(leftPos + 197 + col * 18, topPos + 196 + row * 18,
                        leftPos + 215 + col * 18, topPos + 214 + row * 18, 0xff231b14);
        for (int col = 0; col < 9; col++)
            graphics.fill(leftPos + 197 + col * 18, topPos + 253,
                    leftPos + 215 + col * 18, topPos + 271, 0xff231b14);
        graphics.fill(leftPos + 138, topPos + 203, leftPos + 175, topPos + 233, 0xffb59366);
        graphics.fill(leftPos + 140, topPos + 205, leftPos + 173, topPos + 231, 0xff3c2b1e);
        graphics.fill(leftPos + 152, topPos + 209, leftPos + 171, topPos + 228, 0xff241a13);
        graphics.fill(leftPos + 15, topPos + 233, leftPos + 173, topPos + 234, 0xffa77f4b);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Корабельная верфь", 17, 45, 0xfff8e1b5, false);
        ShipBuildConfiguration current = currentConfig();
        ShipBuildConfiguration shown = tab == 0 && !createRafts ? creationConfig() : current;
        if (tab == 0) {
            String name = createRafts ? VESSELS[chosen] : CLASS_NAMES[chosen];
            graphics.drawString(font, font.plainSubstrByWidth(name, 89),
                    94 - font.width(font.plainSubstrByWidth(name, 89)) / 2, 98,
                    0xfff8e1b5, false);
            if (!createRafts && shown != null) {
                ShipSectionPlan.ShipClass type = shown.shipClass();
                int maximumLength = ShipBuildConfiguration.hullLength(type, type.maxLength);
                int maximumWidth = ShipBuildConfiguration.hullWidth(type, type.maxWidth);
                graphics.drawString(font, "Длина", 18, 126, 0xfff2dfbd, false);
                graphics.drawString(font, shown.length() + "/" + maximumLength,
                        61, 126, 0xfff2dfbd, false);
                graphics.drawString(font, "Ширина", 18, 152, 0xfff2dfbd, false);
                graphics.drawString(font, shown.width() + "/" + maximumWidth,
                        61, 152, 0xfff2dfbd, false);
                graphics.drawString(font, "Мачты: 1/" + type.maxMasts
                        + "  Палубы: 1/" + type.maxDecks, 18, 177, 0xffeacfa2, false);
                graphics.drawString(font, "Шаг: длина +4, ширина +2", 18, 190, 0xffeacfa2, false);
            } else if (createRafts) {
                graphics.drawString(font, "Готовый корпус плота или", 18, 131, 0xfff2dfbd, false);
                graphics.drawString(font, "старого корабля 7×11", 18, 143, 0xfff2dfbd, false);
            }
        } else if (current != null) {
            graphics.drawString(font, "Размер " + current.width() + "×" + current.length()
                    + " | мачт " + current.sailTiers().size(), 17, 200, 0xfff2dfbd, false);
            ShipBuildConfiguration next = nextConfig();
            if (next == null || next.equals(current))
                graphics.drawString(font, "Недоступно: предел класса", 17, 188,
                        0xffffb18c, false);
            else
                graphics.drawString(font, font.plainSubstrByWidth(
                                "Раздел: " + MODULAR_CATEGORIES[modCategory], 160),
                        17, 188, 0xffeacfa2, false);
        } else {
            graphics.drawString(font, vesselKind() == 0
                    ? "Положите судно в ячейку"
                    : "Улучшение старого судна", 17, 65, 0xfff2dfbd, false);
        }
        graphics.drawString(font, "СУДНО", 18, 209, 0xfff2dfbd, false);
        graphics.drawString(font, "Вставьте корабль", 18, 219, 0xffeacfa2, false);
        if (tab == 1) graphics.drawString(font,
                "Ресурсы — из инвентаря", 18, 231, 0xffeacfa2, false);

        String heading = tab == 0
                ? createRafts ? VESSELS[chosen] : "Проект: " + CLASS_NAMES[chosen]
                : current != null ? "Изменение: " + MODULAR_OPTIONS[chosen]
                : vesselKind() == 0 ? "Нет судна для модификации"
                : vesselKind() == 2 ? SHIP_OPTIONS[chosen] : RAFT_OPTIONS[chosen];
        graphics.drawString(font, font.plainSubstrByWidth(heading, 254),
                200, 46, DARK, false);
        if (tab == 0 || current != null) drawBlueprint(graphics);
        if (tab == 1 && current == null && vesselKind() == 2) {
            int rig = ShipyardUpgrades.shipRigLevel(menu.inspected());
            int cloth = ShipyardUpgrades.shipFabric(menu.inspected());
            graphics.drawString(font, "Оснастка: " + rig + "/4   Ткань: " + cloth + "/3",
                    201, 88, DARK, false);
        }
        graphics.drawString(font, "Материалы", 200, 114, DARK, false);
        if (minecraft != null && minecraft.player != null
                && minecraft.player.getAbilities().instabuild)
            graphics.drawString(font, "Креатив: бесплатно", 351, 114, 0xff27663c, false);
        List<Cost> requirements = costs();
        if (requirements.isEmpty()) {
            graphics.drawString(font, tab == 0 ? "Выберите чертёж" :
                    current != null && nextConfig() == null ? "Изменение недоступно" :
                    "Дополнительные ресурсы не нужны", 201, 133,
                    tab == 1 && current != null && nextConfig() == null
                            ? 0xffad3838 : DARK, false);
        } else {
            for (int i = 0; i < Math.min(12, requirements.size()); i++) {
                Cost cost = requirements.get(i);
                int column = i % 3, row = i / 3, x = 200 + column * 89, y = 123 + row * 17;
                graphics.renderItem(cost.icon(), x, y);
                graphics.drawString(font, font.plainSubstrByWidth(cost.title(), 69),
                        x + 18, y, DARK, false);
                boolean enough = cost.have() >= cost.need()
                        || minecraft != null && minecraft.player != null
                        && minecraft.player.getAbilities().instabuild;
                graphics.drawString(font, minecraft != null && minecraft.player != null
                        && minecraft.player.getAbilities().instabuild
                        ? "Бесплатно" : cost.have() + "/" + cost.need(),
                        x + 18, y + 8, enough ? 0xff22612b : 0xffa13726, false);
            }
        }
        graphics.drawString(font, "Инвентарь", 198, 182, 0xfff2d9aa, false);
        if (tab == 0 && !menu.inspected().isEmpty())
            graphics.drawString(font, "Заберите готовое судно", 17, 226, 0xffffb18c, false);
    }

    /**
     * Orthographic technical views of the existing real voxel footprint.
     * These are intentionally not decorative models: each colored cell is a
     * distinct physical block column of the current buildable blueprint.
     */
    /**
     * Compact schematic with ONE consistent scale per view.
     *
     * Never stretch the 25x45 deck into a 68x29 rectangle: the previous
     * independent X/Z scales turned every keel into a barcode. Existing blocks
     * use wooden colors; changed sections are outlined green.
     */
    private void drawBlueprint(GuiGraphics graphics) {
        ShipBuildConfiguration original = tab == 1 ? currentConfig() : null;
        ShipBuildConfiguration candidate = tab == 0 ? creationConfig() : nextConfig();
        ShipBuildConfiguration design = candidate != null ? candidate : original;
        final int timber = 0xff98653d, deck = 0xffc79861;
        final int rail = 0xff68452b, highlight = 0xff2c9957;
        final int sail = 0xffded2b6, ink = 0xff40301e;
        graphics.drawString(font, "ПЛАН", 221, 58, ink, false);
        graphics.drawString(font, "ПРОФИЛЬ", 304, 58, ink, false);
        graphics.drawString(font, "НОС", 416, 58, ink, false);

        if (design == null) {
            int width = chosen == 4 ? FirstShipHull.WIDTH
                    : chosen == 0 || chosen == 1 ? 3 : 5;
            int length = chosen == 4 ? FirstShipHull.LENGTH
                    : chosen == 0 || chosen == 2 ? 3 : chosen == 1 ? 5 : 7;
            int x = 246 - width * 2, y = 83 - length * 2;
            graphics.fill(x, y, x + width * 4, y + length * 4, deck);
            graphics.fill(x, y, x + width * 4, y + 1, rail);
            graphics.fill(304, 91, 350, 99, timber);
            graphics.fill(414, 92, 444, 101, timber);
            graphics.drawString(font, width + "×" + length, 204, 104, ink, false);
            return;
        }

        int width = design.width(), length = design.length(), halfLength = length / 2;
        double topScale = Math.min(43D / width, 38D / length);
        int cx = 246, topY = 67;
        for (int z = -halfLength; z <= halfLength; z++) {
            int beam = ShipHullVoxelLayout.halfBeam(design.structure(), z);
            int oldBeam = original == null || Math.abs(z) > original.length() / 2
                    ? -1 : ShipHullVoxelLayout.halfBeam(original.structure(), z);
            int top = topY + (int)Math.floor((z + halfLength) * topScale);
            int bottom = topY + (int)Math.ceil((z + halfLength + 1) * topScale);
            int left = cx - (int)Math.ceil((beam + .5D) * topScale);
            int right = cx + (int)Math.ceil((beam + .5D) * topScale);
            graphics.fill(left, top, right + 1, Math.max(top + 1, bottom), deck);
            graphics.fill(left, top, left + 1, Math.max(top + 1, bottom), rail);
            graphics.fill(right, top, right + 1, Math.max(top + 1, bottom), rail);
            if (original != null && (oldBeam < 0 || beam > oldBeam)) {
                graphics.fill(left, top, left + 1, Math.max(top + 1, bottom), highlight);
                graphics.fill(right, top, right + 1, Math.max(top + 1, bottom), highlight);
            }
        }
        for (int mast = 0; mast < design.sailTiers().size(); mast++) {
            int z = design.structure().mastPositions().get(mast);
            int y = topY + (int)Math.round((z + halfLength + .5D) * topScale);
            int color = original == null || mast < original.sailTiers().size()
                    && original.sailTiers().get(mast).equals(design.sailTiers().get(mast))
                    ? ink : highlight;
            graphics.fill(cx - 3, y, cx + 4, y + 2, color);
        }

        // Profile: a shaped timber hull with elevated bow/stern and
        // independently sized masts; not a repeated column barcode.
        int sideX = 300, bottomY = 101;
        for (int z = -halfLength; z <= halfLength; z++) {
            int x = sideX + (int)Math.floor((z + halfLength) * 85D / length);
            int nextX = sideX + (int)Math.ceil((z + halfLength + 1) * 85D / length);
            int end = Math.abs(z) - (halfLength - 4);
            int lift = end > 0 ? Math.min(4, end) : 0;
            int top = 94 - lift, bottom = bottomY - lift / 2;
            graphics.fill(x, top, Math.max(x + 1, nextX), bottom, timber);
            graphics.fill(x, top, Math.max(x + 1, nextX), top + 1, rail);
            if (original != null && Math.abs(z) > original.length() / 2)
                graphics.fill(x, top, Math.max(x + 1, nextX), top + 1, highlight);
            for (int floor = 2; floor <= design.decks(); floor++) {
                int level = top - 4 * (floor - 1);
                graphics.fill(x, level, Math.max(x + 1, nextX), level + 1,
                        original != null && original.decks() >= floor ? rail : highlight);
            }
        }
        for (int mast = 0; mast < design.sailTiers().size(); mast++) {
            int z = design.structure().mastPositions().get(mast);
            int x = sideX + (int)Math.round((z + halfLength) * 85D / length);
            int tier = design.sailTiers().get(mast);
            int top = tier == 1 ? 79 : tier == 2 ? 74 : 69;
            boolean changed = original != null
                    && (mast >= original.sailTiers().size()
                        || !original.sailTiers().get(mast).equals(tier));
            int tone = changed ? highlight : rail;
            graphics.fill(x, top - 2, x + 2, 94, tone);
            graphics.fill(x - 5, top, x + 7, top + 2, tone);
            for (int y = top + 2; y < 89; y++)
                graphics.fill(x + 2, y, x + 5, y + 1, sail);
        }
        if (design.cabin()) graphics.fill(307, 82, 326, 94,
                original != null && original.cabin() ? timber : highlight);
        if (design.bridge()) graphics.fill(306, 79, 327, 81,
                original != null && original.bridge() ? rail : highlight);

        // Head-on bow shape, centered, with real width proportion.
        int frontCenter = 428, frontHalf = (int)Math.round(Math.min(23D, width * 23D / 29D));
        graphics.fill(frontCenter - frontHalf, 94, frontCenter + frontHalf + 1, 97, timber);
        graphics.fill(frontCenter - frontHalf + 3, 97, frontCenter + frontHalf - 2, 100, timber);
        graphics.fill(frontCenter - Math.max(2, frontHalf / 2), 100,
                frontCenter + Math.max(2, frontHalf / 2) + 1, 102, rail);
        graphics.fill(frontCenter - frontHalf, 91,
                frontCenter - frontHalf + 2, 95, rail);
        graphics.fill(frontCenter + frontHalf - 1, 91,
                frontCenter + frontHalf + 1, 95, rail);
        graphics.drawString(font, width + "×" + length + " | "
                + design.sailTiers().size() + " мачт. | "
                + design.decks() + " пал.", 202, 105, ink, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        // Extended ship item tooltips obscure the entire upgrade menu.
        // Hold Shift to display the full description on demand.
        if (hoveredSlot == null || hoveredSlot.index != 0 || hasShiftDown())
            renderTooltip(graphics, mouseX, mouseY);
    }
}
