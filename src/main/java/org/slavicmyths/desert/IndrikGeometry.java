package org.slavicmyths.desert;
import java.io.*;
import java.util.zip.GZIPInputStream;
/** Exact V3 articulated shell and sampled 23-bone IK transforms. */
public final class IndrikGeometry {
 public static final IndrikGeometry INSTANCE=new IndrikGeometry();
 public final int count;
 public final String[] groups;
 public final int[] colors,part,mask,flags;
 private final byte[] xyz;
 private final float[][][] frames;
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
   }
  }catch(IOException e){throw new IllegalStateException("Cannot load Indrik V3",e);}
 }
 public float[] sample(int action,float progress){
  float[][] list=frames[Math.clamp(action,0,frames.length-1)];float f=Math.clamp(progress,0F,1F)*(list.length-1);int a=(int)f,b=Math.min(a+1,list.length-1);float t=f-a;float[] out=new float[list[0].length];
  for(int i=0;i<out.length;i++)out[i]=list[a][i]+(list[b][i]-list[a][i])*t;return out;
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
