package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.viy.ViyContent;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class ViyGameTests {
 @GameTest(template="empty",timeoutTicks=120,batch="viy")
 public static void registryAndPhase(GameTestHelper t){
  var w=t.getLevel();var b=ViyContent.VIY.get().create(w);t.assertTrue(b!=null&&b.getMaxHealth()==2800,"Viy attributes missing");b.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(4,8,4)));b.setNoGravity(true);w.addFreshEntity(b);b.setHealth(b.getMaxHealth()*.45F);
  t.runAfterDelay(35,()->{t.assertTrue(b.phaseTwo()&&b.eyesOpen()&&b.eyelidProgress()==1,"Phase2 eyelids failed");b.discard();t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=120,batch="viy")
 public static void servantsBindAndExpire(GameTestHelper t){
  var w=t.getLevel();var b=ViyContent.VIY.get().create(w);b.setPos(t.absoluteVec(new net.minecraft.world.phys.Vec3(4,8,4)));b.setNoGravity(true);b.setNoAi(true);w.addFreshEntity(b);
  var types=java.util.List.of(ViyContent.VIY_UPYR.get(),ViyContent.VIY_NAV.get(),ViyContent.VIY_KORNELAP.get());var all=new java.util.ArrayList<org.slavicmyths.sisters.ViyServant>();
  for(var type:types){var s=type.create(w);s.setPos(b.position());s.setNoGravity(true);s.link(b,null);w.addFreshEntity(s);t.assertTrue(b.getUUID().equals(s.ownerId()),"Owner missing");all.add(s);}b.discard();
  t.runAfterDelay(45,()->{for(var s:all)t.assertTrue(s.isRemoved(),"Servant survived absent owner");t.succeed();});
 }
}
