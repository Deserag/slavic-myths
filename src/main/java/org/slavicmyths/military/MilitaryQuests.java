package org.slavicmyths.military;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.bandit.*;
import org.slavicmyths.navigation.*;
/** Server-only validation; accepted definitions/rewards are saved with the player. */
public final class MilitaryQuests {
 public static CompoundTag state(ServerPlayer p){return p.getData(Military.QUESTS);}
 public static List<CompoundTag> active(ServerPlayer p){List<CompoundTag> out=new ArrayList<>();for(Tag t:state(p).getList("Entries",10)){var q=(CompoundTag)t;if(!q.getBoolean("Claimed")&&!q.getBoolean("Abandoned"))out.add(q);}return out;}
 public static CompoundTag accepted(ServerPlayer p,String key){for(Tag t:state(p).getList("Entries",10)){var q=(CompoundTag)t;if(q.getString("Key").equals(key))return q;}return null;}
 private static JsonArray templates(){try(var in=MilitaryQuests.class.getResourceAsStream("/data/slavicmyths/military/quests.json")){return JsonParser.parseReader(new java.io.InputStreamReader(Objects.requireNonNull(in),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}}
 public static List<CompoundTag> available(ServerPlayer p,BlockPos table){return MilitaryBoards.offers(p,table);}
 static List<CompoundTag> generateOffers(ServerPlayer p,BlockPos table){long day=Math.floorDiv(p.serverLevel().getDayTime(),24000);long region=net.minecraft.world.level.ChunkPos.asLong(table.getX()>>6,table.getZ()>>6);Random rand=new Random(p.serverLevel().getSeed()^region^day);List<CompoundTag> result=new ArrayList<>();
  var camp=p.serverLevel().getEntitiesOfClass(BanditEntity.class,new net.minecraft.world.phys.AABB(table).inflate(Military.RAID_SEARCH),b->b.camp!=null&&b.home!=null&&!(b.campTotal==StrongholdRecords.TOTAL?StrongholdRecords.get(p.serverLevel()).record(b.camp).cleared:CampRecords.get(p.serverLevel()).isCleared(b.camp))).stream().min(Comparator.comparingDouble(b->b.home.distSqr(table))).orElse(null);
  for(JsonElement e:templates()){var d=e.getAsJsonObject();String type=d.get("type").getAsString();if(type.equals("boss")&&(!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(d.get("target").getAsString()))||rand.nextInt(d.get("rarity").getAsInt())!=0))continue;if(type.equals("camp")&&camp==null)continue;
   CompoundTag q=new CompoundTag();String id=d.get("id").getAsString();q.putString("Id",id);q.putString("Type",type);q.putString("Target",d.get("target").getAsString());q.putInt("Amount",d.get("amount").getAsInt());q.putInt("Difficulty",d.get("difficulty").getAsInt());q.putString("Key",p.level().dimension().location()+":"+region+":"+day+":"+id);q.putString("Dimension",p.level().dimension().location().toString());q.putLong("Origin",table.asLong());q.putLong("Day",day);q.putLong("Region",region);ListTag rewards=new ListTag();for(JsonElement reward:d.getAsJsonArray("rewards")){var v=reward.getAsJsonObject();Item item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(v.get("item").getAsString()));rewards.add(new ItemStack(item,v.get("count").getAsInt()).save(p.registryAccess()));}q.put("Rewards",rewards);if(camp!=null&&type.equals("camp")){q.putUUID("Camp",camp.camp);q.putBoolean("Stronghold",camp.campTotal==StrongholdRecords.TOTAL);q.putLong("Location",camp.home.asLong());}else if(type.equals("defend"))q.putLong("Location",table.asLong());result.add(q);if(result.size()==5)break;
  }return result;
 }
 public static CompoundTag snapshot(ServerPlayer p,BlockPos table){CompoundTag n=new CompoundTag();ListTag list=new ListTag();Set<String> keys=new HashSet<>();for(var q:active(p)){var view=q.copy();if(q.getString("Type").equals("trophy"))view.putInt("Progress",Math.min(q.getInt("Amount"),p.getInventory().countItem(Military.TOKEN.get())));list.add(view);keys.add(q.getString("Key"));}for(var q:available(p,table)){if(!keys.add(q.getString("Key")))continue;var old=accepted(p,q.getString("Key"));if(old!=null){q=old.copy();}list.add(q);}n.put("Quests",list);n.putLong("Day",Math.floorDiv(p.level().getDayTime(),24000));n.putLong("Table",table.asLong());return n;}
 public static boolean action(ServerPlayer p,BlockPos table,String key,int action){if(key.length()>192)return false;var old=accepted(p,key);
  if(action==0){if(old!=null||active(p).size()>=Military.ACTIVE_LIMIT)return false;var q=available(p,table).stream().filter(x->x.getString("Key").equals(key)).findFirst().orElse(null);if(q==null)return false;q=q.copy();q.putBoolean("Accepted",true);var list=state(p).getList("Entries",10);list.add(q);state(p).put("Entries",list);return true;}
  if(old==null||old.getBoolean("Claimed")||old.getBoolean("Abandoned"))return false;
  if(action==3){old.putBoolean("Abandoned",true);old.putInt("Progress",0);untrack(p,old);return true;}
  if(action==1){BlockPos pos=BlockPos.of(old.getLong(old.contains("Location")?"Location":"Origin"));var s=NavigationRecords.get(p.serverLevel()).player(p.getUUID());UUID id=SlavicMarker.identity("military:"+key);s.upsert(new SlavicMarker(id,"military:"+key,MarkerCategory.QUEST,SlavicMarker.Kind.TEMPORARY_QUEST,SlavicMarker.Lifetime.UNTIL_QUEST_END,"military.slavicmyths.quest."+old.getString("Id"),ResourceLocation.parse(old.getString("Dimension")),pos.getX(),pos.getY(),pos.getZ(),p.level().getGameTime(),0,null));s.track(id);NavigationManager.changed(p);return true;}
  if(action!=2)return false;String type=old.getString("Type");int required=old.getInt("Amount");if(type.equals("trophy")){if(p.getInventory().countItem(Military.TOKEN.get())<required)return false;}else if(old.getInt("Progress")<required)return false;
  List<ItemStack> rewards=new ArrayList<>();for(Tag t:old.getList("Rewards",10))rewards.add(ItemStack.parseOptional(p.registryAccess(),(CompoundTag)t));if(rewards.stream().anyMatch(ItemStack::isEmpty))return false;
  // Claim flag precedes delivery; repeated requests/reconnect cannot duplicate rewards.
  old.putBoolean("Claimed",true);old.putInt("Progress",required);if(type.equals("trophy")){int left=required;for(int i=0;i<p.getInventory().getContainerSize()&&left>0;i++){ItemStack s=p.getInventory().getItem(i);if(s.is(Military.TOKEN.get())){int n=Math.min(left,s.getCount());p.getInventory().removeItem(i,n);left-=n;}}}for(ItemStack reward:rewards)if(!p.getInventory().add(reward))p.drop(reward,false);untrack(p,old);return true;
 }
 private static void untrack(ServerPlayer p,CompoundTag q){var s=NavigationRecords.get(p.serverLevel()).player(p.getUUID());s.markers.remove(SlavicMarker.identity("military:"+q.getString("Key")));if(p.connection!=null)NavigationManager.changed(p);}
 public static void killed(ServerPlayer p,LivingEntity dead){for(var q:active(p)){String type=q.getString("Type");boolean credit=type.equals("kill")&&dead.getType().is(Military.BANDITS)||type.equals("boss")&&BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType()).toString().equals(q.getString("Target"));if(credit)q.putInt("Progress",Math.min(q.getInt("Amount"),q.getInt("Progress")+1));}}
 public static void visit(ServerPlayer p){for(var q:active(p))if(q.getString("Type").equals("camp")&&q.getString("Dimension").equals(p.level().dimension().location().toString())){if(p.blockPosition().distSqr(BlockPos.of(q.getLong("Location")))<48*48)q.putBoolean("Visited",true);if(q.getBoolean("Visited")&&(q.getBoolean("Stronghold")?StrongholdRecords.get(p.serverLevel()).record(q.getUUID("Camp")).cleared:CampRecords.get(p.serverLevel()).isCleared(q.getUUID("Camp"))))q.putInt("Progress",q.getInt("Amount"));}}
 public static void defended(ServerPlayer p,BlockPos anchor){for(var q:active(p))if(q.getString("Type").equals("defend")&&q.getString("Dimension").equals(p.level().dimension().location().toString())&&BlockPos.of(q.getLong("Origin")).distSqr(anchor)<=48*48)q.putInt("Progress",q.getInt("Amount"));}
 private MilitaryQuests(){}
}
