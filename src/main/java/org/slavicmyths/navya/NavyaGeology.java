package org.slavicmyths.navya;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
/** New-chunk local-modification stage, before vanilla ore placement. */
public final class NavyaGeology extends Feature<NoneFeatureConfiguration> {
    public NavyaGeology(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
        var world=context.level();int bx=context.origin().getX()&~15,bz=context.origin().getZ()&~15;
        var origin=new BlockPos(bx,80,bz);if(NavyaWorldgen.protectedChunk(world,origin))return false;
        boolean changed=false;
        for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++) {
            var point=new BlockPos(x,80,z);if(!world.getBiome(point).is(NavyaWorldgen.BIOME))continue;
            // A lava sea remains a lava sea: no forest floor over every ocean.
            if(!world.getFluidState(new BlockPos(x,31,z)).isEmpty())continue;
            int nativeFloor=33;
            for(int y=144;y>=33;y--)if(world.getBlockState(new BlockPos(x,y,z)).is(Blocks.NETHERRACK)){nativeFloor=y;break;}
            int neighbors=1;
            for(Direction d:Direction.Plane.HORIZONTAL) {
                var neighbor=point.relative(d,12);
                if(world.hasChunkAt(neighbor) && world.getBiome(neighbor).is(NavyaWorldgen.BIOME))neighbors++;
            }
            double blend=neighbors/5.;var target=NavyaProfile.at(world.getSeed(),x,z);
            int upper=(int)Math.round(nativeFloor*(1-blend)+target.upper()*blend);
            int middle=(int)Math.round(Math.min(nativeFloor,upper-6)*(1-blend)+target.middle()*blend);
            var column=new NavyaProfile.Column(target.lower(),middle,upper,target.pillar());
            for(int y=33;y<=144;y++) {
                var pos=new BlockPos(x,y,z);var state=world.getBlockState(pos);
                if(!world.ensureCanWrite(pos) || !(state.isAir() || state.is(Blocks.NETHERRACK)))continue;
                var desired=NavyaProfile.solid(column,y)?Blocks.NETHERRACK.defaultBlockState():Blocks.CAVE_AIR.defaultBlockState();
                if(state!=desired){world.setBlock(pos,desired,2);changed=true;}
            }
        }
        return changed;
    }
}
