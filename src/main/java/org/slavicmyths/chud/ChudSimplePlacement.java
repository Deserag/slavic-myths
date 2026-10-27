package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;

/** Full occupied-column roof checks; natural terrain may be excavated, protected buildings may not. */
public final class ChudSimplePlacement {
    /** Final roof clearance checks every occupied X/Z column; the coarse survey only rejects cheaply. */
    private static Integer exactHeight(Structure.GenerationContext c,List<ChudGeometry.Fill> fills,int candidate){
        int minX=fills.stream().mapToInt(f->f.box().minX()).min().orElseThrow(),maxX=fills.stream().mapToInt(f->f.box().maxX()).max().orElseThrow();
        int minZ=fills.stream().mapToInt(f->f.box().minZ()).min().orElseThrow(),maxZ=fills.stream().mapToInt(f->f.box().maxZ()).max().orElseThrow();
        int depth=maxZ-minZ+1;int[] roofs=new int[(maxX-minX+1)*depth];Arrays.fill(roofs,Integer.MIN_VALUE);
        int bottom=fills.stream().mapToInt(f->f.box().minY()).min().orElseThrow(),top=fills.stream().mapToInt(f->f.box().maxY()).max().orElseThrow();
        for(var fill:fills){ChudSearchBudget.check();var b=fill.box();for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++){int i=(x-minX)*depth+z-minZ;roofs[i]=Math.max(roofs[i],b.maxY());}}
        int y=candidate;
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){int roof=roofs[(x-minX)*depth+z-minZ];if(roof==Integer.MIN_VALUE)continue;ChudSearchBudget.check();
            int ground=ChudRegionPlanner.surface(c,x,z);
            y=Math.min(y,ground-20-roof);
            if(y+bottom<c.heightAccessor().getMinBuildHeight()+6)return null;
        }
        return ChudTerrainPolicy.fits(y,bottom,top,c.heightAccessor().getMinBuildHeight()+5,c.heightAccessor().getMaxBuildHeight())?y:null;
    }
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
            anchor=Math.min(anchor,ChudTerrainPolicy.roofAnchor(ground,roof,false));
        }
        if(!ChudTerrainPolicy.fits(anchor,bottom,top,c.heightAccessor().getMinBuildHeight()+5,c.heightAccessor().getMaxBuildHeight())){
            if(Boolean.getBoolean("slavicmyths.chudPlacementDiagnostics"))com.mojang.logging.LogUtils.getLogger().info("Chud vertical refusal {} anchor={} construction={}..{} dimension={}..{}",c.chunkPos(),anchor,anchor+bottom,anchor+top,c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());
            return null;
        }
        return anchor;
    }
    public static ChudPiece city(Structure.GenerationContext c,boolean above,boolean attached) {
        try(var survey=new ChudRegionPlanner.TerrainScope()){return cityPlan(c,above,attached);}
    }
    private static ChudPiece cityPlan(Structure.GenerationContext c,boolean above,boolean attached) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();long seed=c.seed()^c.chunkPos().toLong()^0x43485544L;
        var random=new Random(seed);int variant=random.nextInt(3),rotation=random.nextInt(4);
        for(int size=variant;size>=0;size--)for(int entry:new int[]{56,40}){
            // Keep the original vestibule when it fits; on low ground lower only
            // this landing, rather than exposing the roof or cutting bedrock.
            var probe=new ChudPlan(seed,new BlockPos(x,0,z),size,rotation,entry);
            var extension=attached?new AbandonedPlan(seed,probe.anchor,rotation,AbandonedPlan.Kind.ATTACHED_SMALL,entry,probe.cityId(),0):null;
            var fills=new ArrayList<>(ChudGeometry.build(probe,4));if(extension!=null)fills.addAll(AbandonedGeometry.build(extension));
            Integer y=height(c,fills,above);if(y==null)continue;
            if(!above){y=exactHeight(c,fills,y);if(y==null)continue;}
            var plan=new ChudPlan(seed,new BlockPos(x,y,z),size,rotation,entry);
            extension=attached?new AbandonedPlan(seed,plan.anchor,rotation,AbandonedPlan.Kind.ATTACHED_SMALL,entry,plan.cityId(),0):null;
            var piece=new ChudPiece(plan,extension);
            piece.roofShell=4;
            piece.access=above?null:ChudSurfaceAccess.city(c,plan);
            if(!above&&piece.access==null){if(Boolean.getBoolean("slavicmyths.chudPlacementDiagnostics"))com.mojang.logging.LogUtils.getLogger().info("Chud city {} variant={} vestibule={} refused: no safe dry surface route",c.chunkPos(),size,entry);continue;}
            if(!above)piece.enableRoofCover();if(ChudPlacement.clearFull(c,piece,piece.access))return piece;
        }
        return null;
    }
    public static AbandonedPiece abandoned(Structure.GenerationContext c,boolean above,boolean great) {
        try(var survey=new ChudRegionPlanner.TerrainScope()){return abandonedPlan(c,above,great);}
    }
    private static AbandonedPiece abandonedPlan(Structure.GenerationContext c,boolean above,boolean great) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();long seed=c.seed()^c.chunkPos().toLong()^0x4142414e444f4eL;
        var random=new Random(seed);int rotation=random.nextInt(4),flavor=random.nextInt(3);
        for(int profile=0;profile<(great?3:1);profile++){
            var kind=great?AbandonedPlan.Kind.INDEPENDENT_GREAT:AbandonedPlan.Kind.INDEPENDENT_MEDIUM;
            var probe=new AbandonedPlan(seed,new BlockPos(x,0,z),rotation,kind,32,null,flavor,profile);
            var fills=AbandonedGeometry.build(probe);Integer y=height(c,fills,above);if(y==null)continue;
            if(!above){y=exactHeight(c,fills,y);if(y==null)continue;}
            var plan=new AbandonedPlan(seed,new BlockPos(x,y,z),rotation,kind,32,null,flavor,profile);
            var piece=new AbandonedPiece(plan);piece.access=above?null:ChudSurfaceAccess.abandoned(c,plan);
            if(!above&&piece.access==null&&Boolean.getBoolean("slavicmyths.chudPlacementDiagnostics"))com.mojang.logging.LogUtils.getLogger().info("Chud abandoned {} kind={} shaft={} refused: no safe dry surface route",c.chunkPos(),kind,profile);
            if(!above)piece.enableRoofCover();if((above||piece.access!=null)&&ChudPlacement.clearFull(c,piece,piece.access))return piece;
        }
        return null;
    }
    private ChudSimplePlacement() {}
}
