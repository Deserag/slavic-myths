package org.slavicmyths.rpg.classes;

import net.minecraft.nbt.CompoundTag;

/** Effective parameters shared by authoritative execution and numerical UI previews. */
public final class ClassBalance {
    public static final int BULWARK_DEFENSE_TICKS=40;
    public static boolean evolution(CompoundTag d,String id){var s=ClassDefinitions.SKILLS.get(id);return s!=null&&d.getCompound("Ranks").getInt(id)>0&&(s.branch().equals(d.getString("First"))||s.branch().equals(d.getString("Second")));}
    public static double branch(CompoundTag d,String id){double factor=1;for(String key:java.util.List.of("First","Second")){var b=ClassDefinitions.BRANCHES.get(d.getString(key));if(b!=null&&b.enhancedSkill().equals(id))factor*=b.multiplier();}return factor;}
    public static double power(CompoundTag d,String id,int rank){double value=ClassDefinitions.SKILLS.get(id).power(rank)*branch(d,id);if(id.equals("lunge")&&evolution(d,"onslaught"))value*=1.15;if(id.equals("shield_ram")&&evolution(d,"bulwark"))value*=1.2;if(id.equals("ward")&&evolution(d,"greater_ward"))value*=1.2;if(id.equals("keen_eye")&&evolution(d,"true_shot"))value+=5;if((id.equals("ward")||id.equals("last_rite"))&&d.getString("Passive").equals("ward_power")){int passive=d.getCompound("Ranks").getInt("ward_power");if(passive>0)value*=1+ClassDefinitions.SKILLS.get("ward_power").power(passive);}return value;}
    public static int duration(CompoundTag d,String id,int rank){double factor=branch(d,id);if(id.equals("hex")&&evolution(d,"greater_hex"))factor*=1.25;if(id.equals("ward")||id.equals("last_rite")){if(id.equals("ward")&&evolution(d,"greater_ward"))factor*=1.2;if(d.getString("Passive").equals("ward_power")){int passive=d.getCompound("Ranks").getInt("ward_power");if(passive>0)factor*=1+ClassDefinitions.SKILLS.get("ward_power").power(passive);}}return (int)Math.round(ClassDefinitions.SKILLS.get(id).duration(rank)*factor);}
    public static double range(CompoundTag d,String id,int rank){double value=ClassDefinitions.SKILLS.get(id).range(rank);if(id.equals("dash"))value*=branch(d,id);if(id.equals("lunge")&&evolution(d,"onslaught")||id.equals("dash")&&evolution(d,"raider_dash"))value+=.5;return value;}
    public static int cooldown(CompoundTag d,String id,int rank){return (int)Math.round(ClassDefinitions.SKILLS.get(id).cooldown(rank)*(id.equals("dash")&&evolution(d,"raider_dash")?.85:1));}
    private ClassBalance(){}
}
