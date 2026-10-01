package org.slavicmyths.swamp;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.*;

/** Hand-authored templates: stable names and seeds persist with standard StructureStart NBT. */
public final class SwampPiece extends TemplateStructurePiece {
    private String name;private long seed;private boolean encounterProcessed;
    SwampPiece(TemplateManager manager,String name,BlockPos pos,long seed) {
        super(SwampStructures.PIECE,0);this.name=name;this.seed=seed;this.templatePosition=pos;load(manager);
    }
    public SwampPiece(TemplateManager manager,CompoundNBT nbt) {
        super(SwampStructures.PIECE,nbt);name=nbt.getString("SwampTemplate");seed=nbt.getLong("SwampSeed");encounterProcessed=nbt.getBoolean("EncounterProcessed");load(manager);
    }
    private void load(TemplateManager manager) {
        setup(manager.getOrCreate(new ResourceLocation("slavicmyths","swamp/"+name)),templatePosition,
            new PlacementSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK));
        // Reserve the downward support budget in start references as well as the template volume.
        boundingBox.y0-=8;
    }
    @Override protected void addAdditionalSaveData(CompoundNBT nbt){super.addAdditionalSaveData(nbt);nbt.putString("SwampTemplate",name);nbt.putLong("SwampSeed",seed);nbt.putBoolean("EncounterProcessed",encounterProcessed);}
    @Override public boolean postProcess(ISeedReader world,StructureManager structures,ChunkGenerator generator,Random random,MutableBoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        if(name.startsWith("settlement_paths_")) {
            for(Block block:new Block[]{Blocks.COARSE_DIRT,Blocks.SPRUCE_LOG,Blocks.SPRUCE_FENCE})
                for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)) {
                    int x=info.pos.getX(),z=info.pos.getZ();
                    int surface=generator.getBaseHeight(x,z,net.minecraft.world.gen.Heightmap.Type.WORLD_SURFACE_WG);
                    int floor=generator.getBaseHeight(x,z,net.minecraft.world.gen.Heightmap.Type.OCEAN_FLOOR_WG);
                    BlockPos p=new BlockPos(x,surface-1+info.pos.getY()-templatePosition.getY(),z);
                    if(!clip.isInside(p))continue;
                    BlockState state=info.state;
                    if(block==Blocks.COARSE_DIRT&&floor<surface)state=Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.state.properties.SlabType.TOP).setValue(SlabBlock.WATERLOGGED,true);
                    world.setBlock(p,state,2);
                    if(block==Blocks.COARSE_DIRT&&floor<surface&&(x+z)%4==0)
                        for(int y=surface-2;y>=floor-1&&y>=surface-8;y--)if(clip.isInside(new BlockPos(x,y,z)))world.setBlock(new BlockPos(x,y,z),Blocks.SPRUCE_LOG.defaultBlockState(),2);
                }
            return true;
        }
        super.postProcess(world,structures,generator,new Random(seed),clip,chunk,pivot);
        // Only explicit bottom foundation blocks get supports. Untouched template voids keep
        // natural terrain/water. Supports never write outside the currently decorated chunk.
        for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,Blocks.MOSSY_COBBLESTONE))support(world,clip,info);
        for(Template.BlockInfo info:template.filterBlocks(templatePosition,placeSettings,Blocks.SPRUCE_LOG))
            if(info.state.getValue(RotatedPillarBlock.AXIS)==net.minecraft.util.Direction.Axis.Y)support(world,clip,info);
        return true;
    }
    private void support(ISeedReader world,MutableBoundingBox clip,Template.BlockInfo info) {
        if(info.pos.getY()!=templatePosition.getY())return;
        for(int i=1;i<=8;i++) {
            BlockPos p=info.pos.below(i);if(!clip.isInside(p))continue;
            BlockState old=world.getBlockState(p);
            if(old.getMaterial().isSolid()&&!old.is(net.minecraft.tags.BlockTags.LEAVES))break;
            world.setBlock(p,info.state,2);
        }
    }
    @Override protected void handleDataMarker(String marker,BlockPos pos,IServerWorld world,Random random,MutableBoundingBox box) {
        // Markers are deterministic and clipped by TemplateStructurePiece. No ticking spawners.
        if(marker.startsWith("loot:")) {
            Random choice=new Random(seed^pos.asLong());
            if((marker.endsWith("_home")&&choice.nextInt(4)==0)||(marker.endsWith("_remnants")&&choice.nextBoolean())) {
                world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);return;
            }
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
            if(world.getBlockEntity(pos) instanceof net.minecraft.tileentity.LockableLootTileEntity)
                ((net.minecraft.tileentity.LockableLootTileEntity)world.getBlockEntity(pos)).setLootTable(new ResourceLocation("slavicmyths","chests/"+marker.substring(5)),seed^pos.asLong());
        } else if(marker.startsWith("encounter:")) {
            boolean water=!marker.endsWith("kikimora");
            world.setBlock(pos,(water?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);
            if(encounterProcessed)return;
            encounterProcessed=true;
            if(new Random(seed^pos.asLong()).nextInt(water?8:10)!=0 || world.getLevel().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return;
            net.minecraft.entity.MobEntity mob=marker.endsWith("kikimora")?org.slavicmyths.registry.ModEntities.KIKIMORA.get().create(world.getLevel()):
                marker.endsWith("rusalka")?org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world.getLevel()):org.slavicmyths.registry.ModEntities.VODYANOY.get().create(world.getLevel());
            if(mob==null)return;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            if(!world.noCollision(mob))return;
            if(mob instanceof org.slavicmyths.entity.LandSpiritEntity)((org.slavicmyths.entity.LandSpiritEntity)mob).home=pos;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),net.minecraft.entity.SpawnReason.STRUCTURE,null,null);
            world.addFreshEntity(mob);
        }
    }
}
