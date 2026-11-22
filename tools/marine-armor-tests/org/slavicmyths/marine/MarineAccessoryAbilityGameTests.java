package org.slavicmyths.marine;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import top.theillusivec4.curios.api.CuriosApi;
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineAccessoryAbilityGameTests {
 @GameTest(template="empty",timeoutTicks=40)
 public static void ringRequiresEquipmentAndRespectsCooldown(GameTestHelper t){
  var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"TideTest"));
  var first=new LivingFallEvent(p,12,1);MarineAccessoryAbilities.fall(first);
  t.assertTrue(first.getDamageMultiplier()==1,"Unequipped ring protected player");
  var slot=CuriosApi.getCuriosInventory(p).orElseThrow().getCurios().get("ring").getStacks();slot.setStackInSlot(0,new ItemStack(MarineAccessories.TIDE_RING.get()));
  var saved=new LivingFallEvent(p,12,1);MarineAccessoryAbilities.fall(saved);t.assertTrue(saved.getDamageMultiplier()==0,"Ring failed to save fall");
  var next=new LivingFallEvent(p,12,1);MarineAccessoryAbilities.fall(next);t.assertTrue(next.getDamageMultiplier()==1,"Ring ignored cooldown");
  slot.setStackInSlot(0,ItemStack.EMPTY);p.discard();t.succeed();
 }
}
