package org.slavicmyths.rpg.tools;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.common.util.TriState;
import org.slavicmyths.rpg.Runes;

/** Confirmed origin -> bounded per-player queue -> normal protected native action for each extra. */
@EventBusSubscriber(modid="slavicmyths")
public final class ToolRuneRuntime {
 private record Intent(BlockPos pos,Direction face,ItemStack stack,ResourceKey<Level> dimension,long time){}
 private record Target(BlockPos pos,BlockState expected){}
 private record Removal(BlockPos pos,boolean removed){}
 private enum Mode {MINER,EXCAVATOR,LUMBERJACK,TILL,HARVEST}
 public static final class BreakFrame {
  public final BlockPos origin;public boolean removed;
  private final ServerLevel world;private final GameType gameMode;private final int slot;private final BlockState before;private final ItemStack stack;private final String family;private final Direction face;private final int tier;
  private BreakFrame(ServerPlayer owner,BlockPos pos,BlockState before,ItemStack stack,String family,Direction face,int tier){this.world=owner.serverLevel();gameMode=owner.gameMode.getGameModeForPlayer();slot=owner.getInventory().selected;origin=pos.immutable();this.before=before;this.stack=stack;this.family=family;this.face=face;this.tier=tier;}
 }
 public record UseFrame(ServerLevel world,GameType gameMode,int slot,BlockPos origin,BlockState before,BlockState tilled,ItemStack stack,int tier){}
 private static final Map<UUID,Intent> INTENTS=new HashMap<>();
 private static final Map<UUID,Job> JOBS=new HashMap<>();
 private static final Map<UUID,Removal> REMOVED=new HashMap<>();
 private static final Set<UUID> GUARD=new HashSet<>();
 private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
 private static boolean loaded(ServerLevel w,BlockPos p){return p.getY()>=w.getMinBuildHeight()&&p.getY()<w.getMaxBuildHeight()&&w.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4)!=null;}
 private static boolean access(ServerPlayer p,BlockPos pos){var w=p.serverLevel();var mode=p.gameMode.getGameModeForPlayer();return p.isAlive()&&!p.isSpectator()&&(mode==GameType.SURVIVAL||mode==GameType.CREATIVE)&&p.mayBuild()&&loaded(w,pos)&&w.getWorldBorder().isWithinBounds(pos)&&w.mayInteract(p,pos)&&!p.blockActionRestricted(w,pos,mode);}
 private static boolean safe(ServerLevel w,BlockPos p,BlockState s){return loaded(w,p)&&!s.isAir()&&!s.hasBlockEntity()&&s.getFluidState().isEmpty()&&s.getDestroySpeed(w,p)>=0;}
 private static String family(ItemStack s){if(s.getItem() instanceof PickaxeItem)return "miner";if(s.getItem() instanceof ShovelItem)return "excavator";if(s.getItem() instanceof AxeItem)return "lumberjack";if(s.getItem() instanceof HoeItem)return "farmer";return "";}
 private static boolean dig(ServerPlayer p,BlockPos pos,BlockState s,Mode mode){if(!access(p,pos)||!safe(p.serverLevel(),pos,s))return false;var tool=p.getMainHandItem();return switch(mode){
  case MINER->tool.getItem() instanceof PickaxeItem&&s.is(BlockTags.MINEABLE_WITH_PICKAXE)&&tool.isCorrectToolForDrops(s);
  case EXCAVATOR->tool.getItem() instanceof ShovelItem&&s.is(BlockTags.MINEABLE_WITH_SHOVEL)&&tool.isCorrectToolForDrops(s);
  case LUMBERJACK->tool.getItem() instanceof AxeItem&&s.is(BlockTags.LOGS)&&s.is(BlockTags.MINEABLE_WITH_AXE)&&tool.isCorrectToolForDrops(s)&&!ToolRuneRules.logFamily(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString()).isEmpty();
  default->false;
 };}
 @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true) public static void click(PlayerInteractEvent.LeftClickBlock e){
  if(!(e.getEntity() instanceof ServerPlayer p)||GUARD.contains(p.getUUID()))return;String action=e.getAction().name();
  if(action.equals("ABORT")){INTENTS.remove(p.getUUID());return;}
  if(!action.equals("START")&&!action.equals("STOP"))return;
  if(e.isCanceled()){clear(p);return;}
  if(action.equals("START")){INTENTS.remove(p.getUUID());JOBS.remove(p.getUUID());}
  String f=family(p.getMainHandItem());if(!p.isShiftKeyDown()||f.equals("farmer")||ToolRuneRules.tier(Runes.list(p.getMainHandItem()),f)==0||e.getUseItem()==TriState.FALSE||!access(p,e.getPos()))return;
  // The face is recovered from the server ray, and must agree with the accepted packet's target/face.
  var hit=p.pick(p.blockInteractionRange(),1,false);
  if(!(hit instanceof BlockHitResult block)||hit.getType()!=HitResult.Type.BLOCK||!block.getBlockPos().equals(e.getPos())||block.getDirection()!=e.getFace())return;
  INTENTS.put(p.getUUID(),new Intent(e.getPos().immutable(),block.getDirection(),p.getMainHandItem(),p.level().dimension(),now(p)));
 }
 public static BreakFrame beginBreak(ServerPlayer p,BlockPos pos){
  var empty=new BreakFrame(p,pos,null,ItemStack.EMPTY,"",null,0);if(GUARD.contains(p.getUUID())||!p.isShiftKeyDown())return empty;
  var intent=INTENTS.get(p.getUUID());var stack=p.getMainHandItem();String f=family(stack);int tier=ToolRuneRules.tier(Runes.list(stack),f);
  if(!access(p,pos)||intent==null||!intent.pos.equals(pos)||intent.stack!=stack||!intent.dimension.equals(p.level().dimension())||now(p)-intent.time>1200||tier==0||f.equals("farmer"))return empty;
  var mode=Mode.valueOf(f.toUpperCase(Locale.ROOT));var state=p.serverLevel().getBlockState(pos);if(!dig(p,pos,state,mode))return empty;
  return new BreakFrame(p,pos,state,stack,f,intent.face,tier);
 }
 public static void finishBreak(ServerPlayer p,BreakFrame frame,boolean returned){
  if(GUARD.contains(p.getUUID())){REMOVED.put(p.getUUID(),new Removal(frame.origin,returned&&frame.removed));return;}
  INTENTS.remove(p.getUUID());
  if(p.gameMode.getGameModeForPlayer()!=frame.gameMode||p.getInventory().selected!=frame.slot||p.serverLevel()!=frame.world||frame.family.isEmpty()||!returned||!frame.removed||p.getMainHandItem()!=frame.stack||frame.stack.isEmpty())return;
  var mode=Mode.valueOf(frame.family.toUpperCase(Locale.ROOT));var job=new Job(p,frame.origin,frame.stack,frame.tier,mode);
  if(mode==Mode.LUMBERJACK){
   if(ToolLogOrigins.get(p.serverLevel()).blocked(frame.origin))return;
   String f=ToolRuneRules.logFamily(BuiltInRegistries.BLOCK.getKey(frame.before.getBlock()).toString());
   job.tree=new ToolRuneTrees.Job(pos->job.node(pos,frame.before),frame.origin,f,frame.tier);
  }else job.add(ToolRuneRules.plane(frame.origin,frame.face,frame.tier));
  JOBS.put(p.getUUID(),job);
 }
 public static UseFrame beforeUse(UseOnContext context){
  if(!(context.getPlayer() instanceof ServerPlayer p)||context.getLevel()!=p.serverLevel()||context.getHand()!=InteractionHand.MAIN_HAND||GUARD.contains(p.getUUID())||!p.isShiftKeyDown()||!(context.getItemInHand().getItem() instanceof HoeItem))return null;
  int tier=ToolRuneRules.tier(Runes.list(context.getItemInHand()),"farmer");if(tier==0||!access(p,context.getClickedPos()))return null;
  var pos=context.getClickedPos();var before=p.serverLevel().getBlockState(pos);
  BlockState till=context.getClickedFace()!=Direction.DOWN&&loaded(p.serverLevel(),pos.above())&&p.serverLevel().getBlockState(pos.above()).isAir()&&safe(p.serverLevel(),pos,before)?before.getToolModifiedState(context,net.neoforged.neoforge.common.ItemAbilities.HOE_TILL,true):null;
  return new UseFrame(p.serverLevel(),p.gameMode.getGameModeForPlayer(),p.getInventory().selected,pos.immutable(),before,till,context.getItemInHand(),tier);
 }
 private static String cropGroup(BlockState s){var block=s.getBlock();if(block instanceof CropBlock crop&&(BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft")||block instanceof org.slavicmyths.farming.FarmingCrop)&&crop.isMaxAge(s))return "farmland";if(block instanceof NetherWartBlock&&s.getValue(NetherWartBlock.AGE)==3)return "nether_wart";return "";}
 private static boolean crop(ServerPlayer p,BlockPos pos,BlockState s,String group){return !group.isEmpty()&&cropGroup(s).equals(group)&&access(p,pos)&&safe(p.serverLevel(),pos,s)&&s.getDestroySpeed(p.serverLevel(),pos)==0&&p.getMainHandItem().getItem() instanceof HoeItem;}
 public static InteractionResult afterUse(UseOnContext context,UseFrame frame,InteractionResult result){
  if(frame==null||!(context.getPlayer() instanceof ServerPlayer p)||p.serverLevel()!=frame.world||p.gameMode.getGameModeForPlayer()!=frame.gameMode||p.getInventory().selected!=frame.slot||context.getLevel()!=frame.world||GUARD.contains(p.getUUID())||p.serverLevel().captureBlockSnapshots||p.serverLevel().restoringBlockSnapshots||p.getMainHandItem()!=frame.stack||frame.stack.isEmpty())return result;
  if(!loaded(frame.world,frame.origin))return result;
  var after=frame.world.getBlockState(frame.origin);
  if(result.consumesAction()&&frame.tilled!=null&&after.equals(frame.tilled)&&!after.equals(frame.before)&&!frame.before.hasBlockEntity()&&frame.before.getFluidState().isEmpty()){
   var job=new Job(p,frame.origin,frame.stack,frame.tier,Mode.TILL);job.add(ToolRuneRules.field(frame.origin,frame.tier));JOBS.put(p.getUUID(),job);return result;
  }
  String group=cropGroup(frame.before);
  if(result!=InteractionResult.PASS||!after.equals(frame.before)||!crop(p,frame.origin,frame.before,group))return result;
  if(!harvest(p,frame.origin,frame.before))return result;
  p.swing(InteractionHand.MAIN_HAND,true);
  if(!frame.stack.isEmpty()&&p.getMainHandItem()==frame.stack){var job=new Job(p,frame.origin,frame.stack,frame.tier,Mode.HARVEST);job.cropGroup=group;job.add(ToolRuneRules.field(frame.origin,frame.tier));JOBS.put(p.getUUID(),job);}
  return InteractionResult.CONSUME;
 }
 private static boolean destroy(ServerPlayer p,BlockPos pos){
  var id=p.getUUID();if(!GUARD.add(id))return false;REMOVED.remove(id);
  try{boolean returned=p.gameMode.destroyBlock(pos);var r=REMOVED.get(id);return returned&&r!=null&&r.pos.equals(pos)&&r.removed;}finally{REMOVED.remove(id);GUARD.remove(id);}
 }
 private static boolean harvest(ServerPlayer p,BlockPos pos,BlockState s){
  // Supported cultures have zero destroy hardness: native Hoe.mineBlock charges zero, this explicit use charges one.
  var tool=p.getMainHandItem();var item=tool.getItem();if(!destroy(p,pos))return false;
  if(!p.isCreative())tool.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
  p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(item));return true;
 }
 private static final class Job {
  final ServerPlayer player;final ServerLevel world;final BlockPos origin;final ItemStack stack,snapshot;final int tier,slot;final Mode mode;final GameType gameMode;final long expires;
  final ArrayDeque<Target> targets=new ArrayDeque<>();final Map<BlockPos,BlockState> states=new HashMap<>();
  ToolRuneTrees.Job tree;String cropGroup="";
  Job(ServerPlayer p,BlockPos origin,ItemStack stack,int tier,Mode mode){player=p;world=p.serverLevel();this.origin=origin.immutable();this.stack=stack;snapshot=stack.copy();this.tier=tier;this.mode=mode;slot=p.getInventory().selected;gameMode=p.gameMode.getGameModeForPlayer();expires=now(p)+ToolRuneRules.TTL;}
  boolean valid(){if(player.getInventory().selected!=slot||player.gameMode.getGameModeForPlayer()!=gameMode||!player.mayBuild()||!player.isAlive()||player.isSpectator()||player.serverLevel()!=world||player.getMainHandItem()!=stack||stack.isEmpty()||now(player)>expires||player.distanceToSqr(Vec3.atCenterOf(origin))>64)return false;var check=snapshot.copy();check.setDamageValue(stack.getDamageValue());return ItemStack.isSameItemSameComponents(check,stack);}
  void add(List<BlockPos> positions){for(var pos:positions)if(!pos.equals(origin)&&loaded(world,pos)){var s=world.getBlockState(pos);targets.add(new Target(pos.immutable(),s));}}
  ToolRuneTrees.Node node(BlockPos pos,BlockState original){
   if(!loaded(world,pos))return ToolRuneTrees.Node.UNKNOWN;
   var s=pos.equals(origin)?original:world.getBlockState(pos);states.put(pos.immutable(),s);
   if(s.isAir())return ToolRuneTrees.Node.AIR;
   if(s.hasBlockEntity()||!s.getFluidState().isEmpty())return new ToolRuneTrees.Node(ToolRuneTrees.Kind.OTHER,"",false,false,false);
   if(s.is(BlockTags.LOGS))return new ToolRuneTrees.Node(ToolRuneTrees.Kind.LOG,ToolRuneRules.logFamily(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString()),s.hasProperty(BlockStateProperties.AXIS)&&s.getValue(BlockStateProperties.AXIS)==Direction.Axis.Y,ToolLogOrigins.get(world).blocked(pos),false);
   if(s.getBlock() instanceof LeavesBlock)return new ToolRuneTrees.Node(ToolRuneTrees.Kind.LEAF,ToolRuneRules.leafFamily(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString()),false,false,s.getValue(LeavesBlock.PERSISTENT));
   if(s.is(BlockTags.REPLACEABLE_BY_TREES))return ToolRuneTrees.Node.AIR;
   if(s.is(BlockTags.DIRT)||s.is(Blocks.MUD))return new ToolRuneTrees.Node(ToolRuneTrees.Kind.GROUND,"",false,false,false);
   return new ToolRuneTrees.Node(ToolRuneTrees.Kind.OTHER,"",false,false,false);
  }
  boolean tick(){
   if(!valid())return true;
   if(tree!=null){if(!tree.step(ToolRuneRules.READS_PER_TICK))return false;for(var pos:tree.result()){var s=states.get(pos);if(s!=null)targets.add(new Target(pos,s));}tree=null;states.clear();}
   for(int n=0;n<ToolRuneRules.BREAKS_PER_TICK&&!targets.isEmpty();n++){
    if(!valid())return true;var target=targets.removeFirst();var pos=target.pos;
    if(!access(player,pos))continue;var s=world.getBlockState(pos);if(!s.equals(target.expected)||!safe(world,pos,s))continue;
    if(mode==Mode.TILL){
     if(!loaded(world,pos.above())||!world.getBlockState(pos.above()).isAir())continue;
     var hit=new BlockHitResult(Vec3.atCenterOf(pos).add(0,.5,0),Direction.UP,pos,false);var context=new UseOnContext(player,InteractionHand.MAIN_HAND,hit);
     if(s.getToolModifiedState(context,net.neoforged.neoforge.common.ItemAbilities.HOE_TILL,true)==null)continue;
     GUARD.add(player.getUUID());try{player.gameMode.useItemOn(player,world,stack,InteractionHand.MAIN_HAND,hit);}finally{GUARD.remove(player.getUUID());}
    }else if(mode==Mode.HARVEST){if(crop(player,pos,s,cropGroup))harvest(player,pos,s);}
    else if(dig(player,pos,s,mode)&&(mode!=Mode.LUMBERJACK||!ToolLogOrigins.get(world).blocked(pos)))destroy(player,pos);
   }return targets.isEmpty();
  }
 }
 public static void tick(ServerPlayer p){var job=JOBS.get(p.getUUID());if(job!=null&&(job.player!=p||job.tick()))JOBS.remove(p.getUUID());var intent=INTENTS.get(p.getUUID());if(intent!=null&&now(p)-intent.time>1200)INTENTS.remove(p.getUUID());}
 public static void clear(ServerPlayer p){JOBS.remove(p.getUUID());INTENTS.remove(p.getUUID());REMOVED.remove(p.getUUID());GUARD.remove(p.getUUID());}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){if(e.getOriginal() instanceof ServerPlayer p)clear(p);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){if(!(e.getLevel() instanceof ServerLevel w))return;if(e instanceof BlockEvent.EntityMultiPlaceEvent multi){for(var snap:multi.getReplacedBlockSnapshots())if(snap.getCurrentState().is(BlockTags.LOGS))ToolLogOrigins.get(w).mark(snap.getPos());}else if(e.getPlacedBlock().is(BlockTags.LOGS))ToolLogOrigins.get(w).mark(e.getPos());}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void piston(PistonEvent.Pre e){
  if(!(e.getLevel() instanceof ServerLevel w))return;var pos=e.getPos();boolean allLoaded=true;
  int minX=(pos.getX()-16)>>4,maxX=(pos.getX()+16)>>4,minZ=(pos.getZ()-16)>>4,maxZ=(pos.getZ()+16)>>4;
  for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)if(w.getChunkSource().getChunkNow(x,z)==null)allLoaded=false;
  var origins=ToolLogOrigins.get(w);
  if(!allLoaded){for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)origins.denyChunk(x,z);return;}
  var resolver=e.getStructureHelper();if(resolver==null||!resolver.resolve())return;
  var direction=e.getPistonMoveType().isExtend?e.getDirection():e.getDirection().getOpposite();
  // Native 1.21.1 piston resolver has a 12-block hard limit; no new broad scan or chunk acquisition.
  for(var moved:resolver.getToPush())if(loaded(w,moved)&&w.getBlockState(moved).is(BlockTags.LOGS)){origins.mark(moved);origins.mark(moved.relative(direction));}
 }
 @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){JOBS.clear();INTENTS.clear();REMOVED.clear();GUARD.clear();}
 private ToolRuneRuntime(){}
}
