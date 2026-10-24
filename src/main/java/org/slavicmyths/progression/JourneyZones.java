package org.slavicmyths.progression;

/** Coordinate tests for the persisted Chud plans; no registry or world access. */
public final class JourneyZones {
    public static int[] local(int x,int y,int z,int rotation){
        return switch(rotation){case 1->new int[]{z,y,-x};case 2->new int[]{-x,y,-z};case 3->new int[]{-z,y,x};case 0->new int[]{x,y,z};default->throw new IllegalArgumentException("rotation");};
    }
    public static boolean passage(int x,int y,int z,int ax,int ay,int az,int bx,int by,int bz,int radius){
        if(ay!=by||ax!=bx&&az!=bz)return false;
        return y>ay&&y<=ay+4&&x>=Math.min(ax,bx)-radius&&x<=Math.max(ax,bx)+radius&&z>=Math.min(az,bz)-radius&&z<=Math.max(az,bz)+radius;
    }
    private JourneyZones(){}
}
