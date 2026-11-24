package org.slavicmyths.owl;
import java.util.HashSet;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class WinterOwlGameTests {
 @GameTest(template="empty") public static void winterOwlRegistrationAndPhase(GameTestHelper t){
 var b=WinterOwlContent.BOSS.get().create(t.getLevel());t.assertTrue(b!=null&&b.getMaxHealth()==2000F&&b.getHealth()==2000F,"Winter owl health or registry");
 t.assertTrue(WinterOwlContent.EGG.get() instanceof SpawnEggItem,"Missing egg");t.assertTrue(!b.secondPhase()&&b.belly()==0,"Initial pose");
 b.setHealth(1001);t.assertTrue(!b.secondPhase(),"Premature second phase");b.setHealth(1000);t.assertTrue(b.secondPhase(),"Second phase missing");
 var s=WinterOwlContent.SPIRIT.get().create(t.getLevel());t.assertTrue(s!=null&&s.spirit()&&s.getMaxHealth()==35,"Spirit registry");b.discard();s.discard();t.succeed();}
 @GameTest(template="empty") public static void winterOwlAttackCatalog(GameTestHelper t){var a=new HashSet<Integer>();var b=new HashSet<Integer>();for(int i=0;i<8;i++)a.add(WinterOwl.attackAt(false,i));for(int i=0;i<13;i++)b.add(WinterOwl.attackAt(true,i));t.assertTrue(a.size()==8&&b.size()==13&&b.containsAll(a),"Attack catalog");t.succeed();}
}