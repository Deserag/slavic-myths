package org.slavicmyths.rpg.classes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;

/** Progress lives in the existing persisted Class2 compound. No scans or tick tasks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassTrialService {
    public static int required(String branch){return switch(branch){case "bogatyr","voevoda","chernoknizhnik"->3;case "ratoborets","ataman"->5;case "naletchik"->10;case "morovik","strelok","reznik"->15;case "schitonosets","znakhar"->35;case "oberezhnik"->30;default->0;};}
    private static CompoundTag state(ServerPlayer p){var d=ClassState.data(p);if(!d.contains("Trials",10))d.put("Trials",new CompoundTag());return d.getCompound("Trials");}
    public static boolean enabled(ServerPlayer p,String branch){return ClassXpService.eligible(p)&&ClassState.data(p).getInt("Level")>=40&&ClassState.data(p).getString("Second").equals(branch);}
    public static boolean completed(CompoundTag d,String branch){return d.getCompound("Trials").getBoolean(branch+"Done");}
    public static long encounter(ServerPlayer p){var d=state(p);long now=ClassState.now(p);if(now-d.getLong("LastCombat")>200||!d.contains("Encounter"))d.putLong("Encounter",d.getLong("Encounter")+1);d.putLong("LastCombat",now);return d.getLong("Encounter");}
    public static void progress(ServerPlayer p,String branch,double amount,LivingEntity target,boolean onceEncounter){
        if(!enabled(p,branch)||amount<=0||completed(ClassState.data(p),branch)||target!=null&&!ClassXpService.hostile(p,target))return;
        var d=state(p);long encounter=encounter(p);String key=branch+"Encounter";
        if(onceEncounter&&d.getLong(key)==encounter)return;
        if(target!=null){String mark="ClassTrial:"+p.getUUID()+":"+branch;var t=target.getPersistentData();if(t.getBoolean(mark))return;t.putBoolean(mark,true);}
        d.putLong(key,encounter);d.putDouble(branch,Math.min(required(branch),d.getDouble(branch)+amount));
        if(!d.contains(branch+"First"))d.putLong(branch+"First",encounter);
        if(d.getLong(branch+"LastCounted")!=encounter){d.putLong(branch+"LastCounted",encounter);d.putInt(branch+"Encounters",d.getInt(branch+"Encounters")+1);}
        int minimum=switch(branch){case "schitonosets","oberezhnik","morovik"->3;default->1;};
        if(d.getDouble(branch)>=required(branch)&&d.getInt(branch+"Encounters")>=minimum){d.putBoolean(branch+"Done",true);ClassXpService.once(p,ClassXpService.AwardSource.STORY,"trial/"+branch,400);}
        ClassState.sync(p);
    }
    public static void block(ServerPlayer p,LivingEntity attacker,float amount){if(attacker==null||!ClassXpService.hostile(p,attacker))return;progress(p,"schitonosets",amount,null,false);}
    public static void cast(ServerPlayer p,String skill,LivingEntity target){
        if(target==null||!ClassXpService.hostile(p,target))return;
        if(skill.equals("hex")){progress(p,"morovik",1,target,false);target.getPersistentData().putLong("ClassHex:"+p.getUUID(),ClassState.now(p));}
        if(skill.equals("slumber")&&target.hasEffect(org.slavicmyths.registry.ModEffects.CLASS_ROOT)&&target.getPersistentData().contains("ClassHex:"+p.getUUID())&&ClassState.now(p)-target.getPersistentData().getLong("ClassHex:"+p.getUUID())<=200)progress(p,"chernoknizhnik",1,target,true);
    }
    public static void escaped(ServerPlayer p,String branch,LivingEntity enemy){progress(p,branch,1,enemy,true);if(ClassState.data(p).getString("Base").equals("razboinik"))ClassXpService.role(p,"escape",enemy);}
    @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
        if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof LivingEntity enemy&&ClassXpService.hostile(p,enemy)){
            if(e.getNewDamage()>0){encounter(p);p.getPersistentData().putUUID("ClassHealCreditEnemy",enemy.getUUID());p.getPersistentData().putString("ClassHealCreditDimension",p.level().dimension().location().toString());p.getPersistentData().putFloat("ClassHealCredit",Math.min(80,p.getPersistentData().getFloat("ClassHealCredit")+e.getNewDamage()));p.getPersistentData().putLong("ClassHealCreditUntil",ClassState.now(p)+1200);}
            float absorbed=e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION);var temp=p.getPersistentData();if(absorbed>0&&temp.getLong("ClassWardUntil")>ClassState.now(p)){float counted=Math.min(absorbed,temp.getFloat("ClassWardPool"));temp.putFloat("ClassWardPool",temp.getFloat("ClassWardPool")-counted);progress(p,"oberezhnik",counted,null,false);ClassXpService.role(p,"ward",enemy);}
        }
        if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!ClassXpService.hostile(p,e.getEntity()))return;
        encounter(p);var t=e.getEntity();
        if(e.getSource().getDirectEntity() instanceof AbstractArrow&&p.distanceToSqr(t)>=256)progress(p,"strelok",1,t,false);
        if(!ClassUltimateRuntime.secondary(p)&&e.getSource().getDirectEntity()==p&&"player".equals(e.getSource().getMsgId())){
            var d=state(p);long now=ClassState.now(p);if(now-d.getLong("ComboStart")>160){d.putLong("ComboStart",now);d.putInt("ComboHits",0);}d.putInt("ComboHits",d.getInt("ComboHits")+1);if(d.getInt("ComboHits")>=3)progress(p,"ratoborets",1,t,true);
            if(t.getPersistentData().getLong("ClassDirty:"+p.getUUID())==now)progress(p,"reznik",1,t,true);
        }
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void healed(LivingHealEvent e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||e.getAmount()<=0||ClassState.data(p).getString("Base").isEmpty())return;
        var d=p.getPersistentData();float actual=Math.min(e.getAmount(),p.getMaxHealth()-p.getHealth());if(actual<=0||d.getLong("ClassHealCreditUntil")<ClassState.now(p))return;float credit=Math.min(actual,d.getFloat("ClassHealCredit"));d.putFloat("ClassHealCredit",d.getFloat("ClassHealCredit")-credit);if(credit>0){progress(p,"znakhar",credit,null,false);if(ClassState.data(p).getString("Base").equals("vedun"))ClassXpService.support(p,"heal");}
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void killed(LivingDeathEvent e){
        if(e.isCanceled()||!(e.getSource().getEntity() instanceof ServerPlayer p)||!(e.getEntity() instanceof Mob threat)||!enabled(p,"voevoda")||!ClassXpService.hostile(p,threat))return;
        var ally=threat.getTarget();if(ally!=null&&ally!=p&&ClassUltimateRuntime.friendly(p,ally)&&p.distanceToSqr(ally)<=144&&p.hasLineOfSight(ally))progress(p,"voevoda",1,threat,true);
    }
    private ClassTrialService(){}
}
