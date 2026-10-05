package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.nbt.*;

/** Missing/unloaded UUIDs are never interpreted as death and never respawned. */
public final class KurganEncounterState {
    public boolean triggered,cleared;public final Set<UUID> alive=new HashSet<>();
    public boolean start(){if(triggered)return false;triggered=true;return true;}
    public boolean death(UUID id){if(!alive.remove(id))return false;if(alive.isEmpty())cleared=true;return true;}
    public CompoundTag save(){CompoundTag n=new CompoundTag();n.putBoolean("Triggered",triggered);n.putBoolean("Cleared",cleared);ListTag ids=new ListTag();for(UUID id:alive)ids.add(StringTag.valueOf(id.toString()));n.put("Alive",ids);return n;}
    public static KurganEncounterState load(CompoundTag n){KurganEncounterState s=new KurganEncounterState();s.triggered=n.getBoolean("Triggered");s.cleared=n.getBoolean("Cleared");for(Tag id:n.getList("Alive",8))s.alive.add(UUID.fromString(id.getAsString()));return s;}
}
