package org.slavicmyths.cubeaudit;
import com.mojang.blaze3d.vertex.*;
import org.slavicmyths.client.GuardianClient;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
/** Native VertexConsumer default transforms exercised, no GL required after registry bootstrap. */
public final class VertexAudit {
 static class Sink implements VertexConsumer {
  long hash;int vertices;List<Integer> values;
  Sink(boolean capture){if(capture)values=new ArrayList<>();}
  void put(int x){hash=hash*31+x;if(values!=null)values.add(x);}
  void f(float x){put(Float.floatToIntBits(x));}
  public VertexConsumer addVertex(float x,float y,float z){vertices++;f(x);f(y);f(z);return this;}
  public VertexConsumer setColor(int r,int g,int b,int a){put(r);put(g);put(b);put(a);return this;}
  public VertexConsumer setUv(float u,float v){f(u);f(v);return this;}
  public VertexConsumer setUv1(int u,int v){put(u);put(v);return this;}
  public VertexConsumer setUv2(int u,int v){put(u);put(v);return this;}
  public VertexConsumer setNormal(float x,float y,float z){f(x);f(y);f(z);return this;}
 }
 static volatile long blackhole;
 public static void run(PrintWriter out){
  Random random=new Random(330015);int cases=0;
  for(int i=0;i<1024;i++){
   PoseStack p=new PoseStack();if(i%2==1){p.translate(.123,-4.5,6.7);p.mulPose(com.mojang.math.Axis.YP.rotationDegrees(37));p.scale(.7f,1.4f,-.8f);}
   double x=random.nextDouble()*1e4-5e3,y=random.nextDouble()*20-10,z=random.nextDouble()*20-10,w=i%7==0?0:random.nextDouble()*4-2,h=random.nextDouble()*4-2,d=random.nextDouble()*4-2;int color=random.nextInt();
   Sink a=new Sink(true),b=new Sink(true);ReferenceCube.cube(p,a,x,y,z,w,h,d,color);GuardianClient.cube(p,b,x,y,z,w,h,d,color);
   if(a.vertices!=24||b.vertices!=24||!a.values.equals(b.values))throw new AssertionError("Vertex mismatch case "+i);cases++;
  }
  out.println("VERTEX_PARITY PASS cases="+cases+" bitwise all attributes 24 vertices, identity+translated/rotated/nonuniform negative scale");out.flush();
  var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);long thread=Thread.currentThread().threadId();PoseStack p=new PoseStack();Sink sink=new Sink(false);
  for(int i=0;i<100000;i++){ReferenceCube.cube(p,sink,i*.00001,.1,-.2,1,2,3,0xAA72D9F2);GuardianClient.cube(p,sink,i*.00001,.1,-.2,1,2,3,0xAA72D9F2);}
  for(int round=0;round<6;round++)for(int kind=0;kind<2;kind++){int k=round%2==0?kind:1-kind;long a=bean.getThreadAllocatedBytes(thread),n=System.nanoTime();for(int i=0;i<250000;i++)if(k==0)ReferenceCube.cube(p,sink,i*.00001,.1,-.2,1,2,3,0xAA72D9F2);else GuardianClient.cube(p,sink,i*.00001,.1,-.2,1,2,3,0xAA72D9F2);long elapsed=System.nanoTime()-n,bytes=bean.getThreadAllocatedBytes(thread)-a;blackhole=sink.hash;out.println("MICRO round="+round+" kind="+(k==0?"reference":"production")+" calls=250000 ns="+elapsed+" allocated_bytes="+bytes);out.flush();}
 }
}
