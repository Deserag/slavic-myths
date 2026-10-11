package org.slavicmyths.client;
import org.slavicmyths.underground.EncounterPopulation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import java.util.*;
/** Actual encounter SavedData roundtrip, not an in-game relog. */
public final class UndergroundSaveChecks {
 public static void run() throws Exception {var d=new EncounterPopulation();var pending=EncounterPopulation.class.getDeclaredField("pending");var planned=EncounterPopulation.class.getDeclaredField("planned");pending.setAccessible(true);planned.setAccessible(true);var type=Class.forName("org.slavicmyths.underground.EncounterPopulation$Slot");var constructor=type.getDeclaredConstructors()[0];constructor.setAccessible(true);String key="minecraft:overworld:root-17:INDEPENDENT_GREAT:large";Object slot=constructor.newInstance(key,"large_podzemnik",0,new BlockPos(-401,28,900),new BoundingBox(-425,28,878,-375,60,928),12345L);((Map)pending.get(d)).put(key,slot);((Set)planned.get(d)).add("minecraft:overworld:root-17");var serialized=d.save(new CompoundTag(),null);var load=EncounterPopulation.class.getDeclaredMethod("load",CompoundTag.class,net.minecraft.core.HolderLookup.Provider.class);load.setAccessible(true);var copy=(EncounterPopulation)load.invoke(null,serialized.copy(),null);if(!serialized.equals(copy.save(new CompoundTag(),null)))throw new AssertionError("slot/room/seed/planned roundtrip");copy.defeat(key);var after=(EncounterPopulation)load.invoke(null,copy.save(new CompoundTag(),null),null);if(!((Map)pending.get(after)).isEmpty()||!after.status().contains("defeated=1"))throw new AssertionError("defeated slot must not be queued again");System.out.println("SAVE PASS: real SavedData slot/planned/seed/bounds roundtrip and defeated suppresses queue; no game relog");}
}
