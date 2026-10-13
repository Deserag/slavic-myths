package org.slavicmyths.bogatyr;

import java.util.Comparator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;

/** Local acquisition at the hero controller's existing cooldown; story bosses use their bindings. */
public final class HeroThreats {
    public static boolean allowed(Mob hero, ServerPlayer owner, LivingEntity enemy) {
        if (owner == null || !owner.isAlive() || owner.isSpectator() || owner.level() != hero.level()
                || enemy == null || !enemy.isAlive() || enemy == hero || hero.isAlliedTo(enemy)
                || !(enemy instanceof Enemy) && !(enemy instanceof org.slavicmyths.karachun.NightRaven r&&!r.allied()) && !debugEnemy(enemy) && !blighted(enemy)
                || enemy instanceof org.slavicmyths.karachun.NightRaven r&&r.allied()
                || enemy instanceof org.slavicmyths.gorynych.Gorynych gorynych&&gorynych.state()>=org.slavicmyths.gorynych.GorynychRules.EXHAUSTED
                || enemy instanceof org.slavicmyths.guardians.GuardianEntity guardian&&guardian.state()>=org.slavicmyths.guardians.GuardianRules.PURIFY
                || enemy instanceof org.slavicmyths.earth.EarthSpirit) return false;
        if (enemy instanceof NeutralMob && (!(enemy instanceof Mob m)
                || m.getTarget() != owner && m.getTarget() != hero)) return false;
        return true;
    }
    public static boolean blighted(LivingEntity e){return e instanceof org.slavicmyths.entity.LandSpiritEntity s&&s.morState()==2;}
    public static boolean debugEnemy(LivingEntity e) {
        return e instanceof IdolishcheEntity b&&b.debug
                ||e instanceof org.slavicmyths.marinka.MarinkaEntity m&&m.debug&&m.arena==null&&!m.falseBody()
                ||e instanceof org.slavicmyths.skim.SkimBeastEntity s&&s.arena==null
                ||e instanceof org.slavicmyths.finist.KartausEntity k&&k.encounter==null
                ||e instanceof org.slavicmyths.kalin.KalinEntity c&&c.encounter==null;
    }
    public static boolean availableHero(LivingEntity e) {
        if(e==null)return false;
        if (!(e.level() instanceof net.minecraft.server.level.ServerLevel w)||!e.isAlive())return false;
        var owner=org.slavicmyths.kalin.KalinParty.owner(e);
        var p=owner==null?null:w.getServer().getPlayerList().getPlayer(owner);
        return p!=null&&p.isAlive()&&!p.isSpectator()&&p.level()==e.level()
                &&!org.slavicmyths.kalin.KalinParty.recovering(e)
                &&!e.getPersistentData().getBoolean("HeroRecalling")
                &&w.getGameTime()>=e.getPersistentData().getLong("HeroArrivingUntil");
    }
    public static boolean activeStory(ServerPlayer p) {
        return !p.serverLevel().getEntitiesOfClass(IdolishcheEntity.class,p.getBoundingBox().inflate(80),
                b->b.active&&!b.debug&&b.isAlive()&&(b.origin.multiply(1,0,1)
                        .distanceToSqr(p.position().multiply(1,0,1))<=26*26||p.getUUID().equals(b.initiator))).isEmpty();
    }
    public static LivingEntity debugOpponent(Mob boss,double radius) {
        return boss.level().getEntitiesOfClass(LivingEntity.class,boss.getBoundingBox().inflate(radius),
                e->(availableHero(e)||e instanceof ServerPlayer p&&p.isAlive()&&!p.isCreative()&&!p.isSpectator())
                        &&boss.distanceToSqr(e)<=radius*radius&&boss.hasLineOfSight(e)).stream()
                .min(Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
    }

    public static LivingEntity nearest(Mob hero, ServerPlayer owner, double radius) {
        if (owner == null || owner.level() != hero.level() || hero.distanceToSqr(owner) > 32 * 32) return null;
        var poloz=org.slavicmyths.poloz.PolozEvent.get(owner.serverLevel()).loaded(owner.serverLevel());
        if(poloz!=null&&poloz.isAlive()&&poloz.nearestDistance(hero.position())<=radius*radius&&org.slavicmyths.poloz.PolozAim.sight(hero,poloz))return poloz;
        return hero.level().getEntitiesOfClass(LivingEntity.class, hero.getBoundingBox().inflate(radius),
                e -> allowed(hero, owner, e) && hero.distanceToSqr(e) <= radius * radius
                        && hero.hasLineOfSight(e)).stream()
                .min(Comparator.comparingDouble(hero::distanceToSqr)).orElse(null);
    }
    private HeroThreats() {}
}
