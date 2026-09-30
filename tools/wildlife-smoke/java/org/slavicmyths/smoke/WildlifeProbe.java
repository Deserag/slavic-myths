package org.slavicmyths.smoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.registry.ModEntities;
@Mod("slavicmyths_smoke")
public class WildlifeProbe {
 boolean loaded,ready;int ticks,serverTicks,lastStage=-1;volatile int stage=-1;ServerPlayerEntity player;WildlifeEntity encounter;java.util.Set<Integer> states=new java.util.HashSet<>();int shieldBefore;
 public WildlifeProbe(){MinecraftForge.EVENT_BUS.register(this);}
 @SubscribeEvent public void client(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getInstance();
  mc.options.pauseOnLostFocus=false;
  if(!loaded&&mc.screen instanceof MainMenuScreen){loaded=true;mc.loadLevel("SlavicMyths-Wildlife-065");}
  if(ready&&mc.player!=null&&mc.screen==null){
   if(stage!=lastStage){lastStage=stage;ticks=0;}
   if(++ticks==45)ScreenShotHelper.grab(mc.gameDirectory,"animal-"+stage+".png",mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget(),t->{});
  }
  if(serverTicks>=840&&serverTicks<845){mc.setScreen(null);mc.options.setCameraType(net.minecraft.client.settings.PointOfView.THIRD_PERSON_FRONT);}
  if(serverTicks==870)ScreenShotHelper.grab(mc.gameDirectory,"equipment.png",mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget(),t->{});
  if(serverTicks==900)top.theillusivec4.curios.common.network.NetworkHandler.INSTANCE.sendToServer(new top.theillusivec4.curios.common.network.client.CPacketOpenCurios());
  if(serverTicks==930)ScreenShotHelper.grab(mc.gameDirectory,"accessory-slots.png",mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget(),t->{});
  if(serverTicks==960)mc.player.closeContainer();
  if(serverTicks==1260)ScreenShotHelper.grab(mc.gameDirectory,"bear-stand.png",mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget(),t->{});
  if(serverTicks==1000)mc.options.setCameraType(net.minecraft.client.settings.PointOfView.FIRST_PERSON);
  if(serverTicks>1800)mc.stop();
 }
 @SubscribeEvent public void server(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||player==null)return;
  int next=Math.min(5,serverTicks++/100);double x=-7.5+next*3;
  if(stage!=next){player.teleportTo(player.getLevel(),x+3,151,3,135,15);stage=next;}
  if(serverTicks==30)equipmentStart();
  if(serverTicks==140)equipmentCheck();
  if(serverTicks==700){
   slot("head",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get()));
  }
  if(serverTicks==740){
   check(player.hasEffect(net.minecraft.potion.Effects.INVISIBILITY),"cap invisibility after delay");slot("head",0,net.minecraft.item.ItemStack.EMPTY);
  }
  if(serverTicks==760){
   check(!player.hasEffect(net.minecraft.potion.Effects.INVISIBILITY),"cap removed: invisibility expires");
   slot("charm",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.RETRIBUTION_CHARM.get()));
   player.setGameMode(GameType.SURVIVAL);player.invulnerableTime=0;player.hurt(net.minecraft.util.DamageSource.GENERIC,1000);
   check(player.isAlive()&&player.getHealth()==4,"lethal damage prevented");
   check(org.slavicmyths.item.FolkAccessoryItem.equipped(player,org.slavicmyths.registry.ModItems.RETRIBUTION_CHARM.get()).isEmpty(),"death charm consumed exactly once");player.setGameMode(GameType.CREATIVE);
  }
  if(serverTicks==800){
   slot("necklace",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.VELES_AMULET.get()));
   slot("belt",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.HUNTER_BELT.get()));
   player.getPersistentData().putBoolean("WildlifeEquipmentSaved",true);
   player.setItemSlot(net.minecraft.inventory.EquipmentSlotType.FEET,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.SEVEN_LEAGUE_BOOTS.get()));
   net.minecraft.item.Item[] items={org.slavicmyths.registry.ModItems.INVISIBILITY_CAP.get(),org.slavicmyths.registry.ModItems.VELES_AMULET.get(),org.slavicmyths.registry.ModItems.RETRIBUTION_CHARM.get(),org.slavicmyths.registry.ModItems.PERUN_RING.get(),org.slavicmyths.registry.ModItems.RESIN_RING.get(),org.slavicmyths.registry.ModItems.HUNTER_BELT.get(),org.slavicmyths.registry.ModItems.SEVEN_LEAGUE_BOOTS.get()};
   for(int i=0;i<items.length;i++)player.inventory.setItem(i,new net.minecraft.item.ItemStack(items[i]));
  }
  if(encounter!=null)states.add(encounter.motion());
  if(serverTicks==1060){
   player.setGameMode(GameType.SURVIVAL);player.setHealth(20);player.teleportTo(player.getLevel(),0,151,4,180,0);
   player.setItemInHand(net.minecraft.util.Hand.OFF_HAND,new net.minecraft.item.ItemStack(net.minecraft.item.Items.SHIELD));player.startUsingItem(net.minecraft.util.Hand.OFF_HAND);
   shieldBefore=player.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD));
   encounter=ModEntities.BOAR.get().create(player.getLevel());encounter.moveTo(0,151,-3,0,0);player.getLevel().addFreshEntity(encounter);encounter.hurt(net.minecraft.util.DamageSource.playerAttack(player),.1F);states.clear();
  }
  if(serverTicks==1190){
   check(states.contains(WildlifeEntity.WINDUP)&&states.contains(WildlifeEntity.CHARGE),"boar actual AI telegraph and charge");
   check(player.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DAMAGE_BLOCKED_BY_SHIELD))>shieldBefore,"boar charge blocked by actual shield");
   encounter.remove();encounter=null;player.stopUsingItem();
  }
  if(serverTicks==1230){
   player.teleportTo(player.getLevel(),0,151,4,180,0);slot("necklace",0,net.minecraft.item.ItemStack.EMPTY);
   encounter=ModEntities.BROWN_BEAR.get().create(player.getLevel());encounter.moveTo(0,151,1.5,0,0);player.getLevel().addFreshEntity(encounter);states.clear();
  }
  if(serverTicks==1340){check(states.contains(WildlifeEntity.STAND)&&states.contains(WildlifeEntity.ATTACK),"bear territorial stand and attack");encounter.remove();encounter=null;player.setHealth(20);}
  if(serverTicks==1380){
   player.teleportTo(player.getLevel(),0,151,4,180,0);encounter=ModEntities.DOE.get().create(player.getLevel());encounter.moveTo(0,151,0,0,0);player.getLevel().addFreshEntity(encounter);states.clear();
  }
  if(serverTicks==1490){check(states.contains(WildlifeEntity.ALERT)&&states.contains(WildlifeEntity.FLEE),"doe alert and flee");encounter.remove();encounter=null;player.setGameMode(GameType.CREATIVE);}
  if(serverTicks==1620){
   player.getLevel().getGameRules().getRule(net.minecraft.world.GameRules.RULE_KEEPINVENTORY).set(true,player.server);
   player.setGameMode(GameType.SURVIVAL);player.invulnerableTime=0;player.hurt(net.minecraft.util.DamageSource.OUT_OF_WORLD,1000);
  }
  if(serverTicks==1630)player=player.server.getPlayerList().respawn(player,false);
  if(serverTicks==1640){check(!org.slavicmyths.item.FolkAccessoryItem.equipped(player,org.slavicmyths.registry.ModItems.PERUN_RING.get()).isEmpty(),"keepInventory retains ring after actual death and respawn");player.teleportTo(player.getLevel(),0,151,4,180,0);}
  if(serverTicks==1700){player.getLevel().getGameRules().getRule(net.minecraft.world.GameRules.RULE_KEEPINVENTORY).set(false,player.server);player.invulnerableTime=0;player.hurt(net.minecraft.util.DamageSource.OUT_OF_WORLD,1000);}
  if(serverTicks==1710)player=player.server.getPlayerList().respawn(player,false);
  if(serverTicks==1720){
   check(org.slavicmyths.item.FolkAccessoryItem.equipped(player,org.slavicmyths.registry.ModItems.PERUN_RING.get()).isEmpty(),"normal death removes ring from accessory slot");
   player.setGameMode(GameType.CREATIVE);player.teleportTo(player.getLevel(),0,151,4,180,0);slot("ring",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.PERUN_RING.get()));player.getPersistentData().putBoolean("WildlifeEquipmentSaved",true);
  }
 }
 private void check(boolean ok,String name){if(!ok)throw new IllegalStateException("WILDLIFE FAIL: "+name);org.apache.logging.log4j.LogManager.getLogger().info("WILDLIFE PASS: "+name);}
 private void slot(String name,int index,net.minecraft.item.ItemStack stack){
  top.theillusivec4.curios.api.CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(h->{check(h.getCurios().containsKey(name),"slot "+name);h.getCurios().get(name).getStacks().setStackInSlot(index,stack);});
 }
 private void equipmentStart(){
  if(player.getPersistentData().getBoolean("WildlifeEquipmentSaved"))check(!org.slavicmyths.item.FolkAccessoryItem.equipped(player,org.slavicmyths.registry.ModItems.PERUN_RING.get()).isEmpty(),"ring survived real client restart");
  slot("ring",0,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.PERUN_RING.get()));
  slot("ring",1,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.RESIN_RING.get()));
  player.getLevel().getGameRules().getRule(net.minecraft.world.GameRules.RULE_NATURAL_REGENERATION).set(false,player.server);
  player.setHealth(10);player.getFoodData().setFoodLevel(20);
 }
 private void equipmentCheck(){
  check(player.getHealth()>10,"resin ring heals while equipped");
  check(player.getAdvancements().getOrStartProgress(player.server.getAdvancements().getAdvancement(new ResourceLocation("slavicmyths","two_rings"))).isDone(),"two distinct rings advancement");
  net.minecraft.entity.passive.SheepEntity sheep=net.minecraft.entity.EntityType.SHEEP.create(player.getLevel());sheep.moveTo(0,151,7);player.getLevel().addFreshEntity(sheep);
  float health=sheep.getHealth();sheep.hurt(net.minecraft.util.DamageSource.playerAttack(player),2);check(health-sheep.getHealth()==4,"Perun +2 actual melee damage");
  sheep.invulnerableTime=0;health=sheep.getHealth();sheep.hurt(net.minecraft.util.DamageSource.playerAttack(player),2);check(health-sheep.getHealth()==2,"Perun cooldown prevents repeated bonus");sheep.remove();
 }
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event){
  if(!(event.getPlayer() instanceof ServerPlayerEntity))return;
  ServerPlayerEntity p=(ServerPlayerEntity)event.getPlayer();ServerWorld w=p.getLevel();
  player=p;p.inventory.clearContent();
  for(int x=-2;x<=1;x++)for(int z=-1;z<=1;z++)w.getChunk(x,z);
  w.getServer().getCommands().performCommand(w.getServer().createCommandSourceStack(),"fill -20 150 -12 20 150 12 minecraft:grass_block");
  w.getServer().getCommands().performCommand(w.getServer().createCommandSourceStack(),"fill -20 151 -12 20 160 12 minecraft:air");
  for(WildlifeEntity old:w.getEntitiesOfClass(WildlifeEntity.class,new net.minecraft.util.math.AxisAlignedBB(-20,150,-12,20,160,12)))old.remove();
  for(net.minecraft.entity.MobEntity old:w.getEntitiesOfClass(net.minecraft.entity.MobEntity.class,new net.minecraft.util.math.AxisAlignedBB(-20,150,-12,20,160,12)))old.remove();
  w.setDayTime(6000);p.setGameMode(GameType.CREATIVE);
  net.minecraft.entity.EntityType<?>[] types={ModEntities.BROWN_BEAR.get(),ModEntities.BEAR_CUB.get(),ModEntities.FOREST_WOLF.get(),ModEntities.BOAR.get(),ModEntities.STAG.get(),ModEntities.DOE.get()};
  for(int i=0;i<types.length;i++){
   WildlifeEntity a=(WildlifeEntity)types[i].create(w);a.moveTo(-7.5+i*3,151,0,35,0);a.setNoAi(true);a.setPersistenceRequired();w.addFreshEntity(a);
  }
  p.teleportTo(w,0,152,12,180,8);ready=true;
  org.apache.logging.log4j.LogManager.getLogger().info("WILDLIFE SMOKE: six entity factories and attributes loaded; scene ready");
 }
}
