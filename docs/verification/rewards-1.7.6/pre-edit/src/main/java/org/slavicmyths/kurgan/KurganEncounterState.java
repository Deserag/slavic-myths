package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;

/** Persisted room state. Unloaded entities are not kills; each missing slot has one recovery. */
public final class KurganEncounterState {
    public boolean triggered,cleared;
    public final Set<UUID> alive=new HashSet<>(),retired=new HashSet<>();
    public final List<Slot> slots=new ArrayList<>();
    public static final class Slot {
        public final KurganFighter.Kind kind;
        public UUID uuid; public BlockPos last; public boolean defeated,restored;
        public Slot(KurganFighter.Kind kind){this.kind=kind;}
    }
    public void prepare(List<KurganFighter.Kind> roster){if(!triggered&&slots.isEmpty())for(var kind:roster)slots.add(new Slot(kind));}
    public boolean start(){if(triggered)return false;triggered=true;return true;}
    public boolean death(UUID id){
        if(!alive.remove(id))return false;
        for(var slot:slots)if(id.equals(slot.uuid))slot.defeated=true;
        if(slots.isEmpty()?alive.isEmpty():slots.stream().allMatch(s->s.defeated))cleared=true;
        return true;
    }
    public CompoundTag save(){
        CompoundTag n=new CompoundTag();n.putBoolean("Triggered",triggered);n.putBoolean("Cleared",cleared);
        ListTag ids=new ListTag();for(UUID id:alive)ids.add(StringTag.valueOf(id.toString()));n.put("Alive",ids);
        ListTag old=new ListTag();for(UUID id:retired)old.add(StringTag.valueOf(id.toString()));n.put("Retired",old);
        ListTag list=new ListTag();for(var s:slots){CompoundTag t=new CompoundTag();t.putString("Kind",s.kind.name());if(s.uuid!=null)t.putUUID("UUID",s.uuid);if(s.last!=null)t.putLong("Last",s.last.asLong());t.putBoolean("Defeated",s.defeated);t.putBoolean("Restored",s.restored);list.add(t);}n.put("Slots",list);return n;
    }
    public static KurganEncounterState load(CompoundTag n){
        KurganEncounterState s=new KurganEncounterState();s.triggered=n.getBoolean("Triggered");s.cleared=n.getBoolean("Cleared");
        for(Tag id:n.getList("Alive",8))s.alive.add(UUID.fromString(id.getAsString()));
        for(Tag id:n.getList("Retired",8))s.retired.add(UUID.fromString(id.getAsString()));
        for(Tag entry:n.getList("Slots",10)){CompoundTag t=(CompoundTag)entry;Slot slot=new Slot(KurganFighter.Kind.valueOf(t.getString("Kind")));slot.uuid=t.hasUUID("UUID")?t.getUUID("UUID"):null;slot.last=t.contains("Last")?BlockPos.of(t.getLong("Last")):null;slot.defeated=t.getBoolean("Defeated");slot.restored=t.getBoolean("Restored");s.slots.add(slot);}return s;
    }
}
