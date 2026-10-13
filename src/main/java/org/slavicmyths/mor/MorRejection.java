package org.slavicmyths.mor;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slavicmyths.entity.LandSpiritEntity;
@EventBusSubscriber(modid="slavicmyths")
public final class MorRejection {
 private static final java.util.Set<PathfinderMob> installed=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getLevel().isClientSide||!(e.getEntity() instanceof PathfinderMob mob)||mob instanceof LandSpiritEntity s&&s.morState()!=0||mob instanceof net.minecraft.world.entity.TamableAnimal t&&t.isTame())return;
  if(!installed.add(mob))return;
  if(mob instanceof Enemy)mob.targetSelector.addGoal(3,new NearestAttackableTargetGoal<LandSpiritEntity>(mob,LandSpiritEntity.class,20,true,false,HigherMor::higher));
  else if(mob instanceof Animal||mob instanceof Villager||mob instanceof NeutralMob||mob instanceof org.slavicmyths.entity.LeshyEntity||mob instanceof org.slavicmyths.entity.PoludnitsaEntity||mob instanceof org.slavicmyths.entity.KikimoraEntity)mob.goalSelector.addGoal(1,new AvoidEntityGoal<LandSpiritEntity>(mob,LandSpiritEntity.class,HigherMor::higher,12,1.1,1.35,e2->true){@Override public boolean canUse(){return (!(mob instanceof LandSpiritEntity s)||s.morState()==0)&&super.canUse();}@Override public boolean canContinueToUse(){return (!(mob instanceof LandSpiritEntity s)||s.morState()==0)&&super.canContinueToUse();}});
 }
 private MorRejection(){}
}
