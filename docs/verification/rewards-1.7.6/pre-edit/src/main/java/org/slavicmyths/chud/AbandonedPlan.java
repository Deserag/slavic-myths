package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Saved, finite generation-v2 extension. Base city generation-v1 remains immutable. */
public final class AbandonedPlan {
    public enum Kind { ATTACHED_SMALL, INDEPENDENT_MEDIUM, INDEPENDENT_GREAT }
    public enum Role { QUARTER, GALLERY, SHAFT, LABYRINTH, SEALED_GATE, SECONDARY }
    public enum Module { GUARD_POST, TOOL_ROOM, ORE_SORTING, OLD_INFIRMARY, WORKER_REST, VENT_SHAFT,
        COLLAPSED_STAIRS, MINECART_LOOP, WATER_DRAIN, ABANDONED_STORAGE, BROKEN_BALCONY, CAVE_BASIN }
    public record Chamber(String id,Role role,int x,int y,int z,int width,int depth,int height,Module module) {
        public BlockPos floor(){return new BlockPos(x,y,z);}
    }
    public record Route(String id,List<BlockPos> floors,int width,boolean primary) {}
    public record Edge(int from,int to,boolean extra) {}
    public record RuinedHome(String id,int x,int y,int z,int width,int depth,int family){}
    public final long seed;
    public final BlockPos anchor;
    public final int rotation,entranceFloor,flavor,shaftProfile;
    public final Kind kind;
    public final String rootId;
    public final boolean sealedGate;
    public final List<Chamber> chambers;
    public final List<BlockPos> mazeNodes;
    public final List<Edge> mazeEdges;
    public final List<Route> routes;
    public final List<RuinedHome> ruinedHomes;
    public final Map<String,BlockPos> targets;

    public AbandonedPlan(long seed,BlockPos anchor,int rotation,Kind kind,int entranceFloor,String rootId,int flavor){
        this(seed,anchor,rotation,kind,entranceFloor,rootId,flavor,0);
    }
    public AbandonedPlan(long seed,BlockPos anchor,int rotation,Kind kind,int entranceFloor,String rootId,int flavor,int shaftProfile){
        this.seed=seed;this.anchor=anchor.immutable();this.rotation=rotation;this.kind=kind;
        this.shaftProfile=shaftProfile;
        this.entranceFloor=entranceFloor;this.flavor=flavor;this.rootId=rootId==null?"abandoned-v2-"+anchor.getX()+"-"+anchor.getZ():rootId;
        if(rotation<0||rotation>3||flavor<0||flavor>2)throw new IllegalArgumentException("abandoned orientation/flavor");
        if(shaftProfile<0||shaftProfile>2||kind!=Kind.INDEPENDENT_GREAT&&shaftProfile!=0)throw new IllegalArgumentException("abandoned shaft profile");
        var random=new Random(seed^0x4142414e444f4eL);sealedGate=kind==Kind.INDEPENDENT_GREAT&&random.nextInt(100)<30;
        var rooms=new ArrayList<Chamber>();var paths=new ArrayList<Route>();var goals=new LinkedHashMap<String,BlockPos>();
        var nodes=new ArrayList<BlockPos>();var edges=new ArrayList<Edge>();
        if(kind==Kind.ATTACHED_SMALL){
            rooms.add(new Chamber("old_gallery",Role.GALLERY,-40,-54,-105,flavor==0?54:flavor==1?40:48,flavor==0?36:flavor==1?28:34,18,null));
            rooms.add(new Chamber("ruined_rooms",Role.QUARTER,-40,-54,-69,flavor==0?30:flavor==1?48:42,flavor==0?20:flavor==1?28:24,14,null));
            // The only join is the physical v1 gate: eight flat blocks through its sealed wall.
            route(paths,"city_join",5,new BlockPos(-109,-40,-91),new BlockPos(-109,-40,-82),new BlockPos(-74,-54,-82),new BlockPos(-74,-54,-91),new BlockPos(-66,-54,-91));
            route(paths,"gallery_join",3,new BlockPos(-66,-54,-91),new BlockPos(-40,-54,-91),rooms.getFirst().floor());
            var attachedQuarter=rooms.get(1);int streetX=attachedQuarter.x()+attachedQuarter.width()/2+3,streetZ=attachedQuarter.z()+attachedQuarter.depth()/2+8;
            route(paths,"quarter_join",3,new BlockPos(-40,-54,-91),new BlockPos(streetX,-54,-91),new BlockPos(streetX,-54,streetZ),new BlockPos(attachedQuarter.x(),-54,streetZ));
            int[][] slots={{-15,-105},{-65,-116},{-65,-98},{-15,-88}};
            for(int i=0;i<slots.length;i++){var q=slots[i];var room=new Chamber("side_"+i,Role.SECONDARY,q[0],-54,q[1],9,10,7,Module.values()[(flavor*3+i)%12]);rooms.add(room);route(paths,"side_join_"+i,3,rooms.getFirst().floor(),room.floor());}
            goals.put("entrance",new BlockPos(-109,-39,-96));
        }else{
            boolean great=kind==Kind.INDEPENDENT_GREAT;
            rooms.add(new Chamber("ruined_quarter",Role.QUARTER,great?-77:-57,14,great?-45:-30,great?80:58,great?90:62,great?34:28,null));
            rooms.add(new Chamber("main_gallery",Role.GALLERY,great?-67:-50,0,great?70:62,great?60:44,great?72:52,great?32:26,null));
            if(great)rooms.add(new Chamber("central_shaft",Role.SHAFT,65,shaftFloor(),-55,40,40,shaftHeight(),null));
            int columns=great?6:4,rows=great?7:4,step=great?18:20,mazeY=great?shaftFloor():-28;
            int startX=great?14:12,startZ=great?8:0;
            for(int z=0;z<rows;z++)for(int x=0;x<columns;x++)nodes.add(new BlockPos(startX+x*step,mazeY,startZ+z*step));
            buildMaze(random,columns,rows,edges);
            rooms.add(new Chamber("deep_labyrinth",Role.LABYRINTH,startX+(columns-1)*step/2,mazeY,startZ+(rows-1)*step/2,(columns-1)*step+16,(rows-1)*step+16,11,null));
            if(sealedGate)rooms.add(new Chamber("sealed_lower_gate",Role.SEALED_GATE,5,mazeY,-102,38,32,22,null));
            var gallery=rooms.get(1);var quarter=rooms.getFirst();
            // Entrance is on the slope at the same finite reference window, with landings.
            int entryX=great?124:100;
            goals.put("entrance",new BlockPos(entryX,entranceFloor+1,gallery.z()-(flavor==2?8:0)));
            int march=surfaceMarchLength(),landingY=entranceFloor-(entranceFloor>80?32:entranceFloor>63||flavor==2?16:0);
            route(paths,"entrance",5,new BlockPos(entryX,entranceFloor,gallery.z()),new BlockPos(entryX,landingY,gallery.z()-march),new BlockPos(15,0,gallery.z()-march),new BlockPos(15,0,gallery.z()),gallery.floor());
            if(flavor==2){int buriedZ=gallery.z()-8,floor=paths.getLast().floors().stream().filter(q->Math.abs(q.getX()-entryX)<=2&&Math.abs(q.getZ()-buriedZ)<=2).mapToInt(BlockPos::getY).max().orElseThrow();goals.put("entrance",new BlockPos(entryX,floor+1,buriedZ));}
            int west=gallery.x()-gallery.width()/2-8,quarterApproach=quarter.z()+quarter.depth()/2+8;
            route(paths,"quarter",5,gallery.floor(),new BlockPos(west,0,gallery.z()),new BlockPos(west,14,quarterApproach),new BlockPos(quarter.x(),14,quarterApproach),quarter.floor());
            if(great&&shaftProfile>0){int east=gallery.x()+gallery.width()/2+8,approachZ=gallery.z()+20;route(paths,"maze",5,gallery.floor(),new BlockPos(east,0,gallery.z()),new BlockPos(east,0,approachZ),new BlockPos(startX,mazeY,approachZ),nodes.getFirst());}
            else route(paths,"maze",5,gallery.floor(),new BlockPos(-8,mazeY,gallery.z()),new BlockPos(startX,mazeY,gallery.z()),nodes.getFirst());
            if(great){
                var shaft=rooms.get(2);
                int upperSpine=quarter.z()-quarter.depth()/2-6;
                route(paths,"shaft_upper",5,quarter.floor(),new BlockPos(quarter.x(),14,upperSpine),new BlockPos(10,14,upperSpine),new BlockPos(10,14,-55),new BlockPos(65,14,-55));
                route(paths,"shaft_lower",5,shaft.floor(),new BlockPos(65,mazeY,-5),new BlockPos(startX,mazeY,-5),nodes.getFirst());
                if(sealedGate)route(paths,"sealed_gate",5,shaft.floor(),new BlockPos(5,mazeY,-55),new BlockPos(5,mazeY,-93));
            }
            int[][] slots=great?new int[][]{{-105,15},{-80,15},{-52,15},{-20,-50},{-20,-78},{-15,-17},{30,-18},{60,-18},{93,-18},{111,-60},{111,-95},{73,-110},{45,-110},{-100,116},{-72,116},{-44,116},{-16,112},{111,-2}}
                :new int[][]{{-86,18},{-60,18},{-30,-70},{0,-75},{28,-75},{60,-75},{85,-45},{85,-17},{-85,94}};
            for(int i=0;i<slots.length;i++){
                var slot=slots[i];int y=slot[1]<0?14:0;
                var room=new Chamber("secondary_"+i,Role.SECONDARY,slot[0],y,slot[1],11+(i%3)*2,13+(i%2)*2,9,Module.values()[(i+(int)Math.floorMod(seed,12))%12]);rooms.add(room);
                var source=y==14?quarter:gallery;
                if(y==14){int spine=quarter.z()-quarter.depth()/2-6;route(paths,"secondary_join_"+i,3,source.floor(),new BlockPos(source.x(),y,spine),new BlockPos(room.x(),y,spine),room.floor());}
                else route(paths,"secondary_join_"+i,3,source.floor(),new BlockPos(room.x(),y,source.z()),room.floor());
            }
        }
        var homes=new ArrayList<RuinedHome>();var quarter=rooms.stream().filter(h->h.role()==Role.QUARTER).findFirst().orElseThrow();
        int cols=kind==Kind.ATTACHED_SMALL?2:kind==Kind.INDEPENDENT_GREAT?4:3,left=quarter.x()-quarter.width()/2+6,back=quarter.z()-quarter.depth()/2+6;
        for(int i=0;i<cols;i++)if(kind==Kind.ATTACHED_SMALL)homes.add(new RuinedHome("ruined_home_"+i,left+i*13,quarter.y(),back,9,11,(int)Math.floorMod(seed+i,4)));
            else{homes.add(new RuinedHome("ruined_home_upper_"+i,left+i*16,quarter.y()+12,back,10,12,i%4));homes.add(new RuinedHome("ruined_home_lower_"+i,left+i*16,quarter.y(),quarter.z()+12,10,12,(i+2)%4));}
        ruinedHomes=List.copyOf(homes);
        for(var home:homes){goals.put(home.id(),new BlockPos(home.x()+3,home.y()+1,home.z()+home.depth()-2));if(home.family()==2)goals.put(home.id()+"_upper",new BlockPos(home.x()+3,home.y()+6,home.z()+4));}
        for(var room:rooms)if(room.role()!=Role.LABYRINTH)goals.put(room.id(),kind==Kind.ATTACHED_SMALL&&room.role()==Role.QUARTER?new BlockPos(room.x(),room.y()+1,room.z()+room.depth()/2+8):room.floor().above());
        if(kind!=Kind.ATTACHED_SMALL){goals.put("quarter_upper",new BlockPos(quarter.x()-quarter.width()/2+9,quarter.y()+13,quarter.z()-quarter.depth()/2+20));var gallery=rooms.get(1);goals.put("gallery_upper",new BlockPos(gallery.x(),gallery.y()+11,gallery.z()-12));goals.put("gallery_upper_south",new BlockPos(gallery.x(),gallery.y()+11,gallery.z()+12));}
        if(kind==Kind.INDEPENDENT_GREAT)for(int level=1;level<shaftRings();level++)goals.put("shaft_ring_"+level,new BlockPos(65,shaftFloor()+1+level*shaftGap(),-55));
        for(int i=0;i<nodes.size();i++)goals.put("maze_"+i,nodes.get(i).above());
        chambers=List.copyOf(rooms);mazeNodes=List.copyOf(nodes);mazeEdges=List.copyOf(edges);routes=List.copyOf(paths);targets=Collections.unmodifiableMap(goals);
    }
    private static void buildMaze(Random random,int cols,int rows,List<Edge> edges){
        int size=cols*rows;var visited=new BitSet(size);var frontier=new ArrayList<Edge>();int start=random.nextInt(size);visited.set(start);addFrontier(start,cols,rows,visited,frontier);
        // Randomized Prim grows a branching spanning tree, rather than one near-linear DFS snake.
        while(!frontier.isEmpty()){
            var edge=frontier.remove(random.nextInt(frontier.size()));if(visited.get(edge.to()))continue;
            edges.add(edge);visited.set(edge.to());addFrontier(edge.to(),cols,rows,visited,frontier);
        }
        var possible=new ArrayList<Edge>();
        for(int n=0;n<cols*rows;n++)for(int other:new int[]{n%cols+1<cols?n+1:-1,n+cols<cols*rows?n+cols:-1}){boolean exists=false;for(var e:edges)if(Math.min(e.from(),e.to())==n&&Math.max(e.from(),e.to())==other)exists=true;if(other>=0&&!exists)possible.add(new Edge(n,other,true));}
        Collections.shuffle(possible,random);int count=Math.min(possible.size(),Math.max(1,(size-1)/5));int[] degree=new int[size];for(var edge:edges){degree[edge.from()]++;degree[edge.to()]++;}
        int leaves=0;for(int d:degree)if(d==1)leaves++;int minimum=(size+4)/5,maximum=size*35/100;
        for(int k=0;k<count;k++){
            Edge chosen=null;int best=leaves>maximum?-1:3;
            for(var edge:possible){int lost=(degree[edge.from()]==1?1:0)+(degree[edge.to()]==1?1:0);if(leaves-lost<minimum)continue;if(chosen==null||leaves>maximum&&lost>best||leaves<=maximum&&lost<best){chosen=edge;best=lost;}}
            if(chosen==null)break;possible.remove(chosen);edges.add(chosen);degree[chosen.from()]++;degree[chosen.to()]++;leaves-=best;
        }
    }
    private static void addFrontier(int n,int cols,int rows,BitSet visited,List<Edge> frontier){int x=n%cols,z=n/cols;for(int next:new int[]{x>0?n-1:-1,x+1<cols?n+1:-1,z>0?n-cols:-1,z+1<rows?n+cols:-1})if(next>=0&&!visited.get(next))frontier.add(new Edge(n,next,false));}
    private static void route(List<Route> routes,String id,int width,BlockPos...corners){
        var positions=new ArrayList<BlockPos>();
        for(int s=0;s<corners.length-1;s++){
            var a=corners[s];var b=corners[s+1];int dx=b.getX()-a.getX(),dz=b.getZ()-a.getZ(),run=Math.abs(dx)+Math.abs(dz),dy=b.getY()-a.getY();
            // At most six risers, then a landing; the discrete capacity prevents two-block jumps.
            int steps=Math.abs(dy),capacity=run-run/8*2-Math.max(0,run%8-6);
            if(steps>capacity)throw new IllegalArgumentException("landing capacity "+id);
            for(int k=0;k<run;k++){int progress=k-k/8*2-Math.max(0,k%8-6);int y=a.getY()+Integer.signum(dy)*(int)((long)steps*Math.max(0,progress)/Math.max(1,capacity));positions.add(new BlockPos(a.getX()+Integer.signum(dx)*Math.min(k,Math.abs(dx)),y,a.getZ()+Integer.signum(dz)*Math.max(0,k-Math.abs(dx))));}
        }
        positions.add(corners[corners.length-1]);routes.add(new Route(id,List.copyOf(positions),width,true));
    }
    public Rotation blockRotation(){return Rotation.values()[rotation];}
    public static int shaftStairX(Chamber shaft,int side,int level){return shaft.x()+side*(level%2==0?21:27);}
    public int shaftRings(){return shaftProfile==2?3:4;}
    public int shaftGap(){return shaftProfile==0?18:14;}
    public int shaftFloor(){return 14-(shaftRings()-1)*shaftGap();}
    public int shaftHeight(){return shaftProfile==0?76:shaftProfile==1?60:44;}
    public BlockPos surfacePortal(){return new BlockPos(kind==Kind.INDEPENDENT_GREAT?124:100,entranceFloor,chambers.get(1).z());}
    public int surfaceMarchLength(){return entranceFloor>80?48:32;}
    public BlockPos world(BlockPos p){return switch(rotation){case 1->anchor.offset(-p.getZ(),p.getY(),p.getX());case 2->anchor.offset(-p.getX(),p.getY(),-p.getZ());case 3->anchor.offset(p.getZ(),p.getY(),-p.getX());default->anchor.offset(p);};}
    public String id(){return rootId+":abandoned";}
    public int levels(){return kind==Kind.ATTACHED_SMALL?2:kind==Kind.INDEPENDENT_MEDIUM?3:shaftRings();}
    public BoundingBox nativeBounds(){int r=kind==Kind.INDEPENDENT_MEDIUM?104:127;int top=kind==Kind.ATTACHED_SMALL?73:Math.max(73,entranceFloor+8);return new BoundingBox(anchor.getX()-r,anchor.getY()+(kind==Kind.ATTACHED_SMALL?-58:-44),anchor.getZ()-r,anchor.getX()+r,anchor.getY()+top,anchor.getZ()+r);}
    public BoundingBox envelope(){var b=nativeBounds();return new BoundingBox(b.minX()-12,b.minY(),b.minZ()-12,b.maxX()+12,b.maxY(),b.maxZ()+12);}
    public String futureGateId(){return sealedGate?id()+":sealed_lower_gate":"";}
    public BlockPos futureGateFloor(){return world(new BlockPos(5,shaftFloor(),-105));}
    public Direction futureGateFacing(){return blockRotation().rotate(Direction.SOUTH);}
    /** Finite terrain probes also cover external stairs, which lie outside the main hall. */
    public List<BlockPos> circulationProbes(){
        var result=new ArrayList<BlockPos>();
        for(var home:ruinedHomes)if(home.family()==2)for(int z=home.z()+home.depth()-3;z<=home.z()+home.depth()+9;z+=3)result.add(new BlockPos(home.x()+home.width(),home.y()+5,z));
        for(var h:chambers)if(h.role()==Role.SHAFT)for(int side:new int[]{-1,1})for(int level=0;level<shaftRings()-1;level++)for(int z=h.z()-shaftGap()-2;z<=h.z()+shaftGap()+2;z+=4)result.add(new BlockPos(shaftStairX(h,side,level),h.y()+(level+1)*shaftGap(),z));
        return List.copyOf(result);
    }
    public CompoundTag save(){var n=new CompoundTag();n.putInt("GenerationVersion",2);n.putLong("Seed",seed);n.putLong("Anchor",anchor.asLong());n.putInt("Rotation",rotation);n.putString("Kind",kind.name());n.putInt("EntranceFloor",entranceFloor);n.putString("RootId",rootId);n.putInt("Flavor",flavor);n.putInt("ShaftProfile",shaftProfile);n.putString("FutureGateId",futureGateId());if(sealedGate){n.putLong("FutureGateFloor",futureGateFloor().asLong());n.putString("FutureGateFacing",futureGateFacing().getName());}return n;}
    public static AbandonedPlan load(CompoundTag n){if(n.getInt("GenerationVersion")!=2)throw new IllegalArgumentException("unsupported abandoned generation");return new AbandonedPlan(n.getLong("Seed"),BlockPos.of(n.getLong("Anchor")),n.getInt("Rotation"),Kind.valueOf(n.getString("Kind")),n.getInt("EntranceFloor"),n.getString("RootId"),n.getInt("Flavor"),n.getInt("ShaftProfile"));}
}
