package org.slavicmyths.rpg;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;

/** Stored inside Forge's persisted player compound, never in a global player map. */
public final class PathData {
 public static final String KEY="SlavicPaths";
 public static final String[] PATHS={"druzhinnik","vedun","razboinik","koldun"};
 public static final String[] SKILLS={"drill","heavy_hand","riposte","shield_bash","stance","sweep","herbs","spirit_sense","charm_power","offering","healing_brew","quiet_step","light_step","ambush","aim","dodge","hunter","precision","sign_sense","rune_knowledge","spark","staff_power","resonance","storm_sign"};
 public static final int[] COST={3,5,7,8,10,12};
 public static CompoundTag data(Player p){
  CompoundTag root=p.getPersistentData();
  if(!root.contains(Player.PERSISTED_NBT_TAG,10))root.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
  CompoundTag saved=root.getCompound(Player.PERSISTED_NBT_TAG);
  return RpgNbt.player(saved);
 }
 public static boolean has(Player p,String skill){return !data(p).contains("Class2",10)&&data(p).getCompound("Skills").getInt(skill)>0;}
 public static int points(CompoundTag d,String path){int count=0;for(int i=0;i<24;i++)if(PATHS[i/6].equals(path))count+=Math.max(0,d.getCompound("Skills").getInt(SKILLS[i]));return count;}
 public static void message(Player p,String key,Object... args){p.displayClientMessage(Component.translatable("rpg.slavicmyths."+key,args),true);}
 public static boolean pay(Player p,int cost){if(p.isCreative())return true;if(p.experienceLevel<cost){message(p,"need_xp",cost);return false;}p.giveExperienceLevels(-cost);return true;}
 private static boolean contains(Player p,Item item,int count){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).getItem()==item)n+=p.getInventory().getItem(i).getCount();return n>=count;}
 private static void consume(Player p,Item item,int count){for(int i=0;i<p.getInventory().getContainerSize()&&count>0;i++){ItemStack s=p.getInventory().getItem(i);if(s.getItem()==item){int n=Math.min(count,s.getCount());s.shrink(n);count-=n;}}p.getInventory().setChanged();}
 public static void choose(ServerPlayer p,int path){
  if(data(p).contains("Class2",10))return;
  if(path<0||path>=4)return;CompoundTag d=data(p);String id=PATHS[path];
  boolean main=d.getString("Main").isEmpty();
  if(!main && (!d.getString("Secondary").isEmpty()||id.equals(d.getString("Main"))||points(d,d.getString("Main"))<3)){message(p,"secondary_locked");return;}
  Item[][] offerings={{Items.IRON_SWORD,Items.SHIELD},{ModItems.WARDING_CHARM.get(),ModItems.WORMWOOD.get()},{Items.BOW,ModItems.SILVER_DAGGER.get()},{ModItems.CARVED_STAFF.get(),ModItems.ANCIENT_SIGN.get()}};
  Item a=offerings[path][0],b=offerings[path][1];int n=path==1?4:1;
  if(!contains(p,a,1)||!contains(p,b,n)){message(p,"init_"+id);return;}
  if(!pay(p,10))return;consume(p,a,1);consume(p,b,n);d.putString(main?"Main":"Secondary",id);Knowledge.award(p,"own_path");message(p,"chosen",Component.translatable("path.slavicmyths."+id));
 }
 public static String gate(int i){if(i%6<4)return "";return i==23?"thunder_stone":i/6==1?"friend_domovoy":i/6==2?"meet_leshy":"first_ritual";}
 public static void buy(ServerPlayer p,int i){
  if(data(p).contains("Class2",10))return;
  if(i<0||i>=24)return;CompoundTag d=data(p);String path=PATHS[i/6],skill=SKILLS[i];
  boolean main=path.equals(d.getString("Main")),secondary=path.equals(d.getString("Secondary"));
  if((!main&&!secondary)||has(p,skill)||points(d,path)>=(main?30:10)||secondary&&i%6>=3){message(p,"skill_locked");return;}
  if(i%6>0&&!has(p,SKILLS[i-1])){message(p,"prerequisite",Component.translatable("skill.slavicmyths."+SKILLS[i-1]));return;}
  String gate=gate(i);if(!gate.isEmpty()&&!Knowledge.knows(p,gate)){message(p,"knowledge",Component.translatable("advancements.slavicmyths."+gate+".title"));return;}
  if(!pay(p,COST[i%6]))return;
  if(!d.contains("Skills",10))d.put("Skills",new CompoundTag());d.getCompound("Skills").putInt(skill,1);message(p,"learned",Component.translatable("skill.slavicmyths."+skill));
 }
 public static boolean ready(Player p,String id,int ticks){CompoundTag d=data(p);long now=p.level().getGameTime();String key="CD_"+id;if(d.getLong(key)>now)return false;d.putLong(key,now+ticks);return true;}
 private PathData(){}
}
