from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
def edit(name,old,new):
 p=ROOT/name
 text=p.read_text(encoding='utf-8')
 assert old in text,(name,old[:80])
 p.write_text(text.replace(old,new),encoding='utf-8')
C='src/main/java/org/slavicmyths/rpg/classes/'
edit(C+'ClassDefinitions.java','        SKILLS=Collections.unmodifiableMap(s);','''        add(s,"battle_discipline","druzhinnik",Kind.PASSIVE,10,"steadfast",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.03,.05,.07));
        add(s,"iron_will","druzhinnik",Kind.PASSIVE,15,"temper",1,"",ints(400,400,400),ints(40,40,40),nums(0,0,0),nums(.06,.09,.12));
        add(s,"spiritual_fortitude","vedun",Kind.PASSIVE,10,"herbalism",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.10,.15,.20));
        add(s,"sign_mastery","vedun",Kind.PASSIVE,15,"ward_power",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.08,.12,.16));
        add(s,"ambush_master","razboinik",Kind.PASSIVE,10,"steady_aim",1,"",ints(0,0,0),ints(0,0,0),nums(0,0,0),nums(.08,.12,.16));
        add(s,"survival_instinct","razboinik",Kind.PASSIVE,15,"light_step",1,"",ints(1200,1200,1200),ints(60,60,60),nums(0,0,0),nums(.10,.15,.20));
        SKILLS=Collections.unmodifiableMap(s);''')
edit(C+'ClassState.java','return old.getCompound(KEY);','var d=old.getCompound(KEY);if(d.getInt("Version")<3)d.putInt("Version",3);return d;')
edit(C+'ClassState.java','public static int passive(Player p,String id){return data(p).getString("Passive").equals(id)?rank(p,id):0;}','''public static boolean secondPassive(CompoundTag d){var b=ClassDefinitions.BRANCHES.get(d.getString("Second"));return d.getInt("Level")>=25&&b!=null&&b.level()==25&&b.base().equals(d.getString("Base"))&&b.parent().equals(d.getString("First"));}
    public static int passiveRank(CompoundTag d,String id){var s=ClassDefinitions.SKILLS.get(id);return s!=null&&s.kind()==ClassDefinitions.Kind.PASSIVE&&s.base().equals(d.getString("Base"))&&(d.getString("Passive").equals(id)||secondPassive(d)&&d.getString("Passive2").equals(id))?Math.clamp(d.getCompound("Ranks").getInt(id),0,s.maxRank()):0;}
    public static int passive(Player p,String id){return passiveRank(data(p),id);}''')
start=(ROOT/(C+'ClassState.java')).read_text(encoding='utf-8')
a=start.index('    public static boolean equip(');b=start.index('    public static boolean advance(',a)
edit(C+'ClassState.java',start[a:b],'''    public static boolean equip(ServerPlayer p,String id,int slot){
        var d=data(p);if(slot<0||slot>4||slot==4&&!secondPassive(d))return false;
        String key=slot==4?"Passive2":slot==3?"Passive":"Active"+slot;
        if(id.isEmpty()){d.remove(key);restore(p);sync(p);return true;}
        var def=ClassDefinitions.SKILLS.get(id);
        if(def==null||rank(p,id)==0||(slot>=3?def.kind()!=ClassDefinitions.Kind.PASSIVE:def.kind()!=ClassDefinitions.Kind.ACTIVE))return false;
        if(slot>=3&&d.getString(slot==3?"Passive2":"Passive").equals(id))return false;
        if(slot<3)for(int i=0;i<3;i++)if(d.getString("Active"+i).equals(id))d.remove("Active"+i);
        d.putString(key,id);restore(p);sync(p);return true;
    }
''')
edit(C+'ClassBalance.java','d.getString("Passive").equals("ward_power")','ClassState.passiveRank(d,"ward_power")>0')
edit(C+'ClassBalance.java','return (int)Math.round(ClassDefinitions.SKILLS.get(id).duration(rank)*factor);','if(id.equals("binding_sign")||id.equals("hex")){int passive=ClassState.passiveRank(d,"sign_mastery");if(passive>0)factor*=1+ClassDefinitions.SKILLS.get("sign_mastery").power(passive);}return (int)Math.round(ClassDefinitions.SKILLS.get(id).duration(rank)*factor);')
edit(C+'ClassBalance.java','public static int cooldown(CompoundTag d,String id,int rank){return','public static int cooldown(CompoundTag d,String id,int rank){double discipline=1;int passive=ClassState.passiveRank(d,"battle_discipline");if(passive>0&&(id.equals("trip")||id.equals("shield_ram")))discipline-=ClassDefinitions.SKILLS.get("battle_discipline").power(passive);return')
edit(C+'ClassBalance.java','cooldown(rank)*(id.equals("dash")','cooldown(rank)*discipline*(id.equals("dash")')
edit(C+'ClassEvents.java','||ClassState.rank(p,"riposte")==0||ClassState.remaining(p,"riposte")>0','')
edit(C+'ClassEvents.java','confirmedBlock(p);','{confirmedBlock(p);ClassPassiveEvents.block(p);}')
edit(C+'ClassEvents.java','public static void confirmedBlock(ServerPlayer p){int rank','public static void confirmedBlock(ServerPlayer p){if(ClassState.data(p).getString("Base").equals("druzhinnik"))ClassXpService.role(p,"block",null);int rank')
edit(C+'ClassRuntime.java','if(removed==0)return false;','if(removed==0)return false;ClassXpService.role(p,"cleanse",null);')
edit('src/main/java/org/slavicmyths/tales/TaleEvents.java','if(start!=null&&start.isValid()&&start.getBoundingBox().isInside(pos))TaleService.subject(p,"structure/"+id,2,false);','if(start!=null&&start.isValid()&&start.getBoundingBox().isInside(pos)){TaleService.subject(p,"structure/"+id,2,false);org.slavicmyths.rpg.classes.ClassXpService.once(p,org.slavicmyths.rpg.classes.ClassXpService.AwardSource.EXPLORATION,"structure/"+id,180);}')
edit(C+'ClassXpService.java','target instanceof Enemy && target!=p && !target.isAlliedTo(p)','org.slavicmyths.combat.CombatTargeting.isHostileTo(p,target) && target!=p && !target.isAlliedTo(p) && !target.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)')
