package org.slavicmyths.block;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.slavicmyths.entity.BannikEntity;
public final class BathStoveBlock extends Block {
    public BathStoveBlock(Properties p){super(p);}
    @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand h,BlockRayTraceResult hit){
        for(BannikEntity b:w.getEntitiesOfClass(BannikEntity.class,new AxisAlignedBB(pos).inflate(8)))if(b.offer(p,h))return ActionResultType.sidedSuccess(w.isClientSide);
        return ActionResultType.PASS;
    }
}
