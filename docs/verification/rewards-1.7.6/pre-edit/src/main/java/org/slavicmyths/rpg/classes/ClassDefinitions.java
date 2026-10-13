package org.slavicmyths.rpg.classes;

import java.util.*;

/** Stable save IDs and the complete first-pass balance. Translation never determines behavior. */
public final class ClassDefinitions {
    public enum Kind { ACTIVE, PASSIVE, TRIGGER, EVOLUTION }
    public record Skill(String id,String base,Kind kind,int level,String prerequisite,int prerequisiteRank,String branch,
                        int[] cooldown,int[] duration,double[] range,double[] power) {
        public int maxRank(){return kind==Kind.EVOLUTION?1:3;}
        public int cooldown(int rank){return cooldown[Math.max(0,Math.min(2,rank-1))];}
        public int duration(int rank){return duration[Math.max(0,Math.min(2,rank-1))];}
        public double range(int rank){return range[Math.max(0,Math.min(2,rank-1))];}
        public double power(int rank){return power[Math.max(0,Math.min(2,rank-1))];}
        public String key(){return "classskill.slavicmyths."+id;}
    }
    public record Branch(String id,String parent,String base,int level,String enhancedSkill,double multiplier){}
    public static final List<String> BASES=List.of("druzhinnik","vedun","razboinik");
    public static final int FIRST_LEVEL=10,SECOND_LEVEL=25,MAX_LEVEL=50,MAX_XP_GRANT=1_000_000;
    public static final Map<String,Skill> SKILLS;
    public static final Map<String,Branch> BRANCHES;
    static {
        var s=new LinkedHashMap<String,Skill>();
        add(s,"lunge","druzhinnik",Kind.ACTIVE,1,"",0,"",ints(160,140,120),ints(0,0,0),nums(2,2.5,3),nums(.35,.55,.75));
        add(s,"flurry","druzhinnik",Kind.ACTIVE,2,"lunge",1,"",ints(360,320,280),ints(50,60,75),nums(0,0,0),nums(.25,.35,.45));
        add(s,"trip","druzhinnik",Kind.ACTIVE,3,"lunge",1,"",ints(240,220,200),ints(16,26,36),nums(3,3,3.5),nums(1,1.5,2));
        add(s,"aerial_strike","druzhinnik",Kind.ACTIVE,5,"flurry",1,"",ints(440,400,360),ints(80,90,100),nums(2,2.5,3),nums(.40,.65,.90));
        add(s,"shield_ram","druzhinnik",Kind.ACTIVE,7,"trip",1,"",ints(240,220,200),ints(20,25,30),nums(3,3,3.5),nums(.7,.9,1.1));
        add(s,"temper","druzhinnik",Kind.PASSIVE,2,"",0,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(2,3,4));
        add(s,"steadfast","druzhinnik",Kind.PASSIVE,4,"temper",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.08,.12,.16));
        add(s,"riposte","druzhinnik",Kind.TRIGGER,6,"shield_ram",1,"",ints(280,240,200),ints(60,60,60),nums(0,0,0),nums(1.5,2,2.5));
        add(s,"second_wind","druzhinnik",Kind.TRIGGER,9,"temper",1,"",ints(1800,1500,1200),ints(80,100,120),nums(0,0,0),nums(.3,.3,.3));
        add(s,"binding_sign","vedun",Kind.ACTIVE,1,"",0,"",ints(280,250,220),ints(30,40,50),nums(8,10,12),nums(0,0,0));
        add(s,"ward","vedun",Kind.ACTIVE,2,"binding_sign",1,"",ints(480,440,400),ints(160,200,240),nums(0,0,0),nums(4,6,8));
        add(s,"hex","vedun",Kind.ACTIVE,3,"binding_sign",1,"",ints(360,320,280),ints(100,140,180),nums(8,10,12),nums(0,1,1));
        add(s,"cleanse","vedun",Kind.ACTIVE,5,"ward",1,"",ints(500,460,400),ints(0,0,0),nums(0,0,0),nums(1,2,3));
        add(s,"slumber","vedun",Kind.ACTIVE,7,"hex",1,"",ints(440,400,360),ints(40,60,80),nums(8,10,12),nums(0,0,0));
        add(s,"ward_power","vedun",Kind.PASSIVE,2,"",0,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.10,.20,.30));
        add(s,"herbalism","vedun",Kind.PASSIVE,4,"ward_power",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(1,1.5,2));
        add(s,"spirit_sense","vedun",Kind.PASSIVE,8,"herbalism",1,"",ints(0,0,0),ints(40,40,40),nums(8,10,12),nums(0,0,0));
        add(s,"last_rite","vedun",Kind.TRIGGER,9,"ward",1,"",ints(2400,2100,1800),ints(60,80,100),nums(0,0,0),nums(2,4,4));
        add(s,"keen_eye","razboinik",Kind.ACTIVE,1,"",0,"",ints(300,260,220),ints(60,80,100),nums(24,28,32),nums(10,15,20));
        add(s,"dash","razboinik",Kind.ACTIVE,2,"keen_eye",1,"",ints(200,170,140),ints(40,40,40),nums(2.5,3,3.5),nums(0,0,0));
        add(s,"dirty_strike","razboinik",Kind.ACTIVE,3,"dash",1,"",ints(280,250,220),ints(80,90,100),nums(3,3,3),nums(.35,.50,.65));
        add(s,"net","razboinik",Kind.ACTIVE,5,"keen_eye",1,"",ints(320,280,240),ints(30,40,50),nums(8,10,12),nums(0,0,0));
        add(s,"smoke","razboinik",Kind.ACTIVE,7,"dash",1,"",ints(500,460,420),ints(40,60,80),nums(3,4,5),nums(0,0,0));
        add(s,"light_step","razboinik",Kind.PASSIVE,2,"",0,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.03,.05,.07));
        add(s,"steady_aim","razboinik",Kind.PASSIVE,4,"light_step",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.04,.07,.10));
        add(s,"cold_blood","razboinik",Kind.PASSIVE,8,"steady_aim",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.05,.08,.12));
        add(s,"hunters_fervor","razboinik",Kind.TRIGGER,6,"keen_eye",1,"",ints(240,220,200),ints(40,50,60),nums(0,0,0),nums(0,0,0));
        add(s,"opportunity","razboinik",Kind.TRIGGER,9,"smoke",1,"",ints(300,280,260),ints(60,70,80),nums(0,0,0),nums(.10,.15,.20));
        evolution(s,"onslaught","druzhinnik","lunge","vityaz",10);
        evolution(s,"bulwark","druzhinnik","shield_ram","griden",10);
        evolution(s,"greater_ward","vedun","ward","oberezhnik",25);
        evolution(s,"greater_hex","vedun","hex","koldun",10);
        evolution(s,"true_shot","razboinik","keen_eye","strelok",25);
        evolution(s,"raider_dash","razboinik","dash","naletchik",25);
        SKILLS=Collections.unmodifiableMap(s);
        var b=new LinkedHashMap<String,Branch>();
        branch(b,"vityaz","druzhinnik","druzhinnik",10,"lunge",1.1);branch(b,"griden","druzhinnik","druzhinnik",10,"shield_ram",1.1);
        branch(b,"bogatyr","vityaz","druzhinnik",25,"aerial_strike",1.15);branch(b,"ratoborets","vityaz","druzhinnik",25,"flurry",1.15);
        branch(b,"schitonosets","griden","druzhinnik",25,"shield_ram",1.2);branch(b,"voevoda","griden","druzhinnik",25,"second_wind",1.2);
        branch(b,"volhv","vedun","vedun",10,"ward",1.1);branch(b,"koldun","vedun","vedun",10,"hex",1.1);
        branch(b,"oberezhnik","volhv","vedun",25,"ward",1.2);branch(b,"znakhar","volhv","vedun",25,"herbalism",1.2);
        branch(b,"morovik","koldun","vedun",25,"hex",1.2);branch(b,"chernoknizhnik","koldun","vedun",25,"slumber",1.2);
        branch(b,"ushkuinik","razboinik","razboinik",10,"keen_eye",1.1);branch(b,"glavar","razboinik","razboinik",10,"dirty_strike",1.1);
        branch(b,"naletchik","ushkuinik","razboinik",25,"dash",1.15);branch(b,"strelok","ushkuinik","razboinik",25,"keen_eye",1.2);
        branch(b,"ataman","glavar","razboinik",25,"smoke",1.2);branch(b,"reznik","glavar","razboinik",25,"cold_blood",1.2);
        BRANCHES=Collections.unmodifiableMap(b);
    }
    private static void add(Map<String,Skill>s,String id,String base,Kind kind,int level,String pre,int preRank,String branch,int[]cd,int[]duration,double[]range,double[]power){s.put(id,new Skill(id,base,kind,level,pre,preRank,branch,cd,duration,range,power));}
    private static void evolution(Map<String,Skill>s,String id,String base,String pre,String branch,int level){add(s,id,base,Kind.EVOLUTION,level,pre,3,branch,ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(0,0,0));}
    private static void branch(Map<String,Branch>b,String id,String parent,String base,int level,String skill,double factor){b.put(id,new Branch(id,parent,base,level,skill,factor));}
    private static int[] ints(int a,int b,int c){return new int[]{a,b,c};}
    private static double[] nums(double a,double b,double c){return new double[]{a,b,c};}
    public static long xpForLevel(int level){return 100L+50L*(Math.max(1,level)-1);}
    private ClassDefinitions(){}
}
