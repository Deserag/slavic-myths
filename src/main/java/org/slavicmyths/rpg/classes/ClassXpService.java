package org.slavicmyths.rpg.classes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.slavicmyths.rpg.Abilities;

/** Bounded persisted award ledger. Vanilla experience is never read or changed. */
public final class ClassXpService {
    public enum AwardSource { COMBAT, STORY, EXPLORATION, ROLE }
    public static boolean eligible(ServerPlayer p) {
        return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&!ClassState.data(p).getString("Base").isEmpty();
    }
    public static boolean hostile(ServerPlayer p, LivingEntity target) {
        return org.slavicmyths.combat.CombatTargeting.isHostileTo(p,target) && target!=p && !target.isAlliedTo(p) && !p.isAlliedTo(target) && !target.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)
            && !(target instanceof net.minecraft.world.entity.TamableAnimal animal&&p.getUUID().equals(animal.getOwnerUUID()))
            && !(target instanceof org.slavicmyths.entity.LandSpiritEntity spirit&&spirit.permits(p))
            && !target.getPersistentData().getBoolean("ClassXpArtificial")
            && !(target instanceof org.slavicmyths.entity.LandSpiritEntity spirit&&spirit.morClone());
    }
    public static CompoundTag ledger(ServerPlayer p) {
        var d=ClassState.data(p);if(!d.contains("XpLedger",10))d.put("XpLedger",new CompoundTag());return d.getCompound("XpLedger");
    }
    /** Permanent rewards fail closed when full; old keys are never evicted and rewarded again. */
    public static boolean once(ServerPlayer p,AwardSource source,String key,int amount) {
        if(!eligible(p)||key.length()>192)return false;
        var ledger=ledger(p);String id=source.name()+":"+key;
        if(ledger.contains(id)||ledger.size()>=2048)return false;
        ledger.putBoolean(id,true);grant(p,amount);return true;
    }
    public static boolean role(ServerPlayer p,String action,LivingEntity target) {
        if(!eligible(p)||target!=null&&(!hostile(p,target)||!org.slavicmyths.combat.CombatTargeting.canAttack(p,target)||p.distanceToSqr(target)>1024))return false;
        var d=ClassState.data(p);long now=ClassState.now(p);
        if(now<d.getLong("RoleXpReady"))return false;
        if(target!=null){String key="ClassRoleXp:"+p.getUUID()+":"+action;var t=target.getPersistentData();if(t.getBoolean(key))return false;t.putBoolean(key,true);}
        d.putLong("RoleXpReady",now+200);return limited(p,AwardSource.ROLE,4,48);
    }
    public static boolean support(ServerPlayer p,String action){var d=p.getPersistentData();if(!d.hasUUID("ClassHealCreditEnemy")||d.getLong("ClassHealCreditUntil")<ClassState.now(p)||!d.getString("ClassHealCreditDimension").equals(p.level().dimension().location().toString()))return false;var e=p.serverLevel().getEntity(d.getUUID("ClassHealCreditEnemy"));return e instanceof LivingEntity enemy&&role(p,action,enemy);}
    public static boolean limited(ServerPlayer p,AwardSource source,int amount,int cap) {
        if(!eligible(p))return false;
        var d=ClassState.data(p);String key="XpWindow"+source;long window=ClassState.now(p)/1200;
        if(!d.contains(key)||d.getLong(key)!=window){d.putLong(key,window);d.putInt(key+"Used",0);}
        int used=d.getInt(key+"Used"),award=Math.min(amount,Math.max(0,cap-used));if(award<=0)return false;
        d.putInt(key+"Used",used+award);grant(p,award);return true;
    }
    private static void grant(ServerPlayer p,int amount) {
        if(amount<=0)return;ClassState.awardXp(p,amount);
        var d=ClassState.data(p);int pending=d.getInt("XpNoticePending")+amount;d.putInt("XpNoticePending",pending);
        if(ClassState.now(p)>=d.getLong("XpNoticeReady")){p.displayClientMessage(Component.translatable("classes.slavicmyths.xp_gain",pending),true);d.putInt("XpNoticePending",0);d.putLong("XpNoticeReady",ClassState.now(p)+60);}
    }
    private ClassXpService(){}
}
