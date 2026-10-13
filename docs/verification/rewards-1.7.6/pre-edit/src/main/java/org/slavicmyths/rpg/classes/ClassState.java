package org.slavicmyths.rpg.classes;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.rpg.*;

/** Class 2.0 extends the existing persisted SlavicPaths compound; no second player store. */
public final class ClassState {
    public static final String KEY="Class2";
    public static final ResourceLocation HEALTH=id("class_health"),RESIST=id("class_resist"),SPEED=id("class_speed"),FLURRY=id("class_flurry");
    private static final Map<String,String> LEGACY=Map.ofEntries(Map.entry("drill","steadfast"),Map.entry("heavy_hand","lunge"),Map.entry("riposte","riposte"),Map.entry("shield_bash","shield_ram"),Map.entry("stance","temper"),Map.entry("sweep","flurry"),Map.entry("herbs","herbalism"),Map.entry("spirit_sense","spirit_sense"),Map.entry("charm_power","ward_power"),Map.entry("offering","binding_sign"),Map.entry("healing_brew","cleanse"),Map.entry("quiet_step","smoke"),Map.entry("light_step","light_step"),Map.entry("ambush","dirty_strike"),Map.entry("aim","steady_aim"),Map.entry("dodge","dash"),Map.entry("hunter","cold_blood"),Map.entry("precision","hunters_fervor"),Map.entry("sign_sense","spirit_sense"),Map.entry("rune_knowledge","herbalism"),Map.entry("spark","binding_sign"),Map.entry("staff_power","ward_power"),Map.entry("resonance","ward"),Map.entry("storm_sign","hex"));
    public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    public static long now(Player p){return p.getServer()==null?p.level().getGameTime():p.getServer().overworld().getGameTime();}
    public static CompoundTag data(Player p){var old=PathData.data(p);if(!old.contains(KEY,10))old.put(KEY,migrate(old,now(p)));return old.getCompound(KEY);}
    public static CompoundTag migrate(CompoundTag old,long now){
        var d=new CompoundTag();d.putInt("Version",2);d.putInt("Level",1);d.putInt("Points",1);d.putBoolean("Hud",true);d.put("Ranks",new CompoundTag());d.put("Cooldowns",new CompoundTag());d.put("LegacyPaths",old.copy());
        String original=old.getString("Main"),base=original.equals("koldun")?"vedun":original;if(ClassDefinitions.BASES.contains(base))d.putString("Base",base);
        if(original.equals("koldun")){d.putString("First","koldun");d.putInt("Level",ClassDefinitions.FIRST_LEVEL);d.putInt("Points",ClassDefinitions.FIRST_LEVEL);}
        int needed=1;var ranks=d.getCompound("Ranks");for(var e:LEGACY.entrySet())if(old.getCompound("Skills").getInt(e.getKey())>0){ranks.putInt(e.getValue(),1);var def=ClassDefinitions.SKILLS.get(e.getValue());if(def.base().equals(base))needed=Math.max(needed,def.level());}
        if(needed>d.getInt("Level")){d.putInt("Points",d.getInt("Points")+needed-d.getInt("Level"));d.putInt("Level",needed);}
        var active=new ArrayList<String>();for(String key:ClassDefinitions.SKILLS.keySet()){var def=ClassDefinitions.SKILLS.get(key);if(def.base().equals(base)&&ranks.getInt(key)>0){if(def.kind()==ClassDefinitions.Kind.ACTIVE&&active.size()<3)active.add(key);else if(def.kind()==ClassDefinitions.Kind.PASSIVE&&d.getString("Passive").isEmpty())d.putString("Passive",key);}}
        int selected=old.getInt("Active")-1;if(selected>=0&&selected<PathData.SKILLS.length){String mapped=LEGACY.get(PathData.SKILLS[selected]);if(mapped!=null&&ClassDefinitions.SKILLS.get(mapped).kind()==ClassDefinitions.Kind.ACTIVE&&ClassDefinitions.SKILLS.get(mapped).base().equals(base)){active.remove(mapped);active.add(0,mapped);}}
        for(int i=0;i<Math.min(3,active.size());i++)d.putString("Active"+i,active.get(i));
        for(var e:LEGACY.entrySet()){long until=old.getLong("CD_ability_"+e.getKey());if(until>now)d.getCompound("Cooldowns").putLong(e.getValue(),Math.max(until,d.getCompound("Cooldowns").getLong(e.getValue())));}
        int spent=0;for(var skill:ClassDefinitions.SKILLS.values())if(skill.base().equals(base))spent+=ranks.getInt(skill.id());d.putInt("Points",Math.max(0,d.getInt("Level")-spent));
        return d;
    }
    public static int rank(Player p,String id){var def=ClassDefinitions.SKILLS.get(id);var d=data(p);return def!=null&&def.base().equals(d.getString("Base"))?Math.max(0,Math.min(def.maxRank(),d.getCompound("Ranks").getInt(id))):0;}
    public static boolean trained(Player p,String id){return rank(p,id)>0;}
    public static int passive(Player p,String id){return data(p).getString("Passive").equals(id)?rank(p,id):0;}
    public static long remaining(Player p,String id){return Math.max(0,data(p).getCompound("Cooldowns").getLong(id)-now(p));}
    public static void cooldown(Player p,String id,int ticks){data(p).getCompound("Cooldowns").putLong(id,now(p)+Math.max(0,ticks));}
    public static double branchMultiplier(Player p,String skill){var d=data(p);double result=1;for(String key:List.of("First","Second")){var b=ClassDefinitions.BRANCHES.get(d.getString(key));if(b!=null&&b.enhancedSkill().equals(skill))result*=b.multiplier();}return result;}
    public static boolean branch(Player p,String id){var d=data(p);return d.getString("First").equals(id)||d.getString("Second").equals(id);}
    public static String blocker(CompoundTag d,String id){
        return blocker(d,id,false);
    }
    public static String blocker(CompoundTag d,String id,boolean creative){
        var s=ClassDefinitions.SKILLS.get(id);return ClassLearningRules.blocker(d.getString("Base"),s,d.getCompound("Ranks").getInt(id),d.getInt("Level"),s==null?0:d.getCompound("Ranks").getInt(s.prerequisite()),s!=null&&(s.branch().isEmpty()||s.branch().equals(d.getString("First"))||s.branch().equals(d.getString("Second"))),d.getInt("Points"),creative);
    }
    public static boolean choose(ServerPlayer p,String base){var d=data(p);if(!ClassDefinitions.BASES.contains(base)||!d.getString("Base").isEmpty())return false;d.putString("Base",base);org.slavicmyths.progression.Knowledge.award(p,"own_path");sync(p);return true;}
    public static boolean learn(ServerPlayer p,String id){var d=data(p);if(!blocker(d,id,p.isCreative()).isEmpty())return false;d.getCompound("Ranks").putInt(id,d.getCompound("Ranks").getInt(id)+1);d.putInt("Points",ClassLearningRules.pointsAfterLearning(d.getInt("Points"),p.isCreative()));restore(p);sync(p);return true;}
    public static boolean equip(ServerPlayer p,String id,int slot){var def=ClassDefinitions.SKILLS.get(id);var d=data(p);if(id.isEmpty()&&slot>=0&&slot<=3){d.remove(slot==3?"Passive":"Active"+slot);restore(p);sync(p);return true;}
        if(def==null||rank(p,id)==0||slot<0||slot>3||slot==3&&def.kind()!=ClassDefinitions.Kind.PASSIVE||slot<3&&def.kind()!=ClassDefinitions.Kind.ACTIVE)return false;
        if(slot<3)for(int i=0;i<3;i++)if(d.getString("Active"+i).equals(id))d.remove("Active"+i);d.putString(slot==3?"Passive":"Active"+slot,id);restore(p);sync(p);return true;}
    public static boolean advance(ServerPlayer p,String id){var b=ClassDefinitions.BRANCHES.get(id);var d=data(p);if(b==null||!b.base().equals(d.getString("Base"))||!p.isCreative()&&d.getInt("Level")<b.level())return false;
        String key=b.level()==ClassDefinitions.FIRST_LEVEL?"First":"Second",parent=b.level()==ClassDefinitions.FIRST_LEVEL?d.getString("Base"):d.getString("First");if(!d.getString(key).isEmpty()||!b.parent().equals(parent))return false;d.putString(key,id);restore(p);sync(p);return true;}
    public static void awardXp(ServerPlayer p,int amount){if(amount<=0||amount>ClassDefinitions.MAX_XP_GRANT||data(p).getString("Base").isEmpty())return;var d=data(p);long xp=d.getLong("Xp")+amount;int level=d.getInt("Level");while(level<ClassDefinitions.MAX_LEVEL&&xp>=ClassDefinitions.xpForLevel(level)){xp-=ClassDefinitions.xpForLevel(level++);d.putInt("Points",Math.min(10_000,d.getInt("Points")+1));}d.putInt("Level",level);d.putLong("Xp",Math.min(xp,ClassDefinitions.xpForLevel(level)));sync(p);}
    public static void restore(ServerPlayer p){
        modifier(p,Attributes.MAX_HEALTH,HEALTH,passive(p,"temper")==0?0:ClassDefinitions.SKILLS.get("temper").power(passive(p,"temper")),AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.KNOCKBACK_RESISTANCE,RESIST,passive(p,"steadfast")==0?0:ClassDefinitions.SKILLS.get("steadfast").power(passive(p,"steadfast")),AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.MOVEMENT_SPEED,SPEED,passive(p,"light_step")==0?0:ClassDefinitions.SKILLS.get("light_step").power(passive(p,"light_step")),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        p.setHealth(Math.min(p.getHealth(),p.getMaxHealth()));
    }
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attr,ResourceLocation id,double amount,AttributeModifier.Operation op){var instance=p.getAttribute(attr);if(instance==null)return;instance.removeModifier(id);if(amount>0)instance.addPermanentModifier(new AttributeModifier(id,amount,op));}
    public static void sync(ServerPlayer p){RpgNetwork.sync(p);}
    private ClassState(){}
}
