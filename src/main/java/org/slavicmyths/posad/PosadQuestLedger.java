package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent per-city trust and one-time quest completions, independent of merchant XP. */
public final class PosadQuestLedger extends SavedData {
    public record Task(String id, int tier, Item item, int count, int points, String title) {}
    public static final List<Task> SUPPLIES = List.of(
        new Task("town_food_supply",1,Items.BREAD,16,5,"Хлеб для посада"),
        new Task("town_wood_supply",1,Items.OAK_PLANKS,32,5,"Доски для мастеров"),
        new Task("town_mushroom_supply",1,Items.CRIMSON_FUNGUS,24,5,"Грибы для повара"),
        new Task("town_ore_supply",2,Items.IRON_INGOT,16,8,"Железо для кузнецов"),
        new Task("town_fortress_supply",3,Items.ARROW,24,12,"Стрелы дружине"),
        new Task("town_hard_materials",4,Items.BLACKSTONE,32,15,"Камень для укреплений"));
    private static final class State { int points; final Set<String> completed=new HashSet<>(); final Set<String> active=new HashSet<>(); }
    private final Map<String,State> records=new HashMap<>();
    private static String key(UUID city,UUID player){return city+"/"+player;}
    private State state(UUID city,UUID player){return records.computeIfAbsent(key(city,player),k->new State());}
    public static PosadQuestLedger get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PosadQuestLedger::new,(tag,provider)->{
        PosadQuestLedger ledger=new PosadQuestLedger();
        for(Tag raw:tag.getList("Cities",10)){CompoundTag row=(CompoundTag)raw;State s=new State();s.points=Math.max(0,row.getInt("Points"));for(Tag t:row.getList("Completed",8))s.completed.add(t.getAsString());for(Tag t:row.getList("Active",8))s.active.add(t.getAsString());ledger.records.put(row.getString("Key"),s);}return ledger;
    }),"slavicmyths_posad_quests");}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){ListTag rows=new ListTag();for(var entry:records.entrySet()){CompoundTag row=new CompoundTag();row.putString("Key",entry.getKey());row.putInt("Points",entry.getValue().points);ListTag completed=new ListTag();for(String id:entry.getValue().completed)completed.add(StringTag.valueOf(id));ListTag active=new ListTag();for(String id:entry.getValue().active)active.add(StringTag.valueOf(id));row.put("Completed",completed);row.put("Active",active);rows.add(row);}tag.put("Cities",rows);return tag;}
    public int points(UUID city,UUID player){return state(city,player).points;}
    public int tier(UUID city,UUID player){int p=points(city,player);return p>=200?5:p>=120?4:p>=60?3:p>=20?2:1;}
    public boolean active(UUID city,UUID player,String id){return state(city,player).active.contains(id);}
    public boolean completed(UUID city,UUID player,String id){return state(city,player).completed.contains(id);}
    public int slots(UUID city,UUID player){return new int[]{0,1,2,2,3,4}[tier(city,player)];}
    public boolean accept(UUID city,ServerPlayer player,Task task){State s=state(city,player.getUUID());if(task.tier()>tier(city,player.getUUID())||s.active.contains(task.id())||s.active.size()>=slots(city,player.getUUID()))return false;s.active.add(task.id());setDirty();return true;}
    public boolean deliver(UUID city,ServerPlayer player,Task task){State s=state(city,player.getUUID());if(!s.active.contains(task.id()))return false;int owned=0;for(ItemStack stack:player.getInventory().items)if(stack.is(task.item()))owned+=stack.getCount();if(owned<task.count())return false;int remaining=task.count();for(ItemStack stack:player.getInventory().items){if(stack.is(task.item())){int take=Math.min(remaining,stack.getCount());stack.shrink(take);remaining-=take;if(remaining==0)break;}}s.active.remove(task.id());s.completed.add(task.id());s.points+=task.points();player.getInventory().setChanged();setDirty();return true;}
    public boolean acceptBoss(UUID city,ServerPlayer player,String id) {
        if (!id.equals("hunt_cursed_mytar") && !id.equals("hunt_bauk")) return false;
        State s=state(city,player.getUUID());
        if (tier(city,player.getUUID())<3 || s.active.contains(id) || s.completed.contains(id) || s.active.size()>=slots(city,player.getUUID())) return false;
        s.active.add(id);setDirty();return true;
    }
    /** Find only currently loaded and spawned entities, with no generated or guessed position. */
    public static net.minecraft.core.BlockPos findLoadedBoss(ServerPlayer player,String id) {
        if (!id.equals("hunt_cursed_mytar") && !id.equals("hunt_bauk")) return null;
        if (player.serverLevel().dimension()!=net.minecraft.world.level.Level.NETHER) return null;
        String entityId=id.equals("hunt_cursed_mytar")?"cursed_mytar":"bauk";
        return player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(256D),mob->mob.isAlive()&&net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath().equals(entityId)).stream().min(java.util.Comparator.comparingDouble(mob->player.distanceToSqr(mob))).map(net.minecraft.world.entity.Entity::blockPosition).orElse(null);
    }
    public void recordBossQuest(UUID city,ServerPlayer player,String id,int points){State s=state(city,player.getUUID());if(s.active.remove(id)&&s.completed.add(id)){s.points+=points;setDirty();}}
}
