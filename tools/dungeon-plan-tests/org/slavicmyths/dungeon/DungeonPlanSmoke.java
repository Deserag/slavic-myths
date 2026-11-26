package org.slavicmyths.dungeon;

/** Standalone Java 21 smoke test; no Minecraft dependencies. */
public final class DungeonPlanSmoke {
    public static void main(String[] args){
        int doubleCount=0;
        for(int floors=3;floors<=5;floors++)for(long seed=0;seed<1000;seed++){
            DungeonPlan p=DungeonPlan.create(seed,floors);p.validate();
            DungeonPlan q=DungeonPlan.create(seed,floors);
            if(!p.boxes.equals(q.boxes)||!p.floors.equals(q.floors))throw new AssertionError("Non deterministic "+seed);
            if(p.doubled)doubleCount++;
            if(p.version!=2||DungeonPlan.create(seed,floors,1).bottom()!=-(floors-1)*32)throw new AssertionError("Version");
            int stairs=0;
            for(var box:p.boxes)if(box.kind()==DungeonPlan.Kind.STAIR_DESCENT){
                stairs++;
                var upper=p.floors.get(box.floor()-1).rooms().get(16);
                var lower=p.floors.get(box.floor()).rooms().get(17);
                int gate=0;
                for(int x=box.x0();x<=box.x1();x++)for(int z=box.z0();z<=box.z1();z++){
                    int u=DungeonPlan.stairU(box,x,z),v=DungeonPlan.stairV(box,x,z);
                    if(Math.abs(v)>=2)continue;
                    if(u==0&&!lower.contains(x,box.y0()+1,z))throw new AssertionError("Lower stair landing");
                    if(u==DungeonPlan.PITCH+2&&!upper.contains(x,box.y0()+DungeonPlan.PITCH+1,z))throw new AssertionError("Upper stair landing");
                    if(DungeonPlan.stairGate(box,x,box.y0()+DungeonPlan.PITCH+1,z))gate++;
                }
                if(gate!=3)throw new AssertionError("Gate width");
            }
            if(stairs!=floors-1)throw new AssertionError("Missing stairs");
            for(var box:p.boxes)if(box.kind()==DungeonPlan.Kind.POOL){
                int water=0;
                for(int x=box.x0();x<=box.x1();x++)for(int z=box.z0();z<=box.z1();z++)if(DungeonPlan.poolWater(box,x,z)){
                    water++;if(x==(box.x0()+box.x1())/2||z==(box.z0()+box.z1())/2)throw new AssertionError("Flooded main path");
                }
                if(water==0)throw new AssertionError("Empty pools");
            }
            int flat=p.anchorY(-60,-64,320);
            if(flat+p.bottom()<=-60)throw new AssertionError("Flat placement");
            int deep=p.anchorY(110,-64,320);
            if(deep+p.top()>=110)throw new AssertionError("Underground placement");
        }
        if(doubleCount==0)throw new AssertionError("No combined arena");
        System.out.println("3000 layouts checked; combined arenas: "+doubleCount);
    }
}
