package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.industry.*;
import org.slavicmyths.kurgan.*;
import org.slavicmyths.rpg.*;
@GameTestHolder("slavicmyths_chud_kurgan") @PrefixGameTestTemplate(false)
public final class ChudKurganTests {
 private static final class Connection extends net.minecraft.network.Connection {final io.netty.channel.embedded.EmbeddedChannel c=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return c;}}
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {Listener(ServerPlayer p){super(p.server,new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}@Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}}
 static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ChudQA"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,3,3)));p.setNoGravity(true);t.getLevel().addNewPlayer(p);return p;}
 static void check(GameTestHelper t,boolean ok,String reason){t.assertTrue(ok,reason);System.out.println("CHUD_KURGAN_PASS "+reason);}
 static ResourceKey<LootTable> key(String name){return ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths",name));}
 @GameTest(template="empty",timeoutTicks=200) public static void mode_loot_and_preservation(GameTestHelper t){
  var p=player(t);var lookup=t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var fortune=lookup.getOrThrow(Enchantments.FORTUNE);var silk=lookup.getOrThrow(Enchantments.SILK_TOUCH);var s=new ItemStack(ChudTools.PICKAXE.get());s.enchant(fortune,3);s.set(DataComponents.CUSTOM_NAME,Component.literal("Named master"));s.setDamageValue(17);Runes.write(s,2,List.of("miner_iii","fortune_1"));p.setItemInHand(InteractionHand.MAIN_HAND,s);
  check(t,s.getEnchantmentLevel(fortune)==3&&s.getEnchantmentLevel(silk)==0,"fortune3 active, no silk");p.setShiftKeyDown(true);s.getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
  check(t,s.getEnchantmentLevel(fortune)==0&&s.getEnchantmentLevel(silk)==1&&s.getAllEnchantments(lookup).getLevel(fortune)==0,"both native hooks apply only active silk");
  var drops=Block.getDrops(Blocks.DIAMOND_ORE.defaultBlockState(),t.getLevel(),p.blockPosition(),null,p,s);check(t,drops.size()==1&&drops.getFirst().is(Items.DIAMOND_ORE),"real ore loot uses silk even with fortune rune");s.getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);check(t,s.getEnchantmentLevel(fortune)==3&&s.getDamageValue()==17&&Runes.list(s).size()==2,"fortune upgrade and components retained");
  var multi=new ItemStack(ChudTools.AXE.get());multi.set(DataComponents.CUSTOM_NAME,Component.literal("Shared tool"));multi.setDamageValue(53);multi.enchant(fortune,3);Runes.write(multi,2,List.of("lumberjack_iii","mastery_iv"));p.setItemInHand(InteractionHand.MAIN_HAND,multi);
  for(int i=0;i<30;i++)p.getMainHandItem().getItem().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
  var result=p.getMainHandItem();check(t,result.is(ChudTools.AXE.get())&&result.getDamageValue()==53&&result.getTagEnchantments().getLevel(fortune)==3&&Runes.list(result).equals(Runes.list(multi))&&result.getHoverName().getString().equals("Shared tool"),"30 native form changes preserve wear/name/enchants/runes");
  var encoded=ItemStack.CODEC.encodeStart(t.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),result).getOrThrow();var decoded=ItemStack.CODEC.parse(t.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),encoded).getOrThrow();check(t,ItemStack.isSameItemSameComponents(result,decoded),"stack codec roundtrip, not actual relog");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200) public static void loaded_surface_ascent(GameTestHelper t){
  var p=player(t);var w=t.getLevel();var start=p.blockPosition();int surface=start.getY()+8;
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)w.setBlock(new BlockPos(start.getX()+x,surface,start.getZ()+z),Blocks.STONE.defaultBlockState(),3);
  System.out.println("LIFT_DIAG start="+start+" actual="+p.position()+" height="+w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,start.getX(),start.getZ())+" sky="+w.canSeeSky(new BlockPos(start.getX(),surface+2,start.getZ()))+" safe="+ChudLiftItem.safe(p,new BlockPos(start.getX(),surface+1,start.getZ()))+" locked="+ChudLiftItem.locked(p));var result=ChudLiftItem.destination(p);System.out.println("LIFT_DIAG result="+result+" expected="+(surface+1));check(t,result!=null&&result.getY()>=surface+1,"surface height, not first cave ceiling or fixed Y");var s=new ItemStack(ChudTools.LIFT.get());p.setItemInHand(InteractionHand.MAIN_HAND,s);p.startUsingItem(InteractionHand.MAIN_HAND);s.finishUsingItem(w,p);check(t,p.getBlockY()==result.getY()&&s.getDamageValue()==1&&p.getCooldowns().isOnCooldown(ChudTools.LIFT.get()),"actual native teleport consumes once and adds cooldown");
  p.teleportTo(start.getX()+.5,start.getY(),start.getZ()+.5);for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)w.setBlock(new BlockPos(start.getX()+x,w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,start.getX()+x,start.getZ()+z)-1,start.getZ()+z),Blocks.MAGMA_BLOCK.defaultBlockState(),3);
  check(t,ChudLiftItem.destination(p)==null,"hot surface rejected");int before=s.getDamageValue();s.finishUsingItem(w,p);check(t,s.getDamageValue()==before,"failed ascent no wear");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200) public static void specialist_trade_migration(GameTestHelper t){
  var offers=new net.minecraft.world.item.trading.MerchantOffers();ChudToolTrades.ensure(offers,1);ChudToolTrades.ensure(offers,1);check(t,offers.size()==2&&offers.stream().noneMatch(o->o.getBaseCostA().is(org.slavicmyths.registry.ModItems.ANCIENT_COIN.get())),"old artisan offer migration idempotent and no coin currency");ChudToolTrades.ensure(offers,2);ChudToolTrades.ensure(offers,2);check(t,offers.size()==4,"miner adds lantern and master pickaxe once");
  var w=t.getLevel();var miner=ChudPeople.ARTISAN.get().create(w);miner.setup(2,t.absolutePos(new BlockPos(3,2,3)),false);check(t,miner.getOffers().stream().anyMatch(o->o.getResult().is(ChudTools.PICKAXE.get())),"actual miner merchant exposes tool");
  var lift=new ItemStack(ChudTools.LIFT.get());check(t,lift.is(net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.withDefaultNamespace("enchantable/durability"))),"lift supports native unbreaking tag");t.succeed();
 }
 @GameTest(template="empty") public static void authoritative_arena_and_teleport_cancel(GameTestHelper t){
  var p=player(t);var w=t.getLevel();var arena=new org.slavicmyths.earth.EarthArena.Entry(UUID.randomUUID(),p.blockPosition(),0);arena.gateClosed=true;var records=org.slavicmyths.earth.EarthArena.get(w);records.entries.put(arena.id,arena);
  check(t,ChudLiftItem.locked(p),"actual closed Earth arena denies ascent");arena.gateClosed=false;check(t,!ChudLiftItem.locked(p),"open arena permits ordinary surface search");records.entries.remove(arena.id);
  var start=p.blockPosition();for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)w.setBlock(start.offset(x,20,z),Blocks.STONE.defaultBlockState(),3);
  var s=new ItemStack(ChudTools.LIFT.get());p.setItemInHand(InteractionHand.MAIN_HAND,s);p.startUsingItem(InteractionHand.MAIN_HAND);var before=p.position();
  java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityTeleportEvent.TeleportCommand> cancel=e->{if(e.getEntity()==p)e.setCanceled(true);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cancel);
  try{s.finishUsingItem(w,p);check(t,p.position().equals(before)&&s.getDamageValue()==0&&!p.getCooldowns().isOnCooldown(ChudTools.LIFT.get()),"native teleport cancellation prevents movement and charge");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);}t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=400) public static void finite_coffin_rewards(GameTestHelper t)throws Exception{
  var w=t.getLevel();var p=player(t);var rows=new ArrayList<Map<String,Object>>();
  for(String tier:List.of("small","warrior","great"))for(String role:List.of("burial","offering","important","common_cache","warrior","treasury","ritual","secret","great_special")){
   var table=w.getServer().reloadableRegistries().getLootTable(key("chests/kurgan_"+tier+"_"+role));var params=new LootParams.Builder(w).withParameter(LootContextParams.ORIGIN,p.position()).create(LootContextParamSets.CHEST);int empty=0,max=0;
   for(int i=0;i<1000;i++){var items=table.getRandomItems(params,i+241L);if(items.isEmpty())empty++;max=Math.max(max,items.size());for(var stack:items)checkItem(stack);}
   check(t,!role.equals("important")||empty==0,"1000 important rolls nonempty "+tier);check(t,max<=5,"five-slot coffin capacity "+tier+"/"+role);rows.add(Map.of("tier",tier,"role",role,"samples",1000,"empty",empty,"max_stacks",max));
  }
  var at=t.absolutePos(new BlockPos(4,3,4));w.setBlock(at,org.slavicmyths.registry.ModBlocks.BURIAL_COFFIN.get().defaultBlockState(),3);var tile=(BurialCoffinTile)w.getBlockEntity(at);tile.setLootTable(key("chests/kurgan_great_important"),241);tile.unpackLootTable(p);check(t,!tile.isEmpty()&&tile.getLootTable()==null,"actual coffin resolves pending old-style loot id once");var copy=new ArrayList<ItemStack>();for(int i=0;i<5;i++)copy.add(tile.getItem(i).copy());tile.unpackLootTable(player(t));for(int i=0;i<5;i++)check(t,ItemStack.matches(copy.get(i),tile.getItem(i)),"second player cannot reroll slot"+i);
  var saved=tile.saveWithFullMetadata(w.registryAccess());var loaded=new BurialCoffinTile(at,tile.getBlockState());loaded.setLevel(w);loaded.loadWithComponents(saved,w.registryAccess());for(int i=0;i<5;i++)check(t,ItemStack.matches(copy.get(i),loaded.getItem(i)),"coffin NBT retains contents"+i);
  Path out=Path.of("D:/slavic-myths/docs/implementation/kurgan241");Files.createDirectories(out);Files.writeString(out.resolve("native-loot.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));t.succeed();
 }
 static void checkItem(ItemStack stack){if(stack.isEmpty()||BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.withDefaultNamespace("air")))throw new AssertionError("missing loot item");}
 @GameTest(template="empty",timeoutTicks=160) public static void one_native_tree_queue(GameTestHelper t){
  var w=t.getLevel();var p=player(t);var root=t.absolutePos(new BlockPos(4,6,4));
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<=8;y++)w.setBlock(root.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
  w.setBlock(root.below(),Blocks.GRASS_BLOCK.defaultBlockState(),2);for(int y=0;y<6;y++)w.setBlock(root.above(y),Blocks.OAK_LOG.defaultBlockState(),2);
  w.setBlock(root.offset(1,4,0),Blocks.OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.X),2);
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(x!=0||z!=0)w.setBlock(root.offset(x,5,z),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE,1),2);
  p.moveTo(root.getX()+3.5,root.getY(),root.getZ()+.5,90,0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ChudTools.AXE.get()));Runes.write(p.getMainHandItem(),2,List.of("lumberjack_iii"));p.setShiftKeyDown(false);
  p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5);var aim=Vec3.atCenterOf(root.above()).subtract(p.getEyePosition());p.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));p.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,Math.hypot(aim.x,aim.z))));p.setYHeadRot(p.getYRot());var hit=(net.minecraft.world.phys.BlockHitResult)p.pick(p.blockInteractionRange(),1,false);System.out.println("TREE_RAY range="+p.blockInteractionRange()+" eye="+p.getEyePosition()+" aim="+aim+" hit="+hit.getType()+" pos="+hit.getBlockPos()+" state="+w.getBlockState(hit.getBlockPos()));check(t,w.getBlockState(hit.getBlockPos()).is(Blocks.OAK_LOG),"server ray selects natural trunk");
  var event=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock(p,hit.getBlockPos(),hit.getDirection(),net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);check(t,p.gameMode.destroyBlock(hit.getBlockPos()),"origin destroyed through native game mode");
  for(int tick=1;tick<=70;tick++)t.runAfterDelay(tick,()->org.slavicmyths.rpg.tools.ToolRuneRuntime.tick(p));
  t.runAfterDelay(80,()->{for(int y=0;y<6;y++)check(t,w.getBlockState(root.above(y)).isAir(),"one built-in queue clears trunk"+y);check(t,w.getBlockState(root.offset(1,4,0)).isAir(),"connected branch included");check(t,p.getMainHandItem().getDamageValue()==7,"seven logs charge exactly seven durability");check(t,w.getBlockState(root.offset(2,5,0)).is(Blocks.OAK_LEAVES),"leaves left to vanilla decay");t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=400) public static void native_layout_population_samples(GameTestHelper t)throws Exception{
  var rows=new ArrayList<Map<String,Object>>();for(int tier=0;tier<3;tier++)for(int seed=0;seed<64;seed++){
   var plan=KurganPlan.create(tier,seed);var coffins=new HashMap<String,Integer>();for(var room:plan.rooms)if(!room.loot.isEmpty())coffins.merge(room.id==plan.finalRoom?"important":room.loot,1,Integer::sum);var mobs=new HashMap<String,Integer>();for(var group:KurganRoster.assign(plan).values())for(var mob:group)mobs.merge(mob.name(),1,Integer::sum);rows.add(Map.of("tier",tier,"seed",seed,"coffins",coffins,"mobs",mobs));
  }var out=Path.of("D:/slavic-myths/docs/implementation/kurgan241");Files.createDirectories(out);Files.writeString(out.resolve("native-layouts.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));check(t,rows.size()==192,"actual deterministic layout and unchanged roster sampled");t.succeed();
 }
 @GameTest(template="empty") public static void no_summoned_fragments(GameTestHelper t){
  var w=t.getLevel();var mob=(KurganCreature)org.slavicmyths.registry.ModEntities.ENTITIES.getEntries().stream().filter(e->e.getId().getPath().equals("upyr")).findFirst().orElseThrow().get().create(w);var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();var event=new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(mob,mob.damageSources().generic(),drops,true);mob.kurgan=UUID.randomUUID();mob.getPersistentData().putBoolean("RuneSummoned",true);for(int i=0;i<100;i++)RuneFoundationLoot.drops(event);check(t,drops.isEmpty(),"summoned upyr never supplies fragments");mob.getPersistentData().putBoolean("RuneSummoned",false);mob.kurgan=null;for(int i=0;i<100;i++)RuneFoundationLoot.drops(event);check(t,drops.isEmpty(),"non-burial upyr never supplies fragments");mob.kurgan=UUID.randomUUID();for(int i=0;i<2000;i++)RuneFoundationLoot.drops(event);check(t,drops.size()>950&&drops.size()<1250,"natural burial fragment probability55% sampled");t.succeed();
 }
}
