package org.slavicmyths.swamp;
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

import java.util.Random;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Hand-authored templates: stable names and seeds persist with standard StructureStart NBT. */
public final class SwampPiece extends TemplateStructurePiece {
    private String name;private long seed;private boolean encounterProcessed;
    SwampPiece(StructureTemplateManager manager,String name,BlockPos pos,long seed) {
        super(SwampStructures.PIECE.get(),0,manager,ResourceLocation.fromNamespaceAndPath("slavicmyths","swamp/"+name),"slavicmyths:swamp/"+name,settings(),pos);this.name=name;this.seed=seed;this.templatePosition=pos;load(manager);
    }
    public SwampPiece(StructureTemplateManager manager,CompoundTag nbt) {
        super(SwampStructures.PIECE.get(),modernTag(nbt),manager,id->settings());name=nbt.getString("SwampTemplate");seed=nbt.getLong("SwampSeed");encounterProcessed=nbt.getBoolean("EncounterProcessed");load(manager);
    }
private void load(StructureTemplateManager ignored){boundingBox=new BoundingBox(boundingBox.minX(),boundingBox.minY()-8,boundingBox.minZ()-0,boundingBox.maxX(),boundingBox.maxY(),boundingBox.maxZ());}
 private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 private static CompoundTag modernTag(CompoundTag original){CompoundTag n=original.copy();String name=n.getString("SwampTemplate"); n.putString("Template","slavicmyths:swamp/"+name);return n;}

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag nbt){super.addAdditionalSaveData(context,nbt);nbt.putString("SwampTemplate",name);nbt.putLong("SwampSeed",seed);nbt.putBoolean("EncounterProcessed",encounterProcessed);}
    @Override public void postProcess(WorldGenLevel world,StructureManager structures,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        if(name.startsWith("settlement_paths_")) {
            for(Block block:new Block[]{Blocks.COARSE_DIRT,Blocks.SPRUCE_LOG,Blocks.SPRUCE_FENCE})
                for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,block)) {
                    int x=info.pos().getX(),z=info.pos().getZ();
                    int surface=generator.getBaseHeight(x,z,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,world,world.getLevel().getChunkSource().randomState());
                    int floor=generator.getBaseHeight(x,z,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getLevel().getChunkSource().randomState());
                    BlockPos p=new BlockPos(x,surface-1+info.pos().getY()-templatePosition.getY(),z);
                    if(!clip.isInside(p))continue;
                    BlockState state=info.state();
                    if(block==Blocks.COARSE_DIRT&&floor<surface)state=Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP).setValue(SlabBlock.WATERLOGGED,true);
                    world.setBlock(p,state,2);
                    if(block==Blocks.COARSE_DIRT&&floor<surface&&(x+z)%4==0)
                        for(int y=surface-2;y>=floor-1&&y>=surface-8;y--)if(clip.isInside(new BlockPos(x,y,z)))world.setBlock(new BlockPos(x,y,z),Blocks.SPRUCE_LOG.defaultBlockState(),2);
                }
            return;
        }
        super.postProcess(world,structures,generator,RandomSource.create(seed),clip,chunk,pivot);
        // Only explicit bottom foundation blocks get supports. Untouched template voids keep
        // natural terrain/water. Supports never write outside the currently decorated chunk.
        for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,Blocks.MOSSY_COBBLESTONE))support(world,clip,info);
        for(StructureTemplate.StructureBlockInfo info:template.filterBlocks(templatePosition,placeSettings,Blocks.SPRUCE_LOG))
            if(info.state().getValue(RotatedPillarBlock.AXIS)==net.minecraft.core.Direction.Axis.Y)support(world,clip,info);
        return;
    }
    private void support(WorldGenLevel world,BoundingBox clip,StructureTemplate.StructureBlockInfo info) {
        if(info.pos().getY()!=templatePosition.getY())return;
        for(int i=1;i<=8;i++) {
            BlockPos p=info.pos().below(i);if(!clip.isInside(p))continue;
            BlockState old=world.getBlockState(p);
            if(old.isSolid()&&!old.is(net.minecraft.tags.BlockTags.LEAVES))break;
            world.setBlock(p,info.state(),2);
        }
    }
    @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor world,RandomSource random,BoundingBox box) {
        // Markers are deterministic and clipped by TemplateStructurePiece. No ticking spawners.
        if(marker.startsWith("loot:")) {
            Random choice=new Random(seed^pos.asLong());
            if((marker.endsWith("_home")&&choice.nextInt(4)==0)||(marker.endsWith("_remnants")&&choice.nextBoolean())) {
                world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);return;
            }
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
            if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)
                ((net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)world.getBlockEntity(pos)).setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/"+marker.substring(5))),seed^pos.asLong());
        } else if(marker.startsWith("encounter:")) {
            boolean water=!marker.endsWith("kikimora");
            world.setBlock(pos,(water?Blocks.WATER:Blocks.AIR).defaultBlockState(),2);
            if(encounterProcessed)return;
            encounterProcessed=true;
            if(new Random(seed^pos.asLong()).nextInt(water?8:10)!=0 || world.getLevel().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return;
            net.minecraft.world.entity.Mob mob=marker.endsWith("kikimora")?org.slavicmyths.registry.ModEntities.KIKIMORA.get().create(world.getLevel()):
                marker.endsWith("rusalka")?org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world.getLevel()):org.slavicmyths.registry.ModEntities.VODYANOY.get().create(world.getLevel());
            if(mob==null)return;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            if(!world.noCollision(mob))return;
            if(mob instanceof org.slavicmyths.entity.LandSpiritEntity)((org.slavicmyths.entity.LandSpiritEntity)mob).home=pos;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);
            world.addFreshEntity(mob);
        }
    }
}
