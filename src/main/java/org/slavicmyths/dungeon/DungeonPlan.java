package org.slavicmyths.dungeon;

import java.util.*;

/** Seeded graph and geometry, independent of Minecraft and of the terrain. */
public final class DungeonPlan {
    public static final int VERSION=3, PITCH=20, LEGACY_PITCH=32, STEP=26, ARENA_OFFSET=89;
    public enum Kind { ENTRY, HALL, PILLARS, SHRINE, FLOODED, SECRET, TRIAL, BOSS, DOUBLE, CORRIDOR, SHAFT, WELL, STAIR_DESCENT, POOL }
    public record Point(int x,int z) {}
    public record Box(int x0,int y0,int z0,int x1,int y1,int z1,Kind kind,int floor) {
        public boolean contains(int x,int y,int z){return x>=x0&&x<=x1&&y>=y0&&y<=y1&&z>=z0&&z<=z1;}
        public boolean overlaps(Box b){return x0<=b.x1&&x1>=b.x0&&y0<=b.y1&&y1>=b.y0&&z0<=b.z1&&z1>=b.z0;}
    }
    public record Edge(int a,int b) {
        public Edge {if(a>b){int v=a;a=b;b=v;}}
    }
    public record Encounter(int floor,int room,boolean roaming,List<String> bosses){
        public int key(){return 100+floor*32+room;}
    }
    public record Floor(int index,List<Box> rooms,List<Edge> edges,int entry,int trial,int boss,Point exit,List<String> bosses) {}
    public final long seed;
    public final int version;
    public final List<Floor> floors;
    public final List<Box> boxes;
    public final boolean doubled;
    private DungeonPlan(long seed,List<Floor> floors,List<Box> boxes,boolean doubled,int version){
        this.seed=seed;this.version=version;this.floors=List.copyOf(floors);this.boxes=List.copyOf(boxes);this.doubled=doubled;
    }
    private static Point cell(int i){return new Point((i%4)*STEP-39,(i/4)*STEP-39);}
    private static Point rotate(Point p,int k){return switch(k&3){case 1->new Point(-p.z,p.x);case 2->new Point(-p.x,-p.z);case 3->new Point(p.z,-p.x);default->p;};}
    private static Box room(Point p,int y,int w,int d,int h,Kind kind,int floor){
        return new Box(p.x-w/2,y,p.z-d/2,p.x+w/2,y+h,p.z+d/2,kind,floor);
    }
    private static List<Integer> neighbors(int i){
        List<Integer> n=new ArrayList<>();
        if(i%4>0)n.add(i-1);if(i%4<3)n.add(i+1);if(i/4>0)n.add(i-4);if(i/4<3)n.add(i+4);
        return n;
    }
    private static Point center(Box b){return new Point((b.x0+b.x1)/2,(b.z0+b.z1)/2);}
    private static void segment(List<Box> out,Point a,Point b,int y,int floor){
        if(a.x!=b.x&&a.z!=b.z)throw new IllegalArgumentException("Diagonal connector");
        out.add(new Box(Math.min(a.x,b.x)-2,y,Math.min(a.z,b.z)-2,Math.max(a.x,b.x)+2,y+6,Math.max(a.z,b.z)+2,Kind.CORRIDOR,floor));
    }
    private static void connect(List<Box> out,Box a,Box b,int y,int floor){
        Point p=center(a),q=center(b);
        Point elbow=new Point(p.x,q.z);
        segment(out,p,elbow,y,floor);if(!elbow.equals(q))segment(out,elbow,q,y,floor);
    }
    public static DungeonPlan create(long seed,int count){return create(seed,count,VERSION);}
    public static DungeonPlan create(long seed,int count,int version){
        if(version<1||version>VERSION)throw new IllegalArgumentException("Unknown dungeon plan version "+version);
        int pitch=version==1?LEGACY_PITCH:PITCH;
        if(count<3||count>5)throw new IllegalArgumentException("Expected 3–5 floors");
        Random r=new Random(seed);boolean doubled=r.nextInt(5)==0;
        boolean queenFirst=r.nextBoolean();
        String[] old={"podzemnik","large_podzemnik"};
        List<Floor> floors=new ArrayList<>();List<Box> all=new ArrayList<>();Point previous=null;
        int rotation=r.nextInt(4);
        Random roster=new Random(seed^0x5F444553455254L);
        List<String> regional=new ArrayList<>(List.of("hala","indrik_zmey","storm_idol"));
        Collections.shuffle(regional,roster);
        for(int f=0;f<count;f++){
            int y=-f*pitch;int turn=(rotation+f)%4;
            int bossCell=13; // south edge before rotation; arena is outside the room grid
            int entryCell=f==0?1:11; // previous south edge becomes the local east edge
            Set<Integer> visited=new LinkedHashSet<>();Set<Edge> edges=new LinkedHashSet<>();
            Deque<Integer> stack=new ArrayDeque<>();visited.add(entryCell);stack.push(entryCell);
            while(!stack.isEmpty()){
                int a=stack.peek();List<Integer> choices=new ArrayList<>(neighbors(a));choices.removeAll(visited);
                if(choices.isEmpty()){stack.pop();continue;}
                int b=choices.get(r.nextInt(choices.size()));edges.add(new Edge(a,b));visited.add(b);stack.push(b);
            }
            // Keep at least two independent loops; vary their locations with the seed.
            List<Edge> extras=new ArrayList<>();
            for(int a=0;a<16;a++)for(int b:neighbors(a))if(a<b&&!edges.contains(new Edge(a,b)))extras.add(new Edge(a,b));
            Collections.shuffle(extras,r);edges.addAll(extras.subList(0,Math.min(extras.size(),2+r.nextInt(4))));
            List<Integer> trialOptions=new ArrayList<>();for(int i=0;i<16;i++)if(i!=entryCell&&i!=bossCell)trialOptions.add(i);
            int trial=trialOptions.get(r.nextInt(trialOptions.size()));
            List<Box> rooms=new ArrayList<>();List<Box> passages=new ArrayList<>();
            for(int i=0;i<16;i++){
                Kind kind=i==entryCell?Kind.ENTRY:i==trial?Kind.TRIAL:Kind.values()[1+r.nextInt(5)];
                // Secrets use dead ends only; do not conceal a mandatory route.
                int cellId=i;long degree=edges.stream().filter(e->e.a==cellId||e.b==cellId).count();
                if(kind==Kind.SECRET&&degree!=1)kind=Kind.SHRINE;
                if(i==bossCell&&kind==Kind.SECRET)kind=Kind.HALL;
                int w=kind==Kind.TRIAL?23:13+2*r.nextInt(5);
                int d=kind==Kind.TRIAL?23:13+2*r.nextInt(5);
                int h=kind==Kind.TRIAL?12:kind==Kind.PILLARS?11:7+r.nextInt(3);
                rooms.add(room(rotate(cell(i),turn),y,w,d,h,kind,f));
            }
            for(Edge edge:edges)connect(passages,rooms.get(edge.a),rooms.get(edge.b),y,f);
            Point exit=rotate(new Point(-13,ARENA_OFFSET),turn);
            boolean last=f==count-1;
            int width=last&&doubled?91:65,depth=last&&doubled?77:59;
            Box arena=room(exit,y,(turn&1)==0?width:depth,(turn&1)==0?depth:width,version==1?(last&&doubled?27:23):(last&&doubled?18:16),last&&doubled?Kind.DOUBLE:Kind.BOSS,f);
            rooms.add(arena);connect(passages,rooms.get(bossCell),arena,y,f);
            edges.add(new Edge(bossCell,16));
            if(previous!=null){
                Box landing=room(previous,y,13,13,8,Kind.ENTRY,f);
                rooms.add(landing);connect(passages,landing,rooms.get(entryCell),y,f);
                edges.add(new Edge(entryCell,17));
                if(version==1)all.add(new Box(previous.x-2,y+1,previous.z-2,previous.x+2,y+pitch,previous.z+2,Kind.SHAFT,f));
                else all.add(stairBox(previous,y,f));
            }
            List<String> bosses=last?(doubled?List.of("ant_queen","sand_king"):List.of(queenFirst?"sand_king":"ant_queen")):
                f==count-2&&!doubled?List.of(queenFirst?"ant_queen":"sand_king"):List.of(old[r.nextInt(old.length)]);
            if(version>=3&&!last&&(f!=count-2||doubled)){
                bosses=f==0?List.of(List.of("large_podzemnik","kladovik","ovinnik","fire_serpent").get(roster.nextInt(4))):List.of(regional.removeFirst());
            }
            floors.add(new Floor(f,List.copyOf(rooms),List.copyOf(edges),previous==null?entryCell:17,trial,16,exit,bosses));
            all.addAll(rooms);all.addAll(passages);
            if(version>=2)for(Box b:rooms)if(b.kind==Kind.FLOODED)all.add(new Box(b.x0,b.y0-2,b.z0,b.x1,b.y0,b.z1,Kind.POOL,f));
            previous=exit;
        }
        DungeonPlan plan=new DungeonPlan(seed,floors,all,doubled,version);plan.validate();return plan;
    }
    public void validate(){
        for(Floor floor:floors){
            Set<Integer> seen=new HashSet<>();ArrayDeque<Integer> todo=new ArrayDeque<>();
            seen.add(floor.entry);todo.add(floor.entry);
            while(!todo.isEmpty()){
                int a=todo.remove();
                for(Edge e:floor.edges){int b=e.a==a?e.b:e.b==a?e.a:-1;if(b>=0&&seen.add(b))todo.add(b);}
            }
            if(seen.size()!=floor.rooms.size()||!seen.contains(floor.trial)||!seen.contains(floor.boss))throw new IllegalStateException("Disconnected floor "+floor.index);
            for(int i=0;i<floor.rooms.size();i++)for(int j=i+1;j<floor.rooms.size();j++)if(floor.rooms.get(i).overlaps(floor.rooms.get(j)))throw new IllegalStateException("Overlapping rooms");
        }
        long queen=floors.stream().flatMap(f->f.bosses.stream()).filter("ant_queen"::equals).count();
        long king=floors.stream().flatMap(f->f.bosses.stream()).filter("sand_king"::equals).count();
        if(queen!=1||king!=1)throw new IllegalStateException("Missing/duplicate desert bosses");
    }
    /** Optional encounters have stable IDs independent of the mandatory gate fights. */
    public List<Encounter> encounters(Floor f){
        if(version<3)return List.of();
        Random r=new Random(seed^0x454E434F554E5445L^((long)f.index*0x9E3779B97F4A7C15L));
        List<Integer> candidates=new ArrayList<>();
        for(int i=0;i<f.rooms.size();i++){
            Box b=f.rooms.get(i);
            if(b.kind==Kind.HALL||b.kind==Kind.PILLARS||b.kind==Kind.SHRINE||b.kind==Kind.SECRET)candidates.add(i);
        }
        Collections.shuffle(candidates,r);
        List<Encounter> result=new ArrayList<>();
        if(!candidates.isEmpty()){
            // One optional room guardian, sometimes a pair of small fire serpents.
            int room=candidates.removeFirst();
            Box b=f.rooms.get(room);
            List<String> options=b.kind==Kind.SECRET?List.of("kladovik"):List.of("large_podzemnik","kladovik","fire_serpent");
            String id=options.get(r.nextInt(options.size()));
            result.add(new Encounter(f.index,room,false,id.equals("fire_serpent")&&b.x1-b.x0>=16&&b.z1-b.z0>=16&&r.nextBoolean()?List.of(id,id):List.of(id)));
        }
        int count=1+r.nextInt(3);
        List<String> roam=List.of("podzemnik","large_podzemnik","ovinnik","fire_serpent");
        for(int i=0;i<count&&!candidates.isEmpty();i++){
            int room=candidates.removeFirst();
            result.add(new Encounter(f.index,room,true,List.of(roam.get(r.nextInt(roam.size())))));
        }
        return List.copyOf(result);
    }
    public int expectedBosses(){
        return floors.stream().mapToInt(f->f.bosses.size()+encounters(f).stream().mapToInt(e->e.bosses.size()).sum()).sum();
    }
    public static boolean inRange(double distanceSquared){return distanceSquared<=30.0*30.0;}
    // Stair runs away from the central room grid, under its own upper boss arena.
    // u=0 is the lower landing, u=PITCH+1 is the gated upper landing.
    private static Box stairBox(Point p,int y,int floor){
        int dx=Math.abs(p.x)>Math.abs(p.z)?Integer.signum(p.x):0;
        int dz=dx==0?Integer.signum(p.z):0;
        int x=p.x+dx*(PITCH+2),z=p.z+dz*(PITCH+2);
        return new Box(Math.min(p.x,x)-Math.abs(dz)*2,y,Math.min(p.z,z)-Math.abs(dx)*2,
            Math.max(p.x,x)+Math.abs(dz)*2,y+PITCH+4,Math.max(p.z,z)+Math.abs(dx)*2,Kind.STAIR_DESCENT,floor);
    }
    public static boolean stairAlongX(Box b){return b.x1-b.x0>b.z1-b.z0;}
    public static int stairSign(Box b){return stairAlongX(b)?Integer.signum(b.x0+b.x1):Integer.signum(b.z0+b.z1);}
    public static int stairU(Box b,int x,int z){
        return stairAlongX(b)?(stairSign(b)>0?x-b.x0:b.x1-x):(stairSign(b)>0?z-b.z0:b.z1-z);
    }
    public static int stairV(Box b,int x,int z){return stairAlongX(b)?z-(b.z0+b.z1)/2:x-(b.x0+b.x1)/2;}
    public static int stairY(Box b,int u){return b.y0+Math.min(PITCH,u);}
    public static boolean stairGate(Box b,int x,int y,int z){
        return stairU(b,x,z)==PITCH+1&&Math.abs(stairV(b,x,z))<2&&y>b.y0+PITCH&&y<=b.y0+PITCH+3;
    }
    // Two separated recessed basins in opposite quadrants; both central axes stay dry.
    public static boolean poolWater(Box b,int x,int z){
        int dx=x-(b.x0+b.x1)/2,dz=z-(b.z0+b.z1)/2;
        return dx*dz>0&&Math.abs(dx)>=3&&Math.abs(dz)>=3
            &&Math.abs(dx)<=(b.x1-b.x0)/2-2&&Math.abs(dz)<=(b.z1-b.z0)/2-2;
    }
    public Point entrance(){return center(floors.getFirst().rooms.get(floors.getFirst().entry));}
    public int bottom(){return -(floors.size()-1)*(version==1?LEGACY_PITCH:PITCH)-(version==1?0:2);}
    public int top(){return version==1?23:16;}
    /** Floor zero stays underground if it fits; otherwise raise the complete structure. */
    public int anchorY(int surface,int min,int max){
        int underground=surface-top()-4;
        int y=underground+bottom()>=min+2?underground:Math.max(surface+1-bottom(),min+2-bottom());
        if(y+top()+8>=max)throw new IllegalArgumentException("Dungeon does not fit build height");
        return y;
    }
}
