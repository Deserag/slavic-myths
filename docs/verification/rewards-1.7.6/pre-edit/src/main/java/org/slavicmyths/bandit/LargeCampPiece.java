package org.slavicmyths.bandit;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import org.slavicmyths.registry.*;
import org.slavicmyths.furniture.*;
public final class LargeCampPiece extends TemplateStructurePiece {
 public boolean preparedTerrain;
 private String name;private UUID camp;private long processed;private boolean bossProcessed;
 public LargeCampPiece(StructureTemplateManager tm,String n,BlockPos p,UUID id){super(CampStructures.LARGE_PIECE.get(),0,tm,ResourceLocation.fromNamespaceAndPath("slavicmyths","stronghold/"+n),"slavicmyths:stronghold/"+n,settings(),p);name=n;templatePosition=p;camp=id;load(tm);}
 public LargeCampPiece(StructureTemplateManager tm,CompoundTag n){super(CampStructures.LARGE_PIECE.get(),modernTag(n),tm,id->settings());name=n.getString("StrongholdTemplate");if(name.isEmpty())name=n.getString("Template");camp=n.getUUID("Camp");processed=n.getLong("Processed");bossProcessed=n.getBoolean("BossProcessed");preparedTerrain=n.getBoolean("PreparedTerrain");load(tm);}
 public BlockPos nightingaleArrival(){return name.equals("nightingale_yard")?templatePosition.offset(18,1,25):null;}
private void load(StructureTemplateManager ignored){boundingBox=new BoundingBox(boundingBox.minX(),boundingBox.minY()-10,boundingBox.minZ()-10,boundingBox.maxX(),boundingBox.maxY(),boundingBox.maxZ());}
 private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 private static CompoundTag modernTag(CompoundTag original){CompoundTag n=original.copy();String name=n.getString("StrongholdTemplate");if(name.isEmpty())name=n.getString("Template"); n.putString("Template","slavicmyths:stronghold/"+name);return n;}

 @Override protected synchronized void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag n){super.addAdditionalSaveData(context,n);n.putBoolean("PreparedTerrain",preparedTerrain);n.putString("StrongholdTemplate",name);n.putUUID("Camp",camp);n.putLong("Processed",processed);n.putBoolean("BossProcessed",bossProcessed);}
 @Override public synchronized void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  if(name.startsWith("perimeter")){
   // Sparse fence/path sections follow local columns, without a rectangular terrain platform.
   for(Block block:new Block[]{org.slavicmyths.wood.Woodlands.SETS.get("pine").get("log"),org.slavicmyths.wood.Woodlands.SETS.get("pine").get("planks"),Blocks.COARSE_DIRT,Blocks.GRAVEL,Blocks.DIRT_PATH,Blocks.AIR})for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)){
    BlockPos p=new BlockPos(info.pos().getX(),(preparedTerrain?info.pos().getY():g.getBaseHeight(info.pos().getX(),info.pos().getZ(),Heightmap.Types.WORLD_SURFACE_WG,w,w.getLevel().getChunkSource().randomState())-1+info.pos().getY()-templatePosition.getY()),info.pos().getZ());if(clip.isInside(p))w.setBlock(p,info.state(),2);
   }return;
  }
  // Vanilla resets the box to the template; retain foundations and approaches across chunks.
        BoundingBox acceptedBounds=boundingBox;
        try { super.postProcess(w,sm,g,r,clip,chunk,pivot); } finally { boundingBox=acceptedBounds; }
  for(Block b:new Block[]{Blocks.COBBLESTONE,Blocks.COARSE_DIRT,org.slavicmyths.wood.Woodlands.SETS.get("pine").get("log")})for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,b))if(info.pos().getY()==templatePosition.getY())for(int down=1;down<=10;down++){BlockPos q=info.pos().below(down);if(!clip.isInside(q))continue;if(w.getBlockState(q).isSolid())break;w.setBlock(q,b==Blocks.COARSE_DIRT?Blocks.DIRT.defaultBlockState():info.state(),2);}
  if(!preparedTerrain&&!name.contains("yard")&&!name.startsWith("cache")&&!name.startsWith("tower"))org.slavicmyths.worldgen.FoundationApproach.place(w,g,clip,templatePosition,template.getSize().getX()/2,10,org.slavicmyths.wood.Woodlands.SETS.get("pine").get("stairs").defaultBlockState());
  return;
 }
 @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor w,RandomSource r,BoundingBox clip){
  if(!clip.isInside(pos))return;String[]parts=marker.split(":");StrongholdRecords records=StrongholdRecords.get(w.getLevel());
  if(parts[0].equals("nightingale")){w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);if(!bossProcessed&&!records.nightingaleProcessed(camp)){NightingaleEntity boss=ModEntities.NIGHTINGALE.get().create(w.getLevel());boss.camp=camp;boss.home=pos;boss.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);boss.setUUID(UUID.nameUUIDFromBytes((camp+":nightingale").getBytes(StandardCharsets.UTF_8)));if(w.addFreshEntity(boss)){bossProcessed=true;w.getLevel().getServer().execute(()->records.nightingaleSpawned(camp));}}return;}
  if(parts[0].equals("bell")){w.setBlock(pos,Furniture.get("signal_bell").defaultBlockState(),2);w.getLevel().getServer().execute(()->records.bell(camp,Integer.parseInt(parts[1]),pos.immutable()));return;}
  if(parts[0].equals("loot")){if(w.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity existing&&(existing.getPersistentData().getBoolean("SlavicStrongholdLoot")||existing.getLootTable()!=null||!existing.isEmpty()))return;w.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);net.minecraft.world.level.block.entity.BlockEntity tile=w.getBlockEntity(pos);if(tile instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container){container.getPersistentData().putBoolean("SlavicStrongholdLoot",true);container.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/stronghold_"+parts[1])),camp.getLeastSignificantBits()^pos.asLong());}return;}
  if(parts[0].equals("rack")){w.setBlock(pos,Furniture.get("pine_weapon_rack").defaultBlockState(),2);if(w.getBlockEntity(pos)instanceof RackTile){RackTile rack=(RackTile)w.getBlockEntity(pos);rack.weapon=new ItemStack(parts[1].equals("bow")?Items.BOW:Items.IRON_SWORD);rack.setChanged();}return;}
  if(!parts[0].equals("spawn"))return;int slot=Integer.parseInt(parts[2]);w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);if((processed&(1L<<slot))!=0||records.processed(camp,slot))return;
  String role=parts[1];Entity entity;
  if(role.equals("horse")){if(r.nextInt(4)==0){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));return;}Horse horse=EntityType.HORSE.create(w.getLevel());horse.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.23+r.nextDouble()*.09);horse.setPersistenceRequired();entity=horse;}
  else if(role.equals("prisoner")){int choice=r.nextInt(3);if(choice==0){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));return;}Mob prisoner=(choice==1?EntityType.VILLAGER:EntityType.WANDERING_TRADER).create(w.getLevel());prisoner.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);prisoner.setPersistenceRequired();entity=prisoner;}
  else {
   EntityType<BanditEntity> type=role.startsWith("ataman")?ModEntities.ATAMAN.get():role.equals("archer")?ModEntities.BANDIT_ARCHER.get():role.equals("heavy")?ModEntities.BANDIT_HEAVY.get():role.equals("senior")?ModEntities.BANDIT_SENIOR.get():ModEntities.BANDIT_FIGHTER.get();
   BanditEntity mob=type.create(w.getLevel());mob.finalizeSpawn(w,w.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);mob.camp=camp;mob.campMember=slot;mob.campTotal=StrongholdRecords.TOTAL;mob.zone=Integer.parseInt(parts[3]);mob.duty(Integer.parseInt(parts[4]));mob.home=pos;mob.restrictTo(pos,36);mob.setPersistenceRequired();
   if(role.startsWith("ataman")){boolean gate=role.equals("ataman_gate");mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(150);mob.setHealth(mob.getMaxHealth());mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(gate?.25:.31);mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(gate?Items.IRON_HELMET:Items.CHAINMAIL_HELMET));if(!gate){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.GAMBESON.get()));mob.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}}
   if(mob.duty()==3){mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.MACE.get()));mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.LEATHER_CHESTPLATE));}entity=mob;
  }
  entity.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);entity.setUUID(UUID.nameUUIDFromBytes((camp+":"+slot).getBytes(StandardCharsets.UTF_8)));if(w.addFreshEntity(entity)){processed|=1L<<slot;w.getLevel().getServer().execute(()->records.spawned(camp,slot));}
 }
}
