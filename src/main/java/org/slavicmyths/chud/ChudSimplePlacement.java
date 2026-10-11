package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;

/** Finite roof sampling; caves, fluids and competing buildings do not veto excavation. */
public final class ChudSimplePlacement {
    private static Integer height(Structure.GenerationContext c, List<ChudGeometry.Fill> fills, boolean above) {
        int bottom=fills.stream().mapToInt(f->f.box().minY()).min().orElse(0);
        int top=fills.stream().mapToInt(f->f.box().maxY()).max().orElse(0);
        int minX=fills.stream().mapToInt(f->f.box().minX()).min().orElse(0),maxX=fills.stream().mapToInt(f->f.box().maxX()).max().orElse(0);
        int minZ=fills.stream().mapToInt(f->f.box().minZ()).min().orElse(0),maxZ=fills.stream().mapToInt(f->f.box().maxZ()).max().orElse(0);
        int nx=(maxX-minX)/16+1,nz=(maxZ-minZ)/16+1;
        int[] roofs=new int[nx*nz];Arrays.fill(roofs,Integer.MIN_VALUE);
        for(var fill:fills){ChudSearchBudget.check();var b=fill.box();
            for(int ix=(b.minX()-minX)/16;ix<=(b.maxX()-minX)/16;ix++)for(int iz=(b.minZ()-minZ)/16;iz<=(b.maxZ()-minZ)/16;iz++)roofs[ix*nz+iz]=Math.max(roofs[ix*nz+iz],b.maxY());
        }
        var terrain=new ChudStructure.Terrain(c);int anchor=above?Integer.MIN_VALUE:Integer.MAX_VALUE;
        for(int ix=0;ix<nx;ix++)for(int iz=0;iz<nz;iz++){
            ChudSearchBudget.check();int x=minX+ix*16,z=minZ+iz*16,roof=roofs[ix*nz+iz];
            if(roof==Integer.MIN_VALUE)continue;
            int sx=Math.min(x+8,maxX),sz=Math.min(z+8,maxZ),ground=terrain.surface(sx,sz);
            if(above){anchor=Math.max(anchor,ground+1-bottom);continue;}
            boolean water=!terrain.column(sx,sz).getBlock(ground+1).getFluidState().isEmpty();
            anchor=Math.min(anchor,ChudTerrainPolicy.roofAnchor(ground,roof,water));
        }
        if(!ChudTerrainPolicy.fits(anchor,bottom,top,c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight()))return null;
        return anchor;
    }
    public static ChudPiece city(Structure.GenerationContext c,boolean above,boolean attached) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();long seed=c.seed()^c.chunkPos().toLong()^0x43485544L;
        var random=new Random(seed);int variant=random.nextInt(3),rotation=random.nextInt(4);
        for(int size=variant;size>=0;size--){
            var probe=new ChudPlan(seed,new BlockPos(x,0,z),size,rotation,56);
            var extension=attached?new AbandonedPlan(seed,probe.anchor,rotation,AbandonedPlan.Kind.ATTACHED_SMALL,56,probe.cityId(),0):null;
            var fills=new ArrayList<>(ChudGeometry.build(probe));if(extension!=null)fills.addAll(AbandonedGeometry.build(extension));
            Integer y=height(c,fills,above);if(y==null)continue;
            var plan=new ChudPlan(seed,new BlockPos(x,y,z),size,rotation,56);
            extension=attached?new AbandonedPlan(seed,plan.anchor,rotation,AbandonedPlan.Kind.ATTACHED_SMALL,56,plan.cityId(),0):null;
            var piece=new ChudPiece(plan,extension);
            if(ChudPlacement.clear(c,piece.getBoundingBox()))return piece;
        }
        return null;
    }
    public static AbandonedPiece abandoned(Structure.GenerationContext c,boolean above,boolean great) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();long seed=c.seed()^c.chunkPos().toLong()^0x4142414e444f4eL;
        var random=new Random(seed);int rotation=random.nextInt(4),flavor=random.nextInt(3);
        for(int profile=0;profile<(great?3:1);profile++){
            var kind=great?AbandonedPlan.Kind.INDEPENDENT_GREAT:AbandonedPlan.Kind.INDEPENDENT_MEDIUM;
            var probe=new AbandonedPlan(seed,new BlockPos(x,0,z),rotation,kind,32,null,flavor,profile);
            Integer y=height(c,AbandonedGeometry.build(probe),above);if(y==null)continue;
            var plan=new AbandonedPlan(seed,new BlockPos(x,y,z),rotation,kind,32,null,flavor,profile);
            if(ChudPlacement.clear(c,plan.envelope()))return new AbandonedPiece(plan);
        }
        return null;
    }
    private ChudSimplePlacement() {}
}
