package org.slavicmyths.navigation;

/** Display math consumes only public marker geometry, never the authoritative target. */
public final class NavigationMath {
    public static boolean sameDimension(SlavicMarker marker,String dimension){return marker.dimension().toString().equals(dimension);}
    public static int arrow(double playerX,double playerZ,float yaw,double targetX,double targetZ){
        double bearing=-Math.toDegrees(Math.atan2(targetX-playerX,targetZ-playerZ));
        return Math.floorMod((int)Math.round((bearing-yaw)/45),8);
    }
    public static double distance(SlavicMarker marker,double x,double z){
        return marker.area()==null?Math.hypot(marker.x()+.5-x,marker.z()+.5-z):marker.area().boundaryDistance(x,z);
    }
    private NavigationMath(){}
}
