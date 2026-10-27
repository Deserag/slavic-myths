package org.slavicmyths.chud;

import java.util.*;
import java.util.function.UnaryOperator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.*;

/** Saved new-start annex. Legacy city/abandoned plans and their underground geometry are unchanged. */
public final class ChudSurfaceAccess {
    public final List<BlockPos> floors;
    public final BlockPos mouth;
    private final List<ChudGeometry.Fill> fills;
    public final BoundingBox bounds;
    private ChudSurfaceAccess(List<BlockPos> path) {
        if(path.size()<2||path.size()>640)throw new IllegalArgumentException("access path length");
        floors=List.copyOf(path);mouth=floors.getLast().above();
        var result=new ArrayList<ChudGeometry.Fill>();
        // Build shells before carving: adjacent flights/landings cannot seal one another.
        // The first four cells open onto the existing vestibule. A new shell there
        // would brick up the descending legacy flight immediately beside its landing.
        for(int i=4;i<floors.size();i++){var p=floors.get(i);box(result,p.offset(-2,-2,-2),p.offset(2,5,2),Blocks.STONE_BRICKS.defaultBlockState());}
        for(int i=0;i<floors.size();i++){
            var p=floors.get(i);var next=floors.get(Math.min(i+1,floors.size()-1));
            Direction heading=Direction.getNearest(next.getX()-p.getX(),0,next.getZ()-p.getZ());
            BlockState floor=Blocks.STONE_BRICKS.defaultBlockState();
            if(next.getY()>p.getY())floor=Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,heading);
            box(result,p.offset(-1,0,-1),p.offset(1,0,1),floor);
            box(result,p.offset(-1,1,-1),p.offset(1,4,1),Blocks.AIR.defaultBlockState());
        }
        // The last landing is an open doorway. Three-wide, four-high, with a roof above the opening.
        var end=floors.getLast();var before=floors.get(floors.size()-2);var heading=Direction.getNearest(end.getX()-before.getX(),0,end.getZ()-before.getZ());
        for(int k=0;k<=1;k++){
            var p=end.relative(heading,k);
            box(result,p.offset(-1,-2,-1),p.offset(1,0,1),Blocks.STONE_BRICKS.defaultBlockState());
            box(result,p.offset(-1,1,-1),p.offset(1,4,1),Blocks.AIR.defaultBlockState());
        }
        Direction side=heading.getClockWise();
        for(int k=-2;k<=1;k++)for(int sign:new int[]{-1,1}){var p=end.relative(heading,k).relative(side,2*sign).above();box(result,p,p,Blocks.COBBLESTONE_WALL.defaultBlockState());}
        for(int i=6;i<floors.size()-4;i+=12){var p=floors.get(i);box(result,p.above(4),p.above(4),Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING,true));}
        fills=List.copyOf(result);
        bounds=BoundingBox.encapsulatingBoxes(fills.stream().map(ChudGeometry.Fill::box).toList()).orElseThrow();
    }
    private static void box(List<ChudGeometry.Fill> out,BlockPos a,BlockPos b,BlockState s){out.add(new ChudGeometry.Fill(new BoundingBox(a.getX(),a.getY(),a.getZ(),b.getX(),b.getY(),b.getZ()),s));}
    public List<ChudGeometry.Fill> blueprint(){return fills;}
    public CompoundTag save(){var n=new CompoundTag();n.putInt("Version",1);n.putLongArray("Floors",floors.stream().mapToLong(BlockPos::asLong).toArray());return n;}
    public static ChudSurfaceAccess load(CompoundTag n){if(n.getInt("Version")!=1)throw new IllegalArgumentException("access version");return new ChudSurfaceAccess(Arrays.stream(n.getLongArray("Floors")).mapToObj(BlockPos::of).toList());}
    public static ChudSurfaceAccess city(Structure.GenerationContext c,ChudPlan p){return plan(c,p.points.get(ChudPlan.Role.ENTRANCE).below(),p::world);}
    public static ChudSurfaceAccess abandoned(Structure.GenerationContext c,AbandonedPlan p){return plan(c,p.targets.get("entrance").below(),p::world);}
    private static ChudSurfaceAccess plan(Structure.GenerationContext c,BlockPos local,UnaryOperator<BlockPos> world){
        var t=new ChudStructure.Terrain(c);
        // Stay inside the native reference window (owning chunk +/-8), even for deep mountain starts.
        for(int initial:new int[]{-1,1}){
            int sign=initial;var path=new ArrayList<BlockPos>();int y=local.getY(),x=local.getX(),z=local.getZ();
            path.add(world.apply(local));
            while(x<131){x++;path.add(world.apply(new BlockPos(x,y,z)));}
            int run=0;
            for(int flight=0;flight<3;flight++){
                int end=sign<0?-118:118;
                while(z!=end){
                    z+=sign;run++;if(run%15<12)y++;
                    var p=world.apply(new BlockPos(x,y,z));ChudSearchBudget.check();
                    if(p.getY()+5>=c.heightAccessor().getMaxBuildHeight())break;
                    path.add(p);int ground=t.surface(p.getX(),p.getZ());
                    if(p.getY()>=ground&&p.getY()-ground<=1&&path.size()>12){
                        boolean safe=true;
                        for(int dx=-3;dx<=3&&safe;dx++)for(int dz=-3;dz<=3;dz++){
                            int g=t.surface(p.getX()+dx,p.getZ()+dz);
                            if(Math.abs(g-p.getY())>2||!t.column(p.getX()+dx,p.getZ()+dz).getBlock(g+1).getFluidState().isEmpty()){safe=false;break;}
                        }
                        if(safe){
                            var connected=new ArrayList<>(path);var previous=p;
                            for(int k=1;k<=4;k++){
                                var q=world.apply(new BlockPos(x,y,z+sign*k));int g=t.surface(q.getX(),q.getZ());
                                if(Math.abs(g-previous.getY())>1||!t.column(q.getX(),q.getZ()).getBlock(g+1).getFluidState().isEmpty()){safe=false;break;}
                                previous=q.atY(g);connected.add(previous);
                            }
                            if(!safe)continue;
                            // The outside natural cell must also be reachable on foot in both directions.
                            var q=world.apply(new BlockPos(x,y,z+sign*6));int g=t.surface(q.getX(),q.getZ());
                            if(Math.abs(g-previous.getY())>1||!t.column(q.getX(),q.getZ()).getBlock(g+1).getFluidState().isEmpty())continue;
                            var access=new ChudSurfaceAccess(connected);var b=access.bounds;
                            if((b.minX()>>4)<c.chunkPos().x-8||(b.maxX()>>4)>c.chunkPos().x+8||(b.minZ()>>4)<c.chunkPos().z-8||(b.maxZ()>>4)>c.chunkPos().z+8)continue;
                            return access; // Whole assembly protection is checked once by clearFull.
                        }
                    }
                }
                if(x<111)break;
                for(int k=0;k<10;k++){x--;path.add(world.apply(new BlockPos(x,y,z)));}sign=-sign;
            }
        }
        return null;
    }
}
