package org.slavicmyths.verify;

import java.io.StringReader;
import java.util.*;
import org.slavicmyths.registry.CreativeCatalog;

/** Java catalog/parser contracts only. No Minecraft classes, bootstrap, client, server or world. */
public final class CreativeCatalogChecks {
    private static int checks;
    private static void check(boolean ok, String label) { checks++; if (!ok) throw new AssertionError(label); }
    private static void invalid(String rows) throws Exception {
        try { CreativeCatalog.read(new StringReader(rows)); throw new AssertionError("Invalid catalog accepted"); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    public static void main(String[] args) throws Exception {
        var entries = CreativeCatalog.entries();
        check(entries.size() == 849, "849 source-audited IDs");
        invalid("sword\tweapons\ta\nsword\ttools\tb\n");
        invalid("sword\tunknown\ta\n");
        invalid("sword\tweapons\n");
        invalid("BAD ID\tweapons\ta\n");
        invalid("sword\tweapons\t\n");
        var parsed = CreativeCatalog.read(new StringReader("# comment\n\nsword\tweapons\ta\nbook\tslavicmyths\tb\nknife\tweapons\tc\n"));
        check(parsed.get("sword").order() == 0 && parsed.get("knife").order() == 1 && parsed.get("book").order() == 0, "Independent tab order");
        try { entries.clear(); throw new AssertionError("Mutable catalog"); }
        catch (UnsupportedOperationException expected) { checks++; }
        check(CreativeCatalog.entry("unregistered_future_item") == null, "Unknown is distinguishable from intentionally hidden");
        check(CreativeCatalog.order("unregistered_future_item") == Integer.MAX_VALUE, "Fallback follows known items");
        check(CreativeCatalog.category("bear_cub_spawn_egg") == -1 && CreativeCatalog.category("doe_spawn_egg") == -1, "Family eggs remain hidden");
        for (var entry : entries.entrySet()) {
            var e = entry.getValue();
            check(e.tab().equals("hidden") || CreativeCatalog.category(entry.getKey()) >= 0, "Valid category " + entry.getKey());
        }
        for (var tab : CreativeCatalog.TABS) {
            var orders = entries.values().stream().filter(e -> e.tab().equals(tab)).map(CreativeCatalog.Entry::order).sorted().toList();
            check(!orders.isEmpty(), "Nonempty tab " + tab);
            for (int i = 0; i < orders.size(); i++) check(orders.get(i) == i, "Contiguous order " + tab);
        }
        check(CreativeCatalog.category("karachun_scythe") == 9 && CreativeCatalog.category("morozko_staff") == 9, "Boss combat items");
        check(CreativeCatalog.category("butchering_knife") == 4 && CreativeCatalog.category("perunite_axe") == 4, "Work purpose overrides sword/axe type");
        check(CreativeCatalog.category("three_breath_crossbow") == 9, "Special ranged weapon");
        check(CreativeCatalog.category("thunder_crystal_powder") == 7, "Magical brewing component");
        check(CreativeCatalog.category("wheat_flour") == 3 && CreativeCatalog.category("hearth") == 6, "Kitchen purpose");
        System.out.println("CREATIVE_CATALOG_OFFLINE_PASS checks=" + checks + " items=" + entries.size() + " game_starts=0");
    }
}
