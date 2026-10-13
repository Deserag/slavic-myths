package org.slavicmyths.volkolak;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.mojang.serialization.MapCodec;
public final class PlachTravaBlock extends BushBlock {
 public PlachTravaBlock(){this(BlockBehaviour.Properties.ofFullCopy(Blocks.FERN).noCollission().noOcclusion());}
 private PlachTravaBlock(Properties p){super(p);}
 private static final MapCodec<PlachTravaBlock> CODEC=simpleCodec(PlachTravaBlock::new);
 @Override protected MapCodec<PlachTravaBlock> codec(){return CODEC;}
}
