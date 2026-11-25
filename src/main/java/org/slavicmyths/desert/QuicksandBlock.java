package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public final class QuicksandBlock extends Block {
    public QuicksandBlock(Properties p){super(p);}
    @Override public void entityInside(BlockState s,Level w,BlockPos p,Entity e){e.makeStuckInBlock(s,new Vec3(.28,.18,.28));}
}
