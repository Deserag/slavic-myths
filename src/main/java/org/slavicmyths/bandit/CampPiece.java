package org.slavicmyths.bandit;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.*;
import org.slavicmyths.registry.ModEntities;
public final class CampPiece extends TemplateStructurePiece {
    private String name;private UUID camp;private int total,first,processed;
    CampPiece(TemplateManager manager,String name,BlockPos pos,UUID camp,int total,int first){super(CampStructures.PIECE,0);this.name=name;this.templatePosition=pos;this.camp=camp;this.total=total;this.first=first;load(manager);}
    public CampPiece(TemplateManager manager,CompoundNBT tag){super(CampStructures.PIECE,tag);name=tag.getString("CampTemplate");camp=tag.getUUID("CampId");total=tag.getInt("CampTotal");first=tag.getInt("CampFirst");processed=tag.getInt("CampProcessed");load(manager);}
    private void load(TemplateManager manager){setup(manager.getOrCreate(new ResourceLocation("slavicmyths","bandit/"+name)),templatePosition,new PlacementSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK));boundingBox.y0-=8;}
    protected void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putString("CampTemplate",name);tag.putUUID("CampId",camp);tag.putInt("CampTotal",total);tag.putInt("CampFirst",first);tag.putInt("CampProcessed",processed);}
    @Override public boolean postProcess(ISeedReader w,StructureManager structures,ChunkGenerator g,Random random,MutableBoundingBox clip,ChunkPos chunk,BlockPos pivot){
        if(name.startsWith("yard")) {
            for(Block block:new Block[]{Blocks.COARSE_DIRT,Blocks.SPRUCE_LOG,Blocks.SPRUCE_FENCE,Blocks.CAMPFIRE,Blocks.HAY_BLOCK})for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)){
                BlockPos p=new BlockPos(info.pos.getX(),g.getBaseHeight(info.pos.getX(),info.pos.getZ(),Heightmap.Type.WORLD_SURFACE_WG)-1+info.pos.getY()-templatePosition.getY(),info.pos.getZ());if(clip.isInside(p))w.setBlock(p,info.state,2);
            }return true;
        }
        super.postProcess(w,structures,g,random,clip,chunk,pivot);
        for(Block block:new Block[]{Blocks.SPRUCE_LOG,Blocks.COBBLESTONE})for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,block))if(info.pos.getY()==templatePosition.getY())for(int d=1;d<=8;d++){
            BlockPos p=info.pos.below(d);if(!clip.isInside(p))continue;if(w.getBlockState(p).getMaterial().isSolid())break;w.setBlock(p,info.state,2);
        }return true;
    }
    protected void handleDataMarker(String marker,BlockPos pos,IServerWorld world,Random random,MutableBoundingBox clip){
        if(!clip.isInside(pos))return;
        if(marker.startsWith("loot:")){
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);if(world.getBlockEntity(pos) instanceof net.minecraft.tileentity.LockableLootTileEntity){net.minecraft.tileentity.LockableLootTileEntity chest=(net.minecraft.tileentity.LockableLootTileEntity)world.getBlockEntity(pos);chest.setLootTable(new ResourceLocation("slavicmyths","chests/bandit_"+(total>=7?"medium":"small")),camp.getLeastSignificantBits()^pos.asLong());chest.getTileData().putBoolean("BanditLoot",true);}return;
        }
        if(!marker.startsWith("spawn:"))return;world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        String[] parts=marker.split(":");int local=Integer.parseInt(parts[2]),slot=first+local;if(slot<0||slot>=total)return;
        CampRecords records=CampRecords.get(world.getLevel());if((processed&(1<<local))!=0||records.processed(camp,slot))return;
        String role=parts[1];if(name.equals("leader"))role="ataman";
        EntityType<BanditEntity> type=role.equals("ataman")?ModEntities.ATAMAN.get():role.equals("archer")?ModEntities.BANDIT_ARCHER.get():role.equals("heavy")?ModEntities.BANDIT_HEAVY.get():role.equals("senior")?ModEntities.BANDIT_SENIOR.get():ModEntities.BANDIT_FIGHTER.get();
        BanditEntity mob=type.create(world.getLevel());if(mob==null)return;
        mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),SpawnReason.STRUCTURE,null,null);
        mob.camp=camp;mob.campMember=slot;mob.campTotal=total;mob.home=pos;mob.restrictTo(pos,32);mob.setPersistenceRequired();mob.setUUID(UUID.nameUUIDFromBytes((camp.toString()+":"+slot).getBytes(StandardCharsets.UTF_8)));
        world.addFreshEntity(mob);processed|=1<<local;records.spawned(camp,slot,total);
    }
}
