package org.slavicmyths.tales;

import com.google.gson.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;

/** Reloadable definitions; sparse player knowledge lives separately in TaleData. */
public final class TaleCatalog extends SimpleJsonResourceReloadListener {
    public record Entry(String id,String part,String chapter,String section,String title,String icon,
                        int initial,List<String> subjects,List<String> advancements,List<String> links) {
        public CompoundTag snapshot(){CompoundTag n=new CompoundTag();n.putString("Id",id);n.putString("Part",part);n.putString("Chapter",chapter);n.putString("Section",section);n.putString("Title",title);n.putString("Icon",icon);if(subjects.contains("entity/"+id))n.putString("Entity",id);n.putInt("Initial",initial);ListTag recipes=new ListTag();for(String s:subjects)if(s.startsWith("recipe/"))recipes.add(StringTag.valueOf(s.substring(7)));n.put("Recipes",recipes);ListTag l=new ListTag();for(String s:links)l.add(StringTag.valueOf(s));n.put("Links",l);return n;}
    }
    private static volatile Map<String,Entry> entries=Map.of();
    private static volatile Map<String,List<Entry>> subjects=Map.of(),advancements=Map.of();
    public TaleCatalog(){super(new Gson(),"tales");}
    public static Collection<Entry> entries(){return entries.values();}
    public static Entry entry(String id){return entries.get(id);}
    public static List<Entry> subject(String id){return subjects.getOrDefault(id,List.of());}
    public static List<Entry> advancement(String id){return advancements.getOrDefault(id,List.of());}
    private static String string(JsonObject n,String key){String s=n.get(key).getAsString();if(s.length()>128)throw new IllegalArgumentException("Oversized "+key);return s;}
    private static List<String> list(JsonObject n,String key){List<String> result=new ArrayList<>();if(n.has(key))for(JsonElement v:n.getAsJsonArray(key)){if(result.size()>=2048)throw new IllegalArgumentException("Oversized "+key);result.add(v.getAsString());}return List.copyOf(result);}
    @Override protected void apply(Map<ResourceLocation,JsonElement> data,ResourceManager manager,ProfilerFiller profiler){
        Map<String,Entry> loaded=new TreeMap<>();Map<String,List<Entry>> bySubject=new HashMap<>(),byAdv=new HashMap<>();
        data.forEach((id,value)->{try{JsonObject n=value.getAsJsonObject();Entry e=new Entry(id.toString(),string(n,"part"),string(n,"chapter"),string(n,"section"),string(n,"title"),string(n,"icon"),Math.clamp(n.get("initial").getAsInt(),0,1),list(n,"subjects"),list(n,"advancements"),list(n,"links"));loaded.put(e.id,e);for(String s:e.subjects)bySubject.computeIfAbsent(s,k->new ArrayList<>()).add(e);for(String s:e.advancements)byAdv.computeIfAbsent(s,k->new ArrayList<>()).add(e);}catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().error("Invalid tale entry {}: {}",id,failure.toString());}});
        entries=Collections.unmodifiableMap(loaded);subjects=freeze(bySubject);advancements=freeze(byAdv);TaleService.invalidateRecipes();
    }
    private static Map<String,List<Entry>> freeze(Map<String,List<Entry>> source){Map<String,List<Entry>> result=new HashMap<>();source.forEach((id,v)->result.put(id,List.copyOf(v)));return Map.copyOf(result);}
}
