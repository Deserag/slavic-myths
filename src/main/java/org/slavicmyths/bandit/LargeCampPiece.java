package org.slavicmyths.bandit;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.furniture.*;
public final class LargeCampPiece extends TemplateStructurePiece {
 private String name;private UUID camp;private long processed;
 LargeCampPiece(TemplateManager tm,String n,BlockPos p,UUID id){super(CampStructures.LARGE_PIECE,0);name=n;templatePosition=p;camp=id;load(tm);}
 public LargeCampPiece(TemplateManager tm,CompoundNBT n){super(CampStructures.LARGE_PIECE,n);name=n.getString("Template");camp=n.getUUID("Camp");processed=n.getLong("Processed");load(tm);}
 private void load(TemplateManager tm){setup(tm.getOrCreate(new ResourceLocation("slavicmyths","stronghold/"+name)),templatePosition,new PlacementSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK));boundingBox.y0-=10;boundingBox.z0-=3;}
 @Override protected void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);n.putString("Template",name);n.putUUID("Camp",camp);n.putLong("Processed",processed);}
 @Override public boolean postProcess(ISeedReader w,StructureManager sm,ChunkGenerator g,Random r,MutableBoundingBox clip,ChunkPos chunk,BlockPos pivot){
  if(name.startsWith("perimeter")){
   // Sparse fence/path sections follow local columns, without a rectangular terrain platform.
   for(Block block:new Block[]{org.slavicmyths.wood.Woodlands.SETS.get("pine").get("log"),org.slavicmyths.wood.Woodlands.SETS.get("pine").get("planks"),Blocks.COARSE_DIRT,Blocks.GRAVEL,Blocks.GRASS_PATH,Blocks.AIR})for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)){
    BlockPos p=new BlockPos(info.pos.getX(),g.getBaseHeight(info.pos.getX(),info.pos.getZ(),Heightmap.Type.WORLD_SURFACE_WG)-1+info.pos.getY()-templatePosition.getY(),info.pos.getZ());if(clip.isInside(p))w.setBlock(p,info.state,2);
   }return true;
  }
  super.postProcess(w,sm,g,r,clip,chunk,pivot);
  for(Block b:new Block[]{Blocks.COBBLESTONE,Blocks.COARSE_DIRT,org.slavicmyths.wood.Woodlands.SETS.get("pine").get("log")})for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,b))if(info.pos.getY()==templatePosition.getY())for(int down=1;down<=10;down++){BlockPos q=info.pos.below(down);if(!clip.isInside(q))continue;if(w.getBlockState(q).getMaterial().isSolid())break;w.setBlock(q,b==Blocks.COARSE_DIRT?Blocks.DIRT.defaultBlockState():info.state,2);}
  // Local entrance steps bridge each accepted foundation (maximum three-block relief).
  if(!name.contains("yard")&&!name.startsWith("cache")&&!name.startsWith("tower")){
   int mid=template.getSize().getX()/2;
   for(int dx=0;dx<2;dx++)for(int d=1;d<=3;d++){
    BlockPos q=templatePosition.offset(mid+dx,1-d,-d);if(!clip.isInside(q))continue;
    int ground=g.getBaseHeight(q.getX(),q.getZ(),Heightmap.Type.WORLD_SURFACE_WG);
    if(q.getY()>=ground-1)w.setBlock(q,org.slavicmyths.wood.Woodlands.SETS.get("pine").get("stairs").defaultBlockState().setValue(StairsBlock.FACING,Direction.SOUTH),2);
   }
  }
  return true;
 }
 @Override protected void handleDataMarker(String marker,BlockPos pos,IServerWorld w,Random r,MutableBoundingBox clip){
  if(!clip.isInside(pos))return;String[]parts=marker.split(":");StrongholdRecords records=StrongholdRecords.get(w.getLevel());
  if(parts[0].equals("bell")){w.setBlock(pos,Furniture.get("signal_bell").defaultBlockState(),2);records.bell(camp,Integer.parseInt(parts[1]),pos);return;}
  if(parts[0].equals("loot")){w.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);net.minecraft.tileentity.TileEntity tile=w.getBlockEntity(pos);if(tile instanceof net.minecraft.tileentity.LockableLootTileEntity)((net.minecraft.tileentity.LockableLootTileEntity)tile).setLootTable(new ResourceLocation("slavicmyths","chests/stronghold_"+parts[1]),camp.getLeastSignificantBits()^pos.asLong());return;}
  if(parts[0].equals("rack")){w.setBlock(pos,Furniture.get("pine_weapon_rack").defaultBlockState(),2);if(w.getBlockEntity(pos)instanceof RackTile){RackTile rack=(RackTile)w.getBlockEntity(pos);rack.weapon=new ItemStack(parts[1].equals("bow")?Items.BOW:Items.IRON_SWORD);rack.setChanged();}return;}
  if(!parts[0].equals("spawn"))return;int slot=Integer.parseInt(parts[2]);w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);if((processed&(1L<<slot))!=0||records.processed(camp,slot))return;
  String role=parts[1];Entity entity;
  if(role.equals("horse")){if(r.nextInt(4)==0){processed|=1L<<slot;records.spawned(camp,slot);return;}HorseEntity horse=EntityType.HORSE.create(w.getLevel());horse.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),SpawnReason.STRUCTURE,null,null);horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23+r.nextDouble()*.09);horse.setPersistenceRequired();entity=horse;}
  else if(role.equals("prisoner")){int choice=r.nextInt(3);if(choice==0){processed|=1L<<slot;records.spawned(camp,slot);return;}MobEntity prisoner=(choice==1?EntityType.VILLAGER:EntityType.WANDERING_TRADER).create(w.getLevel());prisoner.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),SpawnReason.STRUCTURE,null,null);prisoner.setPersistenceRequired();entity=prisoner;}
  else {
   EntityType<BanditEntity> type=role.startsWith("ataman")?ModEntities.ATAMAN.get():role.equals("archer")?ModEntities.BANDIT_ARCHER.get():role.equals("heavy")?ModEntities.BANDIT_HEAVY.get():role.equals("senior")?ModEntities.BANDIT_SENIOR.get():ModEntities.BANDIT_FIGHTER.get();
   BanditEntity mob=type.create(w.getLevel());mob.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),SpawnReason.STRUCTURE,null,null);mob.camp=camp;mob.campMember=slot;mob.campTotal=StrongholdRecords.TOTAL;mob.zone=Integer.parseInt(parts[3]);mob.duty(Integer.parseInt(parts[4]));mob.home=pos;mob.restrictTo(pos,36);mob.setPersistenceRequired();
   if(role.startsWith("ataman")){boolean gate=role.equals("ataman_gate");mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(gate?68:64);mob.setHealth(mob.getMaxHealth());mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(gate?.25:.31);mob.setItemSlot(EquipmentSlotType.HEAD,new ItemStack(gate?Items.IRON_HELMET:Items.CHAINMAIL_HELMET));if(!gate){mob.setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(Items.IRON_SWORD));mob.setItemSlot(EquipmentSlotType.CHEST,new ItemStack(ModItems.GAMBESON.get()));mob.setItemSlot(EquipmentSlotType.OFFHAND,ItemStack.EMPTY);}}
   if(mob.duty()==3){mob.setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(ModItems.MACE.get()));mob.setItemSlot(EquipmentSlotType.CHEST,new ItemStack(Items.LEATHER_CHESTPLATE));}entity=mob;
  }
  entity.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);entity.setUUID(UUID.nameUUIDFromBytes((camp+":"+slot).getBytes(StandardCharsets.UTF_8)));if(w.addFreshEntity(entity)){processed|=1L<<slot;records.spawned(camp,slot);}
 }
}
