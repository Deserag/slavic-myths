package org.slavicmyths.desert;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
/** Exact V3 articulated shell and sampled 23-bone IK transforms. */
public final class IndrikGeometry {
 public static final IndrikGeometry INSTANCE=new IndrikGeometry();
 public final int count;
 public final String[] groups;
 public final int[] colors,part,mask,flags;
 private final byte[] xyz;
 private final float[][][] frames;
 private final int[][] support;
 private final int[] undergroundCubes;
 private static final int[][] CORNERS={{0,0,0},{1,0,0},{1,1,0},{0,1,0},{0,0,1},{1,0,1},{1,1,1},{0,1,1}};
 public static final int[][] FACES={{0,4,7,3},{1,2,6,5},{0,1,5,4},{3,7,6,2},{0,3,2,1},{4,5,6,7}};
 private IndrikGeometry(){
  try(var raw=IndrikGeometry.class.getResourceAsStream("/assets/slavicmyths/indrik/geometry.bin.gz")){
   if(raw==null)throw new IOException("Missing Indrik V3 geometry");
   try(var in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)))){
    if(in.readInt()!=0x494e4433)throw new IOException("Invalid Indrik V3 format");
    count=in.readInt();int n=in.readInt(),actions=in.readInt();
    if(count!=3669||n!=23||actions!=13)throw new IOException("Invalid Indrik V3 header");
    groups=new String[n];for(int i=0;i<n;i++)groups[i]=in.readUTF();
    colors=new int[count];part=new int[count];mask=new int[count];flags=new int[count];xyz=new byte[count*3];
    for(int i=0;i<count;i++){in.readFully(xyz,i*3,3);part[i]=in.readUnsignedByte();mask[i]=in.readUnsignedByte();flags[i]=in.readUnsignedByte();in.readUnsignedByte();colors[i]=in.readInt();if(part[i]>=n||mask[i]==0||mask[i]>63)throw new IOException("Invalid cuboid");}
    frames=new float[actions][][];
    for(int a=0;a<actions;a++){int size=in.readInt();if(size!=41)throw new IOException("Invalid frame count");frames[a]=new float[size][n*12];for(float[] f:frames[a])for(int i=0;i<f.length;i++){f[i]=in.readFloat();if(!Float.isFinite(f[i]))throw new IOException("Non-finite transform");}}
    if(in.read()!=-1)throw new IOException("Trailing data");
    support=buildSupport();undergroundCubes=java.util.stream.IntStream.range(0,count).filter(i->(flags[i]&2)!=0).toArray();
   }
  }catch(IOException e){throw new IllegalStateException("Cannot load Indrik V3",e);}
 }
 // Keep only the extreme X coordinates for each (bone,Y,Z) row of local voxel corners.
 // Every discarded point is on a segment between retained points, so any affine pose
 // followed by a world yaw has the same extrema (apart from float rounding).
 private int[][] buildSupport(){
  List<Map<Integer,int[]>> rows=new ArrayList<>();for(int i=0;i<groups.length;i++)rows.add(new HashMap<>());
  for(int i=0;i<count;i++)for(int[] corner:CORNERS){
   int x=xyz[i*3]+corner[0],y=xyz[i*3+1]+corner[1],z=xyz[i*3+2]+corner[2],key=((y+128)<<9)|(z+128);
   int[] range=rows.get(part[i]).computeIfAbsent(key,k->new int[]{x,x});range[0]=Math.min(range[0],x);range[1]=Math.max(range[1],x);
  }
  int[][] out=new int[groups.length][];
  for(int g=0;g<out.length;g++){
   var row=rows.get(g);int[] data=new int[row.size()*6];int k=0;
   for(var entry:row.entrySet()){
    int y=(entry.getKey()>>9)-128,z=(entry.getKey()&511)-128;
    int[] r=entry.getValue();data[k++]=r[0];data[k++]=y;data[k++]=z;
    if(r[1]!=r[0]){data[k++]=r[1];data[k++]=y;data[k++]=z;}
   }
   out[g]=Arrays.copyOf(data,k);
  }
  return out;
 }
 public int supportCount(){int n=0;for(int[] points:support)n+=points.length/3;return n;}
 public float[] sample(int action,float progress){
  float[] out=new float[groups.length*12];sampleInto(action,progress,out);return out;
 }
 public void sampleInto(int action,float progress,float[] out){
  float[][] list=frames[Math.clamp(action,0,frames.length-1)];float f=Math.clamp(progress,0F,1F)*(list.length-1);int a=(int)f,b=Math.min(a+1,list.length-1);float t=f-a;
  for(int i=0;i<out.length;i++)out[i]=list[a][i]+(list[b][i]-list[a][i])*t;
 }
 public void bounds(int action,float progress,float[] pose,float[] scratch,double x0,double y0,double z0,double c,double s,double[][] lo,double[][] hi){
  for(int g=0;g<groups.length;g++){Arrays.fill(lo[g],Double.POSITIVE_INFINITY);Arrays.fill(hi[g],Double.NEGATIVE_INFINITY);}
  if(action!=IndrikRules.BURROW){
   for(int g=0;g<support.length;g++){
    int b=g*12;int[] points=support[g];
    for(int k=0;k<points.length;k+=3){
     float x=points[k],y=points[k+1],z=points[k+2];
     float vx=-IndrikRules.SCALE*(pose[b]*x+pose[b+1]*y+pose[b+2]*z+pose[b+3]);
     float vy=IndrikRules.SCALE*(pose[b+4]*x+pose[b+5]*y+pose[b+6]*z+pose[b+7]);
     float vz=-IndrikRules.SCALE*(pose[b+8]*x+pose[b+9]*y+pose[b+10]*z+pose[b+11]);
     expand(lo[g],hi[g],x0+vx*c-vz*s,y0+vy,z0+vx*s+vz*c);
    }
   }
  }else{
   // The partially buried silhouette depends on each cube's visibility.
   boolean underground=IndrikRules.underground(progress);int n=underground?undergroundCubes.length:count;
   for(int j=0;j<n;j++){int i=underground?undergroundCubes[j]:j;cube(i,pose,scratch);if(!visible(i,action,progress,scratch))continue;int g=part[i];
    for(int k=0;k<24;k+=3)expand(lo[g],hi[g],x0+scratch[k]*c-scratch[k+2]*s,Math.max(y0,y0+scratch[k+1]),z0+scratch[k]*s+scratch[k+2]*c);
   }
  }
 }
 private static void expand(double[] lo,double[] hi,double x,double y,double z){
  lo[0]=Math.min(lo[0],x);lo[1]=Math.min(lo[1],y);lo[2]=Math.min(lo[2],z);
  hi[0]=Math.max(hi[0],x);hi[1]=Math.max(hi[1],y);hi[2]=Math.max(hi[2],z);
 }
 public void cube(int i,float[] pose,float[] out){
  int b=part[i]*12;for(int k=0;k<8;k++){float x=xyz[i*3]+CORNERS[k][0],y=xyz[i*3+1]+CORNERS[k][1],z=xyz[i*3+2]+CORNERS[k][2];int o=k*3;
   out[o]=-IndrikRules.SCALE*(pose[b]*x+pose[b+1]*y+pose[b+2]*z+pose[b+3]);
   out[o+1]=IndrikRules.SCALE*(pose[b+4]*x+pose[b+5]*y+pose[b+6]*z+pose[b+7]);
   out[o+2]=-IndrikRules.SCALE*(pose[b+8]*x+pose[b+9]*y+pose[b+10]*z+pose[b+11]);
  }
 }
 public boolean visible(int i,int a,float progress,float[] v){if(a!=IndrikRules.BURROW)return true;if(IndrikRules.underground(progress)&&(flags[i]&2)==0)return false;for(int k=1;k<24;k+=3)if(v[k]>=0)return true;return false;}
}
