package org.slavicmyths.depths;

import java.util.*;

/** Deterministic finite voxel blueprint. Tokens are resolved against the installed block registry at runtime. */
public final class DepthsLayout {
    public record Cell(int u,int h,int v){}
    private final DepthsPlan p;
    private final LinkedHashMap<Cell,String> blocks=new LinkedHashMap<>();
    private DepthsLayout(DepthsPlan p){this.p=p;}
    public static Map<Cell,String> build(DepthsPlan plan){var g=new DepthsLayout(plan);g.construct();return Collections.unmodifiableMap(g.blocks);}
    private void raw(int u,int h,int v,String block){blocks.put(new Cell(u,h,v),block);}
    private void box(int u0,int h0,int v0,int u1,int h1,int v1,String block){for(int u=u0;u<=u1;u++)for(int h=h0;h<=h1;h++)for(int v=v0;v<=v1;v++)raw(u,h,v,block);}
    private boolean reserved(int u,int h,int v){
        if(u>=-6&&u<=179&&Math.abs(v)<=2&&h>DepthsPlan.floor(u)&&h<=DepthsPlan.floor(u)+5)return true;
        if(u>=61&&u<=64&&Math.abs(v)<=2&&h>=-15&&h<=-10)return true;
        if(u>=96&&u<=112&&Math.abs(v)<=8&&h>=-15&&h<=-4)return true;
        if(u>=142&&u<=170&&Math.abs(v)<=14&&h>=-23&&h<=-7)return true;
        for(var s:DepthsPlan.SPAWNS)if(u==s.u()&&v==s.v()&&h>DepthsPlan.floor(u)&&h<=DepthsPlan.floor(u)+2)return true;
        for(var c:DepthsPlan.CHESTS)if(u>=c.x()-1&&u<=c.x()+1&&v>=c.z()-1&&v<=c.z()+1&&h>=c.y()&&h<=c.y()+2)return true;
        return false;
    }
    private void put(int u,int h,int v,String block){if(!reserved(u,h,v))raw(u,h,v,block);}
    private void decor(int u0,int h0,int v0,int u1,int h1,int v1,String block){for(int u=u0;u<=u1;u++)for(int h=h0;h<=h1;h++)for(int v=v0;v<=v1;v++)put(u,h,v,block);}
    private int hash(int u,int h,int v){long n=p.seed()^u*341873128712L^v*132897987541L^h*42317861L;n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;return Math.floorMod(n^(n>>>27),100);}
    private String masonry(int u,int h,int v,boolean floor){
        int n=hash(u/3,h/4,v/3);if(u<27)return n<65?"cobblestone":n<85?"cracked_stone_bricks":"mossy_cobblestone";
        if(floor)return n<70?"deepslate_bricks":n<90?"cracked_deepslate_bricks":n<96?"tuff":"mossy_stone_bricks";
        return n<62?"deepslate_bricks":n<80?"stone_bricks":n<94?"cracked_deepslate_bricks":"tuff";
    }
    private void room(int u0,int u1,int radius,int floor,int top){
        for(int u=u0;u<=u1;u++)for(int v=-radius;v<=radius;v++){
            int roof=top+(Math.abs(v)<radius-3?0:-2);
            for(int h=floor-2;h<=roof+2;h++)raw(u,h,v,masonry(u,h,v,h<=floor));
            if(u>u0&&u<u1&&Math.abs(v)<radius)for(int h=floor+1;h<=roof;h++)raw(u,h,v,"air");
        }
    }
    private void post(int u,int v,int base,int height,int radius){
        decor(u-radius,base,v-radius,u+radius,base+1,v+radius,"polished_deepslate");
        decor(u-radius+1,base+2,v-radius+1,u+radius,base+height-1,v+radius,"deepslate_bricks");
        decor(u-radius,base+height,v-radius,u+radius,base+height,v+radius,"chiseled_stone_bricks");
    }
    private void lamp(int u,int h,int v){put(u,h,v,"polished_deepslate");put(u,h+1,v,"lantern");}
    private void hanging(int u,int h,int v){
        int anchor=h+2;
        if(u>=91&&u<=116)anchor=Math.abs(v)>=10?-5:-3;
        if(u>=133)anchor=-3-Math.max(0,(Math.max(Math.abs(u-156),Math.abs(v))-15)/3);
        put(u,anchor,v,"polished_deepslate");for(int y=h+1;y<anchor;y++)put(u,y,v,"chain");put(u,h,v,"lantern[hanging=true]");
    }
    private void stair(int u,int h,int v,boolean back){raw(u,h,v,"stone_brick_stairs[facing="+p.facing(back)+"]");}
    private void passage(int u0,int u1,boolean wood){
        for(int u=u0;u<=u1;u++){
            int f=DepthsPlan.floor(u),r=u>u1-4?4:3;
            for(int v=-4;v<=4;v++){
                for(int h=f-2;h<=f+7;h++)raw(u,h,v,masonry(u,h,v,h<=f));
                if(Math.abs(v)<=r)for(int h=f+1;h<=f+5;h++)raw(u,h,v,"air");
            }
            if(wood&&(u==11||u==19||u==27)){
                for(int side:new int[]{-3,3})decor(u,f+1,side,u,f+5,side,"spruce_log");
                decor(u,f+6,-3,u,f+6,3,"spruce_log[axis="+(p.fx()==0?"x":"z")+"]");
            }
        }
        for(int u=u0+3;u<u1;u+=wood?9:7)hanging(u,DepthsPlan.floor(u)+4,3);
    }
    private void construct(){
        // E0: a low 13x13 ruin with a short approach, never a surface temple.
        box(-6,-3,-6,6,0,6,"coarse_dirt");box(-6,1,-6,6,5,6,"air");
        for(int u=-6;u<=6;u++)for(int v=-1;v<=1;v++)raw(u,0,v,u<0?"gravel":"cobblestone");
        for(int v:new int[]{-3,3}){decor(3,0,v,5,4,v,"spruce_log");decor(2,0,v-1,5,1,v+1,"mossy_cobblestone");}
        decor(3,5,-3,5,5,3,"spruce_log[axis="+(p.fx()==0?"x":"z")+"]");
        decor(4,4,-5,5,5,-4,"cracked_stone_bricks");decor(4,4,4,5,4,5,"cobblestone");
        decor(-4,1,-5,1,2,-5,"mossy_cobblestone");decor(-2,1,5,1,1,5,"spruce_fence");
        for(int u:new int[]{-4,0,4})put(u,1,-4,"moss_carpet");
        put(-5,1,3,"short_grass");put(-1,1,4,"short_grass");put(1,1,-4,"short_grass");hanging(4,3,-3);
        // E1 + K: supported half-step descents, continuous walkable landings.
        passage(7,43,true);passage(117,132,false);
        box(18,-8,-6,22,-2,-3,"cobblestone");box(19,-5,-5,21,-3,-3,"air");decor(20,-5,-5,20,-4,-5,"spruce_planks");put(21,-3,-5,"cobweb");
        box(34,-14,3,38,-8,6,"deepslate_bricks");box(35,-11,3,37,-9,5,"air");put(36,-11,5,"cracked_stone_bricks");
        // E2: monumental open 11x12 gate with four side supports.
        room(44,65,11,-16,-4);
        for(int u:new int[]{49,59})for(int v:new int[]{-8,8})post(u,v,-16,10,1);
        decor(61,-15,-5,63,-4,5,"polished_deepslate");
        for(int side:new int[]{-1,1}){
            decor(60,-15,side<0?-5:3,60,-6,side<0?-3:5,"dark_oak_planks");
            for(int h:new int[]{-13,-9,-6})decor(60,h,side*4,60,h,side*4,"iron_bars");
        }
        box(60,-15,-2,64,-10,2,"air");
        for(int u:new int[]{47,55,62})for(int v:new int[]{-7,7})lamp(u,-15,v);
        // E3: one short guarded bridge, shallow dry fissures with obvious return steps.
        room(66,90,6,-16,-8);
        for(int side:new int[]{-1,1}){
            int v0=side<0?-5:4,v1=side<0?-4:5;
            box(75,-23,side<0?-6:3,85,-17,side<0?-3:6,"deepslate_bricks");
            box(76,-23,v0,84,-21,v1,"stone");box(76,-20,v0,84,-15,v1,"air");
            for(int u=80;u<=84;u++)for(int v=v0;v<=v1;v++){int h=-21+(u-80);box(u,-23,v,u,h-1,v,"deepslate_bricks");stair(u,h,v,false);box(u,h+1,v,u,h+4,v,"air");}
            decor(76,-15,side*3,84,-15,side*3,"deepslate_brick_wall");
            for(int u:new int[]{73,87})lamp(u,-15,side*4);
        }
        for(int u:new int[]{66,89}){decor(u,-15,-4,u,-11,-4,"deepslate_bricks");decor(u,-15,4,u,-11,4,"deepslate_bricks");decor(u,-10,-4,u,-10,4,"chiseled_stone_bricks");}
        decor(69,-15,-5,70,-13,-5,"polished_deepslate");decor(88,-15,5,88,-14,5,"mossy_cobblestone");
        // E4: clear central combat field, four edge piers, two small side platforms.
        room(91,116,13,-16,-4);
        for(int u:new int[]{96,110})for(int v:new int[]{-10,10})post(u,v,-16,10,1);
        for(int side:new int[]{-1,1}){
            decor(101,-15,side<0?-12:9,107,-14,side<0?-9:12,"deepslate_tiles");
            for(int v:new int[]{side*8,side*9})decor(103,-16,v,105,Math.abs(v)==8?-15:-14,v,"stone_brick_stairs[facing="+new String[]{"north","east","south","west"}[(p.direction()+(side<0?3:1))&3]+"]");
            for(int u:new int[]{94,103,113})hanging(u,-10,side*11);
        }
        // E5: irregular octagonal cavern, one open 29x29 arena, four edge columns.
        for(int u=133;u<=179;u++)for(int v=-23;v<=23;v++){
            int dx=Math.abs(u-156),dz=Math.abs(v),edge=Math.max(dx,dz);
            int roof=-4-Math.max(0,(edge-15)/3);boolean interior=edge<=21&&dx+dz<=37;
            for(int h=-27;h<=roof+2;h++)raw(u,h,v,h>roof&&hash(u,h,v)<8?"calcite":masonry(u,h,v,h<=-24));
            if(interior)for(int h=-23;h<=roof;h++)raw(u,h,v,"air");
        }
        for(int u:new int[]{138,173})for(int v:new int[]{-18,18})post(u,v,-24,u==138?14:11,1);
        for(int v:new int[]{-17,17})lamp(141,-23,v);for(int v:new int[]{-15,15})lamp(174,-23,v);
        for(int u:new int[]{139,173})for(int v:new int[]{-9,9})hanging(u,-15,v);
        for(int u=152;u<=160;u++)for(int v=-4;v<=4;v++)if(Math.abs(u-156)+Math.abs(v)==4)raw(u,-24,v,"chiseled_deepslate");
        for(int u:new int[]{137,175})decor(u,-23,-20,u+1,-20,-19,"cracked_deepslate_bricks");
        for(int side:new int[]{-1,1}){
            decor(151,-23,side*20,160,-22,side*21,"deepslate_tiles");
            for(int u:new int[]{149,150})decor(u,-24,side*20,u,u==149?-23:-22,side*20,"stone_brick_stairs[facing="+p.facing(false)+"]");
            decor(162,-23,side*20,164,-23,side*21,"moss_carpet");
            for(int u:new int[]{145,167})if(side>0||u==145){put(u,-5,side*20,"pointed_dripstone[vertical_direction=down,thickness=frustum]");put(u,-6,side*20,"pointed_dripstone[vertical_direction=down,thickness=tip]");}
        }
        if(p.skylight()){
            for(int u=161;u<=169;u++)for(int v=15;v<=23;v++)if(Math.abs(u-165)+Math.abs(v-19)<=6){
                if(Math.abs(u-165)+Math.abs(v-19)<5)box(u,-6,v,u,p.skyTop(),v,"air");
                else{box(u,p.skyTop()-1,v,u,p.skyTop(),v,"cobblestone");box(u,p.skyTop()+1,v,u,p.skyTop()+2,v,"cobblestone_wall");}
            }
            decor(161,-22,15,169,-22,23,"deepslate_bricks");
            for(int u=159;u<=160;u++)decor(u,-24,13,u,u==159?-23:-22,17,"stone_brick_stairs[facing="+p.facing(false)+"]");
        }
        // Final axis owns all seams. Decor cannot close the doors or alter stair grades.
        for(int u=-6;u<=179;u++)for(int v=-2;v<=2;v++){
            int f=DepthsPlan.floor(u);raw(u,f,v,masonry(u,f,v,true));
            if((u>=7&&u<=43||u>=117&&u<=132)&&f<DepthsPlan.floor(u-1))stair(u,f,v,true);
            box(u,f+1,v,u,f+5,v,"air");
        }
        box(61,-15,-2,64,-10,2,"air");
        for(var spawn:DepthsPlan.SPAWNS){int f=DepthsPlan.floor(spawn.u());raw(spawn.u(),f,spawn.v(),masonry(spawn.u(),f,spawn.v(),true));box(spawn.u(),f+1,spawn.v(),spawn.u(),f+2,spawn.v(),"air");}
        for(var chest:DepthsPlan.CHESTS){raw(chest.x(),chest.y()-1,chest.z(),"deepslate_bricks");box(chest.x()-1,chest.y(),chest.z()-1,chest.x()+1,chest.y()+2,chest.z()+1,"air");}
        for(int u:new int[]{152,160})for(int v:new int[]{-4,4})raw(u,-24,v,"glowstone");
        validate(p,blocks);
    }
    public static void validate(DepthsPlan p,Map<Cell,String> blocks){
        for(int u=-6;u<=179;u++)for(int v=-1;v<=1;v++)for(int h=DepthsPlan.floor(u)+1;h<=DepthsPlan.floor(u)+4;h++)if(!"air".equals(blocks.get(new Cell(u,h,v))))throw new IllegalStateException("blocked axis "+u+","+h+","+v);
        for(int u=142;u<=170;u++)for(int v=-14;v<=14;v++)for(int h=-23;h<=-7;h++)if(!"air".equals(blocks.get(new Cell(u,h,v))))throw new IllegalStateException("blocked arena");
        var b=p.bounds();for(var c:blocks.keySet()){var w=p.world(c.u,c.h,c.v);if(w.x()<b.x0()||w.x()>b.x1()||w.y()<b.y0()||w.y()>b.y1()||w.z()<b.z0()||w.z()>b.z1())throw new IllegalStateException("outside bounds");}
    }
}
