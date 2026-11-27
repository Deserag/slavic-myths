package org.slavicmyths.savanna;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.combat.WeaponProjectile;
import org.slavicmyths.savanna.SteppeWeapons.Kind;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class SteppeWeaponGameTests {
 @GameTest(template="empty")
 public static void registryAndModes(GameTestHelper h){
  h.assertTrue(Kind.values().length==20&&SteppeWeapons.ITEMS.size()==20&&SteppeWeapons.MATERIALS.size()==6,"Weapon registry size mismatch");
  for(Kind k:Kind.values()){
   var stack=new ItemStack(SteppeWeapons.item(k));h.assertTrue(!stack.isEmpty()&&stack.isDamageableItem()&&stack.getMaxStackSize()==1&&stack.getMaxDamage()==k.durability,"Weapon durability mismatch: "+k);
  }
  var blade=new ItemStack(SteppeWeapons.item(Kind.SKARAPEYA_BLADE));blade.setDamageValue(37);
  h.assertTrue(!SteppeWeapons.split(blade)&&!SteppeWeapons.visualPair(blade),"Combined mode starts split");
  SteppeWeapons.mode(blade,true);SteppeWeapons.poison(blade,6);
  h.assertTrue(SteppeWeapons.split(blade)&&SteppeWeapons.visualPair(blade)&&blade.getCount()==1&&blade.getDamageValue()==37,"Mode change duplicated or damaged stack");
  var saved=ItemStack.parseOptional(h.getLevel().registryAccess(),blade.save(h.getLevel().registryAccess()));
  h.assertTrue(SteppeWeapons.split(saved)&&SteppeWeapons.poison(saved)==6&&saved.getDamageValue()==37,"Mode/poison failed save-load");
  SteppeWeapons.mode(saved,false);h.assertTrue(!SteppeWeapons.visualPair(saved)&&saved.getDamageValue()==37&&SteppeWeapons.poison(saved)==6,"Combined mode did not restore correctly");
  h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void needleNoAmmoAndWear(GameTestHelper h){
  var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"SteppeNeedleQA"));
  p.moveTo(h.absoluteVec(new Vec3(3,3,3)));p.setGameMode(GameType.SURVIVAL);p.setShiftKeyDown(false);
  p.getInventory().clearContent();
  var stack=new ItemStack(SteppeWeapons.item(Kind.SKARAPEYA_NEEDLE));p.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var handler=(WeaponInputHandler)stack.getItem();handler.onWeaponAction(p,stack,WeaponInputHandler.Action.LEFT_CLICK);
  h.assertTrue(stack.getDamageValue()==1,"Needle did not fire without ammunition");
  var shots=h.getLevel().getEntitiesOfClass(WeaponProjectile.class,p.getBoundingBox().inflate(4),a->a.getPersistentData().getBoolean("SteppeNeedle")&&a.getOwner()==p);
  h.assertTrue(shots.size()==1&&shots.getFirst().pickup==net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED,"Needle projectile missing or recoverable");
  handler.onWeaponAction(p,stack,WeaponInputHandler.Action.LEFT_CLICK);h.assertTrue(stack.getDamageValue()==1,"Cooldown did not block repeat");
  p.setShiftKeyDown(true);handler.onWeaponAction(p,stack,WeaponInputHandler.Action.SHIFT_RIGHT_CLICK);
  h.assertTrue(SteppeWeapons.poison(stack)==1&&stack.getDamageValue()==1,"Poison selection spent durability");
  handler.onWeaponAction(p,stack,WeaponInputHandler.Action.SHIFT_LEFT_CLICK);h.assertTrue(stack.getDamageValue()==4,"Triple shot did not consume three durability");
  h.getLevel().getEntitiesOfClass(WeaponProjectile.class,p.getBoundingBox().inflate(6),a->a.getPersistentData().getBoolean("SteppeNeedle")&&a.getOwner()==p).forEach(a->a.discard());
  p.discard();h.succeed();
 }
}
