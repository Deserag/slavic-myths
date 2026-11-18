package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.netherboss.*;
import org.slavicmyths.rpg.Runes;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("slavicmyths_netherboss") @PrefixGameTestTemplate(false)
public final class NetherBossAbilityTests {
 private static final class Connection extends net.minecraft.network.Connection {
  private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
  Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
  @Override public io.netty.channel.Channel channel(){return channel;}
 }
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
  Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
  @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener listener){}
 }
 private static ServerPlayer player(GameTestHelper t){
  var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"NetherAbilityQA"),ClientInformation.createDefault());
  p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new net.minecraft.world.phys.Vec3(3,2,3)));p.setNoGravity(true);
  for(int i=0;i<61;i++)p.tick();return p;
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void curiosAndPersistentOwnerCooldown(GameTestHelper t){
  var p=player(t);var inv=CuriosApi.getCuriosInventory(p).orElseThrow();
  t.assertTrue(!NetherTrophies.activateEquipped(p,true),"Unequipped remote activation accepted");
  var pendant=new ItemStack(NetherBossItems.item("morova_silence_pendant"));
  t.assertTrue(CuriosApi.isStackValid(new top.theillusivec4.curios.api.SlotContext("necklace",p,0,false,true),pendant),"Pendant slot invalid");
  inv.setEquippedCurio("necklace",0,pendant);p.tick();t.assertTrue(NetherTrophies.activateEquipped(p,true),"Equipped activation failed");
  inv.setEquippedCurio("necklace",0,ItemStack.EMPTY);inv.setEquippedCurio("necklace",0,pendant.copy());
  t.assertTrue(!NetherTrophies.activateEquipped(p,true),"Swap reset cooldown");
  var saved=new net.minecraft.nbt.CompoundTag();p.saveWithoutId(saved);var restored=player(t);restored.load(saved);
  t.assertTrue(!NetherTrophies.activate(restored,true),"Saved player cooldown lost");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void neutralAndPvpSafety(GameTestHelper t){
  var p=player(t);var other=player(t);var neutral=net.minecraft.world.entity.EntityType.ENDERMAN.create(t.getLevel());
  var sheep=net.minecraft.world.entity.EntityType.SHEEP.create(t.getLevel());var enemy=net.minecraft.world.entity.EntityType.ZOMBIE.create(t.getLevel());
  t.assertTrue(!NetherTrophies.hostile(p,other),"PvP permitted");t.assertTrue(!NetherTrophies.hostile(p,sheep),"Peaceful target permitted");
  t.assertTrue(!NetherTrophies.hostile(p,neutral),"Unprovoked neutral permitted");t.assertTrue(NetherTrophies.hostile(p,enemy),"Ordinary enemy rejected");
  var ally=org.slavicmyths.registry.ModEntities.NETHER_RETAINER.get().create(t.getLevel());ally.configure(other,40,7,1200);
  t.assertTrue(!NetherTrophies.hostile(p,ally),"Other player's summon permitted");ally.discard();neutral.discard();sheep.discard();enemy.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void lingeringExistingEffectsOnly(GameTestHelper t){
  var p=player(t);var target=net.minecraft.world.entity.EntityType.ZOMBIE.create(t.getLevel());
  var sword=new ItemStack(Items.DIAMOND_SWORD);Runes.write(sword,1,List.of("morova_lingering"));p.setItemSlot(EquipmentSlot.MAINHAND,sword);
  target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,400,0));
  target.invulnerableTime=0;target.hurt(p.damageSources().playerAttack(p),1);
  t.assertTrue(target.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getDuration()==500,"Extension is not capped +100");
  target.invulnerableTime=0;target.hurt(p.damageSources().playerAttack(p),1);
  t.assertTrue(target.getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getDuration()==500,"Per-effect cooldown bypassed");
  t.assertTrue(!target.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"Rune created a new effect");target.discard();t.succeed();
 }
 private static net.minecraft.core.BlockPos arena(GameTestHelper t,int offset){
  var w=t.getLevel();var a=t.absolutePos(new net.minecraft.core.BlockPos(offset,1,offset));
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
   var cp=new net.minecraft.world.level.ChunkPos(a.offset(x,0,z));w.setChunkForced(cp.x,cp.z,true);w.getChunk(cp.x,cp.z);
   w.setBlock(a.offset(x,0,z),net.minecraft.world.level.block.Blocks.BLACKSTONE.defaultBlockState(),2);
   for(int y=1;y<=5;y++)w.setBlock(a.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
  }return a;
 }
 @GameTest(template="empty",timeoutTicks=500)
 public static void silenceActuallyBlocksDamageAndProjectiles(GameTestHelper t){
  var w=t.getLevel();var a=arena(t,160);var p=player(t);p.setPos(a.getX(),a.getY()+1,a.getZ());
  t.runAtTickTime(100,()->{
   var enemy=net.minecraft.world.entity.EntityType.HUSK.create(w);enemy.moveTo(a.getX()+2,a.getY()+1,a.getZ(),0,0);w.addFreshEntity(enemy);
   var victim=net.minecraft.world.entity.EntityType.HUSK.create(w);victim.setNoAi(true);victim.moveTo(a.getX(),a.getY()+1,a.getZ()+2,0,0);w.addFreshEntity(victim);
   t.assertTrue(NetherTrophies.activate(p,true),"Silence activation failed");t.assertTrue(enemy.isNoAi(),"AI not suspended");
   float health=victim.getHealth();victim.hurt(victim.damageSources().mobAttack(enemy),5);t.assertTrue(victim.getHealth()==health,"Silenced source damaged a victim");
   var arrow=new net.minecraft.world.entity.projectile.Arrow(net.minecraft.world.entity.EntityType.ARROW,w);arrow.setOwner(enemy);arrow.moveTo(enemy.position());
   t.assertTrue(!w.addFreshEntity(arrow),"Silenced source spawned a projectile");
   t.runAtTickTime(240,()->{
    t.assertTrue(!enemy.isNoAi(),"AI not restored");
    t.assertTrue(victim.isNoAi(),"Originally disabled AI was incorrectly enabled");
    t.assertTrue(enemy.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)&&enemy.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)&&enemy.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION),"Post-control debuffs missing");
    enemy.discard();victim.discard();t.succeed();
   });
  });
 }
 @GameTest(template="empty",timeoutTicks=900)
 public static void emberHealingSlidingCapAndFireImmunity(GameTestHelper t){
  var w=t.getLevel();var a=arena(t,192);var p=player(t);p.setPos(a.getX(),a.getY()+1,a.getZ());p.setHealth(10);
  var chest=new ItemStack(Items.DIAMOND_CHESTPLATE);Runes.write(chest,1,List.of("trizna_ember_ward"));p.setItemSlot(EquipmentSlot.CHEST,chest);
  var enemy=net.minecraft.world.entity.EntityType.HUSK.create(w);enemy.setNoAi(true);enemy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);enemy.setHealth(200);enemy.moveTo(a.getX()+2,a.getY()+1,a.getZ(),0,0);w.addFreshEntity(enemy);
  for(int stamp:new int[]{100,200,300,400,500,600,650,701})t.runAtTickTime(stamp,()->{
   p.setLastHurtByMob(enemy);var root=org.slavicmyths.rpg.PathData.data(p);root.putLong("TriznaAuraNext",0);
   NetherBossRunes.aura(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
   float expected=stamp==650?16:stamp==701?17:10+stamp/100;
   t.assertTrue(p.getHealth()==expected,"Sliding heal cap/cadence failed at "+stamp+" actual="+p.getHealth()+" expected="+expected);
  });
  t.runAtTickTime(750,()->{
   enemy.discard();var immune=net.minecraft.world.entity.EntityType.BLAZE.create(w);immune.setNoAi(true);immune.moveTo(a.getX()+2,a.getY()+1,a.getZ(),0,0);w.addFreshEntity(immune);p.setLastHurtByMob(immune);
   var root=org.slavicmyths.rpg.PathData.data(p);root.putLong("TriznaAuraNext",0);root.putLong("TriznaHealNext",0);float health=p.getHealth();
   NetherBossRunes.aura(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));t.assertTrue(p.getHealth()==health,"Fire-immune target healed owner");immune.discard();t.succeed();
  });
 }

 @GameTest(template="empty",timeoutTicks=200)
 public static void actualRuneAnvilInstallsAndReturnsBossRunes(GameTestHelper t){
  var p=player(t);var pos=p.blockPosition();t.getLevel().setBlock(pos,org.slavicmyths.registry.ModBlocks.RUNIC_ANVIL.get().defaultBlockState(),2);
  var menu=new org.slavicmyths.rpg.RpgMenu(7,p.getInventory(),pos,true);
  for(boolean morova:new boolean[]{true,false}){
   p.experienceLevel=100;var gear=new ItemStack(morova?Items.DIAMOND_SWORD:Items.DIAMOND_CHESTPLATE);
   Runes.write(gear,1,List.of());String rune=morova?"morova_lingering":"trizna_ember_ward";
   menu.input.setItem(0,gear);menu.input.setItem(1,new ItemStack(NetherBossItems.item("rune_"+rune)));
   t.assertTrue(menu.clickMenuButton(p,101),"Native rune anvil install mode rejected");
   t.assertTrue(menu.slots.get(1).mayPlace(menu.input.getItem(1)),"Native rune slot rejected boss rune");
   t.assertTrue(menu.clickMenuButton(p,1)&&Runes.has(gear,rune)&&menu.input.getItem(1).isEmpty(),"Native anvil failed installation/consumption");
   int experience=p.experienceLevel;
   menu.input.setItem(1,new ItemStack(NetherBossItems.item("rune_"+rune)));
   menu.clickMenuButton(p,1);t.assertTrue(p.experienceLevel==experience&&menu.input.getItem(1).getCount()==1&&Runes.list(gear).size()==1,"Duplicate install spent XP/material or duplicated rune");
   t.assertTrue(menu.clickMenuButton(p,102)&&menu.clickMenuButton(p,2)&&!Runes.has(gear,rune),"Native anvil failed removal");
   t.assertTrue(p.getInventory().countItem(NetherBossItems.item("rune_"+rune))==1,"Native anvil did not return exact rune");
  }
  t.getLevel().removeBlock(pos,false);t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=400)
 public static void everyAttackGeometryZonesWavesAndSummonCaps(GameTestHelper t){
  var w=t.getLevel();var at=arena(t,256);
  t.runAtTickTime(100,()->{
   for(boolean morova:new boolean[]{true,false}){
    var boss=(morova?org.slavicmyths.registry.ModEntities.MOROVA_DEVA:org.slavicmyths.registry.ModEntities.TRIZNA_PRINCE).get().create(w);
    boss.moveTo(at.getX(),at.getY()+1,at.getZ(),0,0);boss.setNoAi(true);w.addFreshEntity(boss);
    var target=net.minecraft.world.entity.EntityType.HUSK.create(w);target.setNoAi(true);target.setNoGravity(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.moveTo(at.getX()+3,at.getY()+1,at.getZ(),0,0);w.addFreshEntity(target);boss.setTarget(target);
    var combat=new NetherBossCombat(boss);var aim=new net.minecraft.world.phys.Vec3(1,0,0);
    int[] direct=morova?new int[]{1,3}:new int[]{2,8,9};float[] damage=morova?new float[]{8,12}:new float[]{16,22,14};
    for(int n=0;n<direct.length;n++){
     boss.moveTo(at.getX(),at.getY()+1,at.getZ(),0,0);target.setHealth(1000);target.invulnerableTime=0;
     combat.resolve(direct[n],aim);t.assertTrue(target.getHealth()==1000-damage[n],"Wrong contact damage action="+direct[n]+" hp="+target.getHealth());
    }
    boss.moveTo(at.getX(),at.getY()+1,at.getZ(),0,0);target.setHealth(1000);target.invulnerableTime=0;
    if(morova)t.assertTrue(target.hasEffect(org.slavicmyths.registry.ModEffects.MOROVA_PLAGUE),"Breath omitted plague");
    combat.resolve(morova?4:11,aim);
    for(int tick=1;tick<=20;tick++){boss.tickCount=tick;target.tick();combat.tick();}
    t.assertTrue(target.getHealth()==(morova?997:996),"Overlapping zones stacked or missed: "+target.getHealth());combat.clear();
    target.setHealth(1000);target.invulnerableTime=0;combat.resolve(morova?7:12,aim);
    for(int tick=1;tick<=65;tick++){boss.tickCount=tick;target.tick();combat.tick();}
    t.assertTrue(target.getHealth()==(morova?970:982),"Waves failed exact crossing/one hit per wave: "+target.getHealth());combat.clear();
    combat.resolve(morova?5:10,aim);combat.resolve(morova?5:10,aim);combat.resolve(morova?5:10,aim);
    var minions=w.getEntitiesOfClass(NetherRetainer.class,boss.getBoundingBox().inflate(10),r->boss.getUUID().equals(r.owner()));
    t.assertTrue(minions.size()>=2&&minions.size()<=4,"Summon cap failed: "+minions.size());
    for(var minion:minions)t.assertTrue(minion.getMaxHealth()==(morova?30:50)&&minion.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)==(morova?5:7),"Wrong minion attributes");
    if(morova){combat.resolve(6,aim);t.assertTrue(boss.hasCopies(),"Copies animation not activated");t.assertTrue(w.getEntitiesOfClass(NetherSummonBoss.class,boss.getBoundingBox().inflate(10),b->b!=boss).isEmpty(),"Copies created loot-bearing bosses");}
    combat.clear();t.assertTrue(w.getEntitiesOfClass(NetherRetainer.class,boss.getBoundingBox().inflate(10),r->boss.getUUID().equals(r.owner())).isEmpty(),"Encounter cleanup leaked minions");boss.discard();target.discard();
   }t.succeed();
  });
 }

}
