package org.slavicmyths.marine;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class MarineArmorBonusGameTests {
 @GameTest(template="empty",timeoutTicks=40)
 public static void healthAndIndependentWaterBonuses(GameTestHelper t){
  var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"MarineArmorBonus"));
  float baseline=p.getMaxHealth();
  for(int x=1;x<6;x++)for(int y=1;y<6;y++)for(int z=1;z<6;z++)t.setBlock(new BlockPos(x,y,z),Blocks.WATER);
  p.moveTo(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,2,3)));p.baseTick();
  p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(MarineArmor.HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(MarineArmor.CHESTPLATE.get()));
  p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(MarineArmor.LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(MarineArmor.BOOTS.get()));
  MarineArmorEffects.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(p.getMaxHealth()==baseline+16,"Full armor must add eight hearts");
  t.assertTrue(p.hasEffect(MobEffects.REGENERATION),"Chest lacks water regeneration");
  t.assertTrue(p.getEffect(MobEffects.DAMAGE_BOOST)!=null&&p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier()==1,"Leggings lack Strength II");
  p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);MarineArmorEffects.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(p.getMaxHealth()==baseline,"Health bonus survived partial armor");
  t.assertTrue(p.hasEffect(MobEffects.REGENERATION)&&p.hasEffect(MobEffects.DAMAGE_BOOST),"Piece effects incorrectly require the whole set");
  p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);MarineArmorEffects.tick(new PlayerTickEvent.Post(p));
  t.assertTrue(!p.hasEffect(MobEffects.REGENERATION)&&!p.hasEffect(MobEffects.DAMAGE_BOOST),"Removed pieces retained bonuses");
  t.assertTrue(MarineArmor.MATERIAL.get().toughness()>2&&MarineArmor.MATERIAL.get().toughness()<3,"Defense must lie between diamond and netherite");
  p.discard();t.succeed();
 }
}