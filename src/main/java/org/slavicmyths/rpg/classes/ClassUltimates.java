package org.slavicmyths.rpg.classes;

import java.util.*;
import net.minecraft.nbt.CompoundTag;

/** Separate slot and immutable definitions; never consumes ordinary skill points. */
public final class ClassUltimates {
    public record Definition(String parent,String id,String skill,String base,int cooldown,int duration,double range,double power){}
    public static final Map<String,Definition> ALL;
    static {
        var m=new LinkedHashMap<String,Definition>();
        add(m,"bogatyr","gromoboy","earth_thunder","druzhinnik",100,5,4,9);
        add(m,"ratoborets","master_sechi","unyielding_flurry","druzhinnik",85,6,2.5,.4);
        add(m,"schitonosets","nesokrushimy","living_wall","druzhinnik",100,6,4,.30);
        add(m,"voevoda","verkhovny_voevoda","banner_of_druzhina","druzhinnik",150,12,10,.10);
        add(m,"oberezhnik","khranitel_kruga","sacred_circle","vedun",110,10,5,.20);
        add(m,"znakhar","zhivotvorets","living_water","vedun",120,0,8,6);
        add(m,"morovik","povelitel_mora","plague_seal","vedun",110,8,12,1);
        add(m,"chernoknizhnik","tainoved","shadow_pact","vedun",105,0,12,10);
        add(m,"naletchik","vikhrevoy_naletchik","piercing_breakthrough","razboinik",80,3,3,3);
        add(m,"strelok","sokoliny_glaz","vulnerable_point","razboinik",90,6,32,6);
        add(m,"ataman","tenevoy_ataman","false_ambush","razboinik",125,6,8,.10);
        add(m,"reznik","krovnik","blood_mark","razboinik",100,10,12,8);
        ALL=Collections.unmodifiableMap(m);
    }
    private static void add(Map<String,Definition> m,String p,String id,String skill,String base,int cd,int duration,double range,double power){m.put(id,new Definition(p,id,skill,base,cd*20,duration*20,range,power));}
    public static Definition selected(CompoundTag d){var u=ALL.get(d.getString("Third"));return u!=null&&next(d)==u?u:null;}
    public static Definition next(CompoundTag d){if(!ClassState.secondPassive(d))return null;var first=ClassDefinitions.BRANCHES.get(d.getString("First"));if(first==null||!first.parent().equals(d.getString("Base")))return null;return ALL.values().stream().filter(u->u.parent().equals(d.getString("Second"))&&u.base().equals(d.getString("Base"))).findFirst().orElse(null);}
    private ClassUltimates(){}
}
