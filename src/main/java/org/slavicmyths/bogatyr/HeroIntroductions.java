package org.slavicmyths.bogatyr;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.tales.*;

/** Request-driven, shared meetings. All progress is in the existing tale save;
 * neither proximity nor possession of a summon item constitutes help. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class HeroIntroductions {
 public static final List<String> IDS=List.of("ilya_muromets","dobrynya_nikitich","alyosha_popovich","finist");
 public static final String TAG="HeroIntroduction",DONATIONS="HeroDonations";
 private static final String[] TOKENS={"ilya_call_token","dobrynya_call_emblem","alyosha_call_whistle","finist_call_feather"};
 private static final Item[] SUPPLIES={Items.BREAD,Items.HONEY_BOTTLE,Items.IRON_INGOT};
 private static final int[] REQUIRED={6,1,4};
 public static String entry(String hero){return "slavicmyths:"+(hero.equals(IDS.getFirst())?"ilya_introduction":hero+"_introduction");}
 private static CompoundTag state(TaleData data,UUID player,String hero){return TaleData.compound(data.player(player),hero.equals(IDS.getFirst())?"IlyaIntroduction":"HeroIntroduction/"+hero);}
 public static CompoundTag state(ServerPlayer p,String hero){return state(TaleData.get(p.serverLevel()),p.getUUID(),hero);}
 public static boolean unlocked(ServerPlayer p,String hero){return IDS.contains(hero)&&state(p,hero).getBoolean("Helped");}
 public static boolean canCraft(ServerPlayer p,ItemStack stack){String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();for(int i=0;i<TOKENS.length;i++)if(id.equals("slavicmyths:"+TOKENS[i]))return unlocked(p,IDS.get(i));return true;}
 public static boolean mayCall(ServerPlayer p,String hero){if(unlocked(p,hero))return true;msg(p,"locked");return false;}
 private static void msg(ServerPlayer p,String key,Object...args){p.displayClientMessage(Component.translatable("hero_intro."+key,args),false);}
 private static Item token(String hero){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",TOKENS[IDS.indexOf(hero)]));}
 private static void deliver(ServerPlayer p,String hero){var n=state(p,hero);if(!n.getBoolean("RewardPending"))return;var item=token(hero);if(org.slavicmyths.rpg.Runes.inventoryCount(p,item)==0)org.slavicmyths.yaga.YagaServices.give(p,new ItemStack(item));n.remove("RewardPending");TaleData.get(p.serverLevel()).setDirty();TaleService.promote(p,entry(hero),4,false);org.slavicmyths.progression.Knowledge.award(p,"recipes/"+TOKENS[IDS.indexOf(hero)]);org.slavicmyths.progression.AdvancementJourney.award(p,hero.equals(IDS.getFirst())?"ilya_friendship":hero+"_friendship");TaleNetwork.send(p,TaleService.snapshot(p),false,true);msg(p,"helped");}
 private static void reward(ServerLevel w,UUID player,String hero){var data=TaleData.get(w);var n=state(data,player,hero);if(n.getBoolean("Helped"))return;n.putBoolean("Helped",true);n.putBoolean("RewardPending",true);data.setDirty();var online=w.getServer().getPlayerList().getPlayer(player);if(online!=null)deliver(online,hero);}
 public static boolean offer(ServerPlayer p,String giver,String hero){
  if(!IDS.contains(hero)||!p.isAlive()||p.isSpectator()||p.level().dimension()!=Level.OVERWORLD)return false;
  if(giver.equals("yaga")&&org.slavicmyths.yaga.YagaData.get(p.server.overworld()).progress(p.getUUID()).stage<1){msg(p,"guest");return false;}
  if(unlocked(p,hero)){deliver(p,hero);msg(p,"already");return true;}
  var w=p.serverLevel();var n=state(p,hero);
  if(n.hasUUID("Npc")){var place=BlockPos.of(n.getLong("Place"));if(w.getChunkSource().getChunkNow(place.getX()>>4,place.getZ()>>4)!=null&&w.getEntity(n.getUUID("Npc"))==null){n.remove("Npc");n.remove("Met");TaleData.get(w).setDirty();}else{guide(p,hero,n);return true;}}
  // Join a loaded shared reservation before creating another meeting. No chunk loads.
  for(var entity:w.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(80),e->e.isAlive()&&e.getPersistentData().getString(TAG).equals(hero)&&!e.getPersistentData().getBoolean("HeroHelpComplete"))){reserve(p,hero,giver,entity);return true;}
  Vec3 at=null;for(int i=0;i<48&&at==null;i++){double angle=i*Math.PI/8;int radius=24+i/16*8;int x=p.getBlockX()+(int)Math.round(Math.cos(angle)*radius),z=p.getBlockZ()+(int)Math.round(Math.sin(angle)*radius);if(w.getChunkSource().getChunkNow(x>>4,z>>4)==null)continue;int y=w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);at=BogatyrLedger.safe(w,new Vec3(x+.5,y,z+.5),.93,2.45,3);}
  if(at==null){msg(p,"no_space");return false;}
  var npc=create(w,hero);if(npc==null)return false;npc.getPersistentData().putString(TAG,hero);npc.getPersistentData().putUUID(IlyaIntroduction.OWNER,p.getUUID());npc.setPersistenceRequired();npc.setNoAi(true);npc.setInvulnerable(true);npc.setDropChance(EquipmentSlot.MAINHAND,0);npc.setDropChance(EquipmentSlot.OFFHAND,0);npc.moveTo(at.x,at.y,at.z,p.getYRot()+180,0);if(!w.addFreshEntity(npc))return false;reserve(p,hero,giver,npc);return true;
 }
 private static Mob create(ServerLevel w,String hero){return switch(hero){case "ilya_muromets"->BogatyrContent.ILYA.get().create(w);case "dobrynya_nikitich"->org.slavicmyths.marinka.MarinkaContent.DOBRYNYA.get().create(w);case "alyosha_popovich"->org.slavicmyths.skim.SkimContent.ALYOSHA.get().create(w);default->org.slavicmyths.finist.FinistContent.FINIST.get().create(w);};}
 private static void reserve(ServerPlayer p,String hero,String giver,Mob npc){var participants=TaleData.compound(npc.getPersistentData(),"HeroParticipants");if(!participants.contains(p.getUUID().toString())&&participants.size()>=64){msg(p,"no_space");return;}participants.putBoolean(p.getUUID().toString(),true);var n=state(p,hero);n.putUUID("Npc",npc.getUUID());n.putLong("Place",npc.blockPosition().asLong());n.putString("Dimension",p.level().dimension().location().toString());n.putString("Giver",giver);TaleData.get(p.serverLevel()).setDirty();TaleService.promote(p,entry(hero),1,false);guide(p,hero,n);}
 private static void guide(ServerPlayer p,String hero,CompoundTag n){TaleService.hint(p,entry(hero),n.getString("Dimension"),n.getUUID("Npc"),BlockPos.of(n.getLong("Place")));TaleNetwork.send(p,TaleService.snapshot(p),false,true);msg(p,"direction");}
 public static InteractionResult interact(Mob npc,ServerPlayer p,InteractionHand hand){
  String hero=npc.getPersistentData().getString(TAG);
  // Migrate an uncompleted meeting from the first Ilya implementation.
  if(hero.isEmpty()&&npc instanceof IlyaEntity&&npc.getPersistentData().hasUUID(IlyaIntroduction.OWNER)){hero=IDS.getFirst();npc.getPersistentData().putString(TAG,hero);}
  if(!IDS.contains(hero))return InteractionResult.PASS;
  if(hand!=InteractionHand.MAIN_HAND)return InteractionResult.CONSUME;
  if(!npc.isAlive()||!p.isAlive()||p.isSpectator()||p.distanceToSqr(npc)>64)return InteractionResult.FAIL;
  if(unlocked(p,hero)){deliver(p,hero);msg(p,"already");return InteractionResult.CONSUME;}
  var n=state(p,hero);
  if(!n.hasUUID("Npc")||!n.getUUID("Npc").equals(npc.getUUID())){msg(p,"request_first");return InteractionResult.CONSUME;}
  if(!n.getBoolean("Met")){n.putBoolean("Met",true);TaleData.get(p.serverLevel()).setDirty();TaleService.promote(p,entry(hero),2,false);msg(p,"meet");return InteractionResult.CONSUME;}
  if(npc.getPersistentData().getBoolean("HeroHelpComplete")){n.remove("Npc");TaleData.get(p.serverLevel()).setDirty();msg(p,"insufficient");return InteractionResult.CONSUME;}
  TaleData.compound(npc.getPersistentData(),"HeroParticipants").putBoolean(p.getUUID().toString(),true);
  var shared=TaleData.compound(npc.getPersistentData(),DONATIONS);var scores=TaleData.compound(shared,"Scores");int given=0;
  var held=p.getMainHandItem();var chosen=held.isEmpty()?null:held.getItem();
  for(int i=0;i<SUPPLIES.length;i++){
   if(chosen!=null&&chosen!=SUPPLIES[i])continue;
   int need=REQUIRED[i]-shared.getInt("Item"+i);int available=chosen==null?org.slavicmyths.rpg.Runes.inventoryCount(p,SUPPLIES[i]):held.getCount();int take=Math.min(need,available);if(take<=0)continue;
   consume(p,SUPPLIES[i],take);shared.putInt("Item"+i,shared.getInt("Item"+i)+take);given+=take;if(i==1)org.slavicmyths.yaga.YagaServices.give(p,new ItemStack(Items.GLASS_BOTTLE,take));
  }
  if(given>0)scores.putInt(p.getUUID().toString(),scores.getInt(p.getUUID().toString())+given);
  boolean complete=true;for(int i=0;i<REQUIRED.length;i++)complete&=shared.getInt("Item"+i)>=REQUIRED[i];
  if(complete){npc.getPersistentData().putBoolean("HeroHelpComplete",true);for(String key:scores.getAllKeys())if(scores.getInt(key)*100>=15*11)try{reward(p.serverLevel(),UUID.fromString(key),hero);}catch(IllegalArgumentException ignored){}clearMeeting(p.serverLevel(),npc,hero);if(unlocked(p,hero))deliver(p,hero);npc.discard();}
  else msg(p,"progress",shared.getInt("Item0"),shared.getInt("Item1"),shared.getInt("Item2"),scores.getInt(p.getUUID().toString()));
  return InteractionResult.CONSUME;
 }
 private static void consume(ServerPlayer p,Item item,int count){for(int i=0;i<p.getInventory().getContainerSize()&&count>0;i++){var s=p.getInventory().getItem(i);if(s.is(item)){int take=Math.min(count,s.getCount());s.shrink(take);count-=take;}}p.getInventory().setChanged();}
 private static void clearMeeting(ServerLevel w,Mob npc,String hero){var data=TaleData.get(w);var participants=npc.getPersistentData().getCompound("HeroParticipants");for(String key:participants.getAllKeys())try{UUID player=UUID.fromString(key);var n=state(data,player,hero);if(n.hasUUID("Npc")&&n.getUUID("Npc").equals(npc.getUUID())){n.remove("Npc");if(!n.getBoolean("Helped"))n.remove("Met");data.player(player).getCompound("Hints").remove(entry(hero));}}catch(IllegalArgumentException ignored){}data.setDirty();}
 private static String bossHero(LivingEntity e){return e instanceof IdolishcheEntity?IDS.get(0):e instanceof org.slavicmyths.marinka.MarinkaEntity?IDS.get(1):e instanceof org.slavicmyths.skim.SkimBeastEntity?IDS.get(2):e instanceof org.slavicmyths.finist.KartausEntity?IDS.get(3):null;}
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
  var enemy=e.getEntity();String hero=bossHero(enemy);if(hero==null||!(enemy.level() instanceof ServerLevel)||e.getNewDamage()<=0)return;
  var n=TaleData.compound(enemy.getPersistentData(),"HeroHelpDamage");
  UUID encounter=enemy instanceof IdolishcheEntity b?b.encounter:enemy instanceof org.slavicmyths.marinka.MarinkaEntity b?b.encounter:enemy instanceof org.slavicmyths.skim.SkimBeastEntity b?b.encounter:((org.slavicmyths.finist.KartausEntity)enemy).encounter;
  String attempt=String.valueOf(encounter);if(!n.getString("Attempt").equals(attempt)){n.remove("Total");n.remove("Scores");n.putString("Attempt",attempt);}
  double room=Math.max(0,enemy.getMaxHealth()-n.getDouble("Total")),amount=Math.min(room,e.getNewDamage());n.putDouble("Total",n.getDouble("Total")+amount);
  if(e.getSource().getEntity() instanceof ServerPlayer p&&!p.isCreative()&&!p.isSpectator()&&state(p,hero).getBoolean("Met")&&p.distanceToSqr(enemy)<=128*128){var scores=TaleData.compound(n,"Scores");String key=p.getUUID().toString();if(scores.contains(key)||scores.size()<64)scores.putDouble(key,scores.getDouble(key)+amount);}
  if(!enemy.isAlive()&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))rewardBoss((ServerLevel)enemy.level(),enemy,hero);
 }
 private static void rewardBoss(ServerLevel w,LivingEntity entity,String hero){var scores=entity.getPersistentData().getCompound("HeroHelpDamage").getCompound("Scores");for(String key:scores.getAllKeys())if(scores.getDouble(key)+1e-6>=entity.getMaxHealth()*.15)try{reward(w,UUID.fromString(key),hero);}catch(IllegalArgumentException ignored){}}
 @SubscribeEvent public static void died(LivingDeathEvent e){
  if(!(e.getEntity().level() instanceof ServerLevel w))return;var entity=e.getEntity();String hero=bossHero(entity);
  if(hero!=null&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))w.getServer().execute(()->{if(!entity.isAlive())rewardBoss(w,entity,hero);});
  if(entity.getPersistentData().contains(TAG)){
   String id=entity.getPersistentData().getString(TAG);var data=TaleData.get(w);
   // Player records are addressed by their own saved reservations on next request.
   for(var p:w.getServer().getPlayerList().getPlayers()){var n=state(p,id);if(n.hasUUID("Npc")&&n.getUUID("Npc").equals(entity.getUUID())&&!n.getBoolean("Helped")){n.remove("Npc");n.remove("Met");data.setDirty();}}
  }
 }
 @SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)for(String hero:IDS)deliver(p,hero);}
 private HeroIntroductions(){}
}
