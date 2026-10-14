package org.slavicmyths.rpg;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.combat.CombatTargeting;

/** Hooks in the existing rune dispatcher. No new event bus or world/player traversal. */
public final class CraftRunes {
    private static final Map<ServerPlayer,State> STATES=new WeakHashMap<>();
    private static final class State {
        final CraftRuneRules.Series series=new CraftRuneRules.Series();
        final CraftRuneRules.Healing healing=new CraftRuneRules.Healing();
        ItemStack weapon=ItemStack.EMPTY;List<String> runes=List.of();
        UUID target;long attackAt=-1,lastDamage,lastNightCast=-1;
        boolean strong;DamageSource defense;
        State(long now){lastDamage=now;}
    }
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static State state(ServerPlayer p){return STATES.computeIfAbsent(p,x->new State(now(p)));}
    private static CompoundTag data(ServerPlayer p){var d=PathData.data(p);if(!d.contains("CraftRunesIV",10))d.put("CraftRunesIV",new CompoundTag());return d.getCompound("CraftRunesIV");}
    private static boolean equipped(ServerPlayer p,EquipmentSlot slot,String id){var s=p.getItemBySlot(slot);return AdvancedRunes.supports(s,id)&&Runes.has(s,id);}
    private static boolean forest(ServerPlayer p){return equipped(p,EquipmentSlot.CHEST,"forest_spirit_iv")||equipped(p,EquipmentSlot.HEAD,"forest_spirit_iv");}
    private static boolean weapon(ServerPlayer p){return AdvancedRunes.supports(p.getMainHandItem(),"perun_rage_iv")&&Runes.has(p.getMainHandItem(),"perun_rage_iv");}
    public static boolean hostile(ServerPlayer p,LivingEntity target){return CombatTargeting.canAttack(p,target)&&(CombatTargeting.isHostileTo(p,target)||target instanceof net.minecraft.world.entity.player.Player&&p.server.isPvpAllowed())&&Abilities.canAffect(p,target)&&!p.getUUID().equals(org.slavicmyths.kalin.KalinParty.owner(target));}
    public static void attack(ServerPlayer p,Entity target){
        if(!weapon(p)||!(target instanceof LivingEntity living)||!hostile(p,living)||!p.hasLineOfSight(living))return;
        validate(p);var s=state(p);s.weapon=p.getMainHandItem();s.runes=List.copyOf(Runes.list(s.weapon));
        s.target=target.getUUID();s.attackAt=now(p);s.strong=p.getAttackStrengthScale(.5F)>=.9F;
    }
    /** The scythe owns a delayed primary swing; capture one contact, never its secondary area hits. */
    public static void prepare(ServerPlayer p,LivingEntity target,DamageSource source){
        if(!weapon(p)||!p.getMainHandItem().is(org.slavicmyths.karachun.NightContent.SCYTHE.get())||!source.is(DamageTypes.PLAYER_ATTACK)||source.getDirectEntity()!=p||!hostile(p,target)||!p.hasLineOfSight(target))return;
        var cast=p.getPersistentData().getCompound(org.slavicmyths.karachun.NightCombat.KEY);
        if(cast.isEmpty()||cast.getInt("Mode")!=0||!cast.getBoolean("Hit")||now(p)-cast.getLong("Start")!=cast.getInt("Windup"))return;
        var s=state(p);if(s.lastNightCast==cast.getLong("Start"))return;
        validate(p);s.lastNightCast=cast.getLong("Start");s.weapon=p.getMainHandItem();s.runes=List.copyOf(Runes.list(s.weapon));
        s.target=target.getUUID();s.attackAt=now(p);s.strong=true; // Mode 0 is admitted by the native server only at charge >= .9.
    }
    public static boolean rage(ServerPlayer p){var s=STATES.get(p);return s!=null&&weapon(p)&&s.weapon==p.getMainHandItem()&&s.runes.equals(Runes.list(s.weapon))&&s.series.until>now(p);}
    private static boolean direct(ServerPlayer p,LivingEntity target,DamageSource source){var s=STATES.get(p);return s!=null&&s.attackAt==now(p)&&target.getUUID().equals(s.target)&&source.is(DamageTypes.PLAYER_ATTACK)&&source.getDirectEntity()==p&&!org.slavicmyths.poloz.PolozRewardEvents.secondary(p);}
    public static boolean directRage(ServerPlayer p,LivingEntity target,DamageSource source){return rage(p)&&direct(p,target,source)&&hostile(p,target);}
    public static void validate(ServerPlayer p){var s=STATES.get(p);if(s==null)return;
        if(!weapon(p)){s.series.clear();s.target=null;s.attackAt=-1;}
        else if(s.weapon!=p.getMainHandItem()||!s.runes.equals(Runes.list(s.weapon))){
            // Another eligible weapon preserves the player-wide window, but cannot reuse an attack intent.
            s.weapon=p.getMainHandItem();s.runes=List.copyOf(Runes.list(s.weapon));s.target=null;s.attackAt=-1;
        }
        if(!forest(p))s.healing.stop();
    }
    public static void reset(ServerPlayer p){STATES.remove(p);RuneRuntime.refresh(p);}
    public static void defend(LivingDamageEvent.Pre e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var s=STATES.get(p);if(s!=null)s.defense=null;
        var source=e.getSource();
        if(!(source.getEntity() instanceof LivingEntity attacker)||!hostile(p,attacker)||source.is(DamageTypeTags.BYPASSES_ARMOR)||source.is(DamageTypeTags.BYPASSES_EFFECTS)||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        boolean direct=source.getDirectEntity()==attacker&&(source.is(DamageTypes.PLAYER_ATTACK)||source.is(DamageTypes.MOB_ATTACK)||source.is(DamageTypes.MOB_ATTACK_NO_AGGRO));
        boolean arrow=source.is(DamageTypeTags.IS_PROJECTILE)&&source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
        float absorption=Math.min(e.getNewDamage(),Math.max(0,p.getAbsorptionAmount()));
        float healthDamage=(float)CraftRuneRules.healthDamage(e.getNewDamage(),absorption);
        if(!CraftRuneRules.defense(healthDamage,direct||arrow)||now(p)<data(p).getLong("Defense"))return;
        var shield=p.getUseItem();boolean activeShield=p.isBlocking()&&AdvancedRunes.supports(shield,"unbreakable_iv")&&Runes.has(shield,"unbreakable_iv");
        if(!equipped(p,EquipmentSlot.CHEST,"unbreakable_iv")&&!activeShield)return;
        state(p).defense=source;e.setNewDamage((float)CraftRuneRules.defendedDamage(e.getNewDamage(),absorption));
    }
    public static boolean defending(ServerPlayer p,DamageSource source){var s=STATES.get(p);return s!=null&&s.defense==source;}
    public static void hit(LivingDamageEvent.Post e){
        if(e.getEntity() instanceof ServerPlayer victim){var s=STATES.get(victim);
            if(s!=null&&s.defense==e.getSource()){s.defense=null;if(e.getNewDamage()>0){data(victim).putLong("Defense",now(victim)+CraftRuneRules.DEFENSE_CD);victim.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,victim.getX(),victim.getY()+1,victim.getZ(),4,.15,.15,.15,.01);victim.serverLevel().playSound(null,victim.blockPosition(),net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,net.minecraft.sounds.SoundSource.PLAYERS,.25F,1.1F);}}
            if(e.getNewDamage()>0||e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)>0){s=state(victim);s.lastDamage=now(victim);s.healing.stop();}
        }
        if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!weapon(p)||!direct(p,e.getEntity(),e.getSource()))return;
        var s=state(p);boolean strong=s.strong;s.attackAt=-1;
        // Post can follow a lethal hit; hostility was checked at attack intent, and again in Pre for damage bonus.
        if(!CombatTargeting.isHostileTo(p,e.getEntity())&&!(e.getEntity() instanceof net.minecraft.world.entity.player.Player&&p.server.isPvpAllowed())||e.getEntity().isAlliedTo(p)||p.isAlliedTo(e.getEntity()))return;
        if(s.series.hit(now(p),strong,now(p)>=data(p).getLong("Rage"))){data(p).putLong("Rage",now(p)+CraftRuneRules.RAGE_CD);RuneRuntime.refresh(p);}
    }
    public static void tick(ServerPlayer p){
        var s=STATES.get(p);
        if(s!=null){s.defense=null;boolean had=s.series.until>0;if(had)validate(p);if(had&&!rage(p)){s.series.clear();RuneRuntime.refresh(p);}}
        if(p.tickCount%20!=0)return;
        validate(p);
        if(!p.isAlive()||p.isSpectator()||!forest(p)){if(s!=null)s.healing.stop();return;}
        s=state(p);long time=now(p);
        if(s.healing.remaining>0){if(s.healing.pulse(time))p.heal(1);return;}
        if(CraftRuneRules.forest(p.getHealth(),p.getMaxHealth(),time,s.lastDamage,data(p).getLong("Forest"))){s.healing.start(time);data(p).putLong("Forest",time+CraftRuneRules.FOREST_CD);}
    }
    private CraftRunes(){}
}
