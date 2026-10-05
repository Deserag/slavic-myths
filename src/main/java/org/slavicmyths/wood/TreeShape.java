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
 private void pine(){int h=variant==1?17+nextInt.applyAsInt(5):12+nextInt.applyAsInt(5);trunk(h);
  // Sparse unequal horizontal boughs, open lower half; never a stacked spruce cone.
  for(int i=0;i<6;i++){int y=h/2+i*(h-h/2-2)/6;double a=i*2.4+nextDouble.getAsDouble()*.5;int span=(i<4?3:2)+(variant==2&&i%2==0?1:0);int x=(int)Math.round(Math.cos(a)*span),z=(int)Math.round(Math.sin(a)*span);branch(y,x,y+(i%2),z);crown(x,y+1,z,2,1.1,1.8,true);}
  crown(0,h-1,0,1.7,1.4,1.7,true);
 }
}
