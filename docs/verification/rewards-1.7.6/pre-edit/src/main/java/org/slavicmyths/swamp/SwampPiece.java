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
    private String name;private long seed;private boolean encounterProcessed;private final java.util.Set<Long> processedMarkers=new java.util.HashSet<>();
    SwampPiece(StructureTemplateManager manager,String name,BlockPos pos,long seed) {
        super(SwampStructures.PIECE.get(),0,manager,ResourceLocation.fromNamespaceAndPath("slavicmyths","swamp/"+name),"slavicmyths:swamp/"+name,settings(),pos);this.name=name;this.seed=seed;this.templatePosition=pos;load(manager);
    }
    public SwampPiece(StructureTemplateManager manager,CompoundTag nbt) {
        super(SwampStructures.PIECE.get(),modernTag(nbt),manager,id->settings());name=nbt.getString("SwampTemplate");seed=nbt.getLong("SwampSeed");encounterProcessed=nbt.getBoolean("EncounterProcessed");for(long marker:nbt.getLongArray("SwampProcessedMarkers"))processedMarkers.add(marker);load(manager);
    }
private void load(StructureTemplateManager ignored){boundingBox=new BoundingBox(boundingBox.minX(),boundingBox.minY()-8,boundingBox.minZ()-0,boundingBox.maxX(),boundingBox.maxY(),boundingBox.maxZ());}
 private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 private static CompoundTag modernTag(CompoundTag original){CompoundTag n=original.copy();String name=n.getString("SwampTemplate"); n.putString("Template","slavicmyths:swamp/"+name);return n;}

    @Override protected synchronized void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag nbt){super.addAdditionalSaveData(context,nbt);nbt.putString("SwampTemplate",name);nbt.putLong("SwampSeed",seed);nbt.putBoolean("EncounterProcessed",encounterProcessed);nbt.putLongArray("SwampProcessedMarkers",processedMarkers.stream().mapToLong(Long::longValue).toArray());}
    @Override public synchronized void postProcess(WorldGenLevel world,StructureManager structures,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
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
        // Vanilla resets the box to the template; retain foundations and approaches across chunks.
        BoundingBox acceptedBounds=boundingBox;
        try { super.postProcess(world,structures,generator,RandomSource.create(seed),clip,chunk,pivot); } finally { boundingBox=acceptedBounds; }
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
        if(!box.isInside(pos))return;
        if(marker.startsWith("loot:")) {
            var existing=world.getBlockEntity(pos);
            if(existing instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity&&existing.getPersistentData().getBoolean("SlavicSwampLoot"))return;
            Random choice=new Random(seed^pos.asLong());
            if(!name.startsWith("v097_")&&((marker.endsWith("_home")&&choice.nextInt(4)==0)||(marker.endsWith("_remnants")&&choice.nextBoolean())))return;
            world.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
            if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container){
                container.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/"+marker.substring(5))),seed^pos.asLong());
                container.getPersistentData().putBoolean("SlavicSwampLoot",true);container.setChanged();
            }
        } else if(marker.startsWith("encounter:")) {
            long key=pos.asLong();
            if(processedMarkers.contains(key)||!name.startsWith("v097_")&&encounterProcessed)return;
            processedMarkers.add(key);if(!name.startsWith("v097_"))encounterProcessed=true;
            boolean bog=marker.contains("bolotnik"),elite=marker.endsWith("_elite");
            if(world.getLevel().getDifficulty()==Difficulty.PEACEFUL||!elite&&new Random(seed^key).nextInt(bog?3:8)!=0)return;
            net.minecraft.world.entity.Mob mob=bog?org.slavicmyths.registry.ModEntities.BOLOTNIK.get().create(world.getLevel()):
                marker.endsWith("kikimora")?org.slavicmyths.registry.ModEntities.KIKIMORA.get().create(world.getLevel()):
                marker.endsWith("rusalka")?org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world.getLevel()):org.slavicmyths.registry.ModEntities.VODYANOY.get().create(world.getLevel());
            if(mob==null)return;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            if(!world.noCollision(mob))return;
            if(mob instanceof org.slavicmyths.entity.LandSpiritEntity spirit)spirit.home=pos;
            mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);
            if(elite){mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(112);mob.setHealth(112);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(9);mob.setCustomName(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.bolotnik_elite"));}
            mob.setPersistenceRequired();world.addFreshEntity(mob);
        }
    }
}
