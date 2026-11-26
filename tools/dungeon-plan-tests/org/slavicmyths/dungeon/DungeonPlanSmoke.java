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
            if(p.version!=3||DungeonPlan.create(seed,floors,1).bottom()!=-(floors-1)*32)throw new AssertionError("Version");
            var allowed=java.util.Set.of("hala","indrik_zmey","storm_idol","ant_queen","sand_king");
            var encountered=new java.util.HashSet<String>();
            int expected=0;
            for(var f:p.floors){
                if(f.index()>0)for(String id:f.bosses()){
                    if(!allowed.contains(id)||!encountered.add(id))throw new AssertionError("Regional roster");
                }
                var encounters=p.encounters(f);
                if(!encounters.equals(q.encounters(q.floors.get(f.index()))))throw new AssertionError("Optional seed");
                var keys=new java.util.HashSet<Integer>();
                for(var e:encounters){
                    if(e.key()<100||!keys.add(e.key())||e.room()==f.boss()||e.room()==f.trial()||e.room()==f.entry())throw new AssertionError("Optional room");
                    for(String id:e.bosses())if(!java.util.Set.of("podzemnik","large_podzemnik","kladovik","ovinnik","fire_serpent").contains(id))throw new AssertionError("Optional roster");
                    expected+=e.bosses().size();
                }
                expected+=f.bosses().size();
            }
            if(expected!=p.expectedBosses()||!DungeonPlan.inRange(900)||DungeonPlan.inRange(900.01))throw new AssertionError("Count/radius");
            if(!DungeonPlan.create(seed,floors,2).encounters(p.floors.getFirst()).isEmpty())throw new AssertionError("Legacy encounters");
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
