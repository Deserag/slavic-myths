package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public final class QuicksandBlock extends Block {
    public QuicksandBlock(Properties p){super(p);}
    @Override public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState s,net.minecraft.world.level.BlockGetter w,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext context){
        if(context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ec && ec.getEntity() instanceof SandKingEntity)return net.minecraft.world.phys.shapes.Shapes.block();
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }
    @Override public void entityInside(BlockState s,Level w,BlockPos p,Entity e){if(!(e instanceof SandKingEntity))e.makeStuckInBlock(s,new Vec3(.28,.18,.28));}
}
