package org.slavicmyths.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** Local 1.9.8.2 load adapters only; never scans or subscribes to all entities. */
public final class CreatureHealthBalance {
    public static final String KEY="CreatureHealthBalance1982";
    public static final int VERSION=1;
    private static final String MAX_HEALTH="minecraft:generic.max_health";

    public static float proportional(float health,double oldMax,double newMax){
        if(!Float.isFinite(health)||health<=0)return 0;
        if(!Double.isFinite(oldMax)||oldMax<=0||!Double.isFinite(newMax)||newMax<=0)return 0;
        return (float)Math.clamp(health/oldMax*newMax,0,newMax);
    }
    public static void mark(CompoundTag tag){tag.putInt(KEY,VERSION);}
    public static double savedBase(CompoundTag tag,double fallback){
        for(var value:tag.getList("attributes",10)){
            var row=(CompoundTag)value;
            if(MAX_HEALTH.equals(row.getString("id"))){double base=row.getDouble("base");return Double.isFinite(base)&&base>0?base:fallback;}
        }
        return fallback;
    }
    /** Keep existing permanent modifiers (including mor_health) and their native ordering. */
    private static double maximum(double base,CompoundTag attribute,boolean uncapped){
        double added=base;
        for(var value:attribute.getList("modifiers",10)){
            var m=AttributeModifier.load((CompoundTag)value);
            if(m!=null&&m.operation()==AttributeModifier.Operation.ADD_VALUE)added+=m.amount();
        }
        double result=added;
        for(var value:attribute.getList("modifiers",10)){
            var m=AttributeModifier.load((CompoundTag)value);
            if(m!=null&&m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)result+=added*m.amount();
        }
        for(var value:attribute.getList("modifiers",10)){
            var m=AttributeModifier.load((CompoundTag)value);
            if(m!=null&&m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)result*=1+m.amount();
        }
        return Double.isFinite(result)?Math.clamp(result,1,uncapped?Float.MAX_VALUE:1024):base;
    }
    public static double uncappedMaximum(AttributeInstance attribute){
        double added=attribute.getBaseValue();
        for(var m:attribute.getModifiers())if(m.operation()==AttributeModifier.Operation.ADD_VALUE)added+=m.amount();
        double result=added;
        for(var m:attribute.getModifiers())if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)result+=added*m.amount();
        for(var m:attribute.getModifiers())if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)result*=1+m.amount();
        return Double.isFinite(result)?Math.clamp(result,1,Float.MAX_VALUE):attribute.getBaseValue();
    }
    /** Adapt a copy BEFORE vanilla loads attributes/Health, avoiding a lossy clamp. */
    public static CompoundTag prepare(CompoundTag original,double newBase,double legacyBase,boolean uncapped){
        if(original.getInt(KEY)>=VERSION)return original;
        CompoundTag tag=original.copy(),attribute=null;
        ListTag attributes=tag.getList("attributes",10);
        for(var value:attributes){var row=(CompoundTag)value;if(MAX_HEALTH.equals(row.getString("id"))){attribute=row;break;}}
        if(attribute==null){attribute=new CompoundTag();attribute.putString("id",MAX_HEALTH);attribute.putDouble("base",legacyBase);attributes.add(attribute);tag.put("attributes",attributes);}
        double oldBase=attribute.getDouble("base");if(!Double.isFinite(oldBase)||oldBase<=0)oldBase=legacyBase;
        // Every legacy caller used vanilla's capped attribute, including old Morozko.
        double oldMax=maximum(oldBase,attribute,false),newMax=maximum(newBase,attribute,uncapped);
        if(tag.contains("Health",99))tag.putFloat("Health",proportional(tag.getFloat("Health"),oldMax,newMax));
        attribute.putDouble("base",newBase);mark(tag);return tag;
    }
    private CreatureHealthBalance(){}
}
