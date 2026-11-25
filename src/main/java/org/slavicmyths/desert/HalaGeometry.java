package org.slavicmyths.desert;
import java.io.*;
import java.util.zip.GZIPInputStream;
/** Exact approved V2 cuboids and animation keyframes, shared by server and client. */
public final class HalaGeometry {
 public static final HalaGeometry INSTANCE=new HalaGeometry();
 public final int count,tip;
 public final String[] groups;
 public final int[] colors,part;
 private final float[][][] frames;
 private HalaGeometry(){
  try(var raw=HalaGeometry.class.getResourceAsStream("/assets/slavicmyths/hala/geometry.bin.gz")){
   if(raw==null)throw new IOException("Missing Hala V2 geometry");
   try(var in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)))){
    if(in.readInt()!=0x48414C32)throw new IOException("Invalid Hala format");
    count=in.readInt();int n=in.readInt(),actions=in.readInt();tip=in.readInt();
    if(count!=2781||n<9||n>20||actions!=12||tip<0||tip>=count)throw new IOException("Invalid Hala header");
    groups=new String[n];colors=new int[count];part=new int[count];
    for(int i=0;i<n;i++)groups[i]=in.readUTF();
    for(int i=0;i<count;i++){colors[i]=in.readInt();part[i]=in.readUnsignedShort();if(part[i]>=n)throw new IOException("Invalid group");}
    frames=new float[actions][][];
    for(int a=0;a<actions;a++){int size=in.readInt();if(size<1||size>20)throw new IOException("Invalid frame count");frames[a]=new float[size][count*24];for(float[] f:frames[a])for(int i=0;i<f.length;i++){f[i]=in.readFloat();if(!Float.isFinite(f[i]))throw new IOException("Non-finite geometry");}}
    if(in.read()!=-1)throw new IOException("Trailing geometry data");
   }
  }catch(IOException e){throw new IllegalStateException("Cannot load Hala V2",e);}
 }
 public float[] sample(int action,float progress){
  var list=frames[Math.clamp(action,0,frames.length-1)];float f=Math.clamp(progress,0,1)*(list.length-1);int a=(int)f,b=Math.min(a+1,list.length-1);
  if(a==b)return list[a].clone();
  float[] v=new float[count*24];float t=f-a;for(int i=0;i<v.length;i++)v[i]=list[a][i]+(list[b][i]-list[a][i])*t;return v;
 }
 public double[] tip(float[] vertices){double[] p=new double[3];for(int k=0;k<8;k++)for(int j=0;j<3;j++)p[j]+=vertices[tip*24+k*3+j]/8D;return p;}
}
