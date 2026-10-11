package org.slavicmyths.pesigolovtsy;
import java.util.*;
import java.lang.ref.WeakReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import org.slavicmyths.pesigolovtsy.DogheadRules.*;
/** Loaded UUID index maintained by lifecycle events. No global tick listener or chunk loading. */
@EventBusSubscriber(modid="slavicmyths")
public final class DogheadPacks {
 private static final Map<ServerLevel,Map<UUID,Map<UUID,WeakReference<DogheadEntity>>>> worlds=new WeakHashMap<>();
 private static final Map<ServerLevel,Map<UUID,Long>> ambient=new WeakHashMap<>();
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof DogheadEntity d&&e.getLevel() instanceof ServerLevel w){if(d.spawnRejected){e.setCanceled(true);return;}var pack=worlds.computeIfAbsent(w,k->new HashMap<>()).computeIfAbsent(d.pack,k->new LinkedHashMap<>());if(pack.size()>=6){d.pack=UUID.randomUUID();pack=worlds.get(w).computeIfAbsent(d.pack,k->new LinkedHashMap<>());}pack.put(d.getUUID(),new WeakReference<>(d));}}
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent e){if(e.getEntity() instanceof DogheadEntity d&&e.getLevel() instanceof ServerLevel w){var packs=worlds.get(w);if(packs==null)return;var p=packs.get(d.pack);if(p!=null){p.remove(d.getUUID());if(p.isEmpty()){packs.remove(d.pack);var sounds=ambient.get(w);if(sounds!=null)sounds.remove(d.pack);}}if(packs.isEmpty()){worlds.remove(w);ambient.remove(w);}}}
 public static List<DogheadEntity> members(DogheadEntity d){if(!(d.level() instanceof ServerLevel w))return List.of();var p=worlds.getOrDefault(w,Map.of()).get(d.pack);if(p==null)return List.of(d);var out=new ArrayList<DogheadEntity>(6);p.entrySet().removeIf(e->{var m=e.getValue().get();return m==null||m.isRemoved()||!m.isAlive();});for(var ref:p.values()){var m=ref.get();if(m!=null&&m.isAlive())out.add(m);}out.sort(Comparator.comparing(DogheadEntity::getUUID));return out;}
 public static int alert(DogheadEntity caller,LivingEntity victim,boolean howl){if(!caller.valid(victim)||!caller.hasLineOfSight(victim))return 0;int recipients=0;for(var m:members(caller)){if(m==caller||m.distanceToSqr(caller)>(howl?1024:576)||!m.valid(victim))continue;var revenge=m.getLastHurtByMob();if(revenge!=null&&revenge!=victim&&m.valid(revenge))continue;var existing=m.getTarget();if(existing!=null&&existing!=victim&&m.valid(existing))continue;m.receive(victim,caller,howl);recipients++;}caller.debug("alert target="+victim.getUUID()+" recipients="+recipients+" howl="+howl);return recipients;}
 public static boolean ambient(DogheadEntity d){if(!(d.level() instanceof ServerLevel w))return false;var times=ambient.computeIfAbsent(w,k->new HashMap<>());long now=w.getGameTime();if(times.getOrDefault(d.pack,Long.MIN_VALUE/2)>now-60)return false;times.put(d.pack,now);return true;}
 public static int slot(DogheadEntity d){if(d.role()==Role.SCOUT)return 3;if(d.role()==Role.LEADER)return 0;int hunter=0;for(var m:members(d))if(m.role()==Role.HUNTER){if(m==d)return hunter%3;hunter++;}return 0;}
 private DogheadPacks(){}
}
