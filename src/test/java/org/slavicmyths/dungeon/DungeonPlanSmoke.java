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
            int flat=p.anchorY(-60,-64,320);
            if(flat+p.bottom()<=-60)throw new AssertionError("Flat placement");
            int deep=p.anchorY(100,-64,320);
            if(deep+p.top()>=100)throw new AssertionError("Underground placement");
        }
        if(doubleCount==0)throw new AssertionError("No combined arena");
        System.out.println("3000 layouts checked; combined arenas: "+doubleCount);
    }
}
