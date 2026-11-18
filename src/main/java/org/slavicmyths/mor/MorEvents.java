package org.slavicmyths.mor;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.entity.LandSpiritEntity;

@EventBusSubscriber(modid="slavicmyths")
public final class MorEvents {
    @SubscribeEvent public static void spawn(FinalizeSpawnEvent e){if(e.isSpawnCancelled()||!(e.getEntity() instanceof LandSpiritEntity mob)||(mob.mor.kind()==0||mob.mor.kind()>6)||mob.mor.protectedMob())return;var world=e.getLevel().getLevel();var spawn=world.getSharedSpawnPos();double dx=mob.getX()-spawn.getX(),dz=mob.getZ()-spawn.getZ();boolean reason=e.getSpawnType()==MobSpawnType.NATURAL||e.getSpawnType()==MobSpawnType.SPAWNER;
        if(!MorRules.natural(world.dimension()==Level.OVERWORLD,world.getDayTime(),dx*dx+dz*dz,reason,mob.getRandom().nextInt(100)))return;
        if(world.getEntitiesOfClass(LandSpiritEntity.class,mob.getBoundingBox().inflate(48,24,48),other->other.mor.kind()!=0&&!other.morClone()&&(other.morState()!=0||other.mor.pendingNatural())).isEmpty())mob.mor.naturalCandidate();
    }
    @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity() instanceof LandSpiritEntity m&&m.morClone())e.setCanceled(true);}
    @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity() instanceof LandSpiritEntity m&&m.morClone())e.setCanceled(true);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof LandSpiritEntity m&&m.morState()!=0)m.mor.cleanup();}
    private MorEvents(){}
}
