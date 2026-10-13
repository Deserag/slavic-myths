package org.slavicmyths.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.entity.WildlifeFamilyPlan;
import org.slavicmyths.registry.ModEntities;

/** Family egg logic is shared by player use and dispensers. Old redundant IDs stay registered. */
public final class WildlifeFamilyEgg extends DeferredSpawnEggItem {
 private final boolean bear;
 public WildlifeFamilyEgg(boolean bear,int background,int highlight,Properties properties){
  super(bear?ModEntities.BROWN_BEAR:ModEntities.STAG,background,highlight,properties);this.bear=bear;
 }
 public WildlifeEntity spawnEncounter(ServerLevel world,BlockPos pos,ItemStack stack,net.minecraft.world.entity.player.Player player,MobSpawnType reason){
  return spawnEncounter(world,pos,stack,player,reason,true,true);
 }
 private WildlifeEntity spawnEncounter(ServerLevel world,BlockPos pos,ItemStack stack,net.minecraft.world.entity.player.Player player,MobSpawnType reason,boolean align,boolean lower){
  var plan=bear?WildlifeFamilyPlan.bear(world.random,false):WildlifeFamilyPlan.deer(world.random);
  var type=switch(plan.adult()){case BEAR,MOTHER_BEAR->ModEntities.BROWN_BEAR.get();case STAG->ModEntities.STAG.get();case DOE->ModEntities.DOE.get();};
  var adult=type.spawn(world,stack,player,pos,reason,align,lower);
  if(adult==null)return null;
  if(!world.noCollision(adult)||plan.babies()>0&&!adult.family(plan.babies())){adult.discard();return null;}
  return adult;
 }
 @Override public InteractionResult useOn(UseOnContext context){
  if(!(context.getLevel() instanceof ServerLevel world))return InteractionResult.SUCCESS;
  if(world.getBlockEntity(context.getClickedPos()) instanceof Spawner
    ||context.getItemInHand().has(net.minecraft.core.component.DataComponents.ENTITY_DATA))return super.useOn(context);
  var at=context.getClickedPos();if(!world.getBlockState(at).getCollisionShape(world,at).isEmpty())at=at.relative(context.getClickedFace());
  var adult=spawnEncounter(world,at,context.getItemInHand(),context.getPlayer(),MobSpawnType.SPAWN_EGG);
  if(adult==null)return InteractionResult.FAIL;
  context.getItemInHand().consume(1,context.getPlayer());world.gameEvent(context.getPlayer(),GameEvent.ENTITY_PLACE,adult.position());return InteractionResult.CONSUME;
 }
 @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand){
  var stack=player.getItemInHand(hand);
  if(stack.has(net.minecraft.core.component.DataComponents.ENTITY_DATA))return super.use(level,player,hand);
  var hit=getPlayerPOVHitResult(level,player,net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY);
  if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return net.minecraft.world.InteractionResultHolder.pass(stack);
  if(!(level instanceof ServerLevel world))return net.minecraft.world.InteractionResultHolder.success(stack);
  var pos=hit.getBlockPos();
  if(!(world.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.LiquidBlock))return net.minecraft.world.InteractionResultHolder.pass(stack);
  if(!world.mayInteract(player,pos)||!player.mayUseItemAt(pos,hit.getDirection(),stack))return net.minecraft.world.InteractionResultHolder.fail(stack);
  var adult=spawnEncounter(world,pos,stack,player,MobSpawnType.SPAWN_EGG,false,false);
  if(adult==null)return net.minecraft.world.InteractionResultHolder.fail(stack);
  stack.consume(1,player);player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));world.gameEvent(player,GameEvent.ENTITY_PLACE,adult.position());return net.minecraft.world.InteractionResultHolder.consume(stack);
 }
 @Override protected DispenseItemBehavior createDispenseBehavior(){
 var nativeBehavior=super.createDispenseBehavior();return (source,stack)->{
  if(stack.has(net.minecraft.core.component.DataComponents.ENTITY_DATA))return nativeBehavior.dispense(source,stack);
  var at=source.pos().relative(source.state().getValue(DispenserBlock.FACING));
  if(spawnEncounter(source.level(),at,stack,null,MobSpawnType.DISPENSER)!=null){stack.shrink(1);source.level().gameEvent(null,GameEvent.ENTITY_PLACE,at);}
  return stack;
 };}
}
