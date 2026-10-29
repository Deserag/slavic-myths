package org.slavicmyths.registry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Presentation metadata only; independent of Minecraft, item suppliers and client classes. */
public final class CreativeCatalog {
    public static final List<String> TABS = List.of("slavicmyths", "resources", "farming", "food",
            "tools", "armor", "decor", "magic", "mobs", "weapons", "accessories", "heads");
    public record Entry(String tab, int order, String group) { }
    private static final Map<String, Entry> ENTRIES = load();
    private static Map<String, Entry> load() {
        var stream = CreativeCatalog.class.getResourceAsStream("/assets/slavicmyths/creative_catalog.tsv");
        if (stream == null) throw new IllegalStateException("Missing creative inventory catalog");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) { return read(reader); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static Map<String, Entry> read(Reader source) throws IOException {
        var entries = new LinkedHashMap<String, Entry>();
        var orders = new HashMap<String, Integer>();
        var reader = new BufferedReader(source);
        for (String line; (line = reader.readLine()) != null;) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] fields = line.split("\t", -1);
            if (fields.length != 3 || !fields[0].matches("[a-z0-9_]+") || fields[2].isBlank()
                    || !(TABS.contains(fields[1]) || fields[1].equals("hidden"))) {
                throw new IllegalArgumentException("Invalid creative catalog row: " + line);
            }
            int order = orders.merge(fields[1], 1, Integer::sum) - 1;
            if (entries.putIfAbsent(fields[0], new Entry(fields[1], order, fields[2])) != null)
                throw new IllegalArgumentException("Duplicate creative catalog ID: " + fields[0]);
        }
        return Collections.unmodifiableMap(entries);
    }
    public static Entry entry(String path) { return ENTRIES.get(path); }
    public static Map<String, Entry> entries() { return ENTRIES; }
    public static int category(String path) {
        var entry = entry(path);
        return entry == null || entry.tab().equals("hidden") ? -1 : TABS.indexOf(entry.tab());
    }
    public static int order(String path) {
        var entry = entry(path);
        return entry == null ? Integer.MAX_VALUE : entry.order();
    }
    private CreativeCatalog() { }
}
