package org.slavicmyths.village;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.worldgen.LandTerrain;

/** Bounded local footings, processed once at template placement, clipped by native placement. */
public final class VillageFoundation extends StructureProcessor {
    public static final VillageFoundation INSTANCE = new VillageFoundation();
    public static final MapCodec<VillageFoundation> CODEC = MapCodec.unit(INSTANCE);
    private static final DeferredRegister<StructureProcessorType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR,"slavicmyths");
    public static final DeferredHolder<StructureProcessorType<?>,StructureProcessorType<VillageFoundation>> TYPE = TYPES.register("village_foundation",()->()->CODEC);
    public static void init(IEventBus bus){TYPES.register(bus);}
    @Override protected StructureProcessorType<?> getType(){return TYPE.get();}
    @Override public StructureTemplate.StructureBlockInfo processBlock(net.minecraft.world.level.LevelReader world,BlockPos origin,BlockPos pivot,StructureTemplate.StructureBlockInfo raw,StructureTemplate.StructureBlockInfo processed,StructurePlaceSettings settings){
        if(world.getBlockState(processed.pos()).is(Blocks.BEDROCK))return new StructureTemplate.StructureBlockInfo(processed.pos(),Blocks.BEDROCK.defaultBlockState(),null);
        return processed;
    }
    @Override public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor world,BlockPos origin,BlockPos pivot,List<StructureTemplate.StructureBlockInfo> raw,List<StructureTemplate.StructureBlockInfo> processed,StructurePlaceSettings settings){
        var result=new ArrayList<>(processed);
        Map<Long,StructureTemplate.StructureBlockInfo> bases=new HashMap<>();
        Set<BlockPos> authored=new HashSet<>();for(var info:processed)if(!info.state().isAir())authored.add(info.pos());
        for(var info:processed){
            boolean pillar=info.state().is(net.minecraft.tags.BlockTags.LOGS)&&info.state().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)&&info.state().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)==net.minecraft.core.Direction.Axis.Y;
            if(info.pos().getY()>origin.getY()+2||!(pillar||info.state().is(Blocks.COBBLESTONE)||info.state().is(Blocks.STONE_BRICKS)))continue;
            long column=BlockPos.asLong(info.pos().getX(),0,info.pos().getZ());
            bases.merge(column,info,(a,b)->a.pos().getY()<=b.pos().getY()?a:b);
        }
        for(var info:bases.values()){
            for(int depth=1;depth<=8;depth++){
                var p=info.pos().below(depth);
                if(p.getY()<world.getMinBuildHeight()||!world.hasChunkAt(p)||authored.contains(p))break;
                var old=world.getBlockState(p);
                if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()||!LandTerrain.vegetation(old)&&old.getFluidState().isEmpty())break;
                result.add(new StructureTemplate.StructureBlockInfo(p,info.state(),null));
            }
        }
        return result;
    }
}

