package org.slavicmyths.brewing;

import net.minecraft.nbt.CompoundTag;
import java.util.*;

/** Plain JVM assertions only: never constructs a client, world, server or GameTest. */
public final class FermentationOffline {
    private static int checks;
    private static void need(boolean ok) { checks++; if (!ok) throw new AssertionError("check " + checks); }
    public static void main(String[] args) {
        // Data catalog only; no game registry bootstrap or loader.
        List<String> drinks = List.of("light_beer", "dark_beer", "hopped_beer", "honey_beer",
                "forest_beer", "thunder_beer", "veles_dark_beer", "witch_berry_beer", "kvass", "mead", "apple_cider");
        int[] ages = {0, 2399, 2400, 8399, 8400, 11999, 12000};
        String[] qualities = {"young", "young", "ready", "ready", "aged", "aged", "spoiled"};
        for (String beverage : drinks) {
            for (int a = 0; a < ages.length; a++) for (int servings = 1; servings <= 8; servings++) {
                CompoundTag legacy = new CompoundTag(), brew = new CompoundTag();
                brew.putString("Beverage", beverage); brew.putInt("Age", ages[a]); brew.putInt("Servings", servings);
                legacy.put("Fermentation", brew);
                Fermentation first = new Fermentation(); first.load(legacy, drinks);
                need(first.active() && first.servings == servings && first.age == ages[a] && first.beverage.equals(beverage));
                need(BrewRules.quality(first.age).equals(qualities[a]));
                for (String container : new String[]{"wooden_mug", "dark_bottle", "ceramic_pitcher", "stone", "air"}) {
                    boolean pitcher = Set.of("kvass", "mead", "apple_cider").contains(beverage), mug = beverage.endsWith("beer") || beverage.equals("kvass");
                    int expected = container.equals("ceramic_pitcher") && pitcher ? Math.min(4, servings)
                            : container.equals("dark_bottle") || container.equals("wooden_mug") && mug ? 1 : 0;
                    int amount = BrewRules.pourCount(container, pitcher, mug, servings);
                    need(amount == expected && amount >= 0 && amount <= servings);
                    if (amount > 0) {
                        int remaining = servings, total = 0;
                        while (remaining > 0) { int poured = BrewRules.pourCount(container, pitcher, mug, remaining); remaining -= poured; total += poured; }
                        need(total == servings && BrewRules.pourCount(container, pitcher, mug, remaining) == 0);
                    }
                }
                CompoundTag saved = new CompoundTag(); first.save(saved);
                CompoundTag expected = legacy.copy(); expected.getCompound("Fermentation").putBoolean("JourneyAwarded", false);
                need(saved.equals(expected));
                Fermentation second = new Fermentation(); second.load(saved, drinks);
                need(second.servings == servings && second.age == ages[a] && second.beverage.equals(beverage));
                // Partially drained legacy batches must persist their exact remaining count.
                second.servings--; CompoundTag drained = new CompoundTag(); second.save(drained);
                Fermentation third = new Fermentation(); third.load(drained, drinks);
                need(third.servings == servings - 1 && third.active() == (servings > 1));
            }
        }
        CompoundTag invalid = new CompoundTag(), brew = new CompoundTag(); invalid.put("Fermentation", brew);
        brew.putString("Beverage", "not_a_drink"); brew.putInt("Servings", 8);
        Fermentation f = new Fermentation(); f.load(invalid, drinks); need(!f.active());
        brew.putString("Beverage", "kvass"); brew.putInt("Age", Integer.MAX_VALUE); brew.putInt("Servings", 100);
        f.load(invalid, drinks); need(f.age == 12000 && f.servings == 8);
        brew.putInt("Age", -50); brew.putInt("Servings", -1); f.load(invalid, drinks); need(f.age == 0 && !f.active());
        // Legacy household payload remains opaque to fermentation: slots, item IDs and components survive.
        CompoundTag household = new CompoundTag(), legacyBrew = new CompoundTag();
        net.minecraft.nbt.ListTag inventory = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < 9; slot++) {
            CompoundTag item = new CompoundTag(); item.putByte("Slot", (byte) slot);
            item.putString("id", slot == 0 ? "slavicmyths:wooden_barrel" : "minecraft:apple"); item.putInt("count", slot + 1);
            CompoundTag components = new CompoundTag(); components.putString("minecraft:custom_name", "\"Legacy slot " + slot + "\"");
            item.put("components", components); inventory.add(item);
        }
        household.put("Items", inventory); household.putInt("DataVersion", 1);
        legacyBrew.putString("Beverage", "kvass"); legacyBrew.putInt("Age", 8400); legacyBrew.putInt("Servings", 3);
        household.put("Fermentation", legacyBrew);
        CompoundTag before = household.copy(); f.load(household, drinks); f.save(household);
        before.getCompound("Fermentation").putBoolean("JourneyAwarded", false);
        need(household.equals(before)); need(f.servings == 3 && f.age == 8400);
        f.servings = 2; f.save(household);
        need(household.getList("Items", 10).equals(inventory)); need(household.getInt("DataVersion") == 1);
        CompoundTag tracked = new CompoundTag(), trackedBrew = new CompoundTag();
        trackedBrew.putString("Beverage", "light_beer"); trackedBrew.putInt("Age", 2400); trackedBrew.putInt("Servings", 8);
        UUID owner = UUID.fromString("dace75c4-06ba-39a8-b9bf-c66b92ee16f0");
        trackedBrew.putUUID("JourneyStarter", owner); trackedBrew.putBoolean("JourneyAwarded", true); tracked.put("Fermentation", trackedBrew);
        f.load(tracked, drinks); CompoundTag trackedSaved = new CompoundTag(); f.save(trackedSaved); need(trackedSaved.equals(tracked));
        trackedBrew.putBoolean("JourneyAwarded", false); f.load(tracked, drinks); trackedSaved = new CompoundTag(); f.save(trackedSaved); need(trackedSaved.equals(tracked));
        System.out.println("Fermentation offline NBT/quality assertions: " + checks + "; game launches: 0");
    }
}
