package org.slavicmyths.verify;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.posad.PosadPopulationPlan;
import org.slavicmyths.posad.PosadResidentEntity;
import org.slavicmyths.registry.ModEntities;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadGuardGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void guardAttacksSlime(GameTestHelper t){
  var world=t.getLevel();var base=t.absolutePos(new BlockPos(1,2,1));
  for(int dx=-2;dx<=7;dx++)for(int dz=-2;dz<=4;dz++)world.setBlock(base.offset(dx,-1,dz),Blocks.POLISHED_BLACKSTONE.defaultBlockState(),3);
  var identity=UUID.randomUUID();var city=UUID.randomUUID();
  PosadResidentEntity guard=ModEntities.POSAD_RESIDENT.get().create(world);
  t.assertTrue(guard!=null,"Guard not registered");
  guard.moveTo(base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
  var member=new PosadPopulationPlan.Member("guard_test",PosadPopulationPlan.Role.GUARD,PosadPopulationPlan.Weapon.SWORD,70,1,1);
  guard.configure(new PosadPopulationPlan.WorldMember(identity,member,base.getX(),base.getZ()),city,base);
  t.assertTrue(world.addFreshEntity(guard),"Guard did not spawn");
  Slime slime=EntityType.SLIME.create(world);t.assertTrue(slime!=null,"Slime not registered");
  slime.moveTo(base.getX()+4.5,base.getY(),base.getZ()+.5,0,0);slime.setNoAi(true);
  t.assertTrue(world.addFreshEntity(slime),"Slime did not spawn");
  t.runAfterDelay(120,()->{t.assertTrue(!slime.isAlive()||slime.getHealth()<slime.getMaxHealth(),"Guard did not damage hostile slime: target="+guard.getTarget()+" guard="+guard.position()+" slime="+slime.position()+" hp="+slime.getHealth()+" navDone="+guard.getNavigation().isDone());t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void guardAttacksSlimeOutsideCity(GameTestHelper t){
  var world=t.getLevel();var base=t.absolutePos(new BlockPos(1,2,1));
  for(int dx=-2;dx<=7;dx++)for(int dz=-2;dz<=4;dz++)world.setBlock(base.offset(dx,-1,dz),Blocks.POLISHED_BLACKSTONE.defaultBlockState(),3);
  PosadResidentEntity guard=ModEntities.POSAD_RESIDENT.get().create(world);t.assertTrue(guard!=null,"Guard not registered");
  guard.moveTo(base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
  var member=new PosadPopulationPlan.Member("outside_guard",PosadPopulationPlan.Role.GUARD,PosadPopulationPlan.Weapon.SWORD,70,1,1);
  var home=base.offset(-38,0,0);
  guard.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,home.getX(),home.getZ()),UUID.randomUUID(),home);
  t.assertTrue(world.addFreshEntity(guard),"Guard did not spawn");
  Slime slime=EntityType.SLIME.create(world);t.assertTrue(slime!=null,"Slime not registered");
  slime.moveTo(base.getX()+4.5,base.getY(),base.getZ()+.5,0,0);slime.setNoAi(true);
  t.assertTrue(world.addFreshEntity(slime),"Slime did not spawn");
  t.runAfterDelay(150,()->{t.assertTrue(!slime.isAlive()||slime.getHealth()<slime.getMaxHealth(),"Outside guard did not attack: target="+guard.getTarget()+" pos="+guard.position()+" hp="+slime.getHealth());t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=300)
 public static void guardPursuesDistantOutsideSlime(GameTestHelper t){
  var world=t.getLevel();var base=t.absolutePos(new BlockPos(1,2,1));
  for(int dx=-2;dx<=15;dx++)for(int dz=-2;dz<=4;dz++){
   world.setBlock(base.offset(dx,-1,dz),Blocks.POLISHED_BLACKSTONE.defaultBlockState(),3);
   for(int dy=0;dy<=3;dy++)world.setBlock(base.offset(dx,dy,dz),Blocks.AIR.defaultBlockState(),3);
  }
  PosadResidentEntity guard=ModEntities.POSAD_RESIDENT.get().create(world);t.assertTrue(guard!=null,"Guard not registered");
  guard.moveTo(base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
  var member=new PosadPopulationPlan.Member("distant_guard",PosadPopulationPlan.Role.GUARD,PosadPopulationPlan.Weapon.SWORD,70,1,1);
  var home=base.offset(-30,0,0);
  guard.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,home.getX(),home.getZ()),UUID.randomUUID(),home);
  t.assertTrue(world.addFreshEntity(guard),"Guard did not spawn");
  Slime slime=EntityType.SLIME.create(world);t.assertTrue(slime!=null,"Slime not registered");
  slime.moveTo(base.getX()+12.5,base.getY(),base.getZ()+.5,0,0);slime.setNoAi(true);
  t.assertTrue(world.addFreshEntity(slime),"Slime did not spawn");
  t.runAfterDelay(240,()->{t.assertTrue(!slime.isAlive()||slime.getHealth()<slime.getMaxHealth(),"Distant outside guard did not attack: target="+guard.getTarget()+" pos="+guard.position()+" hp="+slime.getHealth());t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void guardAttacksZombie(GameTestHelper t){
  var world=t.getLevel();var base=t.absolutePos(new BlockPos(1,2,1));
  for(int dx=-2;dx<=7;dx++)for(int dz=-2;dz<=4;dz++)world.setBlock(base.offset(dx,-1,dz),Blocks.POLISHED_BLACKSTONE.defaultBlockState(),3);
  PosadResidentEntity guard=ModEntities.POSAD_RESIDENT.get().create(world);t.assertTrue(guard!=null,"Guard not registered");
  guard.moveTo(base.getX()+.5,base.getY(),base.getZ()+.5,0,0);
  var member=new PosadPopulationPlan.Member("zombie_guard",PosadPopulationPlan.Role.GUARD,PosadPopulationPlan.Weapon.SWORD,70,1,1);
  guard.configure(new PosadPopulationPlan.WorldMember(UUID.randomUUID(),member,base.getX(),base.getZ()),UUID.randomUUID(),base);
  t.assertTrue(world.addFreshEntity(guard),"Guard did not spawn");
  Zombie zombie=EntityType.ZOMBIE.create(world);t.assertTrue(zombie!=null,"Zombie not registered");
  zombie.moveTo(base.getX()+4.5,base.getY(),base.getZ()+.5,0,0);zombie.setNoAi(true);
  t.assertTrue(world.addFreshEntity(zombie),"Zombie did not spawn");
  t.runAfterDelay(150,()->{t.assertTrue(!zombie.isAlive()||zombie.getHealth()<zombie.getMaxHealth(),"Guard did not attack zombie");t.succeed();});
 }
}
