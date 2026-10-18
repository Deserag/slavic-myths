import java.util.*;
public class CubeParity {
 static class PoseStack {static class Pose{} Pose last(){return new Pose();}}
 static class VertexConsumer {
  List<Integer> v=new ArrayList<>(); VertexConsumer f(float a){v.add(Float.floatToIntBits(a));return this;}
  VertexConsumer addVertex(PoseStack.Pose m,float x,float y,float z){return f(x).f(y).f(z);}
  VertexConsumer setColor(int c){v.add(c);return this;}
  VertexConsumer setUv(float u,float v){return f(u).f(v);}
  VertexConsumer setOverlay(int x){v.add(x);return this;}
  VertexConsumer setLight(int x){v.add(x);return this;}
  VertexConsumer setNormal(PoseStack.Pose m,float x,float y,float z){return f(x).f(y).f(z);}
 }
static class Baseline{ public static void cube(PoseStack pose,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color){var m=pose.last();float x0=(float)x,y0=(float)y,z0=(float)z,x1=(float)(x+w),y1=(float)(y+h),z1=(float)(z+d);float[][][] faces={{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}},{{x1,y0,z1},{x1,y1,z1},{x0,y1,z1},{x0,y0,z1}},{{x0,y0,z1},{x0,y1,z1},{x0,y1,z0},{x0,y0,z0}},{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}},{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},{{x0,y0,z1},{x0,y0,z0},{x1,y0,z0},{x1,y0,z1}}};float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};for(int f=0;f<6;f++)for(int i=0;i<4;i++){var v=faces[f][i];out.addVertex(m,v[0],v[1],v[2]).setColor(color).setUv(i==0||i==1?0:1,i==0||i==3?1:0).setOverlay(0).setLight(15728880).setNormal(m,normals[f][0],normals[f][1],normals[f][2]);}}
}
static class Candidate{ public static void cube(PoseStack pose,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color){
  var m=pose.last();
  float x0=(float)x,y0=(float)y,z0=(float)z,x1=(float)(x+w),y1=(float)(y+h),z1=(float)(z+d);
  face(m,out,color,0,0,-1,x0,y0,z0,x0,y1,z0,x1,y1,z0,x1,y0,z0);
  face(m,out,color,0,0,1,x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,y0,z1);
  face(m,out,color,-1,0,0,x0,y0,z1,x0,y1,z1,x0,y1,z0,x0,y0,z0);
  face(m,out,color,1,0,0,x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1);
  face(m,out,color,0,1,0,x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0);
  face(m,out,color,0,-1,0,x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1);
 }
 private static void face(PoseStack.Pose m,VertexConsumer out,int color,float nx,float ny,float nz,
   float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz){
  vertex(m,out,color,ax,ay,az,0,1,nx,ny,nz);
  vertex(m,out,color,bx,by,bz,0,0,nx,ny,nz);
  vertex(m,out,color,cx,cy,cz,1,0,nx,ny,nz);
  vertex(m,out,color,dx,dy,dz,1,1,nx,ny,nz);
 }
 private static void vertex(PoseStack.Pose m,VertexConsumer out,int color,float x,float y,float z,float u,float v,float nx,float ny,float nz){
  out.addVertex(m,x,y,z).setColor(color).setUv(u,v).setOverlay(0).setLight(15728880).setNormal(m,nx,ny,nz);
 }

}
public static void main(String[] args){Random r=new Random(430037);for(int i=0;i<10000;i++){double x=r.nextDouble()*200-100,y=r.nextDouble()*200-100,z=r.nextDouble()*200-100,w=i%3==0?0:r.nextDouble()*20-10,h=r.nextDouble()*20-10,d=r.nextDouble()*20-10;int c=r.nextInt();var a=new VertexConsumer();var b=new VertexConsumer();Baseline.cube(new PoseStack(),a,x,y,z,w,h,d,c);Candidate.cube(new PoseStack(),b,x,y,z,w,h,d,c);if(!a.v.equals(b.v)||a.v.size()!=24*11)throw new AssertionError("vertex parity "+i+" "+a.v.size());}System.out.println("PASS 10000 extracted-production cube cases; no Minecraft classes or GL; coordinate/UV/normal/order/color/light/overlay parity");}}
