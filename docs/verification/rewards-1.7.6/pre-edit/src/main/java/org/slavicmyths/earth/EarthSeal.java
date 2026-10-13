package org.slavicmyths.earth;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.SoundType;
public final class EarthSeal extends Block {
    public static final MapCodec<EarthSeal> CODEC=simpleCodec(p->new EarthSeal());
    public static final IntegerProperty CRACK=IntegerProperty.create("crack",0,2);
    public EarthSeal(){super(BlockBehaviour.Properties.of().noOcclusion().strength(1.2F,3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).sound(SoundType.STONE).lightLevel(s->s.getValue(CRACK)>0?8:3));registerDefaultState(stateDefinition.any().setValue(CRACK,0));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CRACK);}
    @Override public void attack(BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player player){
        if(level instanceof net.minecraft.server.level.ServerLevel server)EarthArena.activate(server,pos,player);
    }
}
