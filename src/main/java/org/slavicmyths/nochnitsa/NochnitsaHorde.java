package org.slavicmyths.nochnitsa;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import org.slavicmyths.underground.OminousAura;

/** One loaded local query per scream; event-installed low priority, expiring target goal. */
@EventBusSubscriber(modid="slavicmyths")
public final class NochnitsaHorde {
    public static final TagKey<EntityType<?>> RECRUITABLE=TagKey.create(Registries.ENTITY_TYPE,NochnitsaContent.id("nochnitsa_recruitable")),VILLAGER_HUNTERS=TagKey.create(Registries.ENTITY_TYPE,NochnitsaContent.id("nochnitsa_villager_hunters"));
    private record Alert(UUID target,long expires,long written){}
    private static final Map<Mob,Alert> memories=new WeakHashMap<>();
    public static boolean species(Mob m){return m instanceof Monster&&!(m instanceof NochnitsaEntity)&&!(m instanceof org.slavicmyths.underground.RoomBoss)&&!(m instanceof org.slavicmyths.industry.ChudPerson)&&!m.getType().is(OminousAura.IMMUNE)&&m.getMaxHealth()<=50&&(m instanceof Zombie||m instanceof AbstractSkeleton||m instanceof Spider||m instanceof Witch||m instanceof Creeper||m instanceof AbstractIllager||m.getType().is(RECRUITABLE));}
    public static boolean legal(Mob m,LivingEntity t){if(t==null||!t.isAlive()||m.level()!=t.level()||m.isAlliedTo(t)||!m.canAttack(t))return false;
        if(m instanceof NeutralMob n&&!n.isAngryAt(t))return false;
        if(t instanceof Player p)return !p.isCreative()&&!p.isSpectator()&&!OminousAura.ignores(m,p);
        return t instanceof Villager&&(m instanceof Zombie||m instanceof AbstractIllager||m.getType().is(VILLAGER_HUNTERS));
    }
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getLevel().isClientSide)return;if(e.getEntity() instanceof NochnitsaEntity n&&e.loadedFromDisk()&&!NochnitsaRules.night(e.getLevel().getDayTime())&&!n.debugNight()){e.setCanceled(true);return;}if(e.getEntity() instanceof Mob m&&species(m))m.targetSelector.addGoal(3,new MemoryGoal(m));}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent e){if(e.getEntity() instanceof Mob m)memories.remove(m);}
    public static int release(NochnitsaEntity source,LivingEntity victim){if(!(source.level() instanceof ServerLevel w)||!source.mayScreamAt(victim))return 0;
        var candidates=w.getEntitiesOfClass(Monster.class,source.getBoundingBox().inflate(32),m->m!=source&&NochnitsaRules.inRadius(m.distanceToSqr(source)));
        candidates.sort(Comparator.comparingDouble(m->m.distanceToSqr(source)));int examined=0,alerted=0;long now=w.getGameTime();
        for(var m:candidates){if(examined++>=NochnitsaRules.CAP)break;if(!species(m)||!legal(m,victim))continue;var current=m.getTarget();if(current!=null&&current.isAlive()&&m.canAttack(current))continue;var revenge=m.getLastHurtByMob();if(revenge!=null&&revenge!=victim&&revenge.isAlive()&&m.canAttack(revenge))continue;
            var old=memories.get(m);if(old!=null&&(now-old.written<NochnitsaRules.WRITE_GAP||old.target.equals(victim.getUUID())&&old.expires>now))continue;
            memories.put(m,new Alert(victim.getUUID(),now+NochnitsaRules.ALERT_TTL,now));m.setTarget(victim);if(m.getTarget()==victim){m.getNavigation().moveTo(victim,1);alerted++;}else memories.remove(m);
        }source.debug("scream_emit candidates="+Math.min(examined,NochnitsaRules.CAP)+" alerted="+alerted);return alerted;
    }
    private static final class MemoryGoal extends Goal{
        final Mob mob;LivingEntity victim;boolean owns;
        MemoryGoal(Mob m){mob=m;setFlags(EnumSet.of(Flag.TARGET));}
        private boolean resolve(){var a=memories.get(mob);if(a==null||!(mob.level() instanceof ServerLevel w))return false;if(w.getGameTime()>=a.expires){memories.remove(mob);return false;}var entity=w.getEntity(a.target);victim=entity instanceof LivingEntity l?l:null;return legal(mob,victim)&&(mob.getTarget()==null||mob.getTarget()==victim);}
        public boolean canUse(){return resolve();}public boolean canContinueToUse(){return resolve();}public boolean requiresUpdateEveryTick(){return true;}
        public void start(){if(legal(mob,victim)){mob.setTarget(victim);owns=mob.getTarget()==victim;}}
        public void tick(){if(!legal(mob,victim)){if(mob.getTarget()==victim)mob.setTarget(null);memories.remove(mob);}}
        public void stop(){if(owns&&mob.getTarget()==victim&&mob.getLastHurtByMob()!=victim)mob.setTarget(null);owns=false;memories.remove(mob);victim=null;}
    }
    private NochnitsaHorde(){}
}
