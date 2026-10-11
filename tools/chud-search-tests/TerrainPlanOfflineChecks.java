import net.minecraft.core.BlockPos;
import org.slavicmyths.chud.*;

/** Actual immutable layout math on an artificial dry solid terrain; no Minecraft bootstrap/world. */
public final class TerrainPlanOfflineChecks {
    public static int run() {
        int checked=0;
        for(long seed=0;seed<12;seed++)for(int rotation=0;rotation<4;rotation++)
            for(var kind:new AbandonedPlan.Kind[]{AbandonedPlan.Kind.INDEPENDENT_MEDIUM,AbandonedPlan.Kind.INDEPENDENT_GREAT})
                for(int profile=0;profile<(kind==AbandonedPlan.Kind.INDEPENDENT_GREAT?3:1);profile++) {
                    boolean accepted=false;
                    for(int descent:new int[]{32,40,48,56,64,72,80,96,112}) {
                        var p=new AbandonedPlan(seed,new BlockPos(8,64-descent,8),rotation,kind,descent,null,0,profile);
                        int bottom=ChudTerrainPolicy.bottom(false,kind==AbandonedPlan.Kind.INDEPENDENT_GREAT,profile);
                        for(var room:p.chambers)if(room.y()-3<bottom)throw new AssertionError("floor shell below policy: "+room);
                        for(var route:p.routes)for(var floor:route.floors())if(floor.getY()-3<bottom)throw new AssertionError("route shell below policy");
                        if(p.anchor.getY()+bottom < -48)continue;
                        boolean cover=true;
                        for(var room:p.chambers)if(room.role()!=AbandonedPlan.Role.LABYRINTH)
                            for(int dx=-room.width()/2;dx<=room.width()/2;dx+=8)for(int dz=-room.depth()/2;dz<=room.depth()/2;dz+=8)
                                if(AbandonedGeometry.outline(p,room,dx,dz)<=1.16&&p.anchor.getY()+AbandonedGeometry.ceiling(room,dx,dz)+12>64)cover=false;
                        for(var route:p.routes)for(int i=0;i<route.floors().size();i+=8) {
                            var f=route.floors().get(i);
                            boolean entrance=f.getX()==p.surfacePortal().getX()&&f.getZ()>=p.surfacePortal().getZ()-p.surfaceMarchLength()&&f.getZ()<=p.surfacePortal().getZ();
                            if(!entrance&&p.anchor.getY()+f.getY()+15>64)cover=false;
                        }
                        if(cover){accepted=true;break;}
                    }
                    if(!accepted)throw new AssertionError("No dry flat terrain placement: "+kind+" / "+profile+" / "+seed);
                    checked++;
                }
        System.out.println("ARTIFICIAL TERRAIN LAYOUT CHECKS PASS: "+checked+"; no real noise terrain or game world tested");
        return checked;
    }
}
