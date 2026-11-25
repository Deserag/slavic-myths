package org.slavicmyths.desert;
import java.io.*;
import java.util.zip.GZIPInputStream;
/** Exact vertices of V2, exported from tools/sand_king/prototype.py, shared by server and client. */
public final class SandKingGeometry {
 public static final int IDLE=0,WALK=1,STING=2,CLAW=3,JUMP=4,COMBO=5,SLAM=6;
 public static final SandKingGeometry INSTANCE=new SandKingGeometry();
 public final int count;
 public final String[] groups;
 public final int[] colors,part;
 private final float[][][] frames;
 private final int tip;
 private final int[] mirroredGroups;
 private static final int[] TIP_CORNERS={2,3,6,7};
 private SandKingGeometry(){
  try(var raw=SandKingGeometry.class.getResourceAsStream("/assets/slavicmyths/sand_king/geometry.bin.gz")){
   if(raw==null)throw new IOException("Missing Sand King V2 geometry");
   try(var in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)))){
    if(in.readInt()!=0x534B5632)throw new IOException("Wrong Sand King geometry format");
    count=in.readInt();int n=in.readInt(),actions=in.readInt();
    if(count!=327||n<1||n>40||actions!=7)throw new IOException("Invalid geometry header");
    groups=new String[n];colors=new int[count];part=new int[count];
    for(int i=0;i<n;i++)groups[i]=in.readUTF();
    int last=-1;
    for(int i=0;i<count;i++){colors[i]=in.readInt();part[i]=in.readUnsignedShort();if(part[i]>=n)throw new IOException("Invalid geometry group");if(groups[part[i]].equals("stinger"))last=i;}
    if(last<0)throw new IOException("Missing stinger");tip=last;
    frames=new float[actions][][];
    for(int a=0;a<actions;a++){int size=in.readInt();if(size<1||size>100)throw new IOException("Invalid keyframes");frames[a]=new float[size][count*24];for(float[] frame:frames[a])for(int j=0;j<frame.length;j++){frame[j]=in.readFloat();if(!Float.isFinite(frame[j]))throw new IOException("Non-finite vertex");}}
    if(in.read()!=-1)throw new IOException("Unexpected geometry data");
    mirroredGroups=new int[n];for(int i=0;i<n;i++)mirroredGroups[i]=findMirroredGroup(i);
   }
  }catch(IOException ex){throw new IllegalStateException("Cannot load Sand King geometry",ex);}
 }
 public float[] sample(int action,float progress,boolean mirror){
  float[] out=new float[count*24];sampleInto(action,progress,mirror,out);return out;
 }
 public void sampleInto(int action,float progress,boolean mirror,float[] out){
  if(action<0||action>=frames.length)action=IDLE;
  var list=frames[action];float f=Math.max(0,Math.min(1,progress))*(list.length-1);
  int a=(int)f,b=Math.min(a+1,list.length-1);float blend=f-a;
  if(!mirror){for(int i=0;i<out.length;i++)out[i]=list[a][i]+(list[b][i]-list[a][i])*blend;}
  else for(int i=0;i<out.length;i+=3){
   out[i]=-(list[a][i]+(list[b][i]-list[a][i])*blend);
   out[i+1]=list[a][i+1]+(list[b][i+1]-list[a][i+1])*blend;
   out[i+2]=list[a][i+2]+(list[b][i+2]-list[a][i+2])*blend;
  }
 }
 public double[] tip(float[] vertices){double[] v=new double[3];for(int k:TIP_CORNERS)for(int j=0;j<3;j++)v[j]+=vertices[tip*24+k*3+j]*.25;return v;}
 /** Sample only the four stinger corners instead of the full pose. */
 public void tipInto(int action,float progress,boolean mirror,double[] out){
  if(action<0||action>=frames.length)action=IDLE;
  var list=frames[action];float f=Math.max(0,Math.min(1,progress))*(list.length-1);
  int a=(int)f,b=Math.min(a+1,list.length-1);float blend=f-a;
  java.util.Arrays.fill(out,0);
  for(int k:TIP_CORNERS)for(int j=0;j<3;j++){int i=tip*24+k*3+j;float v=list[a][i]+(list[b][i]-list[a][i])*blend;out[j]+=(mirror&&j==0?-v:v)*.25;}
 }
 public float multiplier(int index){String g=groups[index];return g.equals("head")?1.5F:g.equals("stinger")?1.25F:g.startsWith("claw")?.6F:g.startsWith("leg")?.8F:1F;}
 public int groupForSide(int index,boolean mirror){return mirror?mirroredGroups[index]:index;}
 private int findMirroredGroup(int index){String g=groups[index];if(g.startsWith("claw")||g.startsWith("leg")){String other=g.contains("-1")?g.replace("-1","1"):g.replaceFirst("1","-1");for(int i=0;i<groups.length;i++)if(groups[i].equals(other))return i;}return index;}
}
