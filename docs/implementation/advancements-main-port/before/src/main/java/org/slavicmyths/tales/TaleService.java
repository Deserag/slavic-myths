package org.slavicmyths.tales;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;

public final class TaleService {
    private static final Map<net.minecraft.world.item.crafting.RecipeManager,CompoundTag> recipeCache=Collections.synchronizedMap(new WeakHashMap<>());
    public static void invalidateRecipes(){recipeCache.clear();}
    public static boolean holding(Player p){return p.getMainHandItem().is(ModItems.LORE_BOOK.get())||p.getOffhandItem().is(ModItems.LORE_BOOK.get());}
    public static void deliver(ServerPlayer p){
        var data=TaleData.get(p.serverLevel());if(!data.compatible())return;boolean fresh=!data.contains(p.getUUID());var state=data.player(p.getUUID());
        boolean has=p.getInventory().contains(new ItemStack(ModItems.LORE_BOOK.get()));
        if(fresh&&(has||Knowledge.knows(p,"lore_book")||p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))>0)){
            state.putString("Delivery","DELIVERED");state.putBoolean("LegacyDelivery",true);data.setDirty();return;
        }
        if(state.getString("Delivery").equals("DELIVERED"))return;
        if(has){state.putString("Delivery","DELIVERED");data.setDirty();return;}
        state.putString("Delivery","PENDING");data.setDirty();ItemStack book=new ItemStack(ModItems.LORE_BOOK.get());
        if(p.getInventory().add(book)){state.putString("Delivery","DELIVERED");data.setDirty();p.displayClientMessage(Component.translatable("tales.received"),false);}
    }
    public static void subject(ServerPlayer p,String subject,int stage,boolean defeated){for(var e:TaleCatalog.subject(subject))promote(p,e.id(),stage,defeated);}
    public static void promote(ServerPlayer p,String id,int stage,boolean defeated){var data=TaleData.get(p.serverLevel());if(!data.promote(p.getUUID(),id,stage,p.level().getGameTime(),defeated))return;
        CompoundTag delta=new CompoundTag(),entries=new CompoundTag();entries.put(id,data.player(p.getUUID()).getCompound("Entries").getCompound(id).copy());delta.put("Entries",entries);TaleNetwork.send(p,delta,false,false);
        if(stage>=4){int count=0;for(String key:data.player(p.getUUID()).getCompound("Entries").getAllKeys())if(data.stage(p.getUUID(),key)>=4)count++;if(count>=10&&!Knowledge.knows(p,"bookkeeper"))Knowledge.award(p,"bookkeeper");}
    }
    public static int advancementStage(String id){String s=id.substring(id.indexOf(':')+1);if(Set.of("defeat_nightingale","defeat_depth_master","steppe_champion","earth_heart","friend_domovoy","trusted_by_yaga","friend_of_bayun").contains(s))return 4;return s.startsWith("meet_")||s.startsWith("find_")||s.startsWith("explore_")?2:3;}
    /** Read existing vanilla progress; never invoke the old mask's award side effects. */
    public static void reconcile(ServerPlayer p){var data=TaleData.get(p.serverLevel());for(var a:p.server.getAdvancements().getAllAdvancements())if(a.id().getNamespace().equals("slavicmyths")&&p.getAdvancements().getOrStartProgress(a).isDone())for(var e:TaleCatalog.advancement(a.id().toString()))data.promote(p.getUUID(),e.id(),advancementStage(a.id().toString()),p.level().getGameTime(),false);}
    public static void open(ServerPlayer p){if(!holding(p)||!p.isAlive()||p.level().getGameTime()<p.getPersistentData().getLong("TaleOpenReady"))return;p.getPersistentData().putLong("TaleOpenReady",p.level().getGameTime()+10);reconcile(p);Knowledge.award(p,"first_book_entry");TaleNetwork.send(p,snapshot(p),true,true);}
    public static CompoundTag snapshot(ServerPlayer p){
        var state=TaleData.get(p.serverLevel()).player(p.getUUID());CompoundTag n=new CompoundTag();n.put("Entries",state.getCompound("Entries").copy());n.put("Hints",publicHints(p,state));ListTag catalog=new ListTag();for(var e:TaleCatalog.entries())catalog.add(e.snapshot());n.put("Catalog",catalog);n.put("Advancements",advancements(p));n.putString("Dimension",p.level().dimension().location().toString());n.put("Recipes",recipeSnapshot(p));n.putInt("Rewards176",org.slavicmyths.rewards.RewardLedger.available(p));
        // Read-only bridge: no call to progress(), accept(), complete() or reward methods.
        var yaga=org.slavicmyths.yaga.YagaData.get(p.serverLevel()).players.get(p.getUUID());CompoundTag quests=new CompoundTag();if(yaga!=null){quests.putInt("YagaStage",yaga.stage);if(yaga.active.startsWith("main:")&&yaga.stage<3)quests.putString("YagaActive","yaga.quest."+org.slavicmyths.yaga.YagaServices.MAIN[yaga.stage].id+".title");else if(yaga.active.startsWith("contract:"))quests.putString("YagaActive","yaga.quest."+org.slavicmyths.yaga.YagaServices.CONTRACTS[org.slavicmyths.yaga.YagaServices.POOL[yaga.contract]].id+".title");}n.put("Quests",quests);return n;
    }
    public static ListTag advancements(ServerPlayer p){ListTag list=new ListTag();for(var a:p.server.getAdvancements().getAllAdvancements()){
        if(!a.id().getNamespace().equals("slavicmyths")||a.value().display().isEmpty())continue;var d=a.value().display().get();var progress=p.getAdvancements().getOrStartProgress(a);boolean parentDone=a.value().parent().map(id->{var parent=p.server.getAdvancements().get(id);return parent!=null&&p.getAdvancements().getOrStartProgress(parent).isDone();}).orElse(true);
        if(d.isHidden()&&!progress.isDone()&&!parentDone)continue;CompoundTag n=new CompoundTag();n.putString("Id",a.id().toString());n.putString("Parent",a.value().parent().map(Object::toString).orElse(""));n.putBoolean("Done",progress.isDone());n.putBoolean("Available",parentDone);n.putFloat("Progress",progress.getPercent());n.putString("Title",Component.Serializer.toJson(d.getTitle(),p.registryAccess()));n.putString("Description",Component.Serializer.toJson(d.getDescription(),p.registryAccess()));n.putString("Icon",BuiltInRegistries.ITEM.getKey(d.getIcon().getItem()).toString());list.add(n);
    }return list;}
    /** Serialize actual loaded recipes through their installed serializers. */
    public static CompoundTag recipeSnapshot(ServerPlayer p){var manager=p.serverLevel().getRecipeManager();var cached=recipeCache.get(manager);if(cached!=null)return cached.copy();CompoundTag recipes=new CompoundTag();var ops=p.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);for(var holder:manager.getRecipes()){if(!holder.id().getNamespace().equals("slavicmyths"))continue;var json=net.minecraft.world.item.crafting.Recipe.CODEC.encodeStart(ops,holder.value()).getOrThrow();recipes.putString(holder.id().toString(),json.toString());}recipeCache.put(manager,recipes);return recipes.copy();}
    public static void read(ServerPlayer p,String id){if(id.equals("__rewards176")){if(holding(p)&&p.isAlive()&&p.level().getGameTime()>=p.getPersistentData().getLong("RewardClaimReady")){p.getPersistentData().putLong("RewardClaimReady",p.level().getGameTime()+10);org.slavicmyths.rewards.RewardLedger.claimInbox(p);}return;}if(!holding(p)||TaleCatalog.entry(id)==null||p.level().getGameTime()<p.getPersistentData().getLong("TaleReadReady"))return;p.getPersistentData().putLong("TaleReadReady",p.level().getGameTime()+4);if(id.equals("slavicmyths:great_poloz")&&p.getInventory().contains(new ItemStack(org.slavicmyths.poloz.PolozContent.CORE.get()))){promote(p,id,4,true);Knowledge.award(p,"great_poloz_fall");}var data=TaleData.get(p.serverLevel());var e=data.player(p.getUUID()).getCompound("Entries").getCompound(id);if(e.getBoolean("Unread")){e.putBoolean("Unread",false);data.setDirty();}}
    public static void hint(ServerPlayer p,String id,String dimension,UUID target,net.minecraft.core.BlockPos anchor){var data=TaleData.get(p.serverLevel());var hints=TaleData.compound(data.player(p.getUUID()),"Hints");if(!hints.contains(id)&&hints.size()>=64)return;long seed=target.getMostSignificantBits()^target.getLeastSignificantBits()^p.getUUID().getLeastSignificantBits();var random=new Random(seed);double angle=random.nextDouble()*Math.PI*2,offset=25+random.nextDouble()*55;CompoundTag n=new CompoundTag();n.putString("Dimension",dimension);n.putUUID("Target",target);n.putLong("Anchor",anchor.asLong());n.putDouble("X",anchor.getX()+Math.cos(angle)*offset);n.putDouble("Z",anchor.getZ()+Math.sin(angle)*offset);n.putInt("Radius",250);n.putLong("Expires",p.level().getGameTime()+24000*7);n.putString("Source","entity.slavicmyths.kot_bayun");double dx=n.getDouble("X")-p.getX(),dz=n.getDouble("Z")-p.getZ();int compass=Math.floorMod((int)Math.round(Math.atan2(dx,-dz)/(Math.PI/4)),8);n.putInt("Direction",compass);double distance=Math.hypot(dx,dz);n.putInt("Distance",distance<500?0:distance<1500?1:2);hints.put(id,n);data.setDirty();}
    private static CompoundTag publicHints(ServerPlayer p,CompoundTag state){CompoundTag hints=state.getCompound("Hints").copy();for(String key:hints.getAllKeys()){var h=hints.getCompound(key);boolean stale=p.level().getGameTime()>h.getLong("Expires");if(h.hasUUID("Target")&&h.getString("Dimension").equals(p.level().dimension().location().toString())){var entity=p.serverLevel().getEntity(h.getUUID("Target"));if(entity==null&&h.contains("Anchor")&&p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.of(h.getLong("Anchor"))))stale=true;if(entity!=null&&entity.position().distanceToSqr(new net.minecraft.world.phys.Vec3(h.getDouble("X"),entity.getY(),h.getDouble("Z")))>h.getInt("Radius")*(double)h.getInt("Radius"))stale=true;}h.putBoolean("Stale",stale);h.remove("Target");h.remove("Anchor");}return hints;}
    private TaleService(){}
}
