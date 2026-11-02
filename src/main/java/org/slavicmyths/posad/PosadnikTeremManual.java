package org.slavicmyths.posad;

import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.furniture.FurnitureBlock;
import org.slavicmyths.rpg.tools.ToolLogOrigins;
import org.slavicmyths.worldgen.ManualStructureJobs;
import org.slavicmyths.worldgen.ManualStructureRecords;
import org.slavicmyths.worldgen.ManualTerrain;

/**
 * Manually place a standalone A+ from the SAME source as the native city.
 * Deliberately OP-only, only in a fully loaded, unoccupied, natural test area.
 * Never repairs or migrates a saved city.
 */
public final class PosadnikTeremManual {
    private static boolean late(BlockState state){
        if(state.getBlock() instanceof FurnitureBlock furniture && "shelf".equals(furniture.kind))return true;
        return (state.is(Blocks.LANTERN)||state.is(Blocks.SOUL_LANTERN))&&state.getValue(LanternBlock.HANGING);
    }
    public static int place(CommandSourceStack source,BlockPos origin){
        if(!source.hasPermission(2))return 0;
        var world=source.getLevel();
        BlockPos end=origin.offset(PosadnikTerem.MAX_X-PosadnikTerem.MIN_X,PosadnikTerem.HEIGHT-1,PosadnikTerem.MAX_Z-PosadnikTerem.MIN_Z);
        if(origin.getY()<world.getMinBuildHeight()||end.getY()>=world.getMaxBuildHeight()||
            !world.getWorldBorder().isWithinBounds(origin)||!world.getWorldBorder().isWithinBounds(end)){
            source.sendFailure(Component.literal("Посадничий терем: вне допустимой высоты/границы"));return 0;
        }
        BoundingBox bounds=new BoundingBox(origin.getX(),origin.getY(),origin.getZ(),end.getX(),end.getY(),end.getZ());
        for(int cx=origin.getX()>>4;cx<=end.getX()>>4;cx++)for(int cz=origin.getZ()>>4;cz<=end.getZ()>>4;cz++){
            if(world.getChunkSource().getChunkNow(cx,cz)==null){
                source.sendFailure(Component.literal("Посадничий терем: все чанки должны быть загружены"));return 0;
            }
        }
        var records=ManualStructureRecords.get(world);
        if(!records.overlapping(bounds).isEmpty()){
            source.sendFailure(Component.literal("Посадничий терем: пересечение с сохранённой структурой"));return 0;
        }
        var origins=ToolLogOrigins.get(world);
        for(var at:BlockPos.betweenClosed(origin,end)){
            var state=world.getBlockState(at);
            if(state.hasBlockEntity()||world.getBlockEntity(at)!=null||state.is(Blocks.BEDROCK)||origins.blocked(at)||
                    (!state.isAir()&&!ManualStructureJobs.natural(state))){
                source.sendFailure(Component.literal("Посадничий терем: защищённый/чужой блок "+at.toShortString()));return 0;
            }
        }
        java.util.Map<BlockPos,BlockState> foundation;
        try{foundation=ManualTerrain.foundation(world,origin,end);}
        catch(IllegalArgumentException error){source.sendFailure(Component.literal(error.getMessage()));return 0;}
        UUID token=UUID.randomUUID();
        records.add(token,"construction",0,origin,bounds);
        world.getDataStorage().save();
        // The reservation is deliberately retained if any later write/save fails.
        foundation.forEach((at,state)->world.setBlock(at,state,2));
        for(var at:BlockPos.betweenClosed(origin,end))if(!world.getBlockState(at).isAir())world.setBlock(at,Blocks.AIR.defaultBlockState(),2);
        int placed=0;
        for(int pass=0;pass<2;pass++)for(int y=0;y<PosadnikTerem.HEIGHT;y++)
            for(int z=PosadnikTerem.MIN_Z;z<=PosadnikTerem.MAX_Z;z++)
                for(int x=PosadnikTerem.MIN_X;x<=PosadnikTerem.MAX_X;x++){
                    BlockState state=PosadnikTerem.voxel(x,y,z);
                    if(state==null||late(state)!=(pass==1))continue;
                    BlockPos at=origin.offset(x-PosadnikTerem.MIN_X,y,z-PosadnikTerem.MIN_Z);
                    if(state.getBlock() instanceof FurnitureBlock f&&"shelf".equals(f.kind)){
                        world.getChunk(at.getX()>>4,at.getZ()>>4).setBlockState(at,state,false);
                    }else world.setBlock(at,state,2);
                    placed++;
                }
        if(placed!=PosadnikTerem.BLOCK_COUNT)throw new IllegalStateException("Partial Posadnik placement "+placed);
        if(!world.getServer().saveEverything(false,true,true))throw new IllegalStateException("Posadnik save failed; reservation kept");
        records.replace(token,"posad_building/posadnik_terem_2_2_9",0,origin,bounds);
        world.getDataStorage().save();
        source.sendSuccess(()->Component.literal("Посадничий терем A+ поставлен: "+PosadnikTerem.BLOCK_COUNT+" блоков. NPC/квесты не создаются."),false);
        return 1;
    }
    private PosadnikTeremManual(){}
}
