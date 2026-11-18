package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.losthalls.*;
import org.slavicmyths.registry.ModItems;

/** Owner-authorized diagnostic certificates only. No final loot, trust or reward balance. */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadTestProofs {
 public enum Kind { MYTAR, BAUK, RAVEN, ZMEY, FEVER_SISTERS, ELEMENT_SISTERS;
  public Component title(){return Component.translatable("posad.test.kind."+name().toLowerCase(Locale.ROOT));}
 }
 public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> PROOF=ModItems.ITEMS.register("posad_test_proof",()->new Item(new Item.Properties().stacksTo(1)));
 public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> RECEIPT=ModItems.ITEMS.register("posad_test_receipt",()->new Item(new Item.Properties().stacksTo(1)));
 private static final String DAMAGE="PosadTestProofDamage",HEALTH="PosadTestProofPreHealth";
 public static void init(){}
 public static Kind kind(LivingEntity entity){
  var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());if(!id.getNamespace().equals("slavicmyths"))return null;
  return switch(id.getPath()){
   case "cursed_mytar"->Kind.MYTAR;case "bauk"->Kind.BAUK;case "morovoy_raven"->Kind.RAVEN;case "morovoy_fire_zmey"->Kind.ZMEY;
   case "gneteya","tryaseya"->Kind.FEVER_SISTERS;case "agneya","ledeya"->Kind.ELEMENT_SISTERS;default->null;
  };
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void beforeDamage(LivingDamageEvent.Pre event){
  if(!event.getEntity().level().isClientSide&&kind(event.getEntity())!=null)
   event.getEntity().getPersistentData().putFloat(HEALTH,event.getEntity().getHealth());
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void damage(LivingDamageEvent.Post event){
  var boss=event.getEntity();if(!(boss.level() instanceof ServerLevel world)||kind(boss)==null)return;
  var data=boss.getPersistentData();float before=data.getFloat(HEALTH);data.remove(HEALTH);
  if(!(event.getSource().getEntity() instanceof ServerPlayer player)||player.isCreative()||player.isSpectator())return;
  float actual=Math.max(0,Math.min(event.getNewDamage(),before-boss.getHealth()));if(actual<=0)return;
  var participants=data.getCompound(DAMAGE);String id=player.getUUID().toString();
  if(!participants.contains(id)&&participants.size()>=64)return;
  var row=participants.getCompound(id);row.putFloat("Damage",Math.min(boss.getMaxHealth()*10,row.getFloat("Damage")+actual));
  row.putLong("Last",world.getGameTime());participants.put(id,row);data.put(DAMAGE,participants);
 }
 private static UUID encounter(LivingEntity boss,Kind kind){
  if(kind==Kind.FEVER_SISTERS&&boss instanceof org.slavicmyths.sisters.SisterBoss sister)return sister.encounterId();
  if(kind==Kind.ELEMENT_SISTERS)return boss.getPersistentData().hasUUID("PosadTestEncounter")?boss.getPersistentData().getUUID("PosadTestEncounter"):null;
  return boss.getUUID();
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST,receiveCanceled=true)
 public static void death(LivingDropsEvent event){
  // Drops follow an accepted death; a later cancellation of LivingDeathEvent cannot mint proof.
  var boss=event.getEntity();var kind=kind(boss);if(kind==null||!(boss.level() instanceof ServerLevel world))return;
  UUID encounter=encounter(boss,kind);if(encounter==null)return; // Unlinked diagnostic sister eggs are not a completed pair.
  var participants=boss.getPersistentData().getCompound(DAMAGE);
  for(String id:participants.getAllKeys()){
   UUID uuid;try{uuid=UUID.fromString(id);}catch(IllegalArgumentException invalid){continue;}
   if(!(world.getPlayerByUUID(uuid) instanceof ServerPlayer player)||player.isCreative()||player.isSpectator()||!player.isAlive()||player.distanceToSqr(boss)>24*24)continue;
   var row=participants.getCompound(id);long elapsed=world.getGameTime()-row.getLong("Last");
   if(elapsed<0||elapsed>600||row.getFloat("Damage")<boss.getMaxHealth()*.05F)continue;
   if(kind==Kind.FEVER_SISTERS||kind==Kind.ELEMENT_SISTERS){
    String type=BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).getPath();int side=type.equals("tryaseya")||type.equals("ledeya")?1:0;
    String root="posad_test_half/"+encounter+"/";
    var half=BossRewardInbox.prepare(world.getServer(),root+side+"/"+uuid,"posad_test_pair_half",uuid,kind.ordinal()+side*6,world.getServer().overworld().getGameTime());
    var other=BossRewardInbox.find(world.getServer(),root+(1-side)+"/"+uuid);
    if(half==null||other==null||!other.kind().equals("posad_test_pair_half")||other.cores()!=kind.ordinal()+(1-side)*6||!other.recipient().equals(uuid))continue;
   }
   String key="posad_test_proof/"+kind.name()+"/"+encounter+"/"+uuid;
   if(BossRewardInbox.prepare(world.getServer(),key,"posad_test_proof",uuid,kind.ordinal(),world.getServer().overworld().getGameTime())!=null)BossRewardInbox.reconcile(player);
  }
 }
 public static ItemStack token(BossRewardAuthority.Entry entry,boolean receipt){
  var item=new ItemStack(receipt?RECEIPT.get():PROOF.get());var data=new CompoundTag();
  data.putString("PosadTestPin",entry.key());data.putUUID("PosadTestWinner",entry.recipient());data.putInt("PosadTestKind",entry.cores());
  item.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
  item.set(DataComponents.ITEM_NAME,Component.translatable(receipt?"posad.test.receipt.named":"posad.test.proof.named",Kind.values()[entry.cores()].title()));
  item.set(DataComponents.LORE,new ItemLore(List.of(Component.translatable("posad.test.only"))));return item;
 }
 /** Main-hand interaction at the existing leader. Pin award before consuming proof. */
 public static boolean exchange(ServerPlayer player,PosadResidentEntity leader){
  var stack=player.getMainHandItem();if(!stack.is(PROOF.get())||leader.role()!=PosadPopulationPlan.Role.POSADNIK)return false;
  if(leader.role()!=PosadPopulationPlan.Role.POSADNIK||leader.city()==null||leader.distanceToSqr(player)>64||player.isSpectator())return true;
  var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();String key=data.getString("PosadTestPin");
  var source=BossRewardInbox.find(player.getServer(),key);
  if(source==null||!source.kind().equals("posad_test_proof")||source.cores()!=data.getInt("PosadTestKind")||!data.hasUUID("PosadTestWinner")||!source.recipient().equals(data.getUUID("PosadTestWinner"))){
   player.displayClientMessage(Component.translatable("posad.test.invalid"),true);return true;
  }
  String exchange="posad_test_exchange/"+key;var previous=BossRewardInbox.find(player.getServer(),exchange);
  if(previous!=null){player.displayClientMessage(Component.translatable("posad.test.redeemed"),true);return true;}
  var pin=BossRewardInbox.prepare(player.getServer(),exchange,"posad_test_exchange",player.getUUID(),source.cores(),player.getServer().overworld().getGameTime());
  if(pin==null||!pin.recipient().equals(player.getUUID()))return true;
  stack.shrink(1);player.getInventory().setChanged();BossRewardInbox.reconcile(player);
  player.displayClientMessage(Component.translatable("posad.test.exchanged"),true);return true;
 }
 private PosadTestProofs(){}
}
