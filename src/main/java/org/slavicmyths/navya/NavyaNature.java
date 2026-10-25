package org.slavicmyths.navya;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
/** Re-skin after vanilla ores, then flora; no retro-generation or per-tick scanning. */
public final class NavyaNature extends Feature<NoneFeatureConfiguration> {
    public NavyaNature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
        var w=context.level();var random=context.random();int bx=context.origin().getX()&~15,bz=context.origin().getZ()&~15;
        var origin=new BlockPos(bx,80,bz);if(NavyaWorldgen.protectedChunk(w,origin))return false;
        boolean heart=NavyaWorldgen.heartChunk(w,origin),changed=false;
        for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++) {
            if(!w.getBiome(new BlockPos(x,80,z)).is(NavyaWorldgen.BIOME))continue;
            var column=NavyaProfile.at(w.getSeed(),x,z);
            for(int y=33;y<=144;y++) {
                var pos=new BlockPos(x,y,z);var state=w.getBlockState(pos);if(!state.is(Blocks.NETHERRACK))continue;
                boolean floor=w.isEmptyBlock(pos.above()),ceiling=w.isEmptyBlock(pos.below());
                String material="navya_stone";
                if(floor && y>=column.middle()+3)material=random.nextInt(4)==0?"mossy_navya_soil":"navya_soil";
                else if(y>=column.lower()+7 && y<=column.middle()+5)material=random.nextInt(3)==0?"rooted_navya_stone":"navya_stone";
                else if(random.nextInt(7)==0)material="cracked_navya_stone";
                w.setBlock(pos,NavyaBlocks.get(material).defaultBlockState(),2);changed=true;
                if(heart)continue;
                if(floor && y<=column.middle()-6 && random.nextInt(30)==0)spike(w,pos.above(),Direction.UP,random.nextInt(4)+2);
                else if(floor && random.nextInt(17)==0) {
                    String plant=y>=column.middle()+3?(random.nextBoolean()?"navya_shrub":"navya_dry_branches"):random.nextInt(4)==0?"ghost_fungus":random.nextInt(5)==0?"navya_growth":"navya_roots";
                    var flora=NavyaBlocks.get(plant).defaultBlockState();
                    if(flora.canSurvive(w,pos.above()))w.setBlock(pos.above(),flora,2);
                }
                if(ceiling && y<=column.upper()-5 && random.nextInt(55)==0) {
                    if(random.nextBoolean())spike(w,pos.below(),Direction.DOWN,random.nextInt(4)+2);
                    else w.setBlock(pos.below(),NavyaBlocks.get("navya_hanging_roots").defaultBlockState(),2);
                }
                if(floor && y<column.middle()-7 && random.nextInt(500)==0) {
                    for(int h=1;h<=10 && w.isEmptyBlock(pos.above(h));h++)w.setBlock(pos.above(h),h%3==0?Blocks.BONE_BLOCK.defaultBlockState():NavyaBlocks.get(h%2==0?"grove_bone_column":"grove_bone_block").defaultBlockState(),2);
                }
            }
        }
        if(!heart)for(int attempt=0;attempt<3;attempt++) {
            int x=bx+2+random.nextInt(12),z=bz+2+random.nextInt(12);
            if(!w.getBiome(new BlockPos(x,80,z)).is(NavyaWorldgen.BIOME))continue;
            for(int y=144;y>=40;y--) {
                var pos=new BlockPos(x,y,z);
                if(NavyaBlocks.soil(w.getBlockState(pos)) && w.isEmptyBlock(pos.above())) {
                    int roll=random.nextInt(12);NavyaTreeFeature.grow(w,pos.above(),random,roll==0?2:roll<5?0:1);break;
                }
            }
        }
        return changed;
    }
    private static void spike(net.minecraft.world.level.WorldGenLevel world,BlockPos start,Direction direction,int height) {
        int available=0;
        // Keep at least two air blocks between opposing formations for traversal.
        while(available<height && world.ensureCanWrite(start.relative(direction,available+2)) && world.isEmptyBlock(start.relative(direction,available+2)))available++;
        for(int i=0;i<available;i++) {
            var pos=start.relative(direction,i);
            if(!world.ensureCanWrite(pos) || !world.isEmptyBlock(pos))break;
            var state=NavyaBlocks.get(i==available-1?"grove_bone_spike":"grove_bone_column").defaultBlockState();
            if(i==available-1)state=state.setValue(NavyaBlocks.BoneSpike.TIP,direction);
            world.setBlock(pos,state,2);
        }
    }
}
