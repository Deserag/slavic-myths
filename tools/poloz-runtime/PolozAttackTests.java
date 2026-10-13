package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.poloz.*;
@GameTestHolder("slavicmyths_poloz") @PrefixGameTestTemplate(false)
public final class PolozAttackTests {
 static void run(GameTestHelper t,int attack,int end){var b=PolozGameTests.boss(t);b.setPhaseForTest(2);var z=new Zombie(EntityType.ZOMBIE,t.getLevel());z.setItemSlot(EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET));z.setNoAi(true);z.setNoGravity(true);z.moveTo(b.position().add(0,0,12));z.setTarget(b);t.getLevel().addFreshEntity(z);b.setTarget(z);try{var begin=GreatPoloz.class.getDeclaredMethod("begin",int.class);begin.setAccessible(true);begin.invoke(b,attack);}catch(Exception e){throw new RuntimeException(e);}Vec3 initial=b.position();t.assertTrue(b.action()==attack,"actual native attack selection");t.runAtTickTime(10,()->{t.assertTrue(b.action()==attack,"telegraph remains active");t.assertTrue(z.getHealth()==20,"no telegraph damage: "+z.getHealth());});t.runAtTickTime(end,()->{t.assertTrue(b.action()==0,"attack has finite recovery: "+attack);t.assertTrue(Double.isFinite(b.getX())&&Double.isFinite(b.getY())&&Double.isFinite(b.getZ()),"finite movement");if(attack==1||attack==2||attack==5)t.assertTrue(b.position().distanceTo(initial)>1,"actual movement attack "+attack);t.assertTrue(b.minions.size()<=8,"minion cap");if(attack==6){t.assertTrue(b.minions.size()==4,"phase2 exact summoned party: "+b.minions.size());for(var id:b.minions){var m=t.getLevel().getEntity(id);t.assertTrue(m!=null&&m.getPersistentData().getUUID("summonedByPolozUUID").equals(b.getUUID()),"owned minion");m.discard();}}b.discard();z.discard();t.succeed();});}
 @GameTest(template="empty",timeoutTicks=120) public static void crushingPass(GameTestHelper t){run(t,1,80);}
 @GameTest(template="empty",timeoutTicks=150) public static void burrow(GameTestHelper t){run(t,2,115);}
 @GameTest(template="empty",timeoutTicks=130) public static void rain(GameTestHelper t){run(t,3,100);}
 @GameTest(template="empty",timeoutTicks=80) public static void tail(GameTestHelper t){run(t,4,55);}
 @GameTest(template="empty",timeoutTicks=180) public static void ring(GameTestHelper t){run(t,5,155);}
 @GameTest(template="empty",timeoutTicks=90) public static void summons(GameTestHelper t){run(t,6,55);}
}
