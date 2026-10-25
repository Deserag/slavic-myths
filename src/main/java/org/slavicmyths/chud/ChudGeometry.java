package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Finite vanilla-block construction grammar. No entities, inventory NBT, or tick handlers. */
public final class ChudGeometry {
    public record Fill(BoundingBox box, BlockState state) {}
    private final ChudPlan plan;
    private final List<Fill> fills=new ArrayList<>();
    private ChudGeometry(ChudPlan p) { plan=p; }
    private static BlockState state(Block b) { return b.defaultBlockState(); }
    private void box(int x,int y,int z,int xx,int yy,int zz,BlockState s) {
        var a=plan.world(new BlockPos(x,y,z));var b=plan.world(new BlockPos(xx,yy,zz));
        fills.add(new Fill(new BoundingBox(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()),Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ())),s.rotate(plan.blockRotation())));
    }
    private void box(int x,int y,int z,int xx,int yy,int zz,Block b){box(x,y,z,xx,yy,zz,state(b));}
    private void at(int x,int y,int z,Block b){box(x,y,z,x,y,z,b);}
    private void at(int x,int y,int z,BlockState s){box(x,y,z,x,y,z,s);}
    private void air(int x,int y,int z,int xx,int yy,int zz){box(x,y,z,xx,yy,zz,Blocks.AIR);}
    private static BlockState stair(Block b,Direction d){return state(b).setValue(BlockStateProperties.HORIZONTAL_FACING,d);}
    private static BlockState fence(){return state(Blocks.SPRUCE_FENCE).setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true).setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true);}
    private void lamp(int x,int y,int z){at(x,y,z,Blocks.POLISHED_ANDESITE);box(x,y+1,z,x,y+3,z,Blocks.DARK_OAK_FENCE);at(x,y+4,z,Blocks.LANTERN);}
    private void pendant(int x,int y,int z,int length){at(x,y,z,Blocks.SPRUCE_LOG);box(x,y-length+1,z,x,y-1,z,Blocks.CHAIN);at(x,y-length,z,state(Blocks.LANTERN).setValue(BlockStateProperties.HANGING,true));}
    private void column(int x,int y,int z,int height){
        if(height>=20)for(var h:plan.halls)if(h.y()==y&&Math.abs(x-h.x())<h.width()/2&&Math.abs(z-h.z())<h.depth()/2)height=Math.max(height,2+cavernTop(plan,h,x-h.x(),z-h.z())-y);
        box(x-1,y,z-1,x+1,y+1,z+1,Blocks.POLISHED_ANDESITE);box(x-1,y+2,z-1,x,y+height-2,z,Blocks.STONE_BRICKS);
        for(int k=4;k<height-3;k+=6)box(x-1,y+k,z-1,x,y+k,z,Blocks.CHISELED_STONE_BRICKS);
        box(x-1,y+height-1,z-1,x+1,y+height,z+1,Blocks.STONE_BRICKS);
    }
    private void bench(int x,int y,int z,int length){box(x,y+1,z,x+length-1,y+1,z,stair(Blocks.DARK_OAK_STAIRS,Direction.SOUTH));}
    private void table(int x,int y,int z,int width){at(x,y+1,z,Blocks.DARK_OAK_FENCE);at(x+width-1,y+1,z,Blocks.DARK_OAK_FENCE);box(x,y+2,z,x+width-1,y+2,z+1,state(Blocks.DARK_OAK_SLAB).setValue(BlockStateProperties.SLAB_TYPE,SlabType.TOP));}

    public static List<Fill> build(ChudPlan plan){
        var g=new ChudGeometry(plan);
        for(var hall:plan.halls)g.cavern(hall);
        g.corridors(true);
        g.square(plan.halls.get(0));g.residential(plan.halls.get(1));g.forge(plan.halls.get(2));g.store(plan.halls.get(3));g.council(plan.halls.get(4));g.gate(plan.halls.get(5));g.entrance();
        // Reserved walkways take precedence over furniture and module edges.
        g.corridors(false);
        return List.copyOf(g.fills);
    }
    private void cavern(ChudPlan.Hall h){
        int rx=h.width()/2,rz=h.depth()/2;
        // Eight broad lobes produce coherent roughness, rather than independent per-voxel holes.
        for(int dx=-rx-2;dx<=rx+2;dx++)for(int dz=-rz-2;dz<=rz+2;dz++){
            double q=cavernQ(plan,h,dx,dz);
            if(q>1.10)continue;
            int x=h.x()+dx,z=h.z()+dz;
            int top=cavernTop(plan,h,dx,dz);
            Block rock=((dx/7+dz/9)&3)==0?Blocks.TUFF:((dx/11-dz/8)&3)==0?Blocks.ANDESITE:Blocks.STONE;
            box(x,h.y()-3,z,x,top+14,z,rock);
            if(q<.98){air(x,h.y()+1,z,x,top,z);Block floor=Math.floorMod(dx/5+dz/7,7)==0?Blocks.POLISHED_ANDESITE:Math.floorMod(dx+dz,5)==0?Blocks.ANDESITE:Blocks.STONE_BRICKS;at(x,h.y(),z,floor);}
        }
    }
    public static double cavernQ(ChudPlan p,ChudPlan.Hall h,int dx,int dz){double phase=(p.seed&255)*Math.PI/128,angle=Math.atan2(dz/(h.depth()/2.0),dx/(h.width()/2.0));double lobe=1+.035*Math.sin(5*angle+phase)+.025*Math.cos(3*angle-phase);return (dx*dx/Math.pow(h.width()/2.0,2)+dz*dz/Math.pow(h.depth()/2.0,2))/(lobe*lobe);}
    public static int cavernTop(ChudPlan p,ChudPlan.Hall h,int dx,int dz){return h.y()+Math.max(5,(int)(h.height()*.58+h.height()*.42*Math.sqrt(Math.max(0,1-cavernQ(p,h,dx,dz)))));}
    private void corridors(boolean shell){
        var floors=new LinkedHashMap<Long,BlockPos>();
        for(var c:plan.connectors){int r=c.width()/2;for(var p:c.floors()){
            int x=p.getX(),y=p.getY(),z=p.getZ();
            boolean inHall=plan.halls.stream().anyMatch(h->Math.abs(x-h.x())<=h.width()/2&&Math.abs(z-h.z())<=h.depth()/2&&y>=h.y()-2&&y<h.y()+h.height()-6);
            if(shell&&!inHall)box(x-r-1,y-3,z-r-1,x+r+1,y+c.headroom()+1,z+r+1,Blocks.ANDESITE);
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){long key=BlockPos.asLong(x+dx,0,z+dz);var old=floors.get(key);if(old==null||old.getY()<y)floors.put(key,new BlockPos(x+dx,y,z+dz));}
        }}
        // Junctions on different grades get a finite one-block ramp envelope, rather than a cliff.
        var pending=new ArrayDeque<BlockPos>(floors.values());
        while(!pending.isEmpty()){var p=pending.removeFirst();for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){
            long key=BlockPos.asLong(p.getX()+dir.getStepX(),0,p.getZ()+dir.getStepZ());var q=floors.get(key);
            if(q!=null&&q.getY()<p.getY()-1){var raised=new BlockPos(q.getX(),p.getY()-1,q.getZ());floors.put(key,raised);pending.addLast(raised);}
        }}
        for(var p:floors.values()){
            int x=p.getX(),y=p.getY(),z=p.getZ();BlockState floor=state(Blocks.STONE_BRICKS);
            for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){var adjacent=floors.get(BlockPos.asLong(x+dir.getStepX(),0,z+dir.getStepZ()));if(adjacent!=null&&adjacent.getY()==y+1){floor=stair(Blocks.STONE_BRICK_STAIRS,dir);break;}}
            box(x,y-2,z,x,y,z,Blocks.STONE_BRICKS);at(x,y,z,floor);air(x,y+1,z,x,y+5,z);
        }
        if(!shell)for(var c:plan.connectors)for(int i=6;i<c.floors().size();i+=8){var p=c.floors().get(i);int x=p.getX(),z=p.getZ();var actual=floors.get(BlockPos.asLong(x,0,z));if(actual!=null){at(x,actual.getY()+5,z,state(Blocks.LANTERN).setValue(BlockStateProperties.HANGING,true));at(x,actual.getY()+6,z,Blocks.STONE_BRICKS);}}
    }
    private void platform(int x,int y,int z,int w,int d,int bottom){
        box(x,y-1,z,x+w-1,y,z+d-1,Blocks.STONE_BRICKS);
        for(int xx:new int[]{x,x+w-1})for(int zz:new int[]{z,z+d-1})box(xx,bottom,zz,xx,y-2,zz,Blocks.STONE_BRICKS);
        box(x,y+1,z,x+w-1,y+1,z,fence());box(x,y+1,z+d-1,x+w-1,y+1,z+d-1,fence());
        box(x,y+1,z,x,y+1,z+d-1,fence());box(x+w-1,y+1,z,x+w-1,y+1,z+d-1,fence());
    }
    private void stairs(int x,int y,int z,int rise,int width,Direction up){
        // Connect the foot of the flight to the hall floor, including the natural cavern edge.
        int ax=x-up.getStepX()*3,az=z-up.getStepZ()*3;
        box(Math.min(ax,x)-2,y,Math.min(az,z)-2,Math.max(ax,x)+2,y,Math.max(az,z)+2,Blocks.STONE_BRICKS);
        air(Math.min(ax,x)-2,y+1,Math.min(az,z)-2,Math.max(ax,x)+2,y+4,Math.max(az,z)+2);
        int run=0;
        for(int i=0;i<=rise;i++){
            if(i>0&&i%6==0){for(int k=0;k<3;k++){int xx=x+up.getStepX()*run,zz=z+up.getStepZ()*run;step(xx,y+i-1,zz,width,up,false,y);run++;}}
            int xx=x+up.getStepX()*run,zz=z+up.getStepZ()*run;step(xx,y+i,zz,width,up,i<rise,y);run++;
        }
    }
    private void step(int x,int y,int z,int width,Direction up,boolean stair,int base){
        boolean alongX=up.getAxis()==Direction.Axis.X;int r=width/2;
        int x0=x-(alongX?0:r),z0=z-(alongX?r:0),x1=x+(alongX?0:r),z1=z+(alongX?r:0);
        box(x0,base-1,z0,x1,y-1,z1,Blocks.STONE_BRICKS);air(x0,y+1,z0,x1,y+4,z1);
        box(x0,y,z0,x1,y,z1,stair?stair(Blocks.STONE_BRICK_STAIRS,up):state(Blocks.STONE_BRICKS));
        for(int side:new int[]{-r-1,r+1}){int xx=x+(alongX?0:side),zz=z+(alongX?side:0);box(xx,base,zz,xx,y,zz,Blocks.STONE_BRICKS);at(xx,y+1,zz,fence());}
    }
    private void bridge(int x,int y,int z,int length,int width,int bottom){
        platform(x,y,z,length,width,bottom);
        box(x,y-2,z,x+length-1,y-2,z,state(Blocks.SPRUCE_LOG).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        box(x,y-2,z+width-1,x+length-1,y-2,z+width-1,state(Blocks.SPRUCE_LOG).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        for(int xx=x+6;xx<x+length-3;xx+=8)for(int zz:new int[]{z,z+width-1})box(xx,bottom,zz,xx,y-3,zz,Blocks.POLISHED_ANDESITE);
        // Open both landings, retain only side rails.
        air(x,y+1,z+1,x,y+3,z+width-2);air(x+length-1,y+1,z+1,x+length-1,y+3,z+width-2);
        lamp(x+2,y,z);lamp(x+length-3,y,z+width-1);
    }
    private void planter(int x,int y,int z){box(x,y+1,z,x+1,y+1,z+1,Blocks.MOSS_BLOCK);at(x,y+2,z,Blocks.AZALEA);}
    private void arch(int x,int y,int z,boolean alongX){
        for(int side:new int[]{-1,1})for(int k=0;k<3;k++){
            int offset=side*(5-k),bottom=k==0?y:y+5+k,top=y+6+k;
            if(alongX)box(x-1,bottom,z+offset,x+1,top,z+offset,Blocks.STONE_BRICKS);
            else box(x+offset,bottom,z-1,x+offset,top,z+1,Blocks.STONE_BRICKS);
        }
        if(alongX)box(x-1,y+8,z-2,x+1,y+10,z+2,Blocks.STONE_BRICKS);
        else box(x-2,y+8,z-1,x+2,y+10,z+1,Blocks.STONE_BRICKS);
    }
    private void square(ChudPlan.Hall h){
        // Unobstructed 25x25 central area, interrupted only by its single small civic sign.
        for(int radius:new int[]{8,12}){box(-radius,0,-radius,radius,0,-radius,Blocks.POLISHED_ANDESITE);box(-radius,0,radius,radius,0,radius,Blocks.POLISHED_ANDESITE);box(-radius,0,-radius,-radius,0,radius,Blocks.POLISHED_ANDESITE);box(radius,0,-radius,radius,0,radius,Blocks.POLISHED_ANDESITE);}
        box(-3,0,-25,3,0,24,Blocks.POLISHED_ANDESITE);
        box(-2,1,-2,2,1,2,Blocks.STONE_BRICKS);box(-1,2,-1,1,2,1,Blocks.POLISHED_ANDESITE);box(0,3,0,0,6,0,Blocks.CHISELED_STONE_BRICKS);
        for(int side:new int[]{-1,1}){
            platform(side<0?-24:15,7,-8,10,20,0);
            stairs(side<0?-20:19,0,-18,7,3,Direction.SOUTH);
            box(side<0?-20:0,0,-22,side<0?0:19,0,-19,Blocks.STONE_BRICKS);
            air(side<0?-20:0,1,-22,side<0?0:19,4,-19);
            air(side<0?-21:18,8,-8,side<0?-19:20,11,-8);
            for(int z:new int[]{-10,13,19})column(side*23,0,z,22);
            home(new ChudPlan.Room("square_home"+side,side<0?0:1,side<0?-23:16,7,-3,7,10));
            int lane=side<0?-16:23;box(lane,7,-7,lane,7,10,Blocks.STONE_BRICKS);air(lane,8,-7,lane,11,10);
            box(side<0?-23:16,7,7,side<0?-16:23,7,10,Blocks.STONE_BRICKS);air(side<0?-23:16,8,7,side<0?-16:23,11,10);
            bench(side<0?-14:9,0,14,5);bench(side<0?-17:12,0,-14,4);
            for(int z:new int[]{-20,-8,8,21})lamp(side*12,0,z);
            for(int z:new int[]{-17,0,17})planter(side*15,0,z);
        }
        // Peripheral arcade, reachable rather than a facade painted on rock.
        for(int x=-10;x<=10;x+=5)box(x,1,22,x,5,22,Blocks.SPRUCE_LOG);
        box(-11,6,21,11,6,24,Blocks.SPRUCE_SLAB);
        bridge(-14,7,15,29,5,0);stairs(-12,0,3,7,3,Direction.SOUTH);box(-13,7,13,-11,7,16,Blocks.STONE_BRICKS);air(-13,8,13,-11,11,16);
    }
    private void residential(ChudPlan.Hall h){
        int tiers=plan.variant==0?2:3;
        for(int tier=0;tier<tiers;tier++){
            int y=h.y()+12*tier;
            // Two broad supported terraces; street between them remains open at every tier.
            for(int z:new int[]{17,41}){
                platform(50,y,z,49,15,h.y()-2);
                air(50,y+1,z,98,y+11,z+14);
                air(50,y+1,z+11,98,y+3,z+13);
                for(int x=52;x<97;x+=13)lamp(x,y,z+14);
            }
            bridge(50,y,33,49,7,h.y()-2);
            if(tier>0){int sx=tier==1?101:47;stairs(sx,y-12,17,12,3,Direction.SOUTH);air(sx-4,y-11,14,sx+5,y-8,16);box(sx-4,y-12,14,sx+5,y-12,16,Blocks.STONE_BRICKS);air(sx-2,y+1,36,sx+2,y+4,39);box(sx-2,y,36,sx+2,y,39,Blocks.STONE_BRICKS);}
        }
        for(var room:plan.homes)home(room);
        // Clear circulation after houses, but never touch their declared footprints.
        for(int tier=0;tier<tiers;tier++){
            int y=h.y()+tier*12;air(50,y+1,35,103,y+4,39);box(50,y,35,103,y,39,Blocks.STONE_BRICKS);
            for(int z:new int[]{31,54}){air(50,y+1,z,98,y+3,z+2);box(50,y,z,98,y,z+2,Blocks.STONE_BRICKS);}
            for(int x:new int[]{64,80,96}){air(x,y+1,29,x+2,y+3,55);box(x,y,29,x+2,y,55,Blocks.STONE_BRICKS);}
            if(tier>0){
                box(50,y+1,17,98,y+1,17,fence());box(50,y+1,56,98,y+1,56,fence());
                box(50,y+1,17,50,y+1,56,fence());box(98,y+1,17,98,y+1,56,fence());
                box(50,y+1,39,98,y+1,39,fence());box(50,y+1,41,98,y+1,41,fence());
                for(int x:new int[]{64,80,96}){air(x,y+1,39,x+2,y+3,41);}
                air(98,y+1,35,103,y+3,38);
                if(tier==1){air(97,y+1,17,98,y+3,17);air(50,y+1,14,52,y+3,17);}
                if(tier==2){air(47,y+1,35,52,y+3,38);box(47,y,35,52,y,38,Blocks.STONE_BRICKS);}
            }
        }
    }
    private void home(ChudPlan.Room r){
        int x=r.x(),y=r.y(),z=r.z(),w=r.width(),d=r.depth(),v=r.variant();
        box(x,y-1,z,x+w-1,y,z+d-1,Blocks.STONE_BRICKS);
        box(x,y+1,z,x+w-1,y+4,z+d-1,v==1?Blocks.STONE_BRICKS:Blocks.SPRUCE_PLANKS);
        air(x+1,y+1,z+1,x+w-2,y+4,z+d-2);
        for(int xx:new int[]{x,x+w-1})for(int zz:new int[]{z,z+d-1})box(xx,y+1,zz,xx,y+5,zz,v==3?Blocks.DARK_OAK_LOG:Blocks.SPRUCE_LOG);
        box(x,y+5,z,x+w-1,y+5,z+d-1,state(Blocks.SPRUCE_LOG).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        int door=x+2+(v%3);air(door,y+1,z+d-1,door+1,y+3,z+d);
        if(v==2){
            box(x,y+6,z,x+w-1,y+9,z+d-1,Blocks.SPRUCE_PLANKS);air(x+1,y+6,z+1,x+w-2,y+9,z+d-2);
            box(x+1,y+5,z+1,x+w-2,y+5,z+d-2,Blocks.SPRUCE_PLANKS);
            for(int xx:new int[]{x,x+w-1})for(int zz:new int[]{z,z+d-1})box(xx,y+1,zz,xx,y+10,zz,Blocks.DARK_OAK_LOG);
            box(x,y+10,z,x+w-1,y+10,z+d-1,state(Blocks.SPRUCE_LOG).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
            platform(x+2,y+5,z+d,4,3,y);air(x+2,y+6,z+d-1,x+4,y+8,z+d);
            for(int k=0;k<=5;k++){int sx=x+w-2,sz=z+d-2-k;box(sx,y,sz,sx,y+k,sz,Blocks.STONE_BRICKS);air(sx,y+k+1,sz,sx,y+k+4,sz);at(sx,y+k,sz,k<5?stair(Blocks.SPRUCE_STAIRS,Direction.NORTH):state(Blocks.SPRUCE_PLANKS));}
            box(x+1,y+7,z+d-1,x+1,y+8,z+d-1,Blocks.GLASS_PANE);box(x+w-2,y+7,z,x+w-2,y+8,z,Blocks.GLASS_PANE);
            at(x+1,y+6,z+2,state(Blocks.BROWN_BED).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH).setValue(BlockStateProperties.BED_PART,BedPart.FOOT));
            at(x+1,y+6,z+1,state(Blocks.BROWN_BED).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH).setValue(BlockStateProperties.BED_PART,BedPart.HEAD));
            pendant(x+3,y+10,z+4,1);
        }
        // Real complete two-part door, with open second lane for navigation.
        var ds=state(Blocks.SPRUCE_DOOR).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH);
        at(door,y+1,z+d-1,ds.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.LOWER));at(door,y+2,z+d-1,ds.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.UPPER));
        for(int xx:new int[]{x+1,x+w-2})box(xx,y+2,z+d-1,xx,y+3,z+d-1,Blocks.GLASS_PANE);
        // Distinct roofs: gable, stone-niche shed, shifted ridge, stepped corner.
        if(v==1){for(int k=0;k<d;k+=2)box(x-1,y+6+k/4,z+k,x+w,y+6+k/4,z+Math.min(d,k+1),stair(Blocks.SPRUCE_STAIRS,Direction.NORTH));}
        else{int offset=v==2?1:0,roof=v==2?11:6;for(int k=0;k<=w/2;k++){int yy=y+roof+k;box(x-1+k,yy,z-1,x-1+k,yy,z+d,stair(v==3||v==2?Blocks.DARK_OAK_STAIRS:Blocks.SPRUCE_STAIRS,Direction.EAST));box(x+w-k,yy-offset,z-1,x+w-k,yy-offset,z+d,stair(Blocks.SPRUCE_STAIRS,Direction.WEST));}box(x+w/2,y+roof+w/2,z-1,x+w/2+1,y+roof+w/2,z+d,Blocks.SPRUCE_SLAB);}
        // Distinct furnishing plans keep two clear blocks in front of the entrance.
        int bedX=v%2==0?x+1:x+w-3;
        var bed=state(Blocks.BROWN_BED).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH);
        at(bedX,y+1,z+2,bed.setValue(BlockStateProperties.BED_PART,BedPart.FOOT));at(bedX,y+1,z+1,bed.setValue(BlockStateProperties.BED_PART,BedPart.HEAD));
        table(v%2==0?x+w-4:x+1,y,z+4,2);at(v%2==0?x+w-5:x+3,y+1,z+4,stair(Blocks.SPRUCE_STAIRS,Direction.EAST));
        box(x+2,y+3,z+1,x+w-3,y+3,z+1,Blocks.SPRUCE_TRAPDOOR);
        at(x+w-2,y+1,z+1,v==1?Blocks.BOOKSHELF:Blocks.FURNACE);
        at(door-1,y+2,z+d,Blocks.STONE_BRICKS);at(door-1,y+3,z+d,Blocks.LANTERN);
        pendant(x+w/2,y+5,z+d/2,1);
        if(v==3){box(x+w,y,z+4,x+w+2,y,z+7,Blocks.STONE_BRICKS);box(x+w+2,y+1,z+4,x+w+2,y+4,z+4,Blocks.SPRUCE_LOG);box(x+w,y+5,z+4,x+w+2,y+5,z+7,Blocks.SPRUCE_SLAB);}
        if(v==2){
            // A reserved two-wide exterior flight, beside the house rather than in its street.
            for(int k=0;k<=5;k++){int sz=z+d-2-k;box(x+w,y-1,sz,x+w+1,y+k,sz,Blocks.STONE_BRICKS);air(x+w,y+k+1,sz,x+w+1,y+k+4,sz);box(x+w,y+k,sz,x+w+1,y+k,sz,k<5?stair(Blocks.SPRUCE_STAIRS,Direction.NORTH):state(Blocks.STONE_BRICKS));box(x+w+2,y,sz,x+w+2,y+k,sz,Blocks.STONE_BRICKS);at(x+w+2,y+k+1,sz,fence());}
            box(x+w,y,z+d-1,x+w+1,y,z+d+1,Blocks.STONE_BRICKS);air(x+w,y+1,z+d-1,x+w+1,y+4,z+d+1);
            box(x+w-2,y+5,z+2,x+w+1,y+5,z+3,Blocks.STONE_BRICKS);air(x+w-2,y+6,z+2,x+w+1,y+9,z+3);
        }
    }
    private void forge(ChudPlan.Hall h){
        int x=h.x(),y=h.y(),z=h.z();
        // Offset central furnace preserves a five-wide loading lane across the hall.
        box(x-7,y+1,z+5,x+7,y+2,z+19,Blocks.DEEPSLATE_BRICKS);
        box(x-5,y+3,z+7,x+5,y+12,z+17,Blocks.DEEPSLATE_BRICKS);air(x-2,y+3,z+6,x+2,y+6,z+8);
        box(x-2,y+3,z+8,x+2,y+6,z+8,Blocks.MAGMA_BLOCK);box(x-2,y+3,z+7,x+2,y+6,z+7,Blocks.IRON_BARS);
        for(int xx:new int[]{x-7,x+7})for(int zz:new int[]{z+5,z+19})column(xx,y,zz,14);
        box(x-3,y+13,z+10,x+3,y+22,z+14,Blocks.POLISHED_BASALT);box(x-2,y+23,z+10,x+2,y+23,z+14,Blocks.DEEPSLATE_BRICK_SLAB);
        for(int xx:new int[]{x-21,x+17})for(int zz:new int[]{z-12,z-2,z+10}){
            box(xx,y,zz,xx+4,y,zz+5,Blocks.COBBLED_DEEPSLATE);at(xx+1,y+1,zz+2,Blocks.ANVIL);table(xx+2,y,zz+3,2);lamp(xx+4,y,zz);
        }
        for(int xx:new int[]{x-16,x+13}){box(xx,y+1,z+20,xx+3,y+6,z+23,Blocks.DEEPSLATE_BRICKS);at(xx+1,y+2,z+19,Blocks.BLAST_FURNACE);pendant(xx+1,y+6,z+19,2);}
        bridge(x-23,y+10,z-17,47,5,y);stairs(x-23,y,z-4,10,3,Direction.NORTH);air(x-24,y+11,z-17,x-22,y+14,z-14);
        for(int xx:new int[]{x-24,x+24})for(int zz:new int[]{z-18,z+15})column(xx,y,zz,24);
        box(x-24,y+10,z-19,x-19,y+10,z-18,Blocks.STONE_BRICKS);air(x-24,y+11,z-19,x-19,y+14,z-18);
        air(x-20,y+11,z-17,x-19,y+14,z-15);
    }
    private void store(ChudPlan.Hall h){
        int x=h.x(),y=h.y(),z=h.z(),half=h.width()/2;
        for(int row=0;row<4;row++){
            int xx=x+new int[]{-half+3,-half+8,half-9,half-4}[row];
            for(int zz=z-9;zz<=z+8;zz+=5)box(xx,y+1,zz,xx,y+7,zz,Blocks.SPRUCE_LOG);
            for(int sy:new int[]{2,4,6}){box(xx,y+sy,z-9,xx+1,y+sy,z+9,Blocks.SPRUCE_SLAB);for(int zz=z-8;zz<z+8;zz+=5)at(xx,y+sy-1,zz,row==0?Blocks.HAY_BLOCK:row==1?Blocks.DRIED_KELP_BLOCK:row==2?Blocks.TUFF:Blocks.SPRUCE_PLANKS);}
        }
        for(int xx:new int[]{x-half+2,x+half-2}){column(xx,y,z-8,h.height()-4);column(xx,y,z+8,h.height()-4);lamp(xx,y,z);}
        table(x-4,y,z+h.depth()/2-5,3);bench(x-4,y,z+h.depth()/2-3,3);
        if(h.height()>=22){bridge(x-half+3,y+8,z-16,h.width()-6,5,y);stairs(x-half+3,y,z-6,8,3,Direction.NORTH);air(x-half+2,y+9,z-17,x-half+4,y+12,z-13);}
        // Cross aisle connects production to the mine landing, without inventory crates.
        air(x-half+3,y+1,z-2,x+half-3,y+4,z+2);box(x-half+3,y,z-2,x+half-3,y,z+2,Blocks.STONE_BRICKS);
    }
    private void council(ChudPlan.Hall h){
        int x=h.x(),y=h.y(),z=h.z(),w=h.width()/2;
        // Enclosed council wing has a real wall and doorway separating it from the square.
        if(!h.major()){box(x-w,y+1,z-h.depth()/2,x+w,y+10,z-h.depth()/2,Blocks.STONE_BRICKS);air(x-2,y+1,z-h.depth()/2,x+2,y+6,z-h.depth()/2);}
        if(!h.major())arch(x,y,z-h.depth()/2,false);
        for(int row=0;row<4;row++){bench(x-10,y,z-8+row*4,6);bench(x+5,y,z-8+row*4,6);}
        box(x-7,y+1,z+8,x+7,y+2,z+13,Blocks.STONE_BRICKS);box(x-3,y+1,z+6,x+3,y+1,z+7,stair(Blocks.STONE_BRICK_STAIRS,Direction.SOUTH));
        table(x-3,y+2,z+10,7);bench(x-3,y+2,z+12,7);
        for(int xx:new int[]{x-w+3,x+w-3})for(int zz:new int[]{z-8,z+5}){column(xx,y,zz,h.height()-3);lamp(xx,y,zz+2);at(xx,y+5,zz+1,state(Blocks.BROWN_WALL_BANNER).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH));}
        // Existing-block lozenge civic emblem; no altar, rune item, or interaction.
        int rear=z+h.depth()/2-2;box(x-5,y+3,rear,x+5,y+11,rear,Blocks.DEEPSLATE_BRICKS);
        for(int k=-3;k<=3;k++){at(x+k,y+7+3-Math.abs(k),rear-1,Blocks.CHISELED_STONE_BRICKS);at(x+k,y+7-3+Math.abs(k),rear-1,Blocks.CHISELED_STONE_BRICKS);}
        pendant(x,y+h.height()-3,z,3);
    }
    private void gate(ChudPlan.Hall h){
        int x=h.x(),y=h.y(),z=h.z();
        // Lower square and masonry seal are physical generation-v1 data, not a future system.
        box(x-6,y,z-7,x+6,y,z+9,Blocks.POLISHED_ANDESITE);
        box(x-7,y+1,z+9,x+7,y+12,z+16,Blocks.DEEPSLATE_BRICKS);
        box(x-4,y+1,z+8,x+4,y+8,z+8,Blocks.IRON_BARS);
        for(int xx:new int[]{x-7,x+7})column(xx,y, z+8,12);
        bridge(x-11,y+12,z-8,23,5,y);stairs(x-10,y,z+10,12,3,Direction.NORTH);air(x-11,y+13,z-8,x-9,y+16,z-5);
        bridge(x-10,y+22,z-3,21,5,y);stairs(x+9,y+12,z-12,10,3,Direction.SOUTH);air(x+8,y+23,z-3,x+10,y+26,z+1);
        box(x+8,y+12,z-13,x+12,y+12,z-12,Blocks.STONE_BRICKS);air(x+8,y+13,z-13,x+12,y+16,z-12);
        box(x+11,y+12,z-12,x+12,y+12,z-5,Blocks.STONE_BRICKS);air(x+11,y+13,z-12,x+12,y+16,z-5);
        // Suspended solid platform with four actual chain attachments; no lift entity.
        box(x-2,y+15,z+2,x+2,y+15,z+6,Blocks.SPRUCE_PLANKS);
        for(int xx:new int[]{x-2,x+2})for(int zz:new int[]{z+2,z+6})box(xx,y+16,zz,xx,y+26,zz,Blocks.CHAIN);
        box(x-4,y+27,z+2,x+4,y+27,z+6,state(Blocks.SPRUCE_LOG).setValue(BlockStateProperties.AXIS,Direction.Axis.X));
        for(int xx:new int[]{x-4,x+4})box(xx,y, z+4,xx,y+26,z+4,Blocks.DARK_OAK_LOG);
        for(int k=0;k<10;k++)at(x-5,y+1,z-6+k,Blocks.RAIL);
        lamp(x-6,y,z+5);lamp(x+6,y,z+5);
        // Build flights after bridge supports so those supports cannot seal their treads.
        stairs(x-10,y,z+10,12,3,Direction.NORTH);
        box(x-14,y,z-1,x-12,y,z+13,Blocks.STONE_BRICKS);air(x-14,y+1,z-1,x-12,y+4,z+13);
        box(x-14,y,z+10,x-8,y,z+13,Blocks.STONE_BRICKS);air(x-14,y+1,z+10,x-8,y+4,z+13);
        box(x-14,y,z-2,x,y,z,Blocks.STONE_BRICKS);air(x-14,y+1,z-2,x,y+4,z);
        air(x-11,y+13,z-8,x-9,y+16,z-5);
        box(x-11,y+12,z-10,x-6,y+12,z-9,Blocks.STONE_BRICKS);air(x-11,y+13,z-10,x-6,y+16,z-9);
        air(x-7,y+13,z-8,x-6,y+16,z-5);
        stairs(x+9,y+12,z-12,10,3,Direction.SOUTH);
        box(x+11,y+12,z-16,x+12,y+12,z-5,Blocks.STONE_BRICKS);air(x+11,y+13,z-16,x+12,y+16,z-5);
        box(x+8,y+12,z-16,x+12,y+12,z-15,Blocks.STONE_BRICKS);air(x+8,y+13,z-16,x+12,y+16,z-15);
        box(x-2,y+12,z-16,x,y+12,z-5,Blocks.STONE_BRICKS);air(x-2,y+13,z-16,x,y+16,z-5);
        box(x-2,y+12,z-17,x+10,y+12,z-15,Blocks.STONE_BRICKS);air(x-2,y+13,z-17,x+10,y+16,z-15);
        air(x+8,y+23,z-3,x+10,y+26,z+1);
        box(x+6,y+22,z+2,x+10,y+22,z+3,Blocks.STONE_BRICKS);air(x+6,y+23,z+2,x+10,y+26,z+3);
        air(x+6,y+23,z,x+7,y+26,z+1);
    }
    private void entrance(){
        int x=124,y=plan.entranceFloor,z=38;
        box(x-3,y-3,z-6,x+3,y,z+6,Blocks.STONE_BRICKS);
        for(int zz:new int[]{z-5,z+5})column(x,y,zz,8);
        box(x-1,y+8,z-5,x+1,y+9,z+5,Blocks.STONE_BRICKS);
        air(x-3,y+1,z-2,x+3,y+6,z+2);
        box(x-2,y+10,z-6,x+2,y+10,z+6,Blocks.SPRUCE_SLAB);
        arch(x,y,z,true);
        lamp(x+2,y,z-5);lamp(x+2,y,z+5);
    }
    /** Ordered horizontal buckets: preserves last-write precedence and the two-block lookahead. */
    public static final class ColumnIndex {
        private final Map<Long,List<Fill>> columns=new HashMap<>();
        public ColumnIndex(List<Fill> fills,java.util.function.BooleanSupplier cancelled){
            for(var fill:fills){checkCancelled(cancelled);var b=fill.box();
                for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)
                    columns.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),k->new ArrayList<>()).add(fill);
            }
        }
        public List<Fill> at(int chunkX,int chunkZ){return columns.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(chunkX,chunkZ),List.of());}
    }
    public static void checkCancelled(java.util.function.BooleanSupplier cancelled){if(cancelled.getAsBoolean())throw new java.util.concurrent.CancellationException("Excavation planning cancelled");}
    public static void rasterize(List<Fill> fills,BoundingBox clip,java.util.function.BiConsumer<BlockPos,BlockState> output){
        for(var f:fills){var b=f.box();if(!b.intersects(clip))continue;
            for(int x=Math.max(b.minX(),clip.minX());x<=Math.min(b.maxX(),clip.maxX());x++)for(int z=Math.max(b.minZ(),clip.minZ());z<=Math.min(b.maxZ(),clip.maxZ());z++)for(int y=Math.max(b.minY(),clip.minY());y<=Math.min(b.maxY(),clip.maxY());y++)output.accept(new BlockPos(x,y,z),f.state());
        }
    }
}
