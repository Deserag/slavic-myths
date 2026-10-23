package org.slavicmyths.client;

/** Stateless visual kinematics in model units; never advances gameplay or stores entities. */
public final class FlailMotion {
    public static final int LINKS = 7;
    public static final double SPACING = 2.4;
    public record Point(double x, double y, double z) {
        public Point add(Point p, double scale) { return new Point(x+p.x*scale,y+p.y*scale,z+p.z*scale); }
    }
    public record Pose(Point[] links, Point[] directions, Point ball) {}
    public static Pose sample(float age, float walk, float charge, float swing, boolean attacking, boolean heavy) {
        charge = Math.clamp(charge,0,1); swing = Math.clamp(swing,0,1);
        double contact = attacking ? Math.pow(Math.sin(swing*Math.PI),2) : 0;
        double reach=SPACING; // Fixed link length: swing rotates the chain instead of separating its links.
        double inertia = Math.sin(age*.18)*Math.clamp(walk,0,1)*(heavy?5:8);
        Point anchor = new Point(8,17,8);
        Point[] links = new Point[LINKS], directions = new Point[LINKS];
        for (int i=0;i<LINKS;i++) {
            double lag=(i+1.0)/LINKS;
            double impact=(heavy?.8:1.0)*contact*(52+33*lag);
            double pitch=Math.toRadians(Math.clamp(8+charge*(30-5*lag)+impact+inertia*lag,-12,88));
            double side=Math.toRadians(13+Math.sin(age*.075-lag)*2+inertia*lag*.5);
            Point direction=new Point(Math.sin(side),-Math.cos(pitch)*Math.cos(side),-Math.sin(pitch)*Math.cos(side));
            links[i]=anchor.add(direction,reach*.5);
            directions[i]=direction;
            anchor=anchor.add(direction,reach);
        }
        // Last half-link plus eyelet-to-core offset: the core remains beyond the chain, never on top.
        return new Pose(links,directions,anchor.add(directions[LINKS-1],2.0+contact*1.0));
    }
    private FlailMotion() {}
}
