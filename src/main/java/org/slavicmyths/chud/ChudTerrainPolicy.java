package org.slavicmyths.chud;

/** Lowest actual construction, including floor shells and shaft deck supports; saved boxes stay unchanged. */
public final class ChudTerrainPolicy {
    public static int roofAnchor(int ground,int roof,boolean water) { return ground-ChudRoofCover.DEPTH-roof; }
    public static boolean fits(int anchor,int bottom,int top,int minY,int maxY) { return anchor+bottom>=minY+1&&anchor+top<maxY; }
    public static int bottom(boolean attached,boolean great,int profile) {
        if(attached)return -58;
        if(!great)return -31;
        return switch(profile){case 0->-44;case 1->-36;case 2->-22;default->throw new IllegalArgumentException("shaft profile");};
    }
    private ChudTerrainPolicy() {}
}
