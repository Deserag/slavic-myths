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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import org.slavicmyths.registry.ModEntities;
public final class CampPiece extends TemplateStructurePiece {
 public boolean preparedTerrain;
    private String name;private UUID camp;private int total,first,processed;
    public CampPiece(StructureTemplateManager manager,String name,BlockPos pos,UUID camp,int total,int first){super(CampStructures.PIECE.get(),0,manager,ResourceLocation.fromNamespaceAndPath("slavicmyths","bandit/"+name),"slavicmyths:bandit/"+name,settings(),pos);this.name=name;this.templatePosition=pos;this.camp=camp;this.total=total;this.first=first;load(manager);}
    public CampPiece(StructureTemplateManager manager,CompoundTag tag){super(CampStructures.PIECE.get(),modernTag(tag),manager,id->settings());name=tag.getString("CampTemplate");camp=tag.getUUID("CampId");total=tag.getInt("CampTotal");first=tag.getInt("CampFirst");processed=tag.getInt("CampProcessed");preparedTerrain=tag.getBoolean("PreparedTerrain");load(manager);}
private void load(StructureTemplateManager ignored){boundingBox=new BoundingBox(boundingBox.minX(),boundingBox.minY()-8,boundingBox.minZ()-8,boundingBox.maxX(),boundingBox.maxY(),boundingBox.maxZ());}
 private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 private static CompoundTag modernTag(CompoundTag original){CompoundTag n=original.copy();String name=n.getString("CampTemplate"); n.putString("Template","slavicmyths:bandit/"+name);return n;}

    protected synchronized void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){super.addAdditionalSaveData(context,tag);tag.putBoolean("PreparedTerrain",preparedTerrain);tag.putString("CampTemplate",name);tag.putUUID("CampId",camp);tag.putInt("CampTotal",total);tag.putInt("CampFirst",first);tag.putInt("CampProcessed",processed);}
    @Override public synchronized void postProcess(WorldGenLevel w,StructureManager structures,ChunkGenerator g,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        if(name.startsWith("yard")) {
            for(Block block:new Block[]{Blocks.COARSE_DIRT,Blocks.SPRUCE_LOG,Blocks.SPRUCE_FENCE,Blocks.CAMPFIRE,Blocks.HAY_BLOCK})for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)){
                BlockPos p=new BlockPos(info.pos().getX(),(preparedTerrain?info.pos().getY():g.getBaseHeight(info.pos().getX(),info.pos().getZ(),Heightmap.Types.WORLD_SURFACE_WG,w,w.getLevel().getChunkSource().randomState())-1+info.pos().getY()-templatePosition.getY()),info.pos().getZ());if(clip.isInside(p))w.setBlock(p,info.state(),2);
            }return;
        }
        // Vanilla resets the box to the template; retain foundations and approaches across chunks.
        BoundingBox acceptedBounds=boundingBox;
        try { super.postProcess(w,structures,g,random,clip,chunk,pivot); } finally { boundingBox=acceptedBounds; }
        for(Block block:new Block[]{Blocks.SPRUCE_LOG,Blocks.COBBLESTONE})for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,block))if(info.pos().getY()==templatePosition.getY())for(int d=1;d<=8;d++){
            BlockPos p=info.pos().below(d);if(!clip.isInside(p))continue;if(w.getBlockState(p).isSolid())break;w.setBlock(p,info.state(),2);
        }
        if(!preparedTerrain&&!name.equals("cart"))org.slavicmyths.worldgen.FoundationApproach.place(w,g,clip,templatePosition,template.getSize().getX()/2,8,Blocks.SPRUCE_STAIRS.defaultBlockState());
        return;
    }
    protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor world,RandomSource random,BoundingBox clip){
        if(!clip.isInside(pos))return;
        if(marker.startsWith("loot:")){
            if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity existing && (existing.getPersistentData().getBoolean("BanditLoot")||existing.getLootTable()!=null||!existing.isEmpty()))return;
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity){net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest=(net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)world.getBlockEntity(pos);chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/bandit_"+(total>=7?"medium":"small"))),camp.getLeastSignificantBits()^pos.asLong());chest.getPersistentData().putBoolean("BanditLoot",true);}return;
        }
        if(!marker.startsWith("spawn:"))return;world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        String[] parts=marker.split(":");int local=Integer.parseInt(parts[2]),slot=first+local;if(slot<0||slot>=total)return;
        CampRecords records=CampRecords.get(world.getLevel());if((processed&(1<<local))!=0||records.processed(camp,slot))return;
        String role=parts[1];if(name.equals("leader"))role="ataman";
        EntityType<BanditEntity> type=role.equals("ataman")?ModEntities.ATAMAN.get():role.equals("archer")?ModEntities.BANDIT_ARCHER.get():role.equals("heavy")?ModEntities.BANDIT_HEAVY.get():role.equals("senior")?ModEntities.BANDIT_SENIOR.get():ModEntities.BANDIT_FIGHTER.get();
        BanditEntity mob=type.create(world.getLevel());if(mob==null)return;
        mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);
        mob.camp=camp;mob.campMember=slot;mob.campTotal=total;mob.home=pos;mob.restrictTo(pos,32);mob.setPersistenceRequired();mob.setUUID(UUID.nameUUIDFromBytes((camp.toString()+":"+slot).getBytes(StandardCharsets.UTF_8)));
        if(world.addFreshEntity(mob)){processed|=1<<local;world.getLevel().getServer().execute(()->records.spawned(camp,slot,total));}
    }
}
