package org.slavicmyths.wood;

import java.util.*;

/** Pure geometry, shared by worldgen and saplings; no world writes until validation. */
public final class TreeShape {
 public static final class Cell {
  public final int x,y,z;
  public Cell(int a,int b,int c){x=a;y=b;z=c;}
  public Cell offset(int a,int b,int c){return new Cell(x+a,y+b,z+c);}
  @Override public boolean equals(Object o){if(!(o instanceof Cell))return false;Cell c=(Cell)o;return x==c.x&&y==c.y&&z==c.z;}
  @Override public int hashCode(){return (x*31+y)*31+z;}
 }
 public final Map<Cell,Integer> logs=new LinkedHashMap<>(),leaves=new LinkedHashMap<>();
 public final Set<Cell> hanging=new LinkedHashSet<>();
 private final java.util.function.IntUnaryOperator nextInt;
 private final java.util.function.DoubleSupplier nextDouble;
 private final java.util.function.BooleanSupplier nextBoolean;
 public final int variant;
 public TreeShape(String species,Random r){this(species,r::nextInt,r::nextDouble,r::nextBoolean);}
 public TreeShape(String species,java.util.function.IntUnaryOperator integers,java.util.function.DoubleSupplier doubles,java.util.function.BooleanSupplier booleans){nextInt=integers;nextDouble=doubles;nextBoolean=booleans;variant=nextInt.applyAsInt(3);
  switch(species){case "linden":linden();break;case "rowan":rowan();break;case "willow":willow();break;case "pine":pine();break;default:throw new IllegalArgumentException(species);}
  for(Cell c:logs.keySet())leaves.remove(c);
  // Match vanilla six-face leaf distance, avoiding disconnected leaves that immediately decay.
  ArrayDeque<Cell> queue=new ArrayDeque<>();Map<Cell,Integer> distance=new HashMap<>();
  for(Cell c:logs.keySet()){distance.put(c,0);queue.add(c);}
  while(!queue.isEmpty()){Cell c=queue.remove();int d=distance.get(c)+1;if(d>6)continue;
   for(int[]v:DIRS){Cell n=c.offset(v[0],v[1],v[2]);if(leaves.containsKey(n)&&!distance.containsKey(n)){distance.put(n,d);queue.add(n);}}
  }
  leaves.entrySet().removeIf(e->!distance.containsKey(e.getKey()));
  for(Map.Entry<Cell,Integer>e:leaves.entrySet())e.setValue(distance.get(e.getKey()));
  if(species.equals("willow")){
   for(Cell c:new ArrayList<>(leaves.keySet()))if(c.y>=4&&(Math.abs(c.x)>=3||Math.abs(c.z)>=3)&&!leaves.containsKey(c.offset(0,-1,0))&&nextInt.applyAsInt(3)==0){
    int length=1+nextInt.applyAsInt(3);for(int i=1;i<=length&&c.y-i>=1;i++){Cell n=c.offset(0,-i,0);if(logs.containsKey(n)||leaves.containsKey(n))break;hanging.add(n);}
   }
  }
 }
 public static final int[][]DIRS={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
 private void trunk(int h){for(int y=0;y<h;y++)logs.put(new Cell(0,y,0),1);}
 private void branch(int y,int x,int top,int z){Cell c=new Cell(0,y,0);logs.put(c,1);
  // Manhattan steps keep every branch face-connected to its parent trunk.
  while(c.x!=x||c.y!=top||c.z!=z){if(c.x!=x){c=c.offset(Integer.signum(x-c.x),0,0);logs.put(c,0);}if(c.z!=z){c=c.offset(0,0,Integer.signum(z-c.z));logs.put(c,2);}if(c.y!=top){c=c.offset(0,Integer.signum(top-c.y),0);logs.put(c,1);}}
 }
 private void crown(int cx,int cy,int cz,double rx,double ry,double rz,boolean airy){
  for(int x=-(int)Math.ceil(rx);x<=rx;x++)for(int y=-(int)Math.ceil(ry);y<=ry;y++)for(int z=-(int)Math.ceil(rz);z<=rz;z++){
   double d=x*x/(rx*rx)+y*y/(ry*ry)+z*z/(rz*rz);if(d>1||cy+y<1)continue;
   if(d>.72&&nextInt.applyAsInt(airy?3:7)==0)continue;leaves.put(new Cell(cx+x,cy+y,cz+z),7);
  }
 }
 private void linden(){int h=variant==2?10+nextInt.applyAsInt(2):6+nextInt.applyAsInt(2);trunk(h);
  crown(0,h-1,0,3,2.6,3,false);
  int span=variant==1?3:2;
  for(int i=0;i<5;i++){double a=i*Math.PI*2/5+nextDouble.getAsDouble()*.4;int x=(int)Math.round(Math.cos(a)*span),z=(int)Math.round(Math.sin(a)*span);int y=h-2+nextInt.applyAsInt(2);branch(h-4,x,y,z);crown(x,y,z,2.2,2,2.4,false);}
 }
 private void rowan(){int h=variant==2?4:5+nextInt.applyAsInt(2);trunk(h);
  crown(0,h,0,1.7,1.4,1.7,true);
  int x=nextBoolean.getAsBoolean()?2:-2;branch(variant==1?2:h-2,x,h-1,1);crown(x,h-1,1,1.7,1.5,1.5,true);
  if(variant!=2){branch(h-3,-x,h-2,-1);crown(-x,h-1,-1,1.5,1.3,1.7,true);}
 }
 private void willow(){int h=5+nextInt.applyAsInt(2);trunk(h);crown(0,h,0,2.7,1.4,2.7,false);
  int span=variant==1?4:3;
  for(int i=0;i<5;i++){double a=i*Math.PI*2/5+.2;int x=(int)Math.round(Math.cos(a)*span)+(variant==2?1:0),z=(int)Math.round(Math.sin(a)*span);int y=h-1+nextInt.applyAsInt(2);branch(2+nextInt.applyAsInt(2),x,y,z);crown(x,y,z,2,1.3,2,false);}
 }
 private void pine(){int h=9+nextInt.applyAsInt(4);trunk(h);
  for(int i=0;i<5&&2+i*2<h;i++){int y=2+i*2,r=Math.max(1,3-i/2);tier(y,r,1);}
  leaves.put(new Cell(0,h-1,0),7);
 }
 private void tier(int y,int radius,int width){
  for(int x=-radius;x<width+radius;x++)for(int z=-radius;z<width+radius;z++){
   int dx=x<0?-x:x>=width?x-width+1:0,dz=z<0?-z:z>=width?z-width+1:0;
   if(dx+dz<=radius+1){leaves.put(new Cell(x,y,z),7);}
  }
 }
 /** Giant geometry is selected only by the 2x2 sapling grower, never ordinary worldgen. */
 public static TreeShape giantPine(java.util.function.IntUnaryOperator r){
  TreeShape s=new TreeShape("pine",r,()->.5,()->false);s.logs.clear();s.leaves.clear();
  int h=16+r.applyAsInt(5);for(int y=0;y<h;y++)for(int x=0;x<2;x++)for(int z=0;z<2;z++)s.logs.put(new Cell(x,y,z),1);
  int tiers=6+(h-16)/2;for(int i=0;i<tiers;i++)s.tier(5+i*2,Math.max(0,3-i/2),2);
  for(int y=h-2;y<h;y++)s.tier(y,0,2);
  s.leaves.keySet().removeAll(s.logs.keySet());
  // Every giant leaf is at most six Manhattan steps from the 2x2 trunk.
  s.leaves.replaceAll((c,d)->Math.min(6,Math.max(0,c.x<0?-c.x:c.x>1?c.x-1:0)+Math.max(0,c.z<0?-c.z:c.z>1?c.z-1:0)+Math.max(0,c.y-h+1)));
  return s;
 }
}
