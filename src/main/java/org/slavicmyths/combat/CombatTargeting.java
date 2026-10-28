package org.slavicmyths.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.slavicmyths.entity.LandSpiritEntity;

/** Local combat/control policy. No faction manager or global entity scans. */
public final class CombatTargeting {
    private static TagKey<EntityType<?>> tag(String id){return TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
    public static final TagKey<EntityType<?>> HOSTILE=tag("village_hostiles"),PROTECTED=tag("combat_protected"),CONTROL_IMMUNE=tag("control_immune"),BOSS=tag("trait_boss"),PROXY=tag("combat_proxy"),STORY=tag("trait_story_protected");
    private static final String[] SUMMON_OWNER_KEYS={"IdolOwner","KartausBoss174","greyWolfActor"};
    private static boolean physical(LivingEntity owner,LivingEntity target,Context context){
        if(owner==null||target==null||owner==target||owner.level()!=target.level()
                ||owner.isSpectator()||!owner.isAlive()||!target.isAlive()||target.isRemoved()
                ||target.isSpectator()||CreatureSafety.protectedPlayer(target)||target.isInvulnerable()
                ||target instanceof net.minecraft.world.entity.decoration.ArmorStand)return false;
        // Keep protected-target semantics for selection/skills, while native hit delegates retain their own hurt contract.
        boolean nativePopulationHunt=context==Context.NATIVE_HUNT
                &&owner instanceof net.minecraft.world.entity.monster.Monster
                &&target instanceof net.minecraft.world.entity.npc.AbstractVillager;
        if(target.getType().is(PROTECTED)&&context!=Context.NATIVE_DAMAGE&&!nativePopulationHunt)return false;
        if(target instanceof Player other){
            var responsible=playerOwner(owner);
            if(responsible!=null&&owner.level() instanceof net.minecraft.server.level.ServerLevel w){
                var player=w.getServer().getPlayerList().getPlayer(responsible);
                if(!w.getServer().isPvpAllowed()||player==null||!player.isAlive()||player.isSpectator()||!player.canHarmPlayer(other))return false;
            }
        }
        if(owner instanceof Player player){
            if(target instanceof Player other&&(!player.canHarmPlayer(other)
                    ||player.getServer()!=null&&!player.getServer().isPvpAllowed()))return false;
            if(target instanceof LandSpiritEntity spirit&&spirit.permits(player))return false;
        }
        return true;
    }
    public enum Context { DIRECT, ACQUIRE, RETALIATION, SETTLEMENT, PROJECTILE, AREA, CONTROL, NATIVE_HUNT, NATIVE_DAMAGE }
    public enum Relation { FORBIDDEN, ALLY, NEUTRAL, ENEMY }
    public enum Intent { PEACEFUL, NEUTRAL, HOSTILE, DEFENDER }
    public static java.util.UUID affiliation(LivingEntity e){
        if(e.getPersistentData().hasUUID("summonedByPolozUUID"))return e.getPersistentData().getUUID("summonedByPolozUUID");
        if(e.getPersistentData().hasUUID("GreyPackOwner"))return e.getPersistentData().getUUID("GreyPackOwner");
        for(String key:SUMMON_OWNER_KEYS)if(e.getPersistentData().hasUUID(key))return e.getPersistentData().getUUID(key);
        if(e instanceof org.slavicmyths.finist.BannerEntity prop)return prop.boss;
        if(e instanceof org.slavicmyths.marinka.HexAnchor prop)return prop.boss;
        if(e instanceof org.slavicmyths.hunt.SerpentProjection prop)return prop.parent;
        if(e instanceof org.slavicmyths.kalin.KalinSoldier soldier)return soldier.commander;
        if(e instanceof TamableAnimal a)return a.getOwnerUUID();
        if(e instanceof org.slavicmyths.karachun.NightRaven r)return r.owner;
        if(e instanceof org.slavicmyths.morozko.PlayerDouble d)return d.owner();
        if(e instanceof org.slavicmyths.morozko.IceDouble d)return d.owner;
        if(e instanceof org.slavicmyths.guardians.WaterEcho d)return d.keeper;
        if(e instanceof org.slavicmyths.guardians.WaterPrison d)return d.keeper;
        if(e instanceof org.slavicmyths.marinka.TurMinion d)return d.boss;
        if(e instanceof org.slavicmyths.earth.EarthServant d)return d.combatOwner();
        return org.slavicmyths.kalin.KalinParty.owner(e);
    }
    private static java.util.UUID playerOwner(LivingEntity e){
        if(e instanceof Player p)return p.getUUID();
        if(e instanceof TamableAnimal pet)return pet.getOwnerUUID();
        if(e instanceof org.slavicmyths.morozko.PlayerDouble d)return d.owner();
        if(e instanceof org.slavicmyths.karachun.NightRaven r&&r.allied())return r.owner;
        if(e.getPersistentData().hasUUID("GreyPackOwner"))return e.getPersistentData().getUUID("GreyPackOwner");
        return org.slavicmyths.kalin.KalinParty.owner(e);
    }
    private static net.minecraft.world.scores.Team effectiveTeam(LivingEntity e){
        var own=e.getTeam();if(own!=null)return own;var owner=playerOwner(e);
        if(owner==null||!(e.level() instanceof net.minecraft.server.level.ServerLevel w))return null;
        var player=w.getServer().getPlayerList().getPlayer(owner);if(player!=null)return player.getTeam();
        // UUID cache lookup is local; never request profiles over the network or invent another faction ledger.
        var cache=w.getServer().getProfileCache();if(cache==null)return null;
        return cache.get(owner).map(p->w.getScoreboard().getPlayersTeam(p.getName())).orElse(null);
    }
    private static boolean allied(LivingEntity a,LivingEntity b){
        var x=affiliation(a);var y=affiliation(b);var at=effectiveTeam(a);var bt=effectiveTeam(b);
        return a.isAlliedTo(b)||b.isAlliedTo(a)||at!=null&&at.isAlliedTo(bt)
                ||x!=null&&(x.equals(b.getUUID())||x.equals(y))||y!=null&&y.equals(a.getUUID());
    }
    /** Explicit dynamic gates precede static hostile tags. No target selectors are added here. */
    public static boolean sceneProtected(LivingEntity e){
        if(e instanceof LandSpiritEntity s&&(s.morState()==1||s.morConcealed()))return true;
        if(e instanceof org.slavicmyths.guardians.GuardianEntity g)return true;
        if(e instanceof org.slavicmyths.gorynych.Gorynych g)return g.state()>=org.slavicmyths.gorynych.GorynychRules.EXHAUSTED;
        if(e instanceof org.slavicmyths.wolf.GreyWolf wolf)return wolf.narrativeActor(); // No automatic intervention against higher Mor-Likho.
        if(e instanceof org.slavicmyths.firebird.FirebirdEntity)return true;
        if(e instanceof org.slavicmyths.volkolak.CursedVolkolak b)return b.encounter!=null;
        if(e instanceof org.slavicmyths.poloz.GreatPoloz)return true;
        if(e instanceof org.slavicmyths.skim.SkimBeastEntity b)return b.arena!=null;
        if(e instanceof org.slavicmyths.marinka.MarinkaEntity b)return b.arena!=null;
        if(e instanceof org.slavicmyths.finist.KartausEntity b)return b.encounter!=null;
        if(e instanceof org.slavicmyths.kalin.KalinEntity b)return b.encounter!=null;
        if(e instanceof org.slavicmyths.bogatyr.IdolishcheEntity b)return b.active&&!b.debug;
        if(e instanceof org.slavicmyths.morozko.Morozko)return true;
        if(e instanceof org.slavicmyths.karachun.Karachun)return true;
        return e.getType().is(STORY)||e.getPersistentData().getBoolean("HeroRecalling")||e.getPersistentData().getBoolean("HeroSpent")||org.slavicmyths.kalin.KalinParty.recovering(e);
    }
    public static boolean bossMode(LivingEntity e){
        if(org.slavicmyths.mor.HigherMor.higher(e))return true;
        if(e instanceof org.slavicmyths.morozko.IceDouble)return false;
        if(e instanceof org.slavicmyths.morozko.Morozko b)return b.fighting();
        if(e instanceof org.slavicmyths.karachun.Karachun b)return b.fighting();
        if(e instanceof org.slavicmyths.guardians.GuardianEntity b)return b.fighting();
        if(e instanceof org.slavicmyths.bogatyr.IdolishcheEntity b)return b.active;
        if(e instanceof org.slavicmyths.skim.SkimBeastEntity b)return b.active();
        if(e instanceof org.slavicmyths.marinka.MarinkaEntity b)return b.active;
        if(e instanceof org.slavicmyths.finist.KartausEntity b)return b.validEncounter()&&b.getTarget()!=null;
        if(e instanceof org.slavicmyths.kalin.KalinEntity b)return b.valid()&&b.getTarget()!=null;
        if(e instanceof org.slavicmyths.gorynych.Gorynych b)return b.state()<org.slavicmyths.gorynych.GorynychRules.EXHAUSTED&&b.getTarget()!=null;
        return e instanceof Mob m&&m.getTarget()!=null;
    }
    public static Intent intent(LivingEntity e){
        if(e.getType().is(PROXY))return Intent.NEUTRAL;
        // Explicit biome roles precede Enemy/tag fallbacks. Traits must not provoke neutrals.
        if(e instanceof org.slavicmyths.navya.NavyaMob mob)return mob.kind==org.slavicmyths.navya.NavyaContent.Kind.WISP?Intent.PEACEFUL:Intent.HOSTILE;
        if(e instanceof org.slavicmyths.blackcinder.CinderMob mob)return switch(mob.kind){case SPARKLING->Intent.PEACEFUL;case ASH_SPIRIT->mob.getTarget()==null?Intent.NEUTRAL:Intent.HOSTILE;default->Intent.HOSTILE;};
        if(e instanceof org.slavicmyths.burningislands.IslandsMob mob)return mob.getTarget()==null?Intent.NEUTRAL:Intent.HOSTILE;
        if(e instanceof org.slavicmyths.losthalls.HallsMob mob)return switch(mob.kind){case GRIBOSHMYG->Intent.PEACEFUL;case GULOKRYL->mob.isTame()?Intent.DEFENDER:Intent.NEUTRAL;case EKHOVIK->mob.getTarget()==null?Intent.NEUTRAL:Intent.HOSTILE;default->Intent.HOSTILE;};
        if(e instanceof org.slavicmyths.industry.ChudPerson p)return p.role()==3?Intent.DEFENDER:Intent.PEACEFUL;
        if(e instanceof net.minecraft.world.entity.npc.Villager v)return org.slavicmyths.military.Military.guard(v)?Intent.DEFENDER:Intent.PEACEFUL;
        if(e instanceof net.minecraft.world.entity.npc.WanderingTrader)return Intent.PEACEFUL;
        if(e instanceof org.slavicmyths.underground.EncounterMob m&&m.combatNeutral()&&m.getTarget()==null)return Intent.NEUTRAL;
        if(e instanceof net.minecraft.world.entity.animal.IronGolem)return Intent.DEFENDER;
        if(e instanceof NeutralMob n)return n.getRemainingPersistentAngerTime()>0?Intent.HOSTILE:Intent.NEUTRAL;
        if(e instanceof LandSpiritEntity s){if(s.morState()==1)return Intent.NEUTRAL;if(s.morState()==2)return Intent.HOSTILE;}
        if(bossMode(e)&&e.getType().is(BOSS))return Intent.HOSTILE;
        if(sceneProtected(e))return Intent.NEUTRAL;
        if(org.slavicmyths.kalin.KalinParty.owner(e)!=null)return Intent.DEFENDER;
        if(e instanceof org.slavicmyths.karachun.NightRaven r&&r.allied())return Intent.DEFENDER;
        if(e instanceof LandSpiritEntity && CreatureProperties.nature(e)==CreatureProperties.Nature.SPIRIT && !(e instanceof org.slavicmyths.hunt.WorldBoss b&&b.kind()==org.slavicmyths.hunt.BossKind.TUGARIN))return e instanceof Mob m&&m.getTarget()!=null?Intent.HOSTILE:Intent.NEUTRAL;
        if(e.getType().is(HOSTILE)||e instanceof Enemy)return Intent.HOSTILE;
        return Intent.NEUTRAL;
    }
    private static boolean threatening(LivingEntity observer,LivingEntity target){
        if(observer instanceof org.slavicmyths.industry.ChudPerson chud&&chud.recordedAggressor(target))return true;
        if(target instanceof Mob m&&m.getTarget()!=null){var victim=m.getTarget();if(victim==observer||allied(observer,victim))return true;
            if(observer instanceof org.slavicmyths.industry.ChudPerson a&&victim instanceof org.slavicmyths.industry.ChudPerson b)return a.sameSettlement(b);
            if(observer instanceof net.minecraft.world.entity.npc.Villager||observer instanceof net.minecraft.world.entity.animal.IronGolem)return victim instanceof net.minecraft.world.entity.npc.AbstractVillager;
        }
        return observer.getLastHurtByMob()==target&&observer.tickCount-observer.getLastHurtByMobTimestamp()<=1200;
    }
    public static Relation relation(LivingEntity actor,LivingEntity target,Context context){
        if(actor==null||target==null)return Relation.FORBIDDEN;
        boolean nativePlayerDuel=context==Context.NATIVE_DAMAGE&&actor instanceof Player a&&target instanceof Player b
                &&a!=b&&a.getTeam()!=null&&a.getTeam()==b.getTeam()&&a.getTeam().isAllowFriendlyFire();
        if(allied(actor,target)&&!nativePlayerDuel)return Relation.ALLY;
        if(!physical(actor,target,context))return Relation.FORBIDDEN;
        if(context==Context.NATIVE_DAMAGE&&(org.slavicmyths.kalin.KalinParty.recovering(target)||target.getPersistentData().getBoolean("HeroSpent")||target.getPersistentData().getBoolean("HeroRecalling")))return Relation.FORBIDDEN;
        // STORY_PROTECTED controls acquisition/intervention. Player damage retains the entity's own hurt contract.
        if(context!=Context.NATIVE_DAMAGE&&!(actor instanceof Player)&&sceneProtected(target))return Relation.FORBIDDEN;
        var targetIntent=intent(target);
        // An angry neutral spirit is not a settlement-wide enemy merely for fighting somebody else.
        if((context==Context.SETTLEMENT||context==Context.ACQUIRE)&&target instanceof NeutralMob)targetIntent=Intent.NEUTRAL;
        if(context==Context.SETTLEMENT&&target instanceof LandSpiritEntity s&&s.morState()==0&&CreatureProperties.nature(target)==CreatureProperties.Nature.SPIRIT)targetIntent=Intent.NEUTRAL;
        return decide(context,intent(actor),targetIntent,actor instanceof Player,target instanceof Player,threatening(actor,target));
    }
    /** Legal-entity adapters above feed this single directed intent rule; useful without a game world. */
    public static Relation decide(Context context,Intent actor,Intent target,boolean actorPlayer,boolean targetPlayer,boolean threat){
        if(context==Context.NATIVE_DAMAGE||context==Context.NATIVE_HUNT||context==Context.DIRECT||context==Context.PROJECTILE||context==Context.AREA||context==Context.CONTROL)return Relation.NEUTRAL;
        if(threat)return Relation.ENEMY;
        if(targetPlayer)return actor==Intent.HOSTILE&&context==Context.ACQUIRE?Relation.ENEMY:Relation.NEUTRAL;
        return (context==Context.SETTLEMENT||actorPlayer||actor==Intent.DEFENDER)&&target==Intent.HOSTILE?Relation.ENEMY:Relation.NEUTRAL;
    }
    public static boolean canAttack(LivingEntity owner,LivingEntity target){return canAttack(owner,target,Context.DIRECT);}
    public static boolean canAttack(LivingEntity owner,LivingEntity target,Context context){var r=relation(owner,target,context);return r==Relation.ENEMY||r==Relation.NEUTRAL&&(context==Context.NATIVE_DAMAGE||context==Context.NATIVE_HUNT||context==Context.DIRECT||context==Context.PROJECTILE||context==Context.AREA||context==Context.CONTROL);}
    public static boolean canControl(LivingEntity owner,LivingEntity target){return canAttack(owner,target,Context.CONTROL)&&!target.getType().is(CONTROL_IMMUNE);}
    public static boolean isEnemy(LivingEntity actor,LivingEntity target,Context context){return relation(actor,target,context)==Relation.ENEMY;}
    public static boolean isThreatToSettlement(LivingEntity defender,LivingEntity target){return isEnemy(defender,target,Context.SETTLEMENT);}
    public static boolean isHostileCombatTarget(LivingEntity owner,LivingEntity target){return isHostileTo(owner,target);}
    public static boolean isHostileTo(LivingEntity owner,LivingEntity target){return isEnemy(owner,target,Context.ACQUIRE);}
    public static boolean controlResistant(LivingEntity target){return org.slavicmyths.rpg.classes.ClassControl.resistant(target)||target.getType().is(org.slavicmyths.rpg.RareRuneRuntime.IMMUNE);}
    private CombatTargeting(){}
}
