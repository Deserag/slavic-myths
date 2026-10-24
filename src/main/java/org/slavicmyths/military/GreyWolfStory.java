package org.slavicmyths.military;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.slavicmyths.wolf.*;
import org.slavicmyths.kurgan.*;
import org.slavicmyths.hunt.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.navigation.*;

/** Linear story stored in the existing Military quest attachment; no second questbook. */
public final class GreyWolfStory {
 public static final String[] IDS=WolfStoryDefinition.ALL.stream().map(WolfStoryDefinition::id).toArray(String[]::new);
 public enum Stage {LOCKED,OFFERED,ACTIVE,REWARD_READY,COMPLETED,LOCKED_FUTURE}
 public static CompoundTag data(ServerPlayer p){var root=MilitaryQuests.state(p);if(!root.contains("GreyWolfStory",10)){var d=new CompoundTag();d.putInt("Version",1);root.put("GreyWolfStory",d);}var story=root.getCompound("GreyWolfStory");if(story.getInt("Version")<2)story.putInt("Version",2);return story;}
 public static CompoundTag quest(ServerPlayer p,int i){var root=data(p);String key=IDS[i];if(!root.contains(key,10))root.put(key,new CompoundTag());return root.getCompound(key);}
 public static Stage stage(ServerPlayer p,int i){if(!WolfProgress.passed(p)||i>0&&!quest(p,i-1).getBoolean("Claimed")||i==3&&(!quest(p,0).getBoolean("Claimed")||!quest(p,1).getBoolean("Claimed")))return Stage.LOCKED;var q=quest(p,i);return q.getBoolean("Claimed")?Stage.COMPLETED:q.getBoolean("Ready")?Stage.REWARD_READY:q.getBoolean("Accepted")?Stage.ACTIVE:Stage.OFFERED;}
 public static boolean known(ServerPlayer p,int i){return WolfStoryDefinition.ALL.get(i).rewardPolicy().equals("REVEAL_IMMEDIATELY")||quest(p,i).getBoolean("Discovered")||quest(p,i).getBoolean("Claimed")||i==3&&quest(p,i).getBoolean("Ready");}
 public static boolean action(ServerPlayer p,GreyWolf wolf,int i,int action){
  if(i<0||i>=4||!WolfStoryNetwork.valid(p,wolf))return false;var q=quest(p,i);var state=stage(p,i);
  if(i==3&&(action==4||action==2)){var record=org.slavicmyths.volkolak.VolkolakStory.record(p);if(record==null||!record.wolf.equals(wolf.home()))return false;}
  if(action==4)return i==3&&state==Stage.ACTIVE&&org.slavicmyths.volkolak.VolkolakStory.handOver(p);
  if(action==1){if(state==Stage.LOCKED)return false;q.putBoolean("Discovered",true);return true;}
  if(action==0){if(state!=Stage.OFFERED)return false;q.putBoolean("Accepted",true);q.putInt("Progress",0);q.putLong("AcceptedAt",now(p));data(p).putLong("WolfHome",wolf.home().asLong());refreshTarget(p);cue(p,"quest_accept");return true;}
  if(action==3){if(state!=Stage.ACTIVE&&state!=Stage.REWARD_READY)return false;refreshTarget(p);track(p,i);return true;}
  if(action!=2||state!=Stage.REWARD_READY)return false;
  var rewards=rewards(p,i,true);if(rewards.isEmpty())return false;
  // Simulate native insertion into a detached inventory before committing either rewards or claim.
  var probe=new net.minecraft.world.entity.player.Inventory(p);for(int n=0;n<p.getInventory().getContainerSize();n++)probe.setItem(n,p.getInventory().getItem(n).copy());
  for(var reward:rewards){var remainder=reward.copy();probe.add(remainder);if(!remainder.isEmpty()){p.displayClientMessage(text("inventory_full"),true);return false;}}
  q.putBoolean("Claimed",true);q.putLong("ClaimedAt",now(p));q.putBoolean("Discovered",true);
  for(int n=0;n<p.getInventory().getContainerSize();n++)p.getInventory().setItem(n,probe.getItem(n));p.getInventory().setChanged();if(i<3)org.slavicmyths.progression.AdvancementJourney.award(p,IDS[i]);untrack(p,i);cue(p,"reward_claim");return true;
 }
 public static long now(ServerPlayer p){return p.getServer().overworld().getGameTime();}
 public static net.minecraft.network.chat.MutableComponent text(String key,Object...args){return net.minecraft.network.chat.Component.translatable("wolfstory.slavicmyths."+key,args);}
 public static List<ItemStack> rewards(ServerPlayer p,int i,boolean exact){if(i==0)return List.of(new ItemStack(Items.DIAMOND,3));if(i==2)return List.of(new ItemStack(org.slavicmyths.finist.FinistItems.RING.get()));if(i==3)return List.of(new ItemStack(org.slavicmyths.finist.FinistItems.BOW.get()),new ItemStack(org.slavicmyths.finist.FinistItems.QUIVER.get()));
  var q=quest(p,1);if(exact&&!q.contains("Rune")){var pool=RuneBalance.DEFINITIONS.stream().filter(d->d.tier()==2).map(d->d.id()).toList();q.putString("Rune",pool.get(p.getRandom().nextInt(pool.size())));}
  String rune=exact?q.getString("Rune"):"";ItemStack icon=rune.isEmpty()?RuneFoundation.blank(2,"diamond"):new ItemStack(BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_"+rune)));return List.of(new ItemStack(RuneFoundation.item("empowered_soul")),icon);
 }
 public static void killed(ServerPlayer p,LivingEntity victim){if(stage(p,0)==Stage.ACTIVE&&victim.getType().is(Military.BANDITS)&&!victim.getPersistentData().getBoolean("RuneSummoned")){var q=quest(p,0);int n=Math.min(15,q.getInt("Progress")+1);q.putInt("Progress",n);if(n==15)ready(p,0);else if(n==5||n==10)cue(p,"quest_progress");}
  if(victim instanceof KurganCreature k&&k.kurgan!=null){var w=p.serverLevel();var record=BurialRecords.get(w).instances.get(k.kurgan);if(record!=null&&mainBoss(record,k)){if(!record.bossDefeated)KurganCurse.sourceDefeated(w,record.id);record.bossDefeated=true;record.bossContributors.add(p.getUUID());BurialRecords.get(w).setDirty();if(stage(p,1)==Stage.ACTIVE){var q=quest(p,1);if(!q.hasUUID("Site")||q.getUUID("Site").equals(record.id)){q.putUUID("Site",record.id);q.putInt("Tier",record.plan.tier+1);ready(p,1);}}}}
  if(stage(p,2)==Stage.ACTIVE&&victim instanceof WorldBoss boss&&boss.kind()==BossKind.LIKHO){var q=quest(p,2);if(q.getString("BossKey").equals(boss.encounter)&&q.hasUUID("Target")&&q.getUUID("Target").equals(boss.getUUID())){q.putUUID("Target",boss.getUUID());ready(p,2);}}
  WolfStoryNetwork.changed(p);
 }
 public static boolean mainBoss(KurganInstance i,KurganCreature k){if(i.plan.tier==2&&k.kind==KurganFighter.Kind.PRINCE||i.plan.tier==1&&k.kind==KurganFighter.Kind.VOEVODA)return i.encounters.values().stream().flatMap(s->s.slots.stream()).anyMatch(s->k.getUUID().equals(s.uuid)&&s.kind==k.kind);return false;}
 public static void ready(ServerPlayer p,int i){var q=quest(p,i);if(q.getBoolean("Ready"))return;q.putBoolean("Ready",true);q.putInt("Progress",i==0?15:1);if(i==1)rewards(p,i,true);cue(p,"quest_complete");}
 public static void refreshTarget(ServerPlayer p){
  if(p.level().dimension()!=Level.OVERWORLD)return;var w=p.serverLevel();var home=BlockPos.of(data(p).getLong("WolfHome"));
  if(stage(p,1)==Stage.ACTIVE){var q=quest(p,1);var records=BurialRecords.get(w);var old=q.hasUUID("Site")?records.instances.get(q.getUUID("Site")):null;if(old!=null&&old.bossDefeated&&old.bossContributors.contains(p.getUUID())){ready(p,1);return;}if(old!=null&&!old.bossDefeated)return;
   // Index-only bounded read. Tier III first, then II, no chunk requests or structures created.
   var options=records.instances.values().stream().limit(4096).filter(i->i.plan.tier>=1).toList();var proof=options.stream().filter(i->i.bossDefeated&&i.bossContributors.contains(p.getUUID())).findFirst().orElse(null);var site=proof!=null?proof:options.stream().filter(i->!i.bossDefeated&&i.origin.distSqr(home)<=8192.0*8192).min(Comparator.<KurganInstance>comparingInt(i->-i.plan.tier).thenComparingDouble(i->i.origin.distSqr(home))).orElse(null);
   q.remove("Site");if(site!=null){q.putUUID("Site",site.id);q.putInt("Tier",site.plan.tier+1);q.putLong("Clue",site.origin.asLong());if(q.getBoolean("Tracked"))track(p,1);if(proof!=null)ready(p,1);}
  }
  if(stage(p,2)==Stage.ACTIVE)StoryLikho.ensure(p,home);
  if(stage(p,3)==Stage.ACTIVE)org.slavicmyths.volkolak.VolkolakStory.ensure(p,home);
 }
 public static CompoundTag snapshot(ServerPlayer p,GreyWolf wolf){var out=new CompoundTag();out.putUUID("Wolf",wolf.getUUID());out.putInt("Entity",wolf.getId());var list=new ListTag();for(int i=0;i<4;i++){var q=quest(p,i);var v=new CompoundTag();v.putInt("Index",i);v.putString("State",stage(p,i).name());v.putInt("Progress",Math.clamp(q.getInt("Progress"),0,i==0?15:1));v.putBoolean("Known",known(p,i));v.putInt("Tier",q.getInt("Tier"));if(i==3){v.putString("Phase",org.slavicmyths.volkolak.VolkolakStory.phase(p));var origin=BlockPos.of(data(p).getLong("WolfHome"));var clue=BlockPos.of(q.getLong("Hint"));double angle=Math.atan2(clue.getX()-origin.getX(),origin.getZ()-clue.getZ());String[] directions={"n","ne","e","se","s","sw","w","nw"};v.putString("Direction",directions[Math.floorMod((int)Math.round(angle/(Math.PI/4)),8)]);}v.putBoolean("HasTarget",i==1?q.hasUUID("Site"):i==3?q.hasUUID("Encounter"):!q.getString("BossKey").isEmpty());if(i==1&&q.contains("Clue")||i>=2&&q.contains("Hint"))v.putLong("Clue",q.getLong(i==1?"Clue":"Hint"));if(known(p,i)){var rewards=new ListTag();for(var item:rewards(p,i,i==1&&q.getBoolean("Claimed")))rewards.add(item.save(p.registryAccess()));v.put("Rewards",rewards);}list.add(v);}out.put("Quests",list);return out;}
 public static void track(ServerPlayer p,int i){var q=quest(p,i);String key=i==1?"Clue":"Hint";if(i==0||!q.contains(key))return;q.putBoolean("Tracked",true);NavigationManager.reveal(p,"wolf_story:"+IDS[i],MarkerCategory.QUEST,SlavicMarker.Kind.TEMPORARY_QUEST,"wolfstory.slavicmyths.title."+i,Level.OVERWORLD.location(),BlockPos.of(q.getLong(key)),true,false);}
 public static void untrack(ServerPlayer p,int i){var state=NavigationRecords.get(p.serverLevel()).player(p.getUUID());state.markers.remove(SlavicMarker.identity("wolf_story:"+IDS[i]));NavigationManager.changed(p);}
 public static void cue(ServerPlayer p,String cue){p.playNotifySound(org.slavicmyths.finist.FinistSounds.get(cue),net.minecraft.sounds.SoundSource.PLAYERS,.5F,1);}
 private GreyWolfStory(){}
}
