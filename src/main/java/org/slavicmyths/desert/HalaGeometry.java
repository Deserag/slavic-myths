package org.slavicmyths.desert;
import java.io.*;
import java.util.zip.GZIPInputStream;
/** Exact approved V2 cuboids and animation keyframes, loaded by the client; collision data is separate. */
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
  float[] v=new float[count*24];sampleInto(action,progress,v);return v;
 }
 public void sampleInto(int action,float progress,float[] v){
  var list=frames[Math.clamp(action,0,frames.length-1)];float f=Math.clamp(progress,0,1)*(list.length-1);int a=(int)f,b=Math.min(a+1,list.length-1);
  if(a==b){System.arraycopy(list[a],0,v,0,v.length);return;}
  float t=f-a;for(int i=0;i<v.length;i++)v[i]=list[a][i]+(list[b][i]-list[a][i])*t;
 }
 public double[] tip(float[] vertices){double[] p=new double[3];for(int k=0;k<8;k++)for(int j=0;j<3;j++)p[j]+=vertices[tip*24+k*3+j]/8D;return p;}
 /** Independently initialized collision-only data, sampled at integer game ticks. */
 public static final class Collision {
  public static final Collision INSTANCE=new Collision();
  public final String[] groups;
  private final Frame[][] frames;
  public record Hull(float minY,float maxY,float[] xz){}
  public record Frame(Hull[] hulls,double tipX,double tipY,double tipZ){}
  private Collision(){
   try(var raw=Collision.class.getResourceAsStream("/assets/slavicmyths/hala/collision.bin.gz")){
    if(raw==null)throw new IOException("Missing Hala collision resource");
    try(var in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)))){
     if(in.readInt()!=0x48434c31)throw new IOException("Invalid Hala collision magic");
     int n=in.readInt(),actions=in.readInt();
     if(n<9||n>20||actions!=HalaRules.LENGTH.length)throw new IOException("Invalid Hala collision header");
     groups=new String[n];for(int i=0;i<n;i++)groups[i]=in.readUTF();
     frames=new Frame[actions][];
     for(int a=0;a<actions;a++){
      int size=in.readInt();if(size!=HalaRules.LENGTH[a]+1)throw new IOException("Invalid Hala collision tick count");
      frames[a]=new Frame[size];
      for(int t=0;t<size;t++){
       Hull[] hulls=new Hull[n];
       for(int g=0;g<n;g++){
        float lo=in.readFloat(),hi=in.readFloat();int points=in.readInt();
        if(!Float.isFinite(lo)||!Float.isFinite(hi)||lo>hi||points<1||points>2781*8)throw new IOException("Invalid Hala hull");
        float[] xz=new float[points*2];for(int k=0;k<xz.length;k++){xz[k]=in.readFloat();if(!Float.isFinite(xz[k]))throw new IOException("Invalid Hala hull point");}
        hulls[g]=new Hull(lo,hi,xz);
       }
       double x=in.readDouble(),y=in.readDouble(),z=in.readDouble();
       if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IOException("Invalid Hala staff anchor");
       frames[a][t]=new Frame(hulls,x,y,z);
      }
     }
     if(in.read()!=-1)throw new IOException("Trailing Hala collision data");
    }
   }catch(IOException ex){throw new IllegalStateException("Cannot load Hala collision frames",ex);}
  }
  public Frame sample(int action,float progress){
   int a=Math.clamp(action,0,frames.length-1);return frames[a][Math.round(Math.clamp(progress,0,1)*(frames[a].length-1))];
  }
 }
}
