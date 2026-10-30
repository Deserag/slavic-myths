package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
@GameTestHolder("slavicmyths_netherboss") @PrefixGameTestTemplate(false)
public final class NetherBossGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void attributesAndSave(GameTestHelper t) {
  var morova=ModEntities.MOROVA_DEVA.get().create(t.getLevel());
  var prince=ModEntities.TRIZNA_PRINCE.get().create(t.getLevel());
  t.assertTrue(morova.getMaxHealth()==1350,"Morova HP");
  t.assertTrue(prince.getMaxHealth()==1800,"Trizna HP");
  var saved=new CompoundTag();morova.addAdditionalSaveData(saved);
  saved.putInt("NetherBossAction",1);saved.putInt("NetherBossActionTicks",25);saved.putInt("NetherBossCooldown",100);saved.putBoolean("NetherBossResolved",false);
  morova.readAdditionalSaveData(saved);
  t.assertTrue(morova.action()==1&&morova.actionTicks()==25,"Lost attack state");
  var roundtrip=new CompoundTag();morova.addAdditionalSaveData(roundtrip);
  t.assertTrue(roundtrip.getInt("NetherBossCooldown")==100&&!roundtrip.getBoolean("NetherBossResolved"),"Lost cooldown/contact state");
  morova.discard();prince.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void plagueLevels(GameTestHelper t) {
  var mob=net.minecraft.world.entity.EntityType.ZOMBIE.create(t.getLevel());
  double speed=mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
  for(int phase=0;phase<3;phase++) {
   mob.removeEffect(ModEffects.MOROVA_PLAGUE);
   mob.addEffect(new MobEffectInstance(ModEffects.MOROVA_PLAGUE,200,phase));
   double ratio=mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)/speed;
   t.assertTrue(Math.abs(ratio-(phase==0?.90:phase==1?.85:.80))<.0001,"Wrong plague slow");
  }
  mob.removeEffect(ModEffects.MOROVA_PLAGUE);
  t.assertTrue(Math.abs(mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-speed)<.0001,"Attribute residue");
  mob.discard();t.succeed();
 }
}
