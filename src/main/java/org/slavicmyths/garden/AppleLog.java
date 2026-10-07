package org.slavicmyths.garden;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.*;

public final class AppleLog extends RotatedPillarBlock {
    public static final BooleanProperty CROWN_ANCHOR=BooleanProperty.create("crown_anchor");
    private static final MapCodec<AppleLog> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.fieldOf("stripped_id").forGetter(b->b.strippedId),propertiesCodec()).apply(i,AppleLog::new));
    private final String strippedId;
    public AppleLog(String strippedId,Properties properties){super(properties);this.strippedId=strippedId;registerDefaultState(defaultBlockState().setValue(CROWN_ANCHOR,false));}
    @Override public MapCodec<AppleLog> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(CROWN_ANCHOR);}
    @Override protected boolean isRandomlyTicking(BlockState s){return s.getValue(CROWN_ANCHOR);}
    @Override protected void randomTick(BlockState s,ServerLevel level,BlockPos anchor,RandomSource random){
        if(!s.getValue(CROWN_ANCHOR)||random.nextInt(8)!=0)return;var base=anchor.below(4);
        int start=random.nextInt(AppleTreeShape.CANOPY.size());
        for(int n=0;n<AppleTreeShape.CANOPY.size();n++){
            var off=AppleTreeShape.CANOPY.get((start+n)%AppleTreeShape.CANOPY.size());var target=base.offset(off);
            if(!level.hasChunkAt(target)||!level.isEmptyBlock(target))continue;boolean adjacent=false;
            for(var dir:Direction.values()){
                var neighbor=target.relative(dir);if(!level.hasChunkAt(neighbor))continue;
                var state=level.getBlockState(neighbor);if(state.is(Gardens.APPLE_LOGS)||state.is(Gardens.block("apple_leaves"))){adjacent=true;break;}
            }
            if(adjacent){level.setBlock(target,AppleTreeShape.leaf(off,0),3);return;}
        }
    }
    @Override public BlockState getToolModifiedState(BlockState s,UseOnContext c,ItemAbility ability,boolean simulate){
        if(!strippedId.isEmpty()&&ability==ItemAbilities.AXE_STRIP&&c.getItemInHand().canPerformAction(ability))return Gardens.block(strippedId).defaultBlockState().setValue(AXIS,s.getValue(AXIS));
        return super.getToolModifiedState(s,c,ability,simulate);
    }
    @Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos p,Direction d){return 5;}
    @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos p,Direction d){return 5;}
}
