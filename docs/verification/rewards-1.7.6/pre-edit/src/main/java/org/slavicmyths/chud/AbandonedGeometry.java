package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Real vanilla blocks: five different chamber grammars and twelve functional secondary modules. */
public final class AbandonedGeometry {
    private final AbandonedPlan plan;
    private final List<ChudGeometry.Fill> fills=new ArrayList<>();
    private final List<AbandonedPlan.Route> pendingRoutes=new ArrayList<>();
    private final List<AbandonedPlan.Route> protectedFlights=new ArrayList<>();
    private AbandonedGeometry(AbandonedPlan p){plan=p;}
    private static BlockState state(Block b){return b.defaultBlockState();}
    private void box(int x,int y,int z,int xx,int yy,int zz,BlockState s){var a=plan.world(new BlockPos(x,y,z));var b=plan.world(new BlockPos(xx,yy,zz));fills.add(new ChudGeometry.Fill(new BoundingBox(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()),Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ())),s.rotate(plan.blockRotation())));}
    private void box(int x,int y,int z,int xx,int yy,int zz,Block b){box(x,y,z,xx,yy,zz,state(b));}
    private void at(int x,int y,int z,Block b){box(x,y,z,x,y,z,b);}
    private void at(int x,int y,int z,BlockState s){box(x,y,z,x,y,z,s);}
    private void air(int x,int y,int z,int xx,int yy,int zz){box(x,y,z,xx,yy,zz,Blocks.AIR);}
    private static BlockState stair(Direction d){return state(Blocks.STONE_BRICK_STAIRS).setValue(BlockStateProperties.HORIZONTAL_FACING,d);}
    private static BlockState rail(RailShape s){return state(Blocks.RAIL).setValue(BlockStateProperties.RAIL_SHAPE,s);}
    private static BlockState fence(){return state(Blocks.DARK_OAK_FENCE).setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true).setValue(BlockStateProperties.WEST,true).setValue(BlockStateProperties.EAST,true);}
    private static long hash(long seed,int x,int z){long n=seed^x*341873128712L^z*132897987541L;n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;return n^(n>>>27);}
    public static double outline(AbandonedPlan p,AbandonedPlan.Chamber h,int dx,int dz){double nx=dx/(h.width()/2.0),nz=dz/(h.depth()/2.0),angle=Math.atan2(nz,nx);if(h.role()==AbandonedPlan.Role.QUARTER||h.role()==AbandonedPlan.Role.SHAFT)return Math.max(nx*nx,nz*nz);return (nx*nx+nz*nz)/(1+.06*Math.sin(angle*5+(p.seed&15))+.035*Math.cos(angle*3));}
    public static int ceiling(AbandonedPlan.Chamber h,int dx,int dz){double q=(double)dx*dx/(h.width()*h.width()/4.0)+(double)dz*dz/(h.depth()*h.depth()/4.0);return h.y()+h.height()-(int)(Math.min(1,q)*Math.min(8,h.height()/4));}
    private void chamber(AbandonedPlan.Chamber h){
        if(h.role()==AbandonedPlan.Role.LABYRINTH)return;
        int rx=h.width()/2,rz=h.depth()/2;
        for(int dx=-rx-3;dx<=rx+3;dx++)for(int dz=-rz-3;dz<=rz+3;dz++){
            double q=outline(plan,h,dx,dz);if(q>1.16)continue;int x=h.x()+dx,z=h.z()+dz,top=ceiling(h,dx,dz);
            Block stone=(hash(plan.seed,x/3,z/3)&7)==0?Blocks.TUFF:Blocks.STONE;
            box(x,h.y()-3,z,x,top+4,z,stone);
            if(q<.98){air(x,h.y()+1,z,x,top,z);at(x,h.y(),z,(hash(plan.seed,x/3,z/3)&3)==0?Blocks.CRACKED_STONE_BRICKS:Blocks.STONE_BRICKS);}
        }
    }
    private void light(int x,int y,int z){at(x,y,z,Blocks.POLISHED_ANDESITE);box(x,y+1,z,x,y+2,z,Blocks.DARK_OAK_FENCE);at(x,y+3,z,Blocks.LANTERN);}
    private void pillar(int x,int y,int z,int top){box(x-1,y,z-1,x+1,y+1,z+1,Blocks.POLISHED_ANDESITE);box(x,y+2,z,x,top-1,z,Blocks.STONE_BRICKS);for(int k=y+6;k<top;k+=7)at(x,k,z,Blocks.CHISELED_STONE_BRICKS);box(x-1,top,z-1,x+1,top,z+1,Blocks.STONE_BRICKS);}
    private void beam(int x,int y,int z,int xx,int zz){box(x,y,z,xx,y,zz,state(Blocks.DARK_OAK_LOG).setValue(BlockStateProperties.AXIS,x==xx?Direction.Axis.Z:Direction.Axis.X));}
    private void deck(int x,int y,int z,int xx,int zz,boolean guards){box(x,y,z,xx,y,zz,Blocks.DARK_OAK_PLANKS);if(guards){box(x,y+1,z,xx,y+1,z,fence());box(x,y+1,zz,xx,y+1,zz,fence());}int bottom=plan.nativeBounds().minY()-plan.anchor.getY();for(int px=x;px<=xx;px+=8)box(px,Math.max(bottom,y-8),z,px,y-1,z,Blocks.STONE_BRICKS);}
    private void quarter(AbandonedPlan.Chamber h){
        boolean attached=plan.kind==AbandonedPlan.Kind.ATTACHED_SMALL;int cols=attached?2:plan.kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?4:3;
        int left=h.x()-h.width()/2+6,back=h.z()-h.depth()/2+6;
        if(attached){
            int street=h.z()+h.depth()/2+8;
            flatRoute("quarter_front_street",new BlockPos(left-2,h.y(),street),new BlockPos(h.x()+h.width()/2+3,h.y(),street),3);
            for(var home:plan.ruinedHomes){
                house(home.x(),home.y(),home.z(),home.width(),home.depth(),home.family());
                flatRoute(home.id()+"_approach",new BlockPos(home.x()+3,home.y(),home.z()+home.depth()),new BlockPos(home.x()+3,home.y(),street),3);
            }
            return;
        }
        int upper=h.y()+12;
        box(left-2,h.y(),back-2,left+cols*16-2,upper,back+18,Blocks.STONE_BRICKS);
        for(var home:plan.ruinedHomes)house(home.x(),home.y(),home.z(),home.width(),home.depth(),home.family());
        // A maintained external flight; destroyed decorative stairs never belong to this backbone.
        int sx=left+cols*16+1;flight(sx,h.y(),h.z()+11,Direction.NORTH,12,3,"quarter_stair");
        box(sx-4,upper,h.z()-15,sx+2,upper,back+18,Blocks.STONE_BRICKS);
        air(sx-4,upper+1,back+18,sx+2,upper+5,h.z()-15);
        flatRoute("quarter_upper_join",new BlockPos(sx,upper,h.z()-13),new BlockPos(left+cols*16-4,upper,h.z()-13),3);
        for(int z=back;z<=h.z()+24;z+=14)light(left+cols*16-1,z<back+18?upper:h.y(),z);
        // Coherent roof debris and a crooked abandoned awning, away from the circulation spine.
        box(h.x()-4,h.y()+1,h.z()-3,h.x()-1,h.y()+2,h.z(),Blocks.MOSSY_COBBLESTONE);
    }
    private void house(int x,int y,int z,int w,int d,int family){
        int height=family==2?10:6;
        box(x,y,z,x+w-1,y+height,z+d-1,Blocks.STONE_BRICKS);air(x+1,y+1,z+1,x+w-2,y+height-1,z+d-2);
        for(int px:new int[]{x,x+w-1})for(int pz:new int[]{z,z+d-1})box(px,y+1,pz,px,y+height,pz,Blocks.STRIPPED_DARK_OAK_LOG);
        beam(x,y+height,z,x+w-1,z);beam(x,y+height,z+d-1,x+w-1,z+d-1);
        int door=x+3;air(door,y+1,z+d-1,door+1,y+3,z+d-1);
        air(x+1,y+3,z,x+2,y+4,z);air(x+w-3,y+3,z,x+w-2,y+4,z);
        for(int px=x;px<x+w;px++)for(int pz=z;pz<z+d;pz++){
            // 3x3 damage fields remove about one third of the roof, rather than salt-and-pepper recolor.
            if(Math.floorMod(hash(plan.seed,px/3,pz/3),100)<32)continue;
            int roof=family==0?Math.min(px-x,w-1-(px-x))/2:family==1?(px-x)/3:family==2?Math.abs(px-x-w/2)/3:(pz-z)/4;
            at(px,y+height+1+roof,pz,Blocks.DARK_OAK_PLANKS);
        }
        at(x+1,y+height-1,z+1,Blocks.COBWEB);box(x+1,y+1,z+2,x+2,y+1,z+4,Blocks.WHITE_WOOL);
        at(x+1,y+1,z+5,Blocks.SPRUCE_STAIRS);box(x+1,y+2,z+6,x+3,y+2,z+6,Blocks.SPRUCE_SLAB);
        box(x+w-2,y+1,z+1,x+w-2,y+3,z+2,Blocks.BOOKSHELF);
        if(Math.floorMod(hash(plan.seed,x,z),100)<40)at(x+w-2,y+4,z+d-2,Blocks.LANTERN);
        if(family==2){
            box(x+1,y+5,z+1,x+w-2,y+5,z+d-2,Blocks.SPRUCE_PLANKS);air(x+w-1,y+6,z+d-3,x+w-1,y+8,z+d-2);
            flight(x+w,y,z+d+7,Direction.NORTH,5,2,"house_external");box(x+w-1,y+5,z+d-4,x+w+1,y+5,z+d-3,Blocks.SPRUCE_PLANKS);
            flatRoute("house_stair_approach",new BlockPos(x+3,y,z+d+1),new BlockPos(x+3,y,z+d+9),3);
            flatRoute("house_stair_lower_landing",new BlockPos(x+3,y,z+d+9),new BlockPos(x+w,y,z+d+9),3);
            flatRoute("house_upper_door",new BlockPos(x+w,y+5,z+d-3),new BlockPos(x+w-2,y+5,z+d-3),3);
            at(x+2,y+6,z+2,Blocks.WHITE_WOOL);
        }
        if(family==3){box(x-1,y+height-1,z+d,x+w-1,y+height-1,z+d+2,Blocks.SPRUCE_SLAB);box(x-1,y+1,z+d+2,x-1,y+height-2,z+d+2,Blocks.SPRUCE_FENCE);}
    }
    private void gallery(AbandonedPlan.Chamber h){
        int rx=h.width()/2,rz=h.depth()/2;
        for(int z=h.z()-rz+8;z<h.z()+rz-3;z+=12){
            for(int x:new int[]{h.x()-rx+7,h.x()+rx-7})pillar(x,h.y(),z,ceiling(h,x-h.x(),z-h.z()));
            beam(h.x()-rx+6,h.y()+h.height()-8,z,h.x()+rx-6,z);
            if((z-h.z())%24==0)light(h.x()+rx-9,h.y(),z);
        }
        railLoop(h.x(),h.y(),h.z(),rx/2,rz/2);
        for(int z:h.depth()>40?new int[]{h.z()-12,h.z()+12}:new int[]{h.z()}){deck(h.x()-rx+5,h.y()+10,z-2,h.x()+rx-5,z+2,true);pillar(h.x()-rx+5,h.y(),z,h.y()+10);pillar(h.x()+rx-5,h.y(),z,h.y()+10);}
        flight(h.x()-rx+4,h.y(),h.z()+rz-5,Direction.NORTH,10,3,"gallery_upper");
        int footZ=h.z()+rz-3;
        flatRoute("gallery_stair_approach",h.floor(),new BlockPos(h.x()-8,h.y(),h.z()),3);
        flatRoute("gallery_stair_approach_south",new BlockPos(h.x()-8,h.y(),h.z()),new BlockPos(h.x()-8,h.y(),footZ+2),3);
        flatRoute("gallery_stair_lower_landing",new BlockPos(h.x()-8,h.y(),footZ+2),new BlockPos(h.x()-rx+4,h.y(),footZ+2),3);
        flatRoute("gallery_stair_foot",new BlockPos(h.x()-rx+4,h.y(),footZ+2),new BlockPos(h.x()-rx+4,h.y(),footZ),3);
        deck(h.x()-rx+6,h.y()+10,h.z()-12,h.x()-rx+12,h.z()+12,false);
        box(h.x()-rx+6,h.y()+11,h.z()-12,h.x()-rx+6,h.y()+11,h.z()+12,fence());box(h.x()-rx+12,h.y()+11,h.z()-12,h.x()-rx+12,h.y()+11,h.z()+12,fence());
        int landing=h.z()+rz-25;
        flatRoute("gallery_upper_join",new BlockPos(h.x()-rx+4,h.y()+10,landing),new BlockPos(h.x()-rx+9,h.y()+10,landing),3);
        flatRoute("gallery_upper_north",new BlockPos(h.x()-rx+9,h.y()+10,landing),new BlockPos(h.x()-rx+9,h.y()+10,h.z()-12),3);
        flatRoute("gallery_upper_south",new BlockPos(h.x()-rx+9,h.y()+10,landing),new BlockPos(h.x()-rx+9,h.y()+10,h.z()+12),3);
        flatRoute("gallery_north_bridge",new BlockPos(h.x()-rx+9,h.y()+10,h.z()-12),new BlockPos(h.x(),h.y()+10,h.z()-12),3);
        flatRoute("gallery_south_bridge",new BlockPos(h.x()-rx+9,h.y()+10,h.z()+12),new BlockPos(h.x(),h.y()+10,h.z()+12),3);
        for(int i=0;i<3;i++){int x=h.x()-rx+9+i*7;box(x,h.y()+1,h.z()+rz-8,x+2,h.y()+2,h.z()+rz-6,Blocks.RAW_IRON_BLOCK);}
    }
    private void shaft(AbandonedPlan.Chamber h){
        int r=18,gap=plan.shaftGap();int[] ys=new int[plan.shaftRings()];for(int i=0;i<ys.length;i++)ys[i]=h.y()+i*gap;
        for(int y:ys){
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))>=r-3){at(h.x()+dx,y,h.z()+dz,Blocks.STONE_BRICKS);if(Math.max(Math.abs(dx),Math.abs(dz))==r-3)at(h.x()+dx,y+1,h.z()+dz,fence());}
            deck(h.x()-r,y,h.z()-2,h.x()+r,h.z()+2,true);
            air(h.x()-r,y+1,h.z()-1,h.x()+r,y+4,h.z()+1);
            // Bridge guards stop at the outer ring intersection; its inner guard remains intact.
            air(h.x()-r,y+1,h.z()-3,h.x()-r+2,y+4,h.z()+3);
            air(h.x()+r-2,y+1,h.z()-3,h.x()+r,y+4,h.z()+3);
            for(int dx:new int[]{-r-1,r+1}){pillar(h.x()+dx,h.y(),h.z()+7,h.y()+h.height()-8);light(h.x()+dx,y,h.z()+8);}
        }
        // Two independent protected stair systems, with intermediate landings.
        for(int side:new int[]{-1,1})for(int level=0;level<ys.length-1;level++){
            int x=AbandonedPlan.shaftStairX(h,side,level),y=ys[level],z=h.z()+(level%2==0?-gap:gap);Direction d=level%2==0?Direction.SOUTH:Direction.NORTH;
            flight(x,y,z,d,gap,3,"shaft_"+side+"_"+level);
            int endZ=h.z()+(level%2==0?gap:-gap);
            box(Math.min(x,h.x()+side*18),y,z,Math.max(x+2,h.x()+side*18),y,z+2,Blocks.STONE_BRICKS);
            box(Math.min(x,h.x()+side*18),y+gap,endZ,Math.max(x+2,h.x()+side*18),y+gap,endZ+2,Blocks.STONE_BRICKS);
            air(Math.min(x,h.x()+side*18),y+gap+1,endZ,Math.max(x+2,h.x()+side*18),y+gap+5,endZ+2);
            int edgeX=h.x()+side*18,approachZ=z-d.getStepZ()*2,exitZ=endZ+d.getStepZ()*2;
            flatRoute("shaft_bottom_ring_join",new BlockPos(edgeX,y,z),new BlockPos(edgeX,y,approachZ),3);
            flatRoute("shaft_bottom_stair_join",new BlockPos(edgeX,y,approachZ),new BlockPos(x,y,approachZ),3);
            flatRoute("shaft_top_ring_join",new BlockPos(edgeX,y+gap,endZ),new BlockPos(edgeX,y+gap,exitZ),3);
            flatRoute("shaft_top_stair_join",new BlockPos(edgeX,y+gap,exitZ),new BlockPos(x,y+gap,exitZ),3);
        }
        int suspendedY=h.y()+h.height()-12;
        box(h.x()-2,suspendedY,h.z()-2,h.x()+2,suspendedY+1,h.z()+2,Blocks.DARK_OAK_PLANKS);
        for(int dx:new int[]{-2,2})for(int dz:new int[]{-2,2})box(h.x()+dx,suspendedY+2,h.z()+dz,h.x()+dx,h.y()+h.height()-5,h.z()+dz,Blocks.CHAIN);
    }
    private void sealed(AbandonedPlan.Chamber h){
        int x=h.x(),y=h.y(),z=h.z()-h.depth()/2+12;
        box(x-6,y,z-5,x+6,y+14,z,Blocks.DEEPSLATE_BRICKS);box(x-7,y-3,z-19,x+7,y+16,z-6,Blocks.STONE);
        for(int side:new int[]{-1,1})for(int k=0;k<3;k++)pillar(x+side*(9+k*2),y,z+2, y+18-k*2);
        for(int k=0;k<4;k++)box(x-10+k,y+15+k,z+1,x+10-k,y+15+k,z+2,Blocks.CHISELED_DEEPSLATE);
        for(int px:new int[]{-4,2})for(int py:new int[]{3,9})box(x+px,y+py,z+1,x+px+2,y+py+2,z+1,Blocks.POLISHED_BLACKSTONE);
        light(x-13,y,z-5);light(x+13,y,z-5);
    }
    private void restoreShaftCirculation(AbandonedPlan.Chamber h){
        // Reserve ring headroom after tunnel caps. Pier cores sit outside these three-wide paths.
        for(int level=0;level<plan.shaftRings();level++){
            int y=h.y()+plan.shaftGap()*level;var openings=new HashSet<Long>();
            for(var route:pendingRoutes)for(var p:route.floors())if(p.getY()==y){int radius=route.width()/2;for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)openings.add(BlockPos.asLong(p.getX()+dx,0,p.getZ()+dz));}
            for(int dx=-18;dx<=18;dx++)for(int dz=-18;dz<=18;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))>=15){
                int x=h.x()+dx,z=h.z()+dz;at(x,y,z,Blocks.STONE_BRICKS);air(x,y+1,z,x,y+4,z);
                if(Math.max(Math.abs(dx),Math.abs(dz))==15&&Math.abs(dz)>1&&!openings.contains(BlockPos.asLong(x,0,z)))at(x,y+1,z,fence());
            }
        }
    }
    private void secondary(AbandonedPlan.Chamber h){
        int x=h.x(),y=h.y(),z=h.z();
        switch(h.module()){
            case GUARD_POST->{box(x-4,y+1,z-4,x-1,y+1,z-4,Blocks.STONE_BRICK_STAIRS);box(x+3,y+1,z-4,x+3,y+3,z-2,Blocks.IRON_BARS);}
            case TOOL_ROOM->{box(x-4,y+1,z-4,x-4,y+3,z+2,Blocks.BOOKSHELF);box(x+2,y+1,z-4,x+3,y+1,z-2,Blocks.ANVIL);}
            case ORE_SORTING->{for(int i=0;i<3;i++)box(x-4+i*3,y+1,z-4,x-3+i*3,y+2,z-3,i==0?Blocks.RAW_COPPER_BLOCK:Blocks.RAW_IRON_BLOCK);}
            case OLD_INFIRMARY->{for(int i=0;i<2;i++)box(x-4,y+1,z-3+i*5,x-3,y+1,z-1+i*5,Blocks.WHITE_WOOL);box(x+2,y+2,z-4,x+3,y+2,z-2,Blocks.SPRUCE_SLAB);}
            case WORKER_REST->{box(x-4,y+1,z-4,x-1,y+1,z-4,Blocks.SPRUCE_STAIRS);box(x+2,y+2,z-4,x+3,y+2,z-2,Blocks.SPRUCE_SLAB);}
            case VENT_SHAFT->{box(x+2,y+1,z-4,x+4,y+7,z-2,Blocks.STONE_BRICKS);air(x+3,y+2,z-3,x+3,y+6,z-3);box(x+2,y+2,z-2,x+4,y+6,z-2,Blocks.IRON_BARS);}
            case COLLAPSED_STAIRS->{for(int i=0;i<4;i++)box(x-4,y+1+i,z-4+i,x-3,y+1+i,z-4+i,stair(Direction.SOUTH));box(x-4,y+5,z,x-2,y+6,z+1,Blocks.COBBLESTONE);}
            case MINECART_LOOP->{for(int dx=-3;dx<=3;dx++){at(x+dx,y+1,z-3,rail(RailShape.EAST_WEST));at(x+dx,y+1,z+3,rail(RailShape.EAST_WEST));}for(int dz=-2;dz<=2;dz++){at(x-3,y+1,z+dz,rail(RailShape.NORTH_SOUTH));at(x+3,y+1,z+dz,rail(RailShape.NORTH_SOUTH));}at(x-3,y+1,z-3,rail(RailShape.SOUTH_EAST));at(x+3,y+1,z-3,rail(RailShape.SOUTH_WEST));at(x-3,y+1,z+3,rail(RailShape.NORTH_EAST));at(x+3,y+1,z+3,rail(RailShape.NORTH_WEST));}
            case WATER_DRAIN->basin(x+2,y,z-4,2,5);
            case ABANDONED_STORAGE->{for(int i=0;i<2;i++)box(x-4,y+1,z-4+i*5,x-2,y+2,z-2+i*5,Blocks.STRIPPED_SPRUCE_LOG);box(x+3,y+1,z-4,x+4,y+3,z-2,Blocks.BOOKSHELF);}
            case BROKEN_BALCONY->{deck(x-4,y+4,z-4,x+4,z-2,true);box(x-4,y+1,z-4,x-4,y+3,z-4,Blocks.STONE_BRICKS);air(x+2,y+4,z-3,x+3,y+4,z-2);}
            case CAVE_BASIN->{basin(x-4,y,z-4,3,3);at(x+3,y+1,z-3,Blocks.BROWN_MUSHROOM);box(x+2,y,z-4,x+4,y,z-2,Blocks.MOSS_BLOCK);}
        }
        at(x-h.width()/2+2,y+h.height()-2,z-h.depth()/2+2,Blocks.COBWEB);
        if(Math.floorMod(hash(plan.seed,x,z),100)<40)at(x+3,y+4,z+3,Blocks.LANTERN);
    }
    private void basin(int x,int y,int z,int w,int d){box(x-1,y-2,z-1,x+w,y,z+d,Blocks.STONE_BRICKS);box(x,y-1,z,x+w-1,y-1,z+d-1,Blocks.WATER);air(x,y,z,x+w-1,y,z+d-1);}
    private void railLoop(int x,int y,int z,int rx,int rz){
        for(int dx=-rx+1;dx<rx;dx++){at(x+dx,y+1,z-rz,rail(RailShape.EAST_WEST));at(x+dx,y+1,z+rz,rail(RailShape.EAST_WEST));}
        for(int dz=-rz+1;dz<rz;dz++){at(x-rx,y+1,z+dz,rail(RailShape.NORTH_SOUTH));at(x+rx,y+1,z+dz,rail(RailShape.NORTH_SOUTH));}
        at(x-rx,y+1,z-rz,rail(RailShape.SOUTH_EAST));at(x+rx,y+1,z-rz,rail(RailShape.SOUTH_WEST));
        at(x-rx,y+1,z+rz,rail(RailShape.NORTH_EAST));at(x+rx,y+1,z+rz,rail(RailShape.NORTH_WEST));
    }
    private void mazeLandmark(int index,BlockPos p,boolean decorate){
        int style=Math.floorMod(index+(int)Math.floorMod(plan.seed,8),8),x=p.getX(),y=p.getY(),z=p.getZ();
        // Eight tunnel grammars; decoration occupies shoulders, leaving cardinal junctions open.
        int radius=style==0?3:style==1?6:5;
        if(!decorate){
            box(x-radius,y-3,z-radius,x+radius,y+7,z+radius,Blocks.STONE);
            air(x-radius+1,y+1,z-radius+1,x+radius-1,y+5,z+radius-1);
            box(x-radius+1,y,z-radius+1,x+radius-1,y,z+radius-1,Blocks.STONE_BRICKS);
            return;
        }
        switch(style){
            case 0->beam(x-2,y+5,z-2,x+2,z-2);
            case 1->{pillar(x-4,y,z-4,y+6);pillar(x+4,y,z+4,y+6);}
            case 2->box(x+3,y+1,z+3,x+4,y+3,z+4,Blocks.IRON_ORE);
            case 3->{box(x+3,y+1,z+3,x+4,y+2,z+4,Blocks.COBBLESTONE);at(x-3,y+4,z-3,Blocks.COBWEB);}
            case 4->{box(x+3,y+1,z+3,x+4,y+3,z+4,Blocks.STRIPPED_DARK_OAK_LOG);beam(x-3,y+5,z+3,x+3,z+3);}
            case 5->{box(x+3,y-3,z+3,x+4,y,z+4,Blocks.DEEPSLATE);air(x+3,y-2,z+3,x+3,y-1,z+3);}
            case 6->basin(x+3,y,z+3,1,1);
            case 7->{box(x+3,y+1,z+3,x+4,y+2,z+4,Blocks.RAW_COPPER_BLOCK);at(x-3,y+1,z-3,rail(RailShape.NORTH_SOUTH));}
        }
    }
    private void flight(int x,int y,int z,Direction d,int rise,int width,String id){
        int run=rise*2;var positions=new ArrayList<BlockPos>();
        for(int k=0;k<=run;k++){
            // Six one-block risers followed by a three-block landing, within the fixed run.
            int raised=Math.min(rise,(k/12)*6+Math.min(k%12,10)*6/10);
            positions.add(new BlockPos(x,y+raised,z+d.getStepZ()*k));
        }
        for(int i=1;i<=3;i++)positions.add(new BlockPos(x,y+rise,z+d.getStepZ()*(run+i)));
        var flight=new AbandonedPlan.Route(id,List.copyOf(positions),width,true);corridor(flight);protectedFlights.add(flight);
    }
    private void flatRoute(String id,BlockPos a,BlockPos b,int width){var positions=new ArrayList<BlockPos>();int dx=b.getX()-a.getX(),dz=b.getZ()-a.getZ(),length=Math.abs(dx)+Math.abs(dz);for(int k=0;k<=length;k++)positions.add(new BlockPos(a.getX()+Integer.signum(dx)*Math.min(k,Math.abs(dx)),a.getY(),a.getZ()+Integer.signum(dz)*Math.max(0,k-Math.abs(dx))));corridor(new AbandonedPlan.Route(id,List.copyOf(positions),width,true));}
    private void corridor(AbandonedPlan.Route route){
        pendingRoutes.add(route);
    }
    private void flushRoutes(){
        for(var route:pendingRoutes)emitCorridor(route,true);
        for(var route:pendingRoutes)for(int i=30;i<route.floors().size()-3;i+=30){
            var p=route.floors().get(i);var next=route.floors().get(i+1);int radius=route.width()/2+1;
            if(next.getX()==p.getX()){
                box(p.getX()-radius,p.getY()+6,p.getZ(),p.getX()+radius,p.getY()+6,p.getZ(),Blocks.CHISELED_STONE_BRICKS);
                for(int side:new int[]{-1,1})box(p.getX()+side*radius,p.getY()+1,p.getZ(),p.getX()+side*radius,p.getY()+5,p.getZ(),Blocks.STONE_BRICKS);
            }else{
                box(p.getX(),p.getY()+6,p.getZ()-radius,p.getX(),p.getY()+6,p.getZ()+radius,Blocks.CHISELED_STONE_BRICKS);
                for(int side:new int[]{-1,1})box(p.getX(),p.getY()+1,p.getZ()+side*radius,p.getX(),p.getY()+5,p.getZ()+side*radius,Blocks.STONE_BRICKS);
            }
        }
        for(var route:pendingRoutes)emitCorridor(route,false);
        for(var flight:protectedFlights)for(int i=4;i<flight.floors().size()-5;i++){var p=flight.floors().get(i);int radius=flight.width()/2+1;for(int side:new int[]{-1,1}){int x=p.getX()+side*radius;at(x,p.getY(),p.getZ(),Blocks.STONE_BRICKS);at(x,p.getY()+1,p.getZ(),fence());}}
    }
    private void emitCorridor(AbandonedPlan.Route route,boolean shell){
        // Max-height junctions are propagated before the single final floor/headroom write.
        var floors=new LinkedHashMap<Long,Integer>();int radius=route.width()/2;
        for(var p:route.floors())for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)floors.merge(BlockPos.asLong(p.getX()+dx,0,p.getZ()+dz),p.getY(),Math::max);
        var queue=new ArrayDeque<Long>(floors.keySet());
        while(!queue.isEmpty()){long key=queue.removeFirst();var p=BlockPos.of(key);int y=floors.get(key);for(var d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){long next=p.relative(d).asLong();var old=floors.get(next);if(old!=null&&old<y-1){floors.put(next,y-1);queue.add(next);}}}
        if(shell)for(var entry:floors.entrySet()){
            var p=BlockPos.of(entry.getKey());int x=p.getX(),z=p.getZ(),y=entry.getValue();
            boolean inside=plan.chambers.stream().anyMatch(h->h.role()!=AbandonedPlan.Role.LABYRINTH&&Math.abs(x-h.x())<h.width()/2-2&&Math.abs(z-h.z())<h.depth()/2-2&&y>=h.y()&&y+5<=ceiling(h,x-h.x(),z-h.z()));
            if(!inside)box(x-1,y-3,z-1,x+1,y+7,z+1,Blocks.STONE);
        }
        if(shell)return;
        int count=0;
        // All shells must precede all clearances: adjacent shells cannot refill earlier headroom.
        for(var entry:floors.entrySet()){
            var p=BlockPos.of(entry.getKey());int x=p.getX(),z=p.getZ(),y=entry.getValue();
            box(x,y-3,z,x,y,z,Blocks.STONE_BRICKS);air(x,y+1,z,x,y+5,z);
            BlockState floor=state(Blocks.STONE_BRICKS);
            for(var d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST))if(floors.getOrDefault(p.relative(d).asLong(),y)==y+1){floor=stair(d);break;}
            at(x,y,z,floor);
            if(count++%(route.width()>=5?45:18)==0){at(x,y+6,z,Blocks.STONE_BRICKS);at(x,y+5,z,state(Blocks.LANTERN).setValue(BlockStateProperties.HANGING,true));}
        }
    }
    public static List<ChudGeometry.Fill> build(AbandonedPlan plan){return List.copyOf(construct(plan).fills);}
    public static List<AbandonedPlan.Route> constructionRoutes(AbandonedPlan plan){return List.copyOf(construct(plan).pendingRoutes);}
    private static AbandonedGeometry construct(AbandonedPlan plan){
        var g=new AbandonedGeometry(plan);
        for(var h:plan.chambers)g.chamber(h);
        for(var h:plan.chambers)switch(h.role()){case QUARTER->g.quarter(h);case GALLERY->g.gallery(h);case SHAFT->g.shaft(h);case SEALED_GATE->g.sealed(h);case SECONDARY->g.secondary(h);case LABYRINTH->{}}
        for(var edge:plan.mazeEdges){var a=plan.mazeNodes.get(edge.from());var b=plan.mazeNodes.get(edge.to());var positions=new ArrayList<BlockPos>();int length=a.distManhattan(b);for(int i=0;i<=length;i++)positions.add(a.offset(Integer.signum(b.getX()-a.getX())*i,0,Integer.signum(b.getZ()-a.getZ())*i));g.corridor(new AbandonedPlan.Route("maze_edge_"+edge.from()+"_"+edge.to(),positions,edge.extra()?3:5,true));}
        for(int i=0;i<plan.mazeNodes.size();i++)g.mazeLandmark(i,plan.mazeNodes.get(i),false);
        for(var route:plan.routes)g.corridor(route);
        g.flushRoutes();
        for(int i=0;i<plan.mazeNodes.size();i++)g.mazeLandmark(i,plan.mazeNodes.get(i),true);
        // Rails have no body collision; restore their complete circuits after corridor clearances.
        for(var h:plan.chambers){if(h.role()==AbandonedPlan.Role.GALLERY)g.railLoop(h.x(),h.y(),h.z(),h.width()/4,h.depth()/4);else if(h.module()==AbandonedPlan.Module.MINECART_LOOP)g.railLoop(h.x(),h.y(),h.z(),3,3);}
        for(var chamber:plan.chambers)if(chamber.role()==AbandonedPlan.Role.SHAFT)g.restoreShaftCirculation(chamber);
        if(plan.kind==AbandonedPlan.Kind.ATTACHED_SMALL){
            // Open exactly the saved physical mine gate aperture, only for a saved complete extension.
            g.box(-111,-43,-96,-107,-40,-83,Blocks.STONE_BRICKS);g.air(-111,-39,-96,-107,-35,-83);
        }else{
            var p=plan.surfacePortal();int x=p.getX(),y=p.getY(),z=p.getZ();g.box(x-3,y-3,z,x+3,y,z+4,Blocks.STONE_BRICKS);
            if(plan.flavor==2){
                g.box(x-2,y-2,z-2,x+2,y+1,z+2,Blocks.STONE);
                for(int dx:new int[]{-3,0,3})for(int dz:new int[]{-3,3})g.at(x+dx,y+2,z+dz,Blocks.MOSSY_COBBLESTONE);
            }else if(plan.flavor==1){
                for(int side:new int[]{-1,1})g.box(x+side*2,y,z,x+side*2,y+5,z,Blocks.STRIPPED_DARK_OAK_LOG);
                g.beam(x-2,y+5,z,x+2,z);
                var approach=plan.routes.stream().filter(r->r.id().equals("entrance")).findFirst().orElseThrow();
                for(int k=1;k<=14;k++){
                    final int railZ=z-k;int floor=approach.floors().stream().filter(q->Math.abs(q.getX()-x)<=2&&Math.abs(q.getZ()-railZ)<=2).mapToInt(BlockPos::getY).max().orElseThrow();
                    int higher=approach.floors().stream().filter(q->Math.abs(q.getX()-x)<=2&&Math.abs(q.getZ()-(railZ+1))<=2).mapToInt(BlockPos::getY).max().orElse(floor);
                    g.at(x,floor,railZ,Blocks.STONE_BRICKS);
                    g.at(x,floor+1,railZ,rail(higher>floor?RailShape.ASCENDING_SOUTH:RailShape.NORTH_SOUTH));
                }
            }else{
                for(int side:new int[]{-1,1})g.pillar(x+side*2,y,z,y+6);
                g.box(x-3,y+7,z,x+1,y+7,z,Blocks.CHISELED_STONE_BRICKS);
                g.air(x-1,y+1,z-1,x+1,y+4,z+1);
                g.box(x-2,y+1,z+2,x-2,y+3,z+2,Blocks.IRON_BARS);
            }
        }
        return g;
    }
}
