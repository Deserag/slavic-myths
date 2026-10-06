package org.slavicmyths.kurgan;

import java.util.*;
import static org.slavicmyths.kurgan.KurganPlan.*;
import org.slavicmyths.kurgan.KurganPlan.Module;

/** Graph first, then bounded geometric embedding. No world access or global random state. */
public final class KurganLayout {
    private static final int[][] RING={{0,0},{1,0},{2,0},{2,1},{2,2},{1,2},{0,2},{0,1}};
    public record Metrics(int nodes,int rooms,int loops,int deadEnds,int secrets,int transitions,int shortestPath,int choices,double optionalPercent,int containers){}
    public static KurganPlan create(int tier,long seed){
        if(tier<0||tier>2)throw new IllegalArgumentException("tier");List<String> last=List.of();
        for(int attempt=0;attempt<ATTEMPTS;attempt++){
            KurganPlan p=new KurganPlan(tier,seed);p.formatVersion=2;p.attemptsUsed=attempt+1;
            Random random=new Random(seed+attempt*0x632BE59BD9B4E019L);p.floors=tier==0?1:tier==1?2:3+random.nextInt(2);
            graph(p,random);embed(p,random);markRoutes(p);last=validate(p);if(last.isEmpty())return p;
        }throw new IllegalArgumentException("Kurgan rejected after "+ATTEMPTS+" attempts: "+last);
    }
    private static Room add(KurganPlan p,int floor,int cell,int x,int y,int z,Archetype art,NodeType type,boolean room){
        int rx=3,rz=3,h=5;Role role=Role.ATMOSPHERIC;String loot="";
        if(art!=null)switch(art){
            case VESTIBULE->{rx=3+p.tier;rz=4+p.tier;h=4+p.tier;role=Role.TRANSITION;}
            case CROSSROADS->{rx=4+p.tier;rz=rx;h=5;role=Role.TRANSITION;}
            case BURIAL->{rx=4+p.tier*2;rz=5+p.tier*2;h=6+p.tier;role=Role.LARGE_BURIAL;loot="burial";}
            case WARRIOR->{rx=5+p.tier;rz=7+p.tier;h=6;role=Role.LARGE_BURIAL;loot="warrior";}
            case TREASURY->{rx=4+p.tier;rz=5+p.tier;h=5;role=Role.OFFERING;loot="treasury";}
            case RITUAL->{rx=5+p.tier;rz=rx;h=7+p.tier;role=Role.OFFERING;loot="ritual";}
            case TRAP->{rx=3+p.tier;rz=6+p.tier;h=5;role=Role.RUINED;loot="common_cache";}
            case FLOODED->{rx=4+p.tier;rz=5+p.tier;h=6;role=Role.RUINED;loot="common_cache";}
            case OSSUARY->{rx=4;rz=5+p.tier;h=5;role=Role.SMALL_BURIAL;loot="common_cache";}
            case OFFERING->{rx=3+p.tier;rz=Math.min(5,4+p.tier);h=5;role=Role.OFFERING;loot="common_cache";}
            case RELIQUARY->{rx=p.tier==0?2:3;rz=p.tier==2?4:3;h=4;role=Role.BLOCKED_SIDE;loot="secret";}
            case COLLAPSED->{rx=4+p.tier;rz=5+p.tier;h=6;role=Role.RUINED;loot="common_cache";}
            case DESCENT->{role=Role.TRANSITION;}
            case DEEP->{rx=p.tier==2?11:8;rz=rx;h=p.tier==2?9:7;role=Role.FINAL;loot=p.tier==2?"great_special":"burial";}
        }
        if(type==NodeType.DEAD_END)loot=cell%2==0?"common_cache":"";
        Room r=new Room(p.rooms.size(),floor,cell,x,y,z,role,art==Archetype.FLOODED?1:0,rx,rz,h,art,type,room,loot);p.rooms.add(r);return r;
    }
    private static void edge(KurganPlan p,Room a,Room b,Module module,boolean secret,int style){
        int width=secret?2:module==Module.STAIR?3:style==3?5:3+(Math.floorMod(a.id+b.id+(int)p.seed,2));
        Link l=new Link(a.id,b.id,width,module,a.palette);l.secret=secret;l.style=style;l.transitionStyle=Math.floorMod(a.id+b.id+(int)p.seed,4);
        a.connectors.add(p.links.size());b.connectors.add(p.links.size());p.links.add(l);
    }
    private static void graph(KurganPlan p,Random random){
        Room vest=add(p,0,-1,0,0,p.radius-(4+p.tier)-2,Archetype.VESTIBULE,NodeType.VESTIBULE,true);
        Room[][] bands=new Room[p.floors][];
        for(int f=0;f<p.floors;f++){
            int n=p.tier==0?4:8;bands[f]=new Room[n];
            for(int i=0;i<n;i++){
                int[] c=p.tier==0?new int[][]{{0,0},{1,0},{1,1},{0,1}}[i]:RING[i];
                Archetype art=null;
                if(p.tier==0){if(i==2)art=Archetype.BURIAL;}
                else if(p.tier==1){if(i==0&&f==0)art=Archetype.WARRIOR;else if(i==4)art=f==0?Archetype.TREASURY:Archetype.BURIAL;else if(i==2&&f==1)art=random.nextBoolean()?Archetype.TRAP:Archetype.OSSUARY;}
                else if(f==0){if(i==0)art=Archetype.CROSSROADS;else if(i==4)art=Archetype.BURIAL;}
                else if(f==1){if(i==2)art=Archetype.RITUAL;else if(i==6)art=Archetype.TREASURY;}
                else {if(i==1)art=random.nextBoolean()?Archetype.FLOODED:Archetype.COLLAPSED;else if(i==3)art=new Archetype[]{Archetype.TRAP,Archetype.OSSUARY,Archetype.OFFERING,Archetype.WARRIOR}[random.nextInt(4)];}
                bands[f][i]=add(p,f,i,c[0]*32,-12-f*16,-28+c[1]*32,art,art==null?NodeType.JUNCTION:NodeType.ROOM,art!=null);
            }
            for(int i=0;i<n;i++)edge(p,bands[f][i],bands[f][(i+1)%n],i==n-1?Module.LOOP:Module.NORMAL,false,random.nextInt(4));
            int first=p.tier==0?1:random.nextBoolean()?1:2,second=p.tier==0?3:f==p.floors-1?5:random.nextBoolean()?5:6;
            Room a=bands[f][first],b=bands[f][second];
            boolean secret=p.tier>0||random.nextBoolean();
            Room leaf=add(p,f,12,a.x,a.y,a.z-24,secret?Archetype.RELIQUARY:new Archetype[]{Archetype.TRAP,Archetype.OFFERING,Archetype.OSSUARY}[random.nextInt(3)],secret?NodeType.SECRET_ROOM:NodeType.REWARD_ROOM,true);
            edge(p,a,leaf,Module.ROOM_CONNECTOR,secret,secret?4:random.nextInt(3));
            Room cache=add(p,f,14,b.x,b.y,b.z+24,null,NodeType.DEAD_END,false);edge(p,b,cache,Module.DEAD_END,false,random.nextInt(3));
        }
        edge(p,vest,bands[0][0],Module.STAIR,false,0);
        for(int f=0;f<p.floors-1;f++){
            edge(p,bands[f][7],bands[f+1][0],Module.STAIR,false,0);
            edge(p,bands[f][3],bands[f+1][4],Module.STAIR,false,f%2==0?3:1);
            for(int i:new int[]{0,4})if(!bands[f+1][i].roomNode){bands[f+1][i].archetype=Archetype.DESCENT;bands[f+1][i].nodeType=NodeType.TRANSITION_UP;}
            for(int i:new int[]{7,3})if(!bands[f][i].roomNode){bands[f][i].archetype=Archetype.DESCENT;bands[f][i].nodeType=NodeType.TRANSITION_DOWN;}
        }
        if(p.tier==0)p.finalRoom=bands[0][2].id;
        else {
            Room deep=add(p,p.floors-1,20,32,-12-(p.floors-1)*16,76,Archetype.DEEP,NodeType.DEEP_OBJECTIVE,true);
            Room approach=p.rooms.stream().filter(r->r.floor==p.floors-1&&r.cell==14).findFirst().orElseThrow();
            edge(p,approach,deep,Module.ROOM_CONNECTOR,false,3);p.finalRoom=deep.id;
            if(p.tier==2){
                p.seal=new Box(deep.x-2,deep.y+1,deep.z-deep.rz-1,deep.x+2,deep.y+5,deep.z-deep.rz);
                p.niches.add(new Box(deep.x-3,deep.y+1,deep.z+7,deep.x+3,deep.y+4,deep.z+9));
                p.niches.add(new Box(deep.x-9,deep.y+1,deep.z-3,deep.x-7,deep.y+4,deep.z+3));
                p.niches.add(new Box(deep.x+7,deep.y+1,deep.z-3,deep.x+9,deep.y+4,deep.z+3));
            }
        }
        Link entry=new Link(-1,vest.id,3,Module.NORMAL,0);entry.style=0;
        for(int z=p.radius+3;z>=vest.z+vest.rz;z--)entry.steps.add(new Step(0,0,z));vest.connectors.add(p.links.size());p.links.add(entry);
    }
    private static void point(Link l,int x,int y,int z){if(l.steps.isEmpty()||l.steps.getLast().x!=x||l.steps.getLast().y!=y||l.steps.getLast().z!=z)l.steps.add(new Step(x,y,z));}
    private static void walk(Link l,int x,int z,int targetY){
        Step from=l.steps.getLast();int distance=Math.abs(x-from.x)+Math.abs(z-from.z),dy=targetY-from.y;
        if(Math.abs(dy)>distance)throw new IllegalArgumentException("Insufficient stair run");
        int px=from.x,pz=from.z;for(int i=1;i<=distance;i++){
            if(px!=x)px+=Integer.signum(x-px);else pz+=Integer.signum(z-pz);
            int y=from.y+(int)Math.round((double)dy*i/distance);point(l,px,y,pz);
        }
    }
    private static void embed(KurganPlan p,Random random){
        for(Link l:p.links){if(l.a<0)continue;Room a=p.rooms.get(l.a),b=p.rooms.get(l.b);
            if(a.id==0){
                point(l,a.x-a.rx,a.y,a.z);walk(l,-16,a.z,-6);walk(l,-16,b.z,b.y);walk(l,b.x-b.rx,b.z,b.y);
            }else if(a.floor!=b.floor){
                boolean left=a.x<32;int side=left?-1:1;
                point(l,a.x+side*a.rx,a.y,a.z);int outer=left?-24:88;walk(l,outer,a.z,a.y);
                // Different ends on each band: no central shaft, two physically separated routes.
                int landing=b.z+(b.z>a.z?24:-24);walk(l,outer,landing,b.y);
                walk(l,b.x+side*b.rx,landing,b.y);walk(l,b.x+side*b.rx,b.z,b.y);
            }else{
                int dx=Integer.signum(b.x-a.x),dz=Integer.signum(b.z-a.z);
                point(l,a.x+dx*a.rx,a.y,a.z+dz*a.rz);
                walk(l,b.x-dx*b.rx,b.z-dz*b.rz,b.y);
            }
        }
    }
    private static int[] parents(KurganPlan p){int[] parent=new int[p.rooms.size()];Arrays.fill(parent,-1);parent[0]=0;ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(0);
        while(!queue.isEmpty()){int a=queue.remove();for(int i:p.rooms.get(a).connectors){Link l=p.links.get(i);int b=l.a==a?l.b:l.a;if(b>=0&&parent[b]<0){parent[b]=a;queue.add(b);}}}return parent;}
    private static void markRoutes(KurganPlan p){int[] parent=parents(p);for(int n=p.finalRoom;n>=0;){p.rooms.get(n).critical=true;if(n==0)break;n=parent[n];}
        int mandatoryTarget=(int)Math.round((p.rooms.size()+1)*(p.tier==0?.70:p.tier==1?.62:.55))-1;
        int mandatory=0;for(Room r:p.rooms){r.mandatory=r.critical||r.archetype==Archetype.RITUAL||r.archetype==Archetype.BURIAL||r.archetype==Archetype.WARRIOR; if(r.mandatory)mandatory++;}
        for(Room r:p.rooms)if(mandatory<mandatoryTarget&&!r.mandatory&&r.nodeType!=NodeType.SECRET_ROOM&&r.nodeType!=NodeType.DEAD_END){r.mandatory=true;mandatory++;}
    }
    public static Step rewardPoint(Room r,int index){boolean octagon=r.archetype==Archetype.DEEP||r.archetype==Archetype.RITUAL;int inset=octagon?3:2;return new Step(r.x+(index==0?-r.rx+inset:r.rx-inset),r.y+1,r.z+r.rz-(octagon?4:2));}
    public static int rewards(Room r){if(r.loot.isEmpty()||r.nodeType==NodeType.DEAD_END&&r.connectors.size()>1)return 0;return r.archetype==Archetype.TREASURY?2:r.archetype==Archetype.BURIAL&&r.rx>=8?2:1;}
    public static Metrics metrics(KurganPlan p){int rooms=0,dead=0,secrets=0,transitions=0,optional=0,containers=0,choices=0,path=0;
        for(Room r:p.rooms){if(r.roomNode)rooms++;if(r.connectors.size()==1&&r.id!=0)dead++;if(!r.mandatory)optional++;containers+=rewards(r);if(r.critical){path++;if(r.connectors.size()>=3)choices++;}}
        for(Link l:p.links){if(l.secret)secrets++;if(l.a>=0&&p.rooms.get(l.a).floor!=p.rooms.get(l.b).floor)transitions++;}
        return new Metrics(p.rooms.size()+1,rooms,p.links.size()-p.rooms.size(),dead,secrets,transitions,path+1,choices,100d*optional/(p.rooms.size()+1),containers);
    }
    public static boolean roomAir(Room r,int x,int y,int z){Box b=r.box();if(Math.abs(x-r.x)>=r.rx||Math.abs(z-r.z)>=r.rz||y<=r.y||y>r.y+r.height)return false;
        if(r.archetype==Archetype.DEEP||r.archetype==Archetype.RITUAL){int dx=Math.abs(x-r.x),dz=Math.abs(z-r.z);return dx+dz<=r.rx+r.rz-5;}
        return true;
    }
    public static List<String> validate(KurganPlan p){List<String> errors=new ArrayList<>();Metrics m=metrics(p);int t=p.tier;
        if(m.nodes()<new int[]{7,14,28}[t]||m.nodes()>new int[]{12,24,45}[t])errors.add("node count "+m.nodes());
        if(m.rooms()<new int[]{2,5,8}[t]||m.rooms()>new int[]{4,8,14}[t])errors.add("room count "+m.rooms());
        if(m.loops()<new int[]{1,2,4}[t])errors.add("loops");if(m.secrets()<new int[]{0,1,2}[t]||m.secrets()>new int[]{1,2,4}[t])errors.add("secrets");
        if(m.deadEnds()<new int[]{1,2,4}[t]||m.deadEnds()>new int[]{3,5,9}[t])errors.add("dead ends");
        if(m.optionalPercent()<new double[]{25,30,35}[t]||t>0&&m.optionalPercent()>new double[]{100,45,55}[t])errors.add("optional percentage "+m.optionalPercent());
        if(m.shortestPath()>m.nodes()*new double[]{.70,.60,.50}[t])errors.add("dominant primary route");
        if(m.choices()<new int[]{2,4,7}[t])errors.add("too few route decisions "+m.choices());
        if(m.containers()<new int[]{2,5,10}[t]||m.containers()>new int[]{5,9,18}[t])errors.add("container density "+m.containers());
        int[] parent=parents(p);for(int i:parent)if(i<0){errors.add("disconnected");break;}
        for(Room a:p.rooms){for(int point=0;point<rewards(a);point++){Step s=rewardPoint(a,point);if(!roomAir(a,s.x,s.y,s.z)||!roomAir(a,s.x,s.y+1,s.z))errors.add("blocked reward position "+a.id+"/"+point);if(point==0&&(a.archetype==Archetype.BURIAL||a.archetype==Archetype.WARRIOR||a.archetype==Archetype.DEEP)&&(!roomAir(a,s.x-1,s.y,s.z)||!roomAir(a,s.x+1,s.y,s.z)))errors.add("coffin head outside room "+a.id);}
            if(a.connectors.size()>5)errors.add("excessive degree");for(Room b:p.rooms)if(a.id<b.id&&a.box().intersects(b.box()))errors.add("room overlap "+a.id+"/"+b.id);}
        for(Link l:p.links){if(l.steps.isEmpty()){errors.add("empty path");continue;}
            for(int i=0;i<l.steps.size();i++){Step s=l.steps.get(i);if(i>0){Step a=l.steps.get(i-1);if(Math.abs(s.x-a.x)+Math.abs(s.z-a.z)!=1||Math.abs(s.y-a.y)>1)errors.add("unwalkable path");}
                for(Room r:p.rooms)if(r.id!=l.a&&r.id!=l.b&&l.slice(i).intersects(r.box())){errors.add("corridor/room "+l.a+"-"+l.b+" / "+r.id);break;}}
            Room b=p.rooms.get(l.b);Step end=l.steps.getLast();if(end.y!=b.y||!b.box().contains(end.x,end.y,end.z))errors.add("endpoint mismatch");
        }
        for(int band=0;band<p.floors-1;band++){
            List<Room> starts=new ArrayList<>();for(Link l:p.links)if(l.a>=0&&p.rooms.get(l.a).floor==band&&p.rooms.get(l.b).floor==band+1)starts.add(p.rooms.get(l.a));
            if(starts.size()<2)errors.add("missing alternate transition");else if(Math.hypot(starts.get(0).x-starts.get(1).x,starts.get(0).z-starts.get(1).z)<20)errors.add("clustered transitions");
        }
        if(t==2&&(p.seal==null||p.niches.size()!=3))errors.add("protected tomb contract");
        physicalLinks(p,errors);return errors;
    }
    private record Cell(int x,int y,int z){}
    private static void physicalLinks(KurganPlan p,List<String> errors){
        Map<Cell,Integer> occupied=new HashMap<>();Set<String> reported=new HashSet<>();
        for(int id=0;id<p.links.size();id++){Link l=p.links.get(id);
            for(int i=0;i<l.steps.size();i++){Box b=l.slice(i);int h=l.secret?3:l.height;
                for(int x=b.x0+(b.x0==b.x1?0:1);x<=b.x1-(b.x0==b.x1?0:1);x++)for(int z=b.z0+(b.z0==b.z1?0:1);z<=b.z1-(b.z0==b.z1?0:1);z++)for(int y=b.y0+1;y<=b.y0+h;y++){
                    Cell cell=new Cell(x,y,z);Integer previous=occupied.putIfAbsent(cell,id);if(previous==null||previous==id)continue;Link other=p.links.get(previous);boolean junction=false;
                    for(int node:new int[]{l.a,l.b})if(node>=0&&(node==other.a||node==other.b)){Box room=p.rooms.get(node).box();if(x>=room.x0-1&&x<=room.x1+1&&z>=room.z0-1&&z<=room.z1+1&&y>=room.y0&&y<=room.y1)junction=true;}
                    if(!junction&&reported.add(previous+"/"+id))errors.add("undeclared corridor merge "+previous+"/"+id+" at "+cell);
                }
            }
        }
    }
    private KurganLayout(){}
}
