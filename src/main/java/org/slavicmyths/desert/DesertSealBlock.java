package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public final class DesertSealBlock extends Block {
    public static final BooleanProperty OPEN=BooleanProperty.create("open");
    private static final VoxelShape MARKER=Block.box(6,6,6,10,10,10);
    private static final int MAX_NETWORK=4096;
    public DesertSealBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(OPEN,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(OPEN);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return s.getValue(OPEN)?MARKER:Shapes.block();}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return s.getValue(OPEN)?Shapes.empty():Shapes.block();}
    @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
        if(w.isClientSide)return InteractionResult.SUCCESS;
        if(!player.mayBuild())return InteractionResult.PASS;
        boolean open=!s.getValue(OPEN);
        Set<BlockPos> group=new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        group.add(p.immutable());queue.add(p.immutable());
        while(!queue.isEmpty()){
            BlockPos current=queue.removeFirst();
            for(Direction d:Direction.values()){
                BlockPos n=current.relative(d);
                if(w.isOutsideBuildHeight(n))continue;
                if(!w.hasChunkAt(n)){player.displayClientMessage(Component.translatable("desert.slavicmyths.seal_unloaded"),true);return InteractionResult.FAIL;}
                if(w.getBlockState(n).is(this)&&group.add(n)){
                    if(group.size()>MAX_NETWORK){player.displayClientMessage(Component.translatable("desert.slavicmyths.seal_limit"),true);return InteractionResult.FAIL;}
                    queue.addLast(n);
                }
            }
        }
        if(!open)for(BlockPos n:group)if(!w.getEntitiesOfClass(LivingEntity.class,new AABB(n),e->!e.isSpectator()).isEmpty()){
            player.displayClientMessage(Component.translatable("desert.slavicmyths.seal_occupied"),true);return InteractionResult.FAIL;
        }
        for(BlockPos n:group){BlockState state=w.getBlockState(n);if(state.is(this)&&state.getValue(OPEN)!=open)w.setBlock(n,state.setValue(OPEN,open),Block.UPDATE_ALL);}
        return InteractionResult.CONSUME;
    }
}
