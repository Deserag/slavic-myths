package org.slavicmyths.savanna;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/** Opportunistic local hunting, separate from retaliation and persistent encounter membership. */
final class CooperativeHunt {
    private static final int ALLY_RANGE=14, PREY_RANGE=24, MAX_HUNTERS=12, RECHECK=40;
    private static final double CONFIDENCE=.9;
    private CooperativeHunt(){}

    static LivingEntity choose(SavannaAnimal hunter,ServerLevel world){
        long now=world.getGameTime();
        LivingEntity previous=hunter.sharedHuntTarget;
        if(now<hunter.nextHuntCheck){
            if(previous!=null&&hunter.huntTargetAllowed(previous)&&hunter.distanceToSqr(previous)<=32*32)return previous;
            hunter.sharedHuntTarget=null;return null;
        }
        hunter.nextHuntCheck=now+RECHECK;
        List<SavannaAnimal> nearby=world.getEntitiesOfClass(SavannaAnimal.class,hunter.getBoundingBox().inflate(ALLY_RANGE),
            other->hunter.huntAlly(other)&&hunter.distanceToSqr(other)<=ALLY_RANGE*ALLY_RANGE);
        nearby.sort(Comparator.comparingDouble(hunter::distanceToSqr));
        if(nearby.size()>MAX_HUNTERS-1)nearby=new ArrayList<>(nearby.subList(0,MAX_HUNTERS-1));
        if(nearby.isEmpty()){hunter.sharedHuntTarget=null;return null;}

        if(previous!=null&&hunter.huntTargetAllowed(previous)&&hunter.distanceToSqr(previous)<=32*32){
            List<SavannaAnimal> team=team(hunter,nearby,previous);
            if(canHunt(team,previous)){assign(team,previous,now);return previous;}
        }
        List<LivingEntity> candidates=world.getEntitiesOfClass(LivingEntity.class,hunter.getBoundingBox().inflate(PREY_RANGE),
            prey->hunter.huntTargetAllowed(prey)&&hunter.distanceToSqr(prey)<=PREY_RANGE*PREY_RANGE&&hunter.hasLineOfSight(prey));
        candidates.sort(Comparator.comparingDouble(hunter::distanceToSqr));
        for(LivingEntity prey:candidates){
            List<SavannaAnimal> team=team(hunter,nearby,prey);
            if(canHunt(team,prey)){assign(team,prey,now);return prey;}
        }
        hunter.sharedHuntTarget=null;return null;
    }
    private static List<SavannaAnimal> team(SavannaAnimal hunter,List<SavannaAnimal> nearby,LivingEntity prey){
        List<SavannaAnimal> team=new ArrayList<>();team.add(hunter);
        for(SavannaAnimal other:nearby){
            LivingEntity target=other.getTarget();
            if(other.huntTargetAllowed(prey)&&other.distanceToSqr(prey)<=32*32
                &&(target==null||target==prey))team.add(other);
        }
        return team;
    }
    private static boolean canHunt(List<SavannaAnimal> team,LivingEntity prey){
        if(team.size()<2)return false;
        double health=0;boolean stronger=false;
        for(SavannaAnimal member:team){
            health+=member.getHealth();
            if(prey.getMaxHealth()>member.getMaxHealth()||prey.getBbWidth()>member.getBbWidth()*1.25)stronger=true;
        }
        return stronger&&health*CONFIDENCE>=prey.getHealth();
    }
    private static void assign(List<SavannaAnimal> team,LivingEntity prey,long now){
        team.sort(Comparator.comparing(SavannaAnimal::getUUID));
        for(int i=0;i<team.size();i++){
            SavannaAnimal member=team.get(i);
            member.sharedHuntTarget=prey;
            member.huntFlank=i*12/team.size();
            // The initiating member must periodically recheck actual survivors/health.
            if(member.nextHuntCheck<=now)member.nextHuntCheck=now+RECHECK;
        }
    }
}
