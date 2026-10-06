package org.slavicmyths.kurgan;

import java.util.*;

/** Pure deterministic planning. Coordinates are relative to the mound's ground anchor. */
public final class KurganPlan {
    public static final int VERSION = 2, ATTEMPTS = 12;
    public int formatVersion=1,attemptsUsed=1;
    public enum Archetype { VESTIBULE,CROSSROADS,BURIAL,WARRIOR,TREASURY,RITUAL,TRAP,FLOODED,OSSUARY,OFFERING,RELIQUARY,COLLAPSED,DESCENT,DEEP }
    public enum NodeType { ENTRANCE,VESTIBULE,JUNCTION,ROOM,REWARD_ROOM,DANGER_ROOM,SECRET_ROOM,TRANSITION_UP,TRANSITION_DOWN,DEEP_OBJECTIVE,DEAD_END }
    public enum Role { SMALL_BURIAL, LARGE_BURIAL, OFFERING, ATMOSPHERIC, RUINED, TRANSITION, BLOCKED_SIDE, FINAL }
    public enum Module { SHORT, NORMAL, LONG, CORNER, T_JUNCTION, CROSSROADS, DEAD_END, ROOM_CONNECTOR, STAIR, LOOP }
    public static final class Box {
        public final int x0,y0,z0,x1,y1,z1;
        public Box(int a,int b,int c,int d,int e,int f){x0=a;y0=b;z0=c;x1=d;y1=e;z1=f;}
        public boolean contains(int x,int y,int z){return x>=x0&&x<=x1&&y>=y0&&y<=y1&&z>=z0&&z<=z1;}
        public boolean intersects(Box b){return x0<=b.x1&&x1>=b.x0&&y0<=b.y1&&y1>=b.y0&&z0<=b.z1&&z1>=b.z0;}
        public int[] array(){return new int[]{x0,y0,z0,x1,y1,z1};}
    }
    public static final class Room {
        public final int id,floor,cell,x,y,z,rx,rz,height,palette,disturbance;
        public final Role role; public final String loot; public boolean critical;
        public Archetype archetype;public NodeType nodeType=NodeType.ROOM;public boolean mandatory=true,roomNode=true;
        public final List<Integer> connectors = new ArrayList<>();
        Room(int id,int f,int cell,int x,int y,int z,Role role,int palette,boolean hall){
            this.id=id;floor=f;this.cell=cell;this.x=x;this.y=y;this.z=z;this.role=role;this.palette=palette;
            rx=hall?14:role==Role.LARGE_BURIAL||role==Role.FINAL?6:4;
            rz=hall?14:role==Role.OFFERING?5:rx; height=hall?12:rx==6?5:4;
            disturbance=hall?20:role==Role.LARGE_BURIAL||role==Role.FINAL?15:8;
            loot=role==Role.OFFERING?"offering":role==Role.FINAL?"important":role==Role.SMALL_BURIAL||role==Role.LARGE_BURIAL?"burial":"";
        }
        public boolean hall(){return rx==14||archetype==Archetype.DEEP&&rx>=11;}
        Room(int id,int f,int cell,int x,int y,int z,Role role,int palette,int rx,int rz,int height,Archetype archetype,NodeType nodeType,boolean roomNode,String loot){
            this.id=id;floor=f;this.cell=cell;this.x=x;this.y=y;this.z=z;this.role=role;this.palette=palette;this.rx=rx;this.rz=rz;this.height=height;
            this.archetype=archetype;this.nodeType=nodeType;this.roomNode=roomNode;this.loot=loot;disturbance=role==Role.FINAL?20:roomNode?12:6;
        }
        public Box box(){int outer=archetype==Archetype.DEEP?1:0;return new Box(x-rx-outer,y-outer,z-rz-outer,x+rx+outer,y+height+1+outer,z+rz+outer);}
    }
    public static final class Step {
        public final int x,y,z; public Step(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    }
    public static final class Link {
        public final int a,b,width,height,palette; public Module module; public final boolean stair;
        public boolean secret;public int style,transitionStyle;
        public final List<Step> steps=new ArrayList<>();
        Link(int a,int b,int width,Module module,int palette){this.a=a;this.b=b;this.width=width;this.module=module;this.palette=palette;stair=module==Module.STAIR;height=width==5||stair?5:4;}
        public Box slice(int i){Step s=steps.get(i);Step next=steps.get(i<steps.size()-1?i+1:Math.max(0,i-1));boolean alongX=next.x!=s.x;int lo=-(width/2)-1,hi=lo+width+1;
            return alongX?new Box(s.x,s.y,s.z+lo,s.x,s.y+height+1,s.z+hi):new Box(s.x+lo,s.y,s.z,s.x+hi,s.y+height+1,s.z);}
    }
    public final int tier;public int floors; public final long seed; public final List<Room> rooms=new ArrayList<>();
    public final List<Link> links=new ArrayList<>(); public final List<Box> niches=new ArrayList<>();
    public int finalRoom; public Box seal; public final int radius;
    KurganPlan(int tier,long seed){this.tier=tier;this.seed=seed;Random r=new Random(seed);floors=tier==0?1:tier==1?2+r.nextInt(2):4+r.nextInt(2);radius=new int[]{8,12,18}[tier];}
    public static KurganPlan create(int tier,long seed){return KurganLayout.create(tier,seed);}
    public static KurganPlan createLegacy(int tier,long seed){if(tier<0||tier>2)throw new IllegalArgumentException("tier");for(int attempt=0;attempt<ATTEMPTS;attempt++){
        KurganPlan p=new KurganPlan(tier,seed);p.build(new Random(seed+attempt*0x632BE59BD9B4E019L));if(p.validate().isEmpty())return p;
    }throw new IllegalArgumentException("No valid kurgan layout after "+ATTEMPTS+" attempts");}
    private void build(Random random){
        int count=tier==0?3+random.nextInt(4):tier==1?10+random.nextInt(9):25+random.nextInt(16);
        int regular=count-(tier==2?1:0);int previous=-1;
        for(int f=0;f<floors;f++){
            int n=regular/floors+(f<regular%floors?1:0);Map<Integer,Room> cells=new LinkedHashMap<>();
            Room root=room(f,0,Role.TRANSITION,random);cells.put(0,root);
            if(previous>=0)stairs(rooms.get(previous),root);previous=root.id;
            while(cells.size()<n){List<int[]> options=new ArrayList<>();for(int c:cells.keySet())for(int d:neighbors(c))if(!cells.containsKey(d))options.add(new int[]{c,d});
                int[] pair=options.get(random.nextInt(options.size()));int index=cells.size();
                Role role=index%6==0?Role.ATMOSPHERIC:index%5==0?Role.RUINED:index%4==0?Role.OFFERING:index%3==0?Role.LARGE_BURIAL:Role.SMALL_BURIAL;
                Room next=room(f,pair[1],role,random);cells.put(pair[1],next);connect(cells.get(pair[0]),next,false,random);
            }
            // Loops join existing adjacent cells only; no corridor crosses an unrelated room.
            if(tier>0){List<int[]> options=new ArrayList<>();for(int c:cells.keySet())for(int d:neighbors(c))if(c<d&&cells.containsKey(d)&&!linked(cells.get(c).id,cells.get(d).id))options.add(new int[]{c,d});
                if(!options.isEmpty()){int[] q=options.get(random.nextInt(options.size()));connect(cells.get(q[0]),cells.get(q[1]),true,random);}}
            if(f==floors-1){
                Room end=cells.values().stream().max(Comparator.comparingInt(a->a.cell%4+a.cell/4)).get();
                if(tier==2){Room hall=new Room(rooms.size(),f,-1,-42,root.y,root.z,Role.FINAL,0,true);rooms.add(hall);connect(root,hall,false,random);finalRoom=hall.id;
                    seal=new Box(hall.x+11,hall.y+1,hall.z-2,hall.x+14,hall.y+5,hall.z+2);
                    niches.add(new Box(hall.x-3,hall.y+1,hall.z-13,hall.x+3,hall.y+6,hall.z-10));
                    niches.add(new Box(hall.x-13,hall.y+1,hall.z-3,hall.x-10,hall.y+6,hall.z+3));
                    niches.add(new Box(hall.x-3,hall.y+1,hall.z+10,hall.x+3,hall.y+6,hall.z+13));
                }else{Room replacement=new Room(end.id,end.floor,end.cell,end.x,end.y,end.z,Role.FINAL,0,false);replacement.connectors.addAll(end.connectors);rooms.set(end.id,replacement);finalRoom=end.id;
                    // Rebuild links to the larger final-room boundary.
                    for(int i=0;i<links.size();i++){Link l=links.get(i);if(l.a==end.id||l.b==end.id){l.steps.clear();fillStraight(l,rooms.get(l.a),rooms.get(l.b));}}
                }
            }
        }
        Room entry=rooms.get(0);Link entrance=new Link(-1,entry.id,3,Module.STAIR,1);
        for(int z=radius+1;z>=entry.z+entry.rz;z--)entrance.steps.add(new Step(0,Math.max(entry.y,Math.min(0,z-(entry.z+entry.rz)-12)),z));
        links.add(entrance);entry.connectors.add(links.size()-1);
        // Mark the actual shortest route, including the sealed threshold as its terminal.
        int[] parent=new int[rooms.size()];Arrays.fill(parent,-1);ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(0);parent[0]=0;
        while(!queue.isEmpty()){int a=queue.remove();for(Link l:links){int b=l.a==a?l.b:l.b==a?l.a:-1;if(b>=0&&parent[b]<0){parent[b]=a;queue.add(b);}}}
        for(int a=finalRoom;a>=0&&parent[a]>=0;){rooms.get(a).critical=true;if(a==0)break;a=parent[a];}
        for(Room r:new ArrayList<>(rooms))if(!r.critical&&r.role==Role.SMALL_BURIAL&&r.connectors.size()==1&&r.id%3==0){Room blocked=new Room(r.id,r.floor,r.cell,r.x,r.y,r.z,Role.BLOCKED_SIDE,r.palette,false);blocked.connectors.addAll(r.connectors);rooms.set(r.id,blocked);break;}

    }
    private Room room(int f,int cell,Role role,Random random){Room r=new Room(rooms.size(),f,cell,new int[]{0,20,44,74}[cell%4],-12-f*10,-12-f*24+new int[]{0,22,50}[cell/4],role,random.nextInt(4)<(f>=2?3:1)?0:1,false);rooms.add(r);return r;}
    private static List<Integer> neighbors(int c){List<Integer> a=new ArrayList<>();if(c%4>0)a.add(c-1);if(c%4<3)a.add(c+1);if(c>=4&&c!=4)a.add(c-4);if(c<8&&c!=0)a.add(c+4);return a;}
    private boolean linked(int a,int b){for(Link l:links)if(l.a==a&&l.b==b||l.a==b&&l.b==a)return true;return false;}
    private void connect(Room a,Room b,boolean loop,Random r){int width=b.hall()?5:tier>0&&r.nextBoolean()?4:3;Link l=new Link(a.id,b.id,width,loop?Module.LOOP:Module.ROOM_CONNECTOR,a.palette);fillStraight(l,a,b);if(!loop&&!b.hall())l.module=l.steps.size()<14?Module.SHORT:l.steps.size()>16?Module.LONG:Module.NORMAL;a.connectors.add(links.size());b.connectors.add(links.size());links.add(l);}
    private void fillStraight(Link l,Room a,Room b){int dx=Integer.signum(b.x-a.x),dz=Integer.signum(b.z-a.z);int x=a.x+dx*a.rx,z=a.z+dz*a.rz,ex=b.x-dx*b.rx,ez=b.z-dz*b.rz;
        for(int i=0;i<128;i++){l.steps.add(new Step(x,a.y,z));if(x==ex&&z==ez)return;x+=dx;z+=dz;}throw new IllegalArgumentException("connector length");}
    private void stairs(Room a,Room b){Link l=new Link(a.id,b.id,3,Module.STAIR,1);for(int d=0;d<=16;d++){int z=a.z-a.rz-d;if(z<b.z+b.rz)break;int y=a.y-Math.min(10,d);l.steps.add(new Step(a.x,y,z));}a.connectors.add(links.size());b.connectors.add(links.size());links.add(l);}
    public Module junction(Room r){int degree=0;boolean eastWest=false,northSouth=false;for(int index:r.connectors){Link l=links.get(index);if(l.stair)continue;degree++;Step a=l.steps.get(0),b=l.steps.get(l.steps.size()-1);eastWest|=a.x!=b.x;northSouth|=a.z!=b.z;}
        return degree>=4?Module.CROSSROADS:degree==3?Module.T_JUNCTION:degree==1?Module.DEAD_END:eastWest&&northSouth?Module.CORNER:Module.NORMAL;}
    public boolean roomAir(Room r,int x,int y,int z){Box b=r.box();if(formatVersion>=2)return KurganLayout.roomAir(r,x,y,z);if(!r.hall())return x>b.x0&&x<b.x1&&z>b.z0&&z<b.z1&&y>r.y&&y<r.y+r.height+1;
        int dx=Math.abs(x-r.x),dz=Math.abs(z-r.z);int ceiling=r.y+(Math.max(dx,dz)<=7?13:Math.max(dx,dz)<=10?12:10);
        boolean air=dx<=11&&dz<=11&&dx+dz<=18&&y>r.y&&y<ceiling;
        for(Box niche:niches)if(niche.contains(x,y,z))air=true;return air;
    }
    public List<String> validate(){if(formatVersion>=2)return KurganLayout.validate(this);List<String> errors=new ArrayList<>();int min=tier==0?3:tier==1?10:25,max=tier==0?6:tier==1?18:40;
        if(rooms.size()<min||rooms.size()>max)errors.add("room count");
        if(tier==0){int branches=0;for(Room r:rooms)if(r.id!=finalRoom&&r.id!=0&&r.connectors.size()==1)branches++;if(branches>2)errors.add("too many small branches");}
        if(rooms.get(finalRoom).floor!=floors-1)errors.add("final floor");
        for(Room a:rooms)for(Room b:rooms)if(a.id<b.id&&a.box().intersects(b.box()))errors.add("room overlap");
        for(Link l:links){if(l.steps.isEmpty()){errors.add("empty connector");continue;}for(int i=0;i<l.steps.size();i++){
            Step s=l.steps.get(i);if(i>0){Step p=l.steps.get(i-1);if(Math.abs(s.x-p.x)+Math.abs(s.z-p.z)!=1||Math.abs(s.y-p.y)>1)errors.add("broken stairs");}
            for(Room r:rooms)if(r.id!=l.a&&r.id!=l.b&&l.slice(i).intersects(r.box()))errors.add("connector collision");
        }Step end=l.steps.get(l.steps.size()-1);Room b=rooms.get(l.b);if(end.y!=b.y||!b.box().contains(end.x,end.y,end.z))errors.add("unaligned endpoint");}
        for(int a=0;a<links.size();a++)for(int b=a+1;b<links.size();b++){Link la=links.get(a),lb=links.get(b);for(int x=0;x<la.steps.size();x++)for(int z=0;z<lb.steps.size();z++)if(la.slice(x).intersects(lb.slice(z))){
            int shared=la.a>=0&&(la.a==lb.a||la.a==lb.b)?la.a:la.b==lb.a||la.b==lb.b?la.b:-1;
            if(shared<0)errors.add("unrelated corridor collision");
            else {Box room=rooms.get(shared).box();Step sa=la.steps.get(x),sb=lb.steps.get(z);if(!room.contains(sa.x,sa.y,sa.z)&&!room.contains(sb.x,sb.y,sb.z))errors.add("corridor collision outside junction");}
        }}
        Set<Integer> visited=new HashSet<>();visited.add(0);boolean changed=true;while(changed){changed=false;for(Link l:links)if(l.a>=0&&(visited.contains(l.a)||visited.contains(l.b))){changed|=visited.add(l.a);changed|=visited.add(l.b);}}
        if(visited.size()!=rooms.size())errors.add("disconnected");
        if(tier==2&&(seal==null||niches.size()!=3||!rooms.get(finalRoom).hall()))errors.add("protected tomb");
        return errors;
    }
    public Box bounds(){int x0=-radius-3,z0=-radius-3,y0=-1,x1=radius+3,z1=radius+(formatVersion==2?17:3),y1=14;for(Room r:rooms){Box b=r.box();x0=Math.min(x0,b.x0);y0=Math.min(y0,b.y0);z0=Math.min(z0,b.z0);x1=Math.max(x1,b.x1);z1=Math.max(z1,b.z1);}for(Link l:links)for(int i=0;i<l.steps.size();i++){Box b=l.slice(i);x0=Math.min(x0,b.x0);z0=Math.min(z0,b.z0);x1=Math.max(x1,b.x1);z1=Math.max(z1,b.z1);}return new Box(x0,y0-1,z0,x1,y1,z1);}
}
