package org.slavicmyths.kurgan;

import net.minecraft.core.BlockPos;
import org.slavicmyths.worldgen.StructureCoverageService;
import org.slavicmyths.worldgen.StructureCoverageService.Rejection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Saved construction terrace and graded approach. Native placement uses actual chunk columns. */
public final class KurganEarthwork {
    public record Site(BlockPos origin, KurganEarthwork earthwork) {}
    public final int x0,z0,width,depth;
    private final int[] floors,tops;
    private boolean adaptive;
    public boolean adaptive(){return adaptive;}
    public final long volume;
    private KurganEarthwork(int x0,int z0,int width,int depth,int[] floors,int[] tops,long volume){
        this.x0=x0;this.z0=z0;this.width=width;this.depth=depth;this.floors=floors;this.tops=tops;this.volume=volume;
    }
    public static KurganEarthwork prepared(int x0,int z0,int width,int depth,int[] floors,int[] tops){
        long volume=0;for(int i=0;i<floors.length;i++)volume+=Math.abs(tops[i]-floors[i]);
        return new KurganEarthwork(x0,z0,width,depth,floors.clone(),tops.clone(),volume);
    }
    /** Native generation chooses a construction height and prepares land, rather than rejecting slope. */
    public static Site plan(ChunkGenerator generator,RandomState random,LevelHeightAccessor height,KurganPlan plan,BlockPos ground){
        var bounds=plan.bounds();int target=Math.max(ground.getY(),height.getMinBuildHeight()+2-bounds.y0);
        if(target+bounds.y1>=height.getMaxBuildHeight()-6){StructureCoverageService.note(Rejection.WORLD_BOUNDS,"plannedRoofY",target+bounds.y1);return null;}
        BlockPos origin=ground.atY(target);int raise=target-ground.getY(),margin=(raise+1)/2+8;
        int x0=bounds.x0-margin,z0=bounds.z0-margin,width=bounds.x1-bounds.x0+1+2*margin;
        java.util.List<KurganPlan.Box> roofs=new java.util.ArrayList<>();
        for(var room:plan.rooms){var box=room.box();if(box.y1<0&&box.x0<=1&&box.x1>=-1)roofs.add(box);}
        for(var link:plan.links)for(int i=0;i<link.steps.size();i++){var box=link.slice(i);if(box.y1<0&&box.x0<=1&&box.x1>=-1)roofs.add(box);}
        int zEnd=Math.max(bounds.z1+margin,plan.radius+10+raise);for(var box:roofs)if(box.z1>=plan.radius+1)zEnd=Math.max(zEnd,box.z1+Math.max(0,target+box.y1+2-ground.getY())+3);
        int depth=zEnd-z0+1;int[] floors=new int[width*depth],tops=new int[floors.length];long volume=0;
        // Sparse exact raw height samples describe saved grading; actual chunk columns are used at placement.
        var samples=new java.util.HashMap<Long,Integer>();boolean flat=generator instanceof net.minecraft.world.level.levelgen.FlatLevelSource;int step=16;int[] waterSamples={0,0};
        if(flat&&StructureCoverageService.baseHeight(generator,origin.getX(),origin.getZ(),Heightmap.Types.WORLD_SURFACE_WG,height,random)-1>ground.getY()){StructureCoverageService.note(Rejection.DEEP_WATER,"wetSamples",1);return null;}
        for(int x=0;x<width;x++)for(int z=0;z<depth;z++){
            int dx=x0+x,dz=z0+z,index=x*depth+z;int sx=Math.floorDiv(dx,step)*step,sz=Math.floorDiv(dz,step)*step;long key=((long)sx<<32)^(sz&0xffffffffL);
            int floor=flat?ground.getY():samples.computeIfAbsent(key,k->{int f=StructureCoverageService.baseHeight(generator,origin.getX()+sx,origin.getZ()+sz,Heightmap.Types.OCEAN_FLOOR_WG,height,random)-1;int surface=StructureCoverageService.baseHeight(generator,origin.getX()+sx,origin.getZ()+sz,Heightmap.Types.WORLD_SURFACE_WG,height,random)-1;waterSamples[0]++;if(surface>f)waterSamples[1]++;return f;});
            int distance=Math.max(Math.max(bounds.x0-dx,dx-bounds.x1),Math.max(bounds.z0-dz,dz-bounds.z1));int top=distance<=0?target+3:floor+Integer.signum(target+3-floor)*Math.max(0,Math.abs(target+3-floor)-2*distance);
            floors[index]=floor;tops[index]=top;volume+=Math.abs(top-floor);
        }
        if(waterSamples[1]>waterSamples[0]/8){StructureCoverageService.note(Rejection.DEEP_WATER,"wetSamples",waterSamples[1]);return null;}
        int front=plan.radius+1;int[] ramp=new int[zEnd-front+1];
        for(int z=front;z<=zEnd;z++){int top=Math.max(ground.getY(),target+3-Math.max(0,z-plan.radius-7));for(var box:roofs)if(z>=box.z0&&z<=box.z1)top=Math.max(top,target+box.y1+2);ramp[z-front]=top;}
        for(int z=ramp.length-2;z>=0;z--)ramp[z]=Math.max(ramp[z],ramp[z+1]-1);for(int z=1;z<ramp.length;z++)ramp[z]=Math.max(ramp[z],ramp[z-1]-1);
        for(int dx=-1;dx<=1;dx++)for(int z=front;z<=zEnd;z++){int i=(dx-x0)*depth+z-z0;volume+=Math.abs(ramp[z-front]-floors[i])-Math.abs(tops[i]-floors[i]);tops[i]=Math.min(ramp[z-front],target+Math.max(0,z-front-3));}
        var soil=new KurganEarthwork(x0,z0,width,depth,floors,tops,volume);soil.adaptive=true;return new Site(origin,soil);
    }
    public BoundingBox bounds(BlockPos origin){
        int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
        for(int i=0;i<floors.length;i++){min=Math.min(min,Math.min(floors[i],tops[i]));max=Math.max(max,Math.max(floors[i],tops[i])+4);}
        return new BoundingBox(origin.getX()+x0,min,origin.getZ()+z0,origin.getX()+x0+width-1,max,origin.getZ()+z0+depth-1);
    }
    public void place(WorldGenLevel world,BlockPos origin,BoundingBox clip){place(world,origin,clip,null);}
    public void place(WorldGenLevel world,BlockPos origin,BoundingBox clip,KurganPlan plan){
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        for(int x=Math.max(0,clip.minX()-origin.getX()-x0);x<width&&origin.getX()+x0+x<=clip.maxX();x++)
            for(int z=Math.max(0,clip.minZ()-origin.getZ()-z0);z<depth&&origin.getZ()+z0+z<=clip.maxZ();z++){
                int index=x*depth+z;
                int skin=tops[index];for(int[] d:new int[][]{{-1,0},{1,0},{0,-1},{0,1}}){int nx=x+d[0],nz=z+d[1];if(nx>=0&&nx<width&&nz>=0&&nz<depth)skin=Math.min(skin,tops[nx*depth+nz]);}
                int actualFloor=adaptive?org.slavicmyths.worldgen.LandTerrain.ground(world,origin.getX()+x0+x,origin.getZ()+z0+z):floors[index];
                int upper=adaptive?Math.max(tops[index]+4,world.getHeight(world instanceof net.minecraft.server.level.ServerLevel?Heightmap.Types.WORLD_SURFACE:Heightmap.Types.WORLD_SURFACE_WG,origin.getX()+x0+x,origin.getZ()+z0+z)-1):tops[index];
                for(int y=Math.min(actualFloor,tops[index]);y<=upper;y++){
                    pos.set(origin.getX()+x0+x,y,origin.getZ()+z0+z);
                    if(clip.isInside(pos)&&!world.getBlockState(pos).is(Blocks.BEDROCK)&&!world.getBlockState(pos).hasBlockEntity())world.setBlock(pos,(y>tops[index]?Blocks.AIR:plan!=null&&y>=skin&&!(Math.abs(x0+x)<=1&&z0+z>=plan.radius+1)?KurganSurface.block(world,origin,pos,plan.seed,plan.radius,plan.tier,tops[index]-skin,y==tops[index]):y==tops[index]?Blocks.GRASS_BLOCK:y<tops[index]-4?Blocks.STONE:Blocks.DIRT).defaultBlockState(),2);
                }
                if(plan!=null)KurganSurface.decorate(world,origin,new BlockPos(origin.getX()+x0+x,tops[index],origin.getZ()+z0+z),plan.seed,plan.radius,clip);
            }
    }
    /** Saved surface used by both the approach and its authored stone details. */
    public int entranceSurface(BlockPos origin,int dx,int dz,int radius){
        int x=dx-x0,z=dz-z0;if(x<0||x>=width||z<0||z>=depth)return origin.getY();
        int top=tops[x*depth+z];
        return Math.abs(dx)<=1&&dz>=radius+1?Math.min(top,origin.getY()+Math.max(0,dz-radius-4)):top;
    }
    /** Keep the existing south-facing mouth connected to the graded outside ground. */
    public void approach(WorldGenLevel world,BlockPos origin,BoundingBox clip,int radius){
        int front=radius+1;
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        for(int dx=-1;dx<=1;dx++)for(int dz=front;dz<z0+depth;dz++){
            int x=dx-x0,z=dz-z0;if(x<0||x>=width||z<0||z>=depth)continue;
            int index=x*depth+z;
            int floor=entranceSurface(origin,dx,dz,radius);
            pos.set(origin.getX()+dx,floor,origin.getZ()+dz);
            if(clip.isInside(pos)&&!world.getBlockState(pos).hasBlockEntity()&&approachSoil(world.getBlockState(pos)))world.setBlock(pos,Blocks.DIRT_PATH.defaultBlockState(),2);
            for(int y=floor+1;y<=Math.max(floors[index]+4,Math.max(tops[index],floor+4));y++){
                pos.set(origin.getX()+dx,y,origin.getZ()+dz);
                if(clip.isInside(pos)&&!world.getBlockState(pos).hasBlockEntity()&&approachSoil(world.getBlockState(pos)))world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
            }
        }
    }
    private static boolean approachSoil(net.minecraft.world.level.block.state.BlockState state){return state.isAir()||state.is(Blocks.DIRT)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.DIRT_PATH)||state.is(Blocks.COARSE_DIRT)||state.is(Blocks.ROOTED_DIRT)||state.is(Blocks.PODZOL)||state.is(Blocks.MOSS_BLOCK)||state.is(Blocks.GRAVEL)||state.is(Blocks.SNOW)||state.canBeReplaced();}
    public CompoundTag save(){CompoundTag n=new CompoundTag();n.putBoolean("Adaptive",adaptive);n.putInt("X",x0);n.putInt("Z",z0);n.putInt("Width",width);n.putInt("Depth",depth);n.putIntArray("Floors",floors);n.putIntArray("Tops",tops);n.putLong("Volume",volume);return n;}
    public static KurganEarthwork load(CompoundTag n){
        int width=n.getInt("Width"),depth=n.getInt("Depth");int[] floors=n.getIntArray("Floors"),tops=n.getIntArray("Tops");
        if(width<=0||depth<=0||(long)width*depth>262144||floors.length!=(long)width*depth||tops.length!=floors.length)throw new IllegalArgumentException("Invalid saved kurgan earthwork");
        var result=new KurganEarthwork(n.getInt("X"),n.getInt("Z"),width,depth,floors,tops,n.getLong("Volume"));result.adaptive=n.getBoolean("Adaptive");return result;
    }
}
