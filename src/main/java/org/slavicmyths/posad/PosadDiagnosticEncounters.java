package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Explicit OP fight fixtures. No natural frequency, arena construction, tickets or terrain writes. */
public final class PosadDiagnosticEncounters {
 public static int create(CommandSourceStack source,PosadTestProofs.Kind kind){
  if(!source.hasPermission(2))return 0;var world=source.getLevel();
  String[] ids=switch(kind){case MYTAR->new String[]{"cursed_mytar"};case BAUK->new String[]{"bauk"};case RAVEN->new String[]{"morovoy_raven"};case ZMEY->new String[]{"morovoy_fire_zmey"};case FEVER_SISTERS->new String[]{"gneteya","tryaseya"};case ELEMENT_SISTERS->new String[]{"agneya","ledeya"};};
  var actors=new ArrayList<LivingEntity>();for(String id:ids){
   var key=ResourceLocation.fromNamespaceAndPath("slavicmyths",id);
   if(!BuiltInRegistries.ENTITY_TYPE.containsKey(key)){source.sendFailure(Component.translatable("posad.test.encounter.missing",id));return 0;}
   var entity=BuiltInRegistries.ENTITY_TYPE.get(key).create(world);
   if(!(entity instanceof LivingEntity boss)){source.sendFailure(Component.translatable("posad.test.encounter.missing",id));return 0;}actors.add(boss);
  }
  var origin=BlockPos.containing(source.getPosition());
  for(int distance:new int[]{12,20,28})for(var direction:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1,-2,2}){
   var at=origin.relative(direction,distance).offset(0,dy,0);boolean valid=true;
   for(int i=0;i<actors.size();i++){
    var boss=actors.get(i);boss.setPos(at.getX()+.5+(actors.size()==1?0:i==0?-6:6),at.getY(),at.getZ()+.5);
    if(!safe(world,boss)){valid=false;break;}
   }
   if(!valid)continue;UUID encounter=UUID.randomUUID();
   for(var actor:actors){actor.getPersistentData().putUUID("PosadTestEncounter",encounter);actor.getPersistentData().putBoolean("PosadTestDiagnostic",true);if(actor instanceof Mob mob)mob.setPersistenceRequired();}
   if(kind==PosadTestProofs.Kind.FEVER_SISTERS){
    ((org.slavicmyths.sisters.SisterBoss)actors.get(0)).link(encounter,actors.get(1).getUUID());
    ((org.slavicmyths.sisters.SisterBoss)actors.get(1)).link(encounter,actors.get(0).getUUID());
   }
   var inserted=new ArrayList<LivingEntity>();
   for(var actor:actors){if(!world.addFreshEntity(actor)){for(var previous:inserted)previous.discard();source.sendFailure(Component.translatable("posad.test.encounter.rejected"));return 0;}inserted.add(actor);}
   source.sendSuccess(()->Component.translatable("posad.test.encounter.created",kind.title(),at.getX(),at.getY(),at.getZ()),false);return 1;
  }
  source.sendFailure(Component.translatable("posad.test.encounter.no_space"));return 0;
 }
 private static boolean safe(ServerLevel world,LivingEntity actor){
  var bounds=actor.getBoundingBox();int minX=(int)Math.floor(bounds.minX),maxX=(int)Math.floor(bounds.maxX-1e-6),minZ=(int)Math.floor(bounds.minZ),maxZ=(int)Math.floor(bounds.maxZ-1e-6),feet=(int)Math.floor(bounds.minY),top=(int)Math.ceil(bounds.maxY);
  if(feet<=world.getMinBuildHeight()||top>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(new BlockPos(minX,feet,minZ))||!world.getWorldBorder().isWithinBounds(new BlockPos(maxX,feet,maxZ)))return false;
  for(int cx=minX>>4;cx<=maxX>>4;cx++)for(int cz=minZ>>4;cz<=maxZ>>4;cz++){
   var chunk=world.getChunkSource().getChunkNow(cx,cz);if(chunk==null||chunk.getAllReferences().values().stream().anyMatch(ref->!ref.isEmpty()))return false;
  }
  var box=new BoundingBox(minX,feet-1,minZ,maxX,top,maxZ);
  if(!org.slavicmyths.worldgen.ManualStructureRecords.get(world).overlapping(box).isEmpty())return false;
  for(var pos:BlockPos.betweenClosed(minX,feet-1,minZ,maxX,top,maxZ)){
   if(org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(pos))return false;var state=world.getBlockState(pos);
   if(pos.getY()==feet-1){
    if(state.hasBlockEntity()||!state.isFaceSturdy(world,pos,Direction.UP)||!(state.is(BlockTags.BASE_STONE_NETHER)||state.is(BlockTags.BASE_STONE_OVERWORLD)||state.is(BlockTags.DIRT)||state.is(Blocks.CRIMSON_NYLIUM)||state.is(Blocks.WARPED_NYLIUM)||state.is(Blocks.SOUL_SOIL)||state.is(Blocks.SOUL_SAND)))return false;
   }else if(!state.isAir())return false;
  }
  return world.noCollision(actor);
 }
 public static int clear(CommandSourceStack source){
  if(!source.hasPermission(2))return 0;var world=source.getLevel();int removed=0;
  for(var boss:world.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(BlockPos.containing(source.getPosition())).inflate(64),entity->entity.getPersistentData().getBoolean("PosadTestDiagnostic"))){boss.discard();removed++;}
  int count=removed;source.sendSuccess(()->Component.translatable("posad.test.encounter.cleared",count),false);return removed;
 }
 private PosadDiagnosticEncounters(){}
}
