package org.slavicmyths.world;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.registry.*;
/** Each small building stays within its generating chunk; no runtime structure searches. */
public final class HomesteadFeature extends Feature<NoFeatureConfig> {
    private final boolean barn;
    public HomesteadFeature(boolean barn){super(NoFeatureConfig.CODEC);this.barn=barn;}
    @Override public boolean place(ISeedReader w,ChunkGenerator g,Random r,BlockPos o,NoFeatureConfig c){
        if(w.getLevel().dimension()!=World.OVERWORLD)return false;
        int x=(o.getX()&~15)+8,z=(o.getZ()&~15)+8;BlockPos p=new BlockPos(x,w.getHeight(Heightmap.Type.WORLD_SURFACE_WG,x,z),z);
        int rx=barn?5:3,rz=barn?4:3,wall=barn?4:3;
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++){
            Block ground=w.getBlockState(p.offset(dx,-1,dz)).getBlock();if(ground!=Blocks.GRASS_BLOCK && ground!=Blocks.DIRT && ground!=Blocks.PODZOL && ground!=Blocks.COARSE_DIRT)return false;
            for(int dy=0;dy<wall+4;dy++){BlockPos at=p.offset(dx,dy,dz);if(!w.getFluidState(at).isEmpty() || (!w.isEmptyBlock(at) && w.getBlockState(at).getMaterial()!=net.minecraft.block.material.Material.PLANT && w.getBlockState(at).getMaterial()!=net.minecraft.block.material.Material.REPLACEABLE_PLANT))return false;}
        }
        for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++){
            set(w,p.offset(dx,-1,dz),Blocks.COBBLESTONE);set(w,p.offset(dx,0,dz),Blocks.AIR);
            if(Math.abs(dx)==rx || Math.abs(dz)==rz)for(int dy=0;dy<wall;dy++)set(w,p.offset(dx,dy,dz),(Math.abs(dx)==rx && Math.abs(dz)==rz)?Blocks.DARK_OAK_LOG:Blocks.SPRUCE_PLANKS);
            int roof=wall+(rx-Math.abs(dx))/2;
            if(barn || r.nextInt(18)!=0)set(w,p.offset(dx,roof,dz),Blocks.DARK_OAK_PLANKS);
            if(Math.abs(dz)==rz)for(int dy=wall;dy<roof;dy++)set(w,p.offset(dx,dy,dz),Blocks.SPRUCE_PLANKS);
        }
        set(w,p.offset(0,0,-rz),Blocks.AIR);set(w,p.offset(0,1,-rz),Blocks.AIR);
        set(w,p.offset(rx,1,0),Blocks.GLASS_PANE);
        for(int dz=-rz+1;dz<rz;dz++)set(w,p.offset(-rx+1,0,dz),barn?Blocks.HAY_BLOCK:Blocks.SPRUCE_SLAB);
        if(barn){for(int dx=-rx+1;dx<rx;dx++)set(w,p.offset(dx,3,0),Blocks.DARK_OAK_LOG);set(w,p.offset(3,0,2),Blocks.HAY_BLOCK);set(w,p.offset(3,1,2),Blocks.HAY_BLOCK);}
        set(w,p.offset(-2,0,1),ModBlocks.BATH_STOVE.get());
        if(!barn){set(w,p.offset(1,0,2),ModBlocks.WOODEN_TUB.get());for(int dy=1;dy<=wall+3;dy++)set(w,p.offset(-2,dy,1),Blocks.COBBLESTONE_WALL);}
        set(w,p.offset(2,0,1),Blocks.CHEST);TileEntity te=w.getBlockEntity(p.offset(2,0,1));
        if(te instanceof ChestTileEntity)((ChestTileEntity)te).setLootTable(new ResourceLocation("slavicmyths","chests/"+(barn?"old_barn":"bathhouse")),r.nextLong());
        // Barns provide the hunting environment; 0.9.0 Ovinnik uses rare night placement, not guaranteed worldgen.
        LandSpiritEntity spirit=barn?null:ModEntities.BANNIK.get().create(w.getLevel());
        if(spirit!=null){spirit.moveTo(x+.5,p.getY(),z+.5,180,0);spirit.home=p;spirit.setPersistenceRequired();spirit.finalizeSpawn(w,w.getCurrentDifficultyAt(p),SpawnReason.STRUCTURE,null,null);w.addFreshEntity(spirit);}
        return true;
    }
    private void set(ISeedReader w,BlockPos p,Block b){w.setBlock(p,b.defaultBlockState(),2);}
}
