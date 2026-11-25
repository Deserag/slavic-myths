package org.slavicmyths.desert;
import java.io.*;
import java.util.zip.GZIPInputStream;
/** Collision bounds from the same V6 frames as the client mesh. */
public final class LamyaGeometry {
 public static final LamyaGeometry INSTANCE=new LamyaGeometry();
 public final String[] groups;
 private final float[][][] frames;
 private LamyaGeometry(){
  try(var in=open("bounds.bin.gz")){
   if(in.readInt()!=0x4c414236)throw new IOException("Lamya bounds magic");
   int n=in.readInt(),a=in.readInt(),k=in.readInt();
   if(n!=20||a!=LamyaRules.LENGTH.length||k!=9)throw new IOException("Lamya bounds header");
   groups=new String[n];for(int i=0;i<n;i++)groups[i]=in.readUTF();
   frames=new float[a][k][n*6+9];
   for(var action:frames)for(var frame:action)for(int i=0;i<frame.length;i++){frame[i]=in.readFloat();if(!Float.isFinite(frame[i]))throw new IOException("Lamya nonfinite bound");}
   if(in.read()!=-1)throw new IOException("Trailing bounds");
  }catch(IOException ex){throw new IllegalStateException("Cannot load Lamya bounds",ex);}
 }
 public static DataInputStream open(String name)throws IOException{
  var raw=LamyaGeometry.class.getResourceAsStream("/assets/slavicmyths/lamya/"+name);
  if(raw==null)throw new IOException("Missing Lamya "+name);
  return new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)));
 }
 public float[] sample(int a,float p){
  var f=frames[a];float x=Math.clamp(p,0,1)*(f.length-1);int i=(int)x,j=Math.min(i+1,f.length-1);float[] r=new float[f[i].length];
  for(int k=0;k<r.length;k++)r[k]=f[i][k]+(f[j][k]-f[i][k])*(x-i);return r;
 }
}
