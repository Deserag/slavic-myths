import org.slavicmyths.bandit.WindGeometry;
public final class WindGeometryTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        check(WindGeometry.contains(0,0,16,0,1,16,false),"range endpoint");
        check(!WindGeometry.contains(0,0,16.01,0,1,16,false),"outside range");
        check(!WindGeometry.contains(0,0,-5,0,1,16,false),"behind cone");
        check(WindGeometry.contains(0,0,-5,0,1,5.5,true),"radial rear");
        check(!WindGeometry.contains(0,6,0,0,1,5.5,true),"radial vertical range");
        check(WindGeometry.contains(4,0,8,0,1,16,false),"inside cone edge");
        check(!WindGeometry.contains(8,0,8,0,1,16,false),"outside cone edge");
        WindGeometry.Obstacle wall=(x,y,z)->x==4&&y>=0&&y<=3;
        check(!WindGeometry.clear(.5,1.8,.5,9.5,1,.5,wall),"full cover");
        check(WindGeometry.clear(.5,5,.5,9.5,5,.5,wall),"above cover");
        check(!WindGeometry.clear(9.5,1,.5,.5,1.8,.5,wall),"reverse ray cover");
        check(!WindGeometry.clear(-.01,1,.99,2.01,1,1.01,(x,y,z)->x==1&&z==1),"diagonal corner cover");
        check(WindGeometry.clear(-20,2,-20,-20,2,-20,(x,y,z)->false),"zero ray");
        for(int yaw=0;yaw<360;yaw++){
            double x=Math.sin(Math.toRadians(yaw))*19,z=Math.cos(Math.toRadians(yaw))*19;
            check(WindGeometry.clear(.5,1.5,.5,x+.5,1.5,z+.5,(a,b,c)->false),"open ray "+yaw);
            check(!WindGeometry.clear(.5,1.5,.5,x+.5,1.5,z+.5,(a,b,c)->Math.max(Math.abs(a),Math.abs(c))==6),"enclosing cover "+yaw);
        }
        System.out.println("PASS: cone/radial boundaries; voxel cover including reverse, corner and 360 directions. No Minecraft launch.");
    }
}
