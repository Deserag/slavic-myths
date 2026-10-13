package org.slavicmyths.hunt;
/** Stable save IDs; old boolean horn/encounter tags migrate without losing ownership. */
public enum HuntTarget {
 OVINNIK("ovinnik"), VOLKOLAK("volkolak"), FIRE_SERPENT("fire_serpent"), PODVEY("podvey");
 public static final com.mojang.serialization.Codec<HuntTarget> CODEC=com.mojang.serialization.Codec.STRING.comapFlatMap(id -> java.util.Arrays.stream(values())
  .filter(target -> target.id.equals(id)).findFirst().map(com.mojang.serialization.DataResult::success)
  .orElseGet(() -> com.mojang.serialization.DataResult.error(() -> "Unknown hunt target: "+id)),target -> target.id);
 public final String id;HuntTarget(String id){this.id=id;}
 public HuntTarget next(){return values()[(ordinal()+1)%values().length];}
 public static HuntTarget read(net.minecraft.nbt.CompoundTag n,String key){for(HuntTarget t:values())if(t.id.equals(n.getString(key)))return t;return n.getBoolean("Volkolak")||n.contains("Ovinnik")&&!n.getBoolean("Ovinnik")?VOLKOLAK:OVINNIK;}
}
