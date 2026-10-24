import java.nio.file.*;
import java.util.*;
import org.slavicmyths.progression.JourneyZones;
import org.slavicmyths.client.TaleTreeLayout;
import org.slavicmyths.brewing.BrewRules;

/** Pure JVM: no Minecraft classes, initialization, worlds, clients, servers or GameTests. */
public final class JourneyChecks {
    private static int checks;
    private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        var parents=new LinkedHashMap<String,String>();
        for(String line:Files.readAllLines(Path.of(args[0]))){var fields=line.split(Character.toString((char)9),-1);parents.put(fields[0],fields[1]);}
        check(parents.size()==75,"75 nodes");
        var positions=TaleTreeLayout.create(parents);check(positions.size()==75,"complete book layout");
        var occupied=new HashSet<String>();
        for(var entry:positions.entrySet()){
            int[] pos=entry.getValue();check(occupied.add(pos[0]+":"+pos[1]),"overlap "+entry.getKey());
            var parent=positions.get(parents.get(entry.getKey()));if(parent!=null)check(pos[0]==parent[0]+64,"edge depth");
        }
        var again=TaleTreeLayout.create(parents);for(String id:parents.keySet())check(Arrays.equals(positions.get(id),again.get(id)),"deterministic layout");
        // Every earned leaf remains present even if all of its ancestors are incomplete:
        // graph layout consumes the complete book snapshot, never completion flags.
        for(String earned:parents.keySet())check(positions.containsKey(earned),"independent child layout");
        var random=new Random(73119);
        for(int i=0;i<10000;i++){
            int x=random.nextInt(600)-300,y=random.nextInt(200)-100,z=random.nextInt(600)-300;
            for(int rotation=0;rotation<4;rotation++){
                int wx=switch(rotation){case 1->-z;case 2->-x;case 3->z;default->x;};
                int wz=switch(rotation){case 1->x;case 2->-z;case 3->-x;default->z;};
                check(Arrays.equals(JourneyZones.local(wx,y,wz,rotation),new int[]{x,y,z}),"plan rotation inverse");
            }
        }
        for(int radius:new int[]{1,2})for(boolean reverse:new boolean[]{false,true}){
            int ax=reverse?30:10,bx=reverse?10:30;
            check(JourneyZones.passage(20,-39,6,ax,-40,6,bx,-40,6,radius),"actual passage");
            check(!JourneyZones.passage(20,-40,6,ax,-40,6,bx,-40,6,radius),"floor is not visit");
            check(!JourneyZones.passage(20,-35,6,ax,-40,6,bx,-40,6,radius),"above passage");
            check(!JourneyZones.passage(20,-39,7+radius,ax,-40,6,bx,-40,6,radius),"nearby blocks are not passage");
            check(!JourneyZones.passage(9-radius,-39,6,ax,-40,6,bx,-40,6,radius),"before passage");
        }
        check(!JourneyZones.passage(5,1,5,0,0,0,10,0,10,2),"diagonal envelope rejected");
        check(BrewRules.quality(2399).equals("young"),"not yet brewed");
        check(BrewRules.quality(2400).equals("ready"),"native fermentation completion");
        check(BrewRules.quality(8400).equals("aged"),"aged boundary");
        check(BrewRules.quality(12000).equals("spoiled"),"spoiled boundary");
        System.out.println("PASS "+checks+" pure JVM checks; Minecraft launches: 0");
    }
}
