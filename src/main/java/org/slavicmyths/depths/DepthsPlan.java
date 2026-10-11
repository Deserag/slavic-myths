package org.slavicmyths.depths;

import java.util.*;

/** Fixed E0–E5 layout in (u,h,v). Pure geometry; no registries or game bootstrap. */
public record DepthsPlan(UUID id,Point start,Point origin,int direction,long seed,boolean skylight,int skyTop) {
    public record Point(int x,int y,int z) {
        public Point add(int x,int y,int z){return new Point(this.x+x,this.y+y,this.z+z);}
    }
    public record Box(int x0,int y0,int z0,int x1,int y1,int z1) {
        public boolean intersects(Box b,int margin){return x0-margin<=b.x1&&x1+margin>=b.x0&&y0-margin<=b.y1&&y1+margin>=b.y0&&z0-margin<=b.z1&&z1+margin>=b.z0;}
    }
    public record Spawn(int u,int v,String type,int guardian){}
    public static final List<Spawn> SPAWNS=List.of(new Spawn(30,2,"zombie",0),new Spawn(52,-8,"skeleton",0),new Spawn(55,8,"zombie",0),
        new Spawn(71,-4,"zombie",0),new Spawn(71,4,"zombie",0),new Spawn(86,-4,"skeleton",0),new Spawn(86,4,"skeleton",0),
        new Spawn(95,-7,"zombie",0),new Spawn(111,7,"skeleton",0),new Spawn(175,-16,"zombie",0),new Spawn(101,-4,"zombie",1),new Spawn(105,5,"skeleton",2));
    public static final List<Point> CHESTS=List.of(new Point(94,-15,10),new Point(171,-23,18));
    public DepthsPlan {if(direction<0||direction>3)throw new IllegalArgumentException("direction");}
    /** In flat test worlds the whole bounding box stays above the surface, including its base. */
    public static int placementOriginY(int surface,boolean flat){return flat?surface+32:surface;}
    public int fx(){return switch(direction){case 1->1;case 3->-1;default->0;};}
    public int fz(){return switch(direction){case 0->-1;case 2->1;default->0;};}
    public Point world(int u,int h,int v){return origin.add(u*fx()-v*fz(),h,u*fz()+v*fx());}
    public Box bounds(){var a=world(-8,-31,-25);var b=world(181,Math.max(6,skylight?skyTop+2:6),25);return new Box(Math.min(a.x,b.x),Math.min(a.y,b.y),Math.min(a.z,b.z),Math.max(a.x,b.x),Math.max(a.y,b.y),Math.max(a.z,b.z));}
    public Point bossAnchor(){return world(156,-23,0);}
    public Box bossArea(){var a=world(142,-24,-14);var b=world(170,-7,14);return new Box(Math.min(a.x,b.x),a.y,Math.min(a.z,b.z),Math.max(a.x,b.x),b.y,Math.max(a.z,b.z));}
    public double radius(){return Math.hypot(origin.x-start.x,origin.z-start.z);}
    public static int floor(int u){
        if(u<=6)return 0;if(u<=18)return -(u-5)/2;if(u<=21)return -6;if(u<=33)return -6-(u-20)/2;
        if(u<=35)return -12;if(u<=43)return -12-(u-34)/2;if(u<=116)return -16;if(u<=132)return -16-(u-115)/2;return -24;
    }
    public static Point candidate(Point start,long seed,int i){
        var random=new Random(seed);double base=random.nextDouble()*Math.PI*2,angle=base+i*2.399963229728653;
        int radius=1501+new Random(seed^i*73428767L).nextInt(499);
        return start.add((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius));
    }
    public static int outward(Point start,Point target){int x=target.x-start.x,z=target.z-start.z;return Math.abs(x)>Math.abs(z)?x>0?1:3:z>0?2:0;}
    public String facing(boolean backwards){return new String[]{"north","east","south","west"}[(direction+(backwards?2:0))&3];}
}
