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
 @GameTest(template="empty",timeoutTicks=40)
 public static void respawnRestoresArmorHealthBeforeFirstTick(GameTestHelper t){
  var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"MarineRespawn"));
  float baseline=p.getMaxHealth();
  p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(MarineArmor.HELMET.get()));
  p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(MarineArmor.CHESTPLATE.get()));
  p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(MarineArmor.LEGGINGS.get()));
  p.setItemSlot(EquipmentSlot.FEET,new ItemStack(MarineArmor.BOOTS.get()));
  var bonus=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","marine_full_health");
  p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).removeModifier(bonus);
  p.setHealth(20);
  MarineArmorEffects.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false));
  t.assertTrue(p.getMaxHealth()==baseline+16,"Respawn requires a later tick to restore armor HP");
  t.assertTrue(p.getHealth()==20,"Restoring maximum health unexpectedly healed the player");
  MarineArmorEffects.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false));
  t.assertTrue(p.getMaxHealth()==baseline+16,"Respawn duplicated the armor modifier");
  p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
  MarineArmorEffects.equipment(new net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent(p,EquipmentSlot.HEAD,new ItemStack(MarineArmor.HELMET.get()),ItemStack.EMPTY));
  t.assertTrue(p.getMaxHealth()==baseline,"Removing armor did not immediately remove HP");
  MarineArmorEffects.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false));
  t.assertTrue(p.getMaxHealth()==baseline,"Respawn without a full set granted armor HP");
  p.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void otherArmorSetHealthRestoresWithoutStacking(GameTestHelper t){
  var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"SetRespawn"));
  var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
  var perun=new net.minecraft.world.item.Item[]{org.slavicmyths.registry.ModItems.PERUNITE_HELMET.get(),org.slavicmyths.registry.ModItems.PERUNITE_CHESTPLATE.get(),org.slavicmyths.registry.ModItems.PERUNITE_LEGGINGS.get(),org.slavicmyths.registry.ModItems.PERUNITE_BOOTS.get()};
  for(int kind=0;kind<3;kind++){
   for(int i=0;i<4;i++)p.setItemSlot(slots[i],kind==0?new ItemStack(perun[i]):kind==1?new ItemStack(org.slavicmyths.gorynych.GorynychContent.ARMOR.get(slots[i]).get()):org.slavicmyths.blackcinder.ZharenitsaSeals.seal(new ItemStack(new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.IRON_HELMET,net.minecraft.world.item.Items.IRON_CHESTPLATE,net.minecraft.world.item.Items.IRON_LEGGINGS,net.minecraft.world.item.Items.IRON_BOOTS}[i])));
   var event=new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false);
   org.slavicmyths.poloz.PolozRewardEvents.respawn(event);
   float expected=20+(kind==1?20:5);
   t.assertTrue(p.getMaxHealth()==expected,"Set respawn HP wrong for set "+kind+": "+p.getMaxHealth());
   org.slavicmyths.poloz.PolozRewardEvents.respawn(event);
   t.assertTrue(p.getMaxHealth()==expected,"Repeated refresh stacked set HP");
   for(var slot:slots)p.setItemSlot(slot,ItemStack.EMPTY);
   org.slavicmyths.poloz.PolozRewardEvents.respawn(event);
   t.assertTrue(p.getMaxHealth()==20,"Removed set retained HP");
  }
  p.discard();t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void necklaceRestoresHealthOnRespawn(GameTestHelper t){
  var p=FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"NecklaceRespawn"));
  var item=org.slavicmyths.sisters.SisterArtifacts.ITEMS.get("sisters_necklace").get();
  var inv=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow();
  var slot=inv.getCurios().get("necklace").getStacks();
  slot.setStackInSlot(0,new ItemStack(item));
  var event=new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false);
  org.slavicmyths.sisters.SisterArtifactEvents.respawn(event);
  t.assertTrue(p.getMaxHealth()==24,"Necklace HP missing before first tick");
  org.slavicmyths.sisters.SisterArtifactEvents.respawn(event);
  t.assertTrue(p.getMaxHealth()==24,"Necklace HP stacked on repeated refresh");
  slot.setStackInSlot(0,ItemStack.EMPTY);
  org.slavicmyths.sisters.SisterArtifactEvents.respawn(event);
  t.assertTrue(p.getMaxHealth()==20,"Absent necklace retained HP");p.discard();t.succeed();
 }}