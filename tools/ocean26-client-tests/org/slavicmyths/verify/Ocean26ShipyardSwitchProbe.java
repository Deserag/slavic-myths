package org.slavicmyths.verify;

import java.nio.file.Files;
import java.io.IOException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.ships.RaftContent;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipyardMenu;
import org.slavicmyths.ships.client.ShipyardScreen;

/**
 * Disposable real-client GUI regression fixture. No world cargo, blocks or
 * real inventory are modified; the synthetic menu is client-only.
 * The old implementation crashed when an index-6 modular mast was retained
 * while a six-entry legacy ship was rendered before the next menu tick.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class Ocean26ShipyardSwitchProbe {
    private static final boolean ENABLED = Boolean.getBoolean("ocean26.shipyardSwitch");
    private static int stage, since, noWorldTicks;
    private static ShipyardScreen screen;
    private static ShipyardMenu menu;
    private static ItemStack modular, modularMedium, modularLarge, modularLargest, legacy, raft;
    private static int transitions;
    private static int screenshots;

    private static void screenshot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), message ->
                System.out.println("OCEAN26_SHIPYARD_SCREENSHOT " + name
                        + " " + message.getString()));
        screenshots++;
    }

    private static ItemStack modularFixture(ShipSectionPlan.ShipClass type) {
        ItemStack stack = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 2);
        pack.putInt("GeometryVersion", 9);
        pack.putInt("RigLevel", 0);
        pack.putInt("FabricQuality", 0);
        pack.put(ShipBuildConfiguration.NBT_KEY, ShipBuildConfiguration.initial(
                type).save());
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return stack;
    }

    private static void verifyClass(ShipSectionPlan.ShipClass expected)
            throws ReflectiveOperationException {
        var cfg = org.slavicmyths.ships.ShipyardModular.configuration(menu.getSlot(0).getItem());
        if (cfg == null || cfg.shipClass() != expected || chosen() < 0 || chosen() >= 10)
            throw new AssertionError("invalid shipyard selection/class after swap to " + expected);
    }

    private static int chosen() throws ReflectiveOperationException {
        Method method = ShipyardScreen.class.getDeclaredMethod("chosen");
        method.setAccessible(true);
        return (int)method.invoke(screen);
    }

    /** Click using exactly the same inverse scaling as ShipyardScreen.render. */
    private static void click(int left, int top) {
        double scale = Math.min(1D, Math.max(.1D,
                Math.min((screen.width - 12D) / 476D, (screen.height - 12D) / 282D)));
        double x = (screen.width - 476D * scale) * .5D + left * scale;
        double y = (screen.height - 282D * scale) * .5D + top * scale;
        screen.mouseClicked(x, y, 0);
    }

    private static void swap(ItemStack next) {
        menu.getSlot(0).set(next.copy());
        transitions++;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!ENABLED || stage < 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            if (++noWorldTicks % 100 == 0)
                System.out.println("OCEAN26_GUI_STARTUP_WAIT ticks=" + noWorldTicks
                        + " screen=" + (mc.screen == null ? "null"
                            : mc.screen.getClass().getName()));
            if (noWorldTicks > 1200)
                finish(mc, false, "shipyardSwitch world did not open");
            return;
        }
        try {
            if (stage == 0) {
                modular = modularFixture(ShipSectionPlan.ShipClass.SMALL);
                modularMedium = modularFixture(ShipSectionPlan.ShipClass.MEDIUM);
                modularLarge = modularFixture(ShipSectionPlan.ShipClass.LARGE);
                modularLargest = modularFixture(ShipSectionPlan.ShipClass.LARGEST);
                legacy = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
                raft = new ItemStack(RaftContent.RAFT_ITEM.get());
                if (org.slavicmyths.ships.ShipyardModular.configuration(modular) == null)
                    throw new AssertionError("modular fixture did not decode");
                mc.options.guiScale().set(4);
                mc.resizeDisplay();
                menu = new ShipyardMenu(188, mc.player.getInventory(), mc.player.blockPosition());
                swap(modular);
                screen = new ShipyardScreen(menu, mc.player.getInventory(),
                        Component.literal("Ocean26 Shipyard Regression"));
                mc.setScreen(screen);
                stage = 1;
                since = 0;
                return;
            }
            if (mc.screen != screen) throw new AssertionError("shipyard screen was closed");
            if (++since < 3) return;
            since = 0;
            switch (stage++) {
                case 1 -> click(350, 20); // modifications tab
                case 2 -> click(133, 73); // masts category
                case 3 -> {
                    click(53, 128); // first action in category: mast #2 (index 6)
                    if (chosen() != 6) throw new AssertionError("did not select modular index 6");
                }
                case 4 -> {
                    screenshot(mc, "modular-mast-selection");
                    swap(legacy);
                    if (chosen() < 0 || chosen() >= 6)
                        throw new AssertionError("legacy ship kept incompatible modular index");
                }
                case 5 -> {
                    screenshot(mc, "legacy-after-modular-index-6");
                    swap(raft);
                    if (chosen() < 0 || chosen() >= 9)
                        throw new AssertionError("raft index out of bounds");
                }
                case 6 -> {
                    screenshot(mc, "raft-after-legacy");
                    swap(modular);
                    if (!ItemStack.isSameItemSameComponents(menu.getSlot(0).getItem(), modular))
                        throw new AssertionError("modular item NBT/components changed during switching");
                    if (chosen() < 0 || chosen() >= 10)
                        throw new AssertionError("modular action not restored");
                }
                case 7 -> click(53, 73); // hull category
                case 8 -> click(133, 20); // creation tab (first tab midpoint is 123)
                case 9 -> click(350, 20); // modifications tab again
                case 10 -> {
                    screenshot(mc, "modular-after-tab-switch");
                    swap(ItemStack.EMPTY);
                    if (chosen() >= 10)
                        throw new AssertionError("empty slot selected invalid action");
                }
                case 11 -> {
                    swap(new ItemStack(net.minecraft.world.item.Items.BREAD));
                    if (chosen() != -1)
                        throw new AssertionError("invalid item must not have an upgrade action");
                }
                case 12 -> {
                    screenshot(mc, "invalid-item-no-actions");
                    swap(legacy);
                    if (chosen() < 0 || chosen() >= 6)
                        throw new AssertionError("reinserted ship has invalid selection");
                }
                case 13 -> swap(modular);   // SMALL
                case 14 -> {
                    screenshot(mc, "small-modifications");
                    swap(modularMedium);
                }
                case 15 -> {
                    verifyClass(ShipSectionPlan.ShipClass.MEDIUM);
                    screenshot(mc, "medium-modifications");
                    swap(modularLarge);
                }
                case 16 -> {
                    verifyClass(ShipSectionPlan.ShipClass.LARGE);
                    screenshot(mc, "large-v6-modifications");
                    swap(modularLargest);
                }
                case 17 -> {
                    verifyClass(ShipSectionPlan.ShipClass.LARGEST);
                    screenshot(mc, "largest-v6-modifications");
                    swap(modular);
                }
                case 18 -> {
                    verifyClass(ShipSectionPlan.ShipClass.SMALL);
                    screenshot(mc, "small-after-full-class-cycle");
                    mc.options.guiScale().set(3);
                    mc.resizeDisplay();
                    System.out.println("OCEAN26_SCALE_REQUEST requested=3 option="
                            + mc.options.guiScale().get()
                            + " actual=" + mc.getWindow().getGuiScale());
                }
                case 19 -> {
                    System.out.println("OCEAN26_SCALE_VERIFY requested=3 option="
                            + mc.options.guiScale().get()
                            + " actual=" + mc.getWindow().getGuiScale());
                    // A real screenshot is always captured, even if the
                    // settings backend overrides the requested value.
                    screenshot(mc, "gui-scale-3");
                    if (Math.abs(mc.getWindow().getGuiScale() - 3D) > .05D)
                        throw new AssertionError("GUI scale 3 not applied");
                    mc.options.guiScale().set(2);
                    mc.resizeDisplay();
                }
                case 20 -> {
                    System.out.println("OCEAN26_SCALE_VERIFY requested=2 option="
                            + mc.options.guiScale().get()
                            + " actual=" + mc.getWindow().getGuiScale());
                    if (Math.abs(mc.getWindow().getGuiScale() - 2D) > .05D)
                        throw new AssertionError("GUI scale 2 not applied");
                    screenshot(mc, "gui-scale-2");
                    mc.options.guiScale().set(4);
                    mc.resizeDisplay();
                }
                case 21 -> {
                    System.out.println("OCEAN26_SCALE_VERIFY requested=4 option="
                            + mc.options.guiScale().get()
                            + " actual=" + mc.getWindow().getGuiScale());
                    if (Math.abs(mc.getWindow().getGuiScale() - 4D) > .05D)
                        throw new AssertionError("GUI scale 4 not restored");
                    screenshot(mc, "gui-scale-4");
                }
                case 22 -> {
                    if (screenshots != 13)
                        throw new AssertionError("incomplete shipyard screenshots " + screenshots);
                    System.out.println("OCEAN26_SHIPYARD_RENDER_SEQUENCE swaps=" + transitions
                            + " classes=SMALL,MEDIUM,LARGE,LARGEST"
                            + " guiScales=2,3,4"
                            + " screenshots=" + screenshots
                            + " finalIndex=" + chosen());
                    finish(mc, true, "shipyardSwitch");
                }
                default -> throw new AssertionError("unexpected stage " + stage);
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            finish(mc, false, "shipyardSwitch " + failure.getClass().getSimpleName()
                    + " " + failure.getMessage());
        }
    }

    private static void finish(Minecraft mc, boolean ok, String reason) {
        if (stage < 0) return;
        stage = -1;
        try {
            Files.writeString(mc.gameDirectory.toPath().resolve("ocean26-probe-result.txt"),
                    (ok ? "PASS " : "FAIL ") + reason);
        } catch (IOException error) {
            throw new IllegalStateException(error);
        }
        mc.stop();
    }
}
