package org.slavicmyths.client;

import java.util.*;
import static java.lang.Math.*;

/** Procedural port of the Python V1 voxel construction, with the approved V2 robe and wind changes.
 * Coordinates are prototype voxels, Y up and Z negative in front. Only exposed faces are retained. */
final class StormIdolMesh {
 static final int BODY=0,HEAD=1,CROWN=2,WAIST=3,CLOTH=4,FIST=10,CHARM=20;
 static final int DARK=0x333444,WOOD=0x474755,WOODLIGHT=0x666675,MASK=0xc6b18d,EDGE=0xe3cfaa,GOLD=0xa98a59,GOLDLIGHT=0xd7b17b,STONE=0x9b9aa3,KNUCKLE=0xc5bfb6,SHADOW=0x626579,LINEN=0xe3d2b1,RED=0xa83d39,REDSHADOW=0x672e32,CYAN=0x8ae8ff,EYE=0xd1fbff,BLUE=0x3386c0;
 record V(int x,int y,int z){}
 record Cell(int color,int bone){}
 record Face(float[] vertices,int color,int bone){}
 record Strand(double[][] points,double[] width,double[] depth,int seed){}
 static final int[][] DIR={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
 static final int[][][] CORNERS={{{1,0,0},{1,1,0},{1,1,1},{1,0,1}},{{0,0,0},{0,0,1},{0,1,1},{0,1,0}},{{0,1,0},{0,1,1},{1,1,1},{1,1,0}},{{0,0,0},{1,0,0},{1,0,1},{0,0,1}},{{0,0,1},{1,0,1},{1,1,1},{0,1,1}},{{0,0,0},{0,1,0},{1,1,0},{1,0,0}}};
 static final float[] SHADE={.82F,.91F,1.12F,.65F,.74F,1};
 static final double[][] FIST_CENTERS={{-28,48,-4},{28,48,-4},{-28,29,-4},{28,29,-4}};
 static final List<Face> FACES;
 static final List<Strand> HAIR;
 static final int SURFACE_CUBES;
 private final Map<V,Cell> cells=new LinkedHashMap<>();
 private final List<Strand> strands=new ArrayList<>();
 static {StormIdolMesh m=new StormIdolMesh();m.build();List<Face> faces=new ArrayList<>();int surface=0;
  for(var entry:m.cells.entrySet()){V v=entry.getKey();Cell c=entry.getValue();boolean visible=false;
   for(int j=0;j<6;j++){int[] d=DIR[j];Cell adjacent=m.cells.get(new V(v.x+d[0],v.y+d[1],v.z+d[2]));
    if(adjacent!=null&&adjacent.bone==c.bone)continue;
    visible=true;float[] a=new float[12];for(int k=0;k<4;k++){int[] q=CORNERS[j][k];a[3*k]=v.x+q[0];a[3*k+1]=v.y+q[1];a[3*k+2]=v.z+q[2];}
    float shade=(c.color==CYAN||c.color==EYE)?1:SHADE[j]*(.93F+.14F*((v.hashCode()&255)/255F));
    faces.add(new Face(a,shade(c.color,shade),c.bone));
   }if(visible)surface++;
  }FACES=List.copyOf(faces);HAIR=List.copyOf(m.strands);SURFACE_CUBES=surface;
 }
 static int shade(int c,float s){return 0xff000000|(min(255,(int)(((c>>16)&255)*s))<<16)|(min(255,(int)(((c>>8)&255)*s))<<8)|min(255,(int)((c&255)*s));}
 void box(int x0,int x1,int y0,int y1,int z0,int z1,int c,int b){for(int x=x0;x<x1;x++)for(int y=y0;y<y1;y++)for(int z=z0;z<z1;z++)cells.put(new V(x,y,z),new Cell(c,b));}
 void ell(double x,double y,double z,double rx,double ry,double rz,double power,int c,int b){
  for(int i=(int)floor(x-rx);i<ceil(x+rx);i++)for(int j=(int)floor(y-ry);j<ceil(y+ry);j++)for(int k=(int)floor(z-rz);k<ceil(z+rz);k++)
   if(pow(abs((i+.5-x)/rx),power)+pow(abs((j+.5-y)/ry),power)+pow(abs((k+.5-z)/rz),power)<=1)cells.put(new V(i,j,k),new Cell(c,b));
 }
 static double[] sub(double[] a,double[] b){return new double[]{a[0]-b[0],a[1]-b[1],a[2]-b[2]};}
 static double[] cross(double[] a,double[] b){return new double[]{a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]};}
 static double dot(double[] a,double[] b){return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];}
 static double[] mul(double[] a,double s){return new double[]{a[0]*s,a[1]*s,a[2]*s};}
 static double[] unit(double[] a){double l=sqrt(dot(a,a));return l<1e-8?new double[]{0,1,0}:mul(a,1/l);}
 void seg(double[] a,double[] b,double w,double d,int color,int bone){
  double[] v=sub(b,a);double l=sqrt(dot(v,v));v=unit(v);double[] u=cross(v,new double[]{0,0,1});if(dot(u,u)<.000001)u=cross(v,new double[]{0,1,0});u=unit(u);double[] q=cross(v,u);double r=max(w,d)/2+1;
  for(int x=(int)floor(min(a[0],b[0])-r);x<ceil(max(a[0],b[0])+r);x++)for(int y=(int)floor(min(a[1],b[1])-r);y<ceil(max(a[1],b[1])+r);y++)for(int z=(int)floor(min(a[2],b[2])-r);z<ceil(max(a[2],b[2])+r);z++){
   double[] dv={x+.5-a[0],y+.5-a[1],z+.5-a[2]};double t=dot(dv,v);if(t>=-.35&&t<=l+.35&&abs(dot(dv,u))<=w/2&&abs(dot(dv,q))<=d/2)cells.put(new V(x,y,z),new Cell(color,bone));
  }
 }
 void chain(double[][] p,double[] w,double d,int c,int b){for(int i=0;i<p.length-1;i++)seg(p[i],p[i+1],w[min(i,w.length-1)],d,c,b);}
 static double[][] offset(double[][] p,double y,double z){double[][] q=new double[p.length][3];for(int i=0;i<p.length;i++)q[i]=new double[]{p[i][0],p[i][1]+y,p[i][2]+z};return q;}
 void ornament(int x,int y,int z,String[] pattern,int bone){for(int j=0;j<pattern.length;j++)for(int i=0;i<pattern[j].length();i++)if(pattern[j].charAt(i)=='1'){int xx=x+i-pattern[j].length()/2,yy=y+pattern.length/2-j;box(xx,xx+1,yy,yy+1,z,z+1,RED,bone);}}
 void hair(double[][] p,double[] w,double[] d){strands.add(new Strand(p,w,d,strands.size()));}
 void build(){
  ell(0,38,0,7.6,9,4.3,3,WOOD,BODY);ell(0,43,0,9,5.4,4.4,3,WOODLIGHT,BODY);ell(0,33,0,5.5,5,3.4,3,DARK,WAIST);
  box(-2,2,35,45,-5,-3,DARK,BODY);box(-1,1,37,44,-6,-5,MASK,BODY);box(0,1,38,43,-7,-6,CYAN,BODY);box(-2,2,43,45,-6,-4,GOLDLIGHT,BODY);
  for(int s:new int[]{-1,1}){
   for(int k=0;k<3;k++){double y=44-k*3;double[][] pts={{0,y,-4.5},{s*3,y-1,-5.3},{s*6,y-2.5,-4.5},{s*(8-k),y-3.3,-2.5}};chain(pts,new double[]{2.1},2,GOLD,BODY);chain(offset(pts,.7,-.55),new double[]{1.05},1.05,GOLDLIGHT,BODY);}
   ell(s*8,43,-.5,2.5,2.3,2.6,4,SHADOW,BODY);seg(new double[]{s*6.5,45,-1.7},new double[]{s*10,43.5,-1.7},1.8,3,GOLD,BODY);
   seg(new double[]{s*5,36,-4},new double[]{s*7,32,-3},1.8,1.7,GOLD,WAIST);
  }
  box(-2,2,35,46,3,5,DARK,BODY);for(int y:new int[]{36,39,42,45})box(-3,3,y,y+1,5,6,GOLD,BODY);
  for(int i=0;i<14;i++){double t=2*PI*i/14;ell(5.9*cos(t),31.5,3.8*sin(t),1.9,1.35,1.2,5,i%2==0?GOLDLIGHT:GOLD,WAIST);}
  box(-2,2,29,34,-5,-3,MASK,WAIST);box(0,1,30,33,-6,-5,CYAN,WAIST);
  box(-2,2,46,50,-2,2,WOODLIGHT,HEAD);ell(0,53.5,0,5.3,6.8,3.7,3.1,DARK,HEAD);box(-4,4,54,57,-4,-3,WOOD,HEAD);
  for(int s:new int[]{-1,1}){int x0=s<0?-4:1,x1=s<0?-1:4;box(x0,x1,54,56,-5,-4,BLUE,HEAD);box(x0+1,x1,55,56,-6,-5,EYE,HEAD);seg(new double[]{s*.5,57,-5},new double[]{s*4.7,56,-4.6},1.7,1.5,GOLDLIGHT,HEAD);seg(new double[]{s*4.5,54,-3.5},new double[]{s*3.7,48,-3.5},1.3,1.6,MASK,HEAD);}
  box(-1,1,48,60,-5,-3,MASK,HEAD);box(-1,1,49,56,-6,-5,EDGE,HEAD);box(-3,3,57,62,-4,-2,MASK,HEAD);box(-3,3,58,63,-5,-4,EDGE,HEAD);
  ornament(0,60,-6,new String[]{"00100","10101","01110","11111","01110","10101","00100"},HEAD);
  for(int s:new int[]{-1,1}){
   double[][] p={{s*3,59.5,0},{s*8,59,0},{s*13,60,0},{s*18,62,0},{s*22,65,0},{s*24,63,0}};
   chain(p,new double[]{3.4,3,3,3.3,3},3.1,GOLD,CROWN);chain(offset(p,.8,-.7),new double[]{1.3},1.5,MASK,CROWN);
   for(int[] a:new int[][]{{s*9,59},{s*18,62}}){int x=a[0],y=a[1];box(x-1,x+2,y-2,y+3,-3,2,EDGE,CROWN);ornament(x,y,-4,new String[]{"010","111","010"},CROWN);}
   for(int j=0;j<2;j++){int x=s*(j==0?13:20),y=j==0?59:62,n=j==0?5:6,b=CHARM+(s>0?2:0)+j;for(int k=0;k<n;k++)box(x,x+1,y-k-1,y-k,0,1,GOLDLIGHT,b);box(x-1,x+2,y-n-2,y-n,-1,2,MASK,b);box(x-1,x+2,y-n-4,y-n-2,0,1,REDSHADOW,b);box(x,x+1,y-n-5,y-n-2,-1,0,RED,b);}
   for(int k=0;k<4;k++){
    hair(new double[][]{{s*(1.7+k*.85),61-k*.7,.1+k*.7},{s*(5+k*.8),59-k*1.15,-1.5+k*.75},{s*(7.5+k*.8),55-k*1.4,-2+k*.8},{s*(9+k*1.4),51-k*1.6,-2+k*.8},{s*(13+k*1.5),50-k*1.9,-.5+k*.7},{s*(15+k*1.5),52-k*1.6,1+k*.6}},new double[]{1.8,2.2,2.2,2,1.4,.25},new double[]{1.7,1.9,1.8,1.5,1,.2});
    hair(new double[][]{{s*(1+k),60-k*.5,2.8},{s*(2+k*1.5),56,4.8+k*.45},{s*(4+k*1.5),51-k*.4,6.1+k*.6},{s*(6+k*1.7),46-k*.9,7.2+k*.6},{s*(10+k*1.9),44-k*1.2,7+k*.6},{s*(13+k*1.8),47-k*.9,5+k*.5}},new double[]{1.8,2.5,2.5,2.4,1.5,.2},new double[]{1.8,2,2,1.7,1,.2});
   }
  }
  for(int k=0;k<3;k++){double x=(k-1)*2.1;hair(new double[][]{{x,60,3.8},{x-.5,56,6.5},{x+1,52,8.2},{x-.8,48,8.7},{x-2,44-k,8.3},{x-4,42-k,7.2}},new double[]{2,2.6,2.2,1.9,1.3,.2},new double[]{1.4,1.8,1.7,1.6,1,.15});}
  hair(new double[][]{{0,59,2},{-1,63,2},{-3,66,1},{-7,67,1},{-9,65,1}},new double[]{4,3,2.6,1.4,.2},new double[]{2.2,1.7,1.3,.9,.1});
  hair(new double[][]{{1,59,3},{3,63,3},{6,65,3},{8,68,3}},new double[]{3.6,3.1,1.7,.15},new double[]{2,1.6,1,.1});
  // Opaque dark cloth under the embroidered strips: the narrow cyclone ends beneath this hem.
  for(int y=17;y<31;y++){double t=(31-y)/14.;double rx=4.2+4.1*t,rz=3.1+2.8*t;for(int x=(int)-ceil(rx);x<=ceil(rx);x++)for(int z=(int)-ceil(rz);z<=ceil(rz);z++){
   double q=pow((x+.5)/rx,2)+pow((z+.5)/rz,2);if(q<=1&&q>=.64)box(x,x+1,y,y+1,z,z+1,WOODLIGHT,CLOTH);
  }}
  for(int s:new int[]{-1,1})for(int kind=0;kind<3;kind++)for(int j=0;j<16;j++){
   int y=31-j,w=j<12?5:5-(j-12)/2;
   int x=(int)round(s*(3.2+pow(j/15.,1.6)*(kind==0?2:7))),z;
   if(kind==0){z=(int)round(-5-j*.14);x=s*2+(int)(s*j*.08);}else if(kind==1){z=(int)round(-2+j*.15);x+=s*3;}else z=(int)round(4+j*.20);
   for(int i=0;i<w;i++){if(j==15&&i%2==0)continue;int xx=x+i-w/2;boolean pattern=j==1||j==11||j==13||((i==0||i==w-1)&&j%3==0)||((j==4||j==8)&&i==w/2)||((j==5||j==7)&&abs(i-w/2)==1)||(j==6&&(i==0||i==w/2||i==w-1));box(xx,xx+1,y,y+1,z,z+1,pattern?RED:LINEN,CLOTH);}
  }
  for(int i=0;i<4;i++){int s=i%2==0?-1:1,x=(int)FIST_CENTERS[i][0],y=(int)FIST_CENTERS[i][1],z=-4,b=FIST+i;
   ell(x,y,z,3.4,3.3,2.7,3.5,SHADOW,b);box(x-2,x+3,y-2,y+2,z-3,z+1,STONE,b);
   for(int j=0;j<4;j++){int yy=y-3+j*2,xout=x+s*(j==0||j==3?1:2);ell(xout,yy+.7,z-2.6,2.05,.94,1.85,5,KNUCKLE,b);box(xout-1,xout+1,yy,yy+1,z-5,z-4,EDGE,b);if(s>0)box(x+2,x+4,yy,yy+1,z-3,z,STONE,b);else box(x-4,x-2,yy,yy+1,z-3,z,STONE,b);}
   chain(new double[][]{{x-s*2,y+2,z-1},{x-s*3.5,y+1,z-3},{x-s*2,y-.5,z-4}},new double[]{2.3,2.1},2.2,MASK,b);box(x-1,x+1,y+2,y+3,z+1,z+3,GOLD,b);
  }
 }
 private StormIdolMesh(){}
}
