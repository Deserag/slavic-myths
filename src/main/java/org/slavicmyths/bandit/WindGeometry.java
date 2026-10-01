package org.slavicmyths.bandit;

/** Pure geometry shared by combat and headless tests. No world or client state. */
public final class WindGeometry {
    public interface Obstacle { boolean solid(int x,int y,int z); }
    public static boolean contains(double x,double y,double z,double dx,double dz,double range,boolean radial){
        double length=x*x+y*y+z*z;
        return length<=range*range&&(radial||length<1e-8||(x*dx+z*dz)/Math.sqrt(length)>=.82);
    }
    /** Exact voxel traversal: thin diagonal intersections cannot slip between samples. */
    public static boolean clear(double ax,double ay,double az,double bx,double by,double bz,Obstacle obstacle){
        int x=(int)Math.floor(ax),y=(int)Math.floor(ay),z=(int)Math.floor(az);
        int endX=(int)Math.floor(bx),endY=(int)Math.floor(by),endZ=(int)Math.floor(bz);
        double dx=bx-ax,dy=by-ay,dz=bz-az;
        int sx=dx>0?1:dx<0?-1:0,sy=dy>0?1:dy<0?-1:0,sz=dz>0?1:dz<0?-1:0;
        double tx=first(ax,x,dx),ty=first(ay,y,dy),tz=first(az,z,dz);
        double stepX=dx==0?Double.POSITIVE_INFINITY:Math.abs(1/dx),stepY=dy==0?Double.POSITIVE_INFINITY:Math.abs(1/dy),stepZ=dz==0?Double.POSITIVE_INFINITY:Math.abs(1/dz);
        for(int count=0;count<128;count++){
            if(obstacle.solid(x,y,z))return false;
            if(x==endX&&y==endY&&z==endZ)return true;
            if(tx<=ty&&tx<=tz){x+=sx;tx+=stepX;}else if(ty<=tz){y+=sy;ty+=stepY;}else{z+=sz;tz+=stepZ;}
        }
        return false;
    }
    private static double first(double start,int cell,double delta){return delta==0?Double.POSITIVE_INFINITY:((delta>0?cell+1:cell)-start)/delta;}
    private WindGeometry(){}
}
