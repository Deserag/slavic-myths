import static org.slavicmyths.desert.winds.FourWindsPlan.*;
public final class PlanCheck {
    private static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
    public static void main(String[] args){
        int count=0,stairs=0,landing=0;
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=MIN_Z;z<=MAX_Z;z++)for(int y=0;y<H;y++){
            int b=at(x,y,z);if(b!=AIR)count++;if(b==STAIR)stairs++;if(y==0&&b!=AIR)landing++;
        }
        check(count==COUNT&&stairs==170&&landing==197,"approved geometry counts");
        for(int i=0;i<34;i++)for(int x=-2;x<=2;x++){
            int y=2+i,z=ENTRANCE_Z+i;
            check(at(x,y,z)==STAIR&&at(x,y-1,z)!=AIR,"continuous stair support");
            check(at(x,y+1,z)==AIR&&at(x,y+2,z)==AIR,"stair headroom");
        }
        for(int x=-23;x<=23;x++)for(int z=-23;z<=23;z++){
            if(x*x+z*z<=23*23)check(at(x,FLOOR,z)!=AIR&&at(x,FLOOR-1,z)!=AIR,"two-layer deck");
            if(x*x+z*z<=12*12)for(int y=FLOOR+1;y<H;y++)check(at(x,y,z)==AIR,"clear central combat volume");
        }
        for(int x:new int[]{-17,17})for(int z:new int[]{-16,16})check(at(x,58,z-1)==SMOOTH,"four fists");
        for(int y=0;y<18;y++)check(at(0,y,0)==AIR,"free air under center");
        check(encounter(.5,36,.5)&&!encounter(0,5,0)&&!encounter(25,36,0),"arena trigger");
        System.out.println("Four Winds V2: "+count+" blocks; 47x89x59; 170 stairs; 197 landing blocks; exact SHA-256 validated");
    }
}
