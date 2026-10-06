package org.slavicmyths.swamp;

import java.util.*;

/** Bounded, deterministic geometry. No world or registries are accessed here. */
public final class SwampTreeShape {
    public record Cell(int x,int y,int z){public Cell offset(int a,int b,int c){return new Cell(x+a,y+b,z+c);}}
    public final Map<Cell,Integer> logs=new LinkedHashMap<>();
    public final Map<Cell,Integer> leaves=new LinkedHashMap<>();
    public final Set<Cell> hanging=new LinkedHashSet<>();
    public final String kind;public final int height;
    public SwampTreeShape(String kind,long seed){
        this.kind=kind;Random r=new Random(seed);
        height=switch(kind){case "willow"->7+r.nextInt(6);case "oak"->8+r.nextInt(7);case "pine"->12+r.nextInt(9);case "dead"->7+r.nextInt(5);default->throw new IllegalArgumentException(kind);};
        int dx=r.nextBoolean()?1:-1,dz=r.nextBoolean()?1:-1,x=0,z=0;
        for(int y=0;y<height;y++){
            logs.put(new Cell(x,y,z),1);
            if(kind.equals("oak"))logs.put(new Cell(x+1,y,z),1);
            if(!kind.equals("pine")&&y>2&&y%3==0){x+=dx;logs.put(new Cell(x,y,z),0);if(y%2==0){z+=dz;logs.put(new Cell(x,y,z),2);}}
        }
        for(int[] arm:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int n=1;n<=2+r.nextInt(2);n++){
            logs.put(new Cell(arm[0]*n,0,arm[1]*n),arm[0]!=0?0:2);
            logs.put(new Cell(arm[0]*n,-1,arm[1]*n),1);
        }
        if(kind.equals("pine")){
            for(int y=height-7;y<height;y+=2)canopy(0,y,0,y>=height-3?1:2,1,r);
        }else{
            for(int i=0;i<4;i++){
                int ax=i%2==0?1:-1,az=i<2?1:-1;int by=height-3-r.nextInt(3);final int branchY=by;Cell anchor=logs.keySet().stream().filter(q->q.y()==branchY).findFirst().orElseThrow();int bx=anchor.x(),bz=anchor.z();
                for(int n=0;n<2+r.nextInt(2);n++){bx+=ax;logs.put(new Cell(bx,by,bz),0);bz+=az;logs.put(new Cell(bx,by,bz),2);}
                if(!kind.equals("dead"))canopy(bx,by+1,bz,2,1,r);
            }
            if(!kind.equals("dead"))canopy(x,height-1,z,2,2,r);
        }
        leaves.keySet().removeAll(logs.keySet());
        // Actual leaf connectivity, rather than distance values painted by geometry.
        ArrayDeque<Cell> queue=new ArrayDeque<>();Map<Cell,Integer> distances=new HashMap<>();
        for(Cell c:logs.keySet()){distances.put(c,0);queue.add(c);}
        while(!queue.isEmpty()){Cell c=queue.remove();int d=distances.get(c)+1;if(d>6)continue;
            for(int[] a:AXES){Cell q=c.offset(a[0],a[1],a[2]);if(leaves.containsKey(q)&&!distances.containsKey(q)){distances.put(q,d);queue.add(q);}}
        }
        leaves.entrySet().removeIf(e->!distances.containsKey(e.getKey()));leaves.replaceAll((c,d)->distances.get(c));
        if(kind.equals("willow"))for(Cell c:List.copyOf(leaves.keySet()))if(!leaves.containsKey(c.offset(0,-1,0))&&r.nextInt(4)==0){
            for(int n=1;n<=1+r.nextInt(3);n++){Cell q=c.offset(0,-n,0);if(q.y()<2||logs.containsKey(q)||leaves.containsKey(q))break;hanging.add(q);}
        }
        if(logs.size()+leaves.size()+hanging.size()>2400)throw new IllegalStateException("Tree budget exceeded");
    }
    private static final int[][] AXES={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    private void canopy(int x,int y,int z,int radius,int ry,Random r){for(int a=-radius;a<=radius;a++)for(int b=-ry;b<=ry;b++)for(int c=-radius;c<=radius;c++)
        if(a*a+c*c<=radius*radius+1&&!(Math.abs(a)==radius&&Math.abs(c)==radius)&&r.nextInt(12)!=0)leaves.put(new Cell(x+a,y+b,z+c),7);}
}
