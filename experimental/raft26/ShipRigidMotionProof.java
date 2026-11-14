import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.FirstShipHull;

/** Pure Java 21 regression — does not need Minecraft or GPU. */
public final class ShipRigidMotionProof {
    private static void ok(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static void close(double a,double b,double eps,String message){ok(Math.abs(a-b)<=eps,message+": "+a+" vs "+b);}

    public static void main(String[] args){
        ShipRigidMotion motion=new ShipRigidMotion(new ShipRigidMotion.Pose(0,0,0));
        ShipRigidMotion.HullClearance clear=pose->true;
        double last=0;
        for(int tick=0;tick<90;tick++){
            var frame=motion.advance(new ShipRigidMotion.Control(1,0),clear);
            ok(!frame.blocked(),"Open water forward unexpectedly blocked");
            ok(frame.after().z()>=last,"Forward reversed direction");
            ok(frame.after().z()-last<ShipRigidMotion.MAX_FORWARD + 1E-6D,
                    "Discrete one-block jump remained");
            last=frame.after().z();
        }
        ok(motion.pose().z()>10D,"Acceleration did not move vessel continuously");
        ok(motion.speed()<=ShipRigidMotion.MAX_FORWARD,"Speed cap exceeded");
        double yawBefore=motion.pose().yaw();
        for(int tick=0;tick<24;tick++)motion.advance(new ShipRigidMotion.Control(.6D,1D),clear);
        double yawDelta=ShipRigidMotion.wrapDegrees(motion.pose().yaw()-yawBefore);
        ok(yawDelta>5D&&yawDelta<75D,"Rudder snapped 90 degrees");
        var current=motion.pose();
        var halfway=motion.interpolate(.5D);
        close(halfway.x(),(motion.previous().x()+current.x())*.5D,1E-9,"X interpolation");
        close(halfway.z(),(motion.previous().z()+current.z())*.5D,1E-9,"Z interpolation");
        close(halfway.yaw(),ShipRigidMotion.wrapDegrees(motion.previous().yaw()+
                ShipRigidMotion.wrapDegrees(current.yaw()-motion.previous().yaw())*.5D),1E-9,
                "Continuous heading interpolation");

        // Every crew member keeps their own local position under arbitrary
        // (not cardinal) ship translation/rotation.
        double[][] crew={{0,-3},{2,1},{-.6,2.5}};
        for(double[] local:crew){
            FirstShipHull.Point world=FirstShipHull.toWorld(local[0],local[1],current.x(),current.z(),current.yaw());
            FirstShipHull.Point recovered=FirstShipHull.toLocal(world.x(),world.z(),
                    current.x(),current.z(),current.yaw());
            close(recovered.x(),local[0],1E-9,"Crew local X");
            close(recovered.z(),local[1],1E-9,"Crew local Z");
        }

        // No skipped obstruction: even if bow radius hits between whole-number
        // headings, full-hull collision is queried for intermediate poses.
        ShipRigidMotion turning=new ShipRigidMotion(new ShipRigidMotion.Pose(0,0,0));
        int[] queried={0};
        // At high angular speed, even one tick can sweep the bow far enough
        // to demand several intermediate tests, not one endpoint.
        turning.restore(new ShipRigidMotion.Pose(0,0,0),.15D,2.2D);
        ShipRigidMotion.Frame blocked=turning.advance(
                new ShipRigidMotion.Control(1,1),pose->{queried[0]++;return queried[0]<2;});
        ok(queried[0]>=2&&blocked.blocked()&&turning.pose().equals(blocked.before()),
                "Collision moved vessel through a barrier");
        close(turning.speed(),0,0,"Collision did not stop longitudinal motion");
        close(turning.angularVelocity(),0,0,"Collision did not stop turn");

        // Moving ship state is saved in doubles; reloading does not quantize the
        // pose, force quarter-turns, or cause a jump on the first client frame.
        var saved=new ShipRigidMotion.Pose(137.375,-20.625,47.125);
        ShipRigidMotion loaded=new ShipRigidMotion(new ShipRigidMotion.Pose(0,0,0));
        loaded.restore(saved,.13D,-1.3D);
        ok(loaded.pose().equals(saved),"Non-integral persisted ship pose changed");
        ok(loaded.interpolate(.4D).equals(saved),"Restored ship interpolated from zero");

        // Helm idle drag decelerates; it does not halt instantly.
        double initial=loaded.speed();
        loaded.advance(ShipRigidMotion.Control.IDLE,clear);
        ok(loaded.speed()>0&&loaded.speed()<initial,"Inertia/drag violated");

        boolean threw=false;
        try{new ShipRigidMotion.Pose(Double.NaN,0,0);}catch(IllegalArgumentException e){threw=true;}
        ok(threw,"Invalid NBT pose was accepted");
        System.out.println("SHIP_RIGID_MOTION_PASS fractional=true acceleration=true interpolation=true arbitraryYaw=true noNinetySnap=true collisionSweep=true inertia=true crewTransform=true reload=true");
    }
}
