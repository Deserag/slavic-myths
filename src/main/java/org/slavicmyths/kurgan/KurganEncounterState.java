package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.nbt.*;

/** Missing/unloaded UUIDs are never interpreted as death and never respawned. */
public final class KurganEncounterState {
    public boolean triggered,cleared;public final Set<UUID> alive=new HashSet<>();
    public boolean start(){if(triggered)return false;triggered=true;return true;}
    public boolean death(UUID id){if(!alive.remove(id))return false;if(alive.isEmpty())cleared=true;return true;}
    public CompoundNBT save(){CompoundNBT n=new CompoundNBT();n.putBoolean("Triggered",triggered);n.putBoolean("Cleared",cleared);ListNBT ids=new ListNBT();for(UUID id:alive)ids.add(StringNBT.valueOf(id.toString()));n.put("Alive",ids);return n;}
    public static KurganEncounterState load(CompoundNBT n){KurganEncounterState s=new KurganEncounterState();s.triggered=n.getBoolean("Triggered");s.cleared=n.getBoolean("Cleared");for(INBT id:n.getList("Alive",8))s.alive.add(UUID.fromString(id.getAsString()));return s;}
}
