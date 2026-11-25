package org.slavicmyths.desert;

import java.util.*;
import static java.lang.Math.*;
import static org.slavicmyths.desert.AntRules.*;

/** Procedural cuboids ported from the approved Python prototype, in block units.
 * Intermediate coordinates are (width, longitudinal, height); output is (X,Y,Z).
 * Rendering and multipart collision use this exact same pose. */
public final class AntGeometry {
 public static final int D=0x38252c,B=0x281d24,M=0x632b31,R=0xa53639,L=0xc14a43,S=0x79282e,T=0xd7ad79,I=0xf3d9ac,E=0xff4b28,G=0xfff0a3;
 public static final String[] GROUPS={"head","thorax","abdomen","jaws","leg0","leg1","leg2","leg3","leg4","leg5"};
 public record Box(double[] c,double[] d,double[][] q,int color,int group) {
  public float[] vertices(){float[] out=new float[24];verticesInto(out);return out;}
  public void verticesInto(float[] out){
   for(int k=0;k<8;k++){
    double x=SIGNS[k][0]*d[0]/2,y=SIGNS[k][1]*d[1]/2,z=SIGNS[k][2]*d[2]/2;
    out[k*3]=(float)(c[0]+(q[0][0]*x+q[0][1]*y+q[0][2]*z));
    out[k*3+1]=(float)(c[2]+(q[2][0]*x+q[2][1]*y+q[2][2]*z));
    out[k*3+2]=(float)-(c[1]+(q[1][0]*x+q[1][1]*y+q[1][2]*z));
   }
  }
 }
 private static final int[][] SIGNS={{-1,-1,-1},{1,-1,-1},{1,1,-1},{-1,1,-1},{-1,-1,1},{1,-1,1},{1,1,1},{-1,1,1}};
 // Only static authored ellipsoids and armor call these caches. Pose-dependent
 // legs, antennae, jaws and wing transforms are still evaluated continuously.
 private record Template(int group,double x,double y,double z,double rx,double ry,double rz,double step,int color,boolean belly,int plates){}
 private static final Map<Template,List<Box>> TEMPLATES=new java.util.concurrent.ConcurrentHashMap<>();
 private static Template key(int group,double[] c,double[] r,double step,int color,boolean belly,int plates){
  return new Template(group,c[0],c[1],c[2],r[0],r[1],r[2],step,color,belly,plates);
 }
 static double[] v(double x,double y,double z){return new double[]{x,y,z};}
 static double[] add(double[] a,double[] b){return v(a[0]+b[0],a[1]+b[1],a[2]+b[2]);}
 static double[] sub(double[] a,double[] b){return v(a[0]-b[0],a[1]-b[1],a[2]-b[2]);}
 static double[] scale(double[] a,double s){return v(a[0]*s,a[1]*s,a[2]*s);}
 static double[] cross(double[] a,double[] b){return v(a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]);}
 static double norm(double[] a){return sqrt(a[0]*a[0]+a[1]*a[1]+a[2]*a[2]);}
 static double[][] rot(char axis,double a){double c=cos(a),s=sin(a);return switch(axis){case 'x'->new double[][]{{1,0,0},{0,c,-s},{0,s,c}};case 'y'->new double[][]{{c,0,s},{0,1,0},{-s,0,c}};default->new double[][]{{c,-s,0},{s,c,0},{0,0,1}};};}
 static double[] mul(double[][] a,double[] b){return v(a[0][0]*b[0]+a[0][1]*b[1]+a[0][2]*b[2],a[1][0]*b[0]+a[1][1]*b[1]+a[1][2]*b[2],a[2][0]*b[0]+a[2][1]*b[1]+a[2][2]*b[2]);}
 static double[][] mm(double[][] a,double[][] b){double[][] r=new double[3][3];for(int i=0;i<3;i++)for(int j=0;j<3;j++)for(int k=0;k<3;k++)r[i][j]+=a[i][k]*b[k][j];return r;}
 static final double[][] ID=rot('x',0);
 static final double[] ZERO=v(0,0,0);
 static class Mesh {
  final List<Box> b=new ArrayList<>();int g;
  Mesh(int group){g=group;}
  void box(double[] c,double[] d,int col){box(c,d,col,ID);}
  void box(double[] c,double[] d,int col,double[][] q){b.add(new Box(c,d,q,col,g));}
  void beam(double[] a,double[] b,double w,double h,int col){double[] u=sub(b,a);double ln=norm(u);if(ln<1e-8)return;u=scale(u,1/ln);double[] x=cross(u,v(0,0,1));x=norm(x)>1e-6?scale(x,1/norm(x)):v(1,0,0);double[] z=cross(x,u);box(scale(add(a,b),.5),v(w,ln+.012,h),col,new double[][]{{x[0],u[0],z[0]},{x[1],u[1],z[1]},{x[2],u[2],z[2]}});}
  void addMesh(Mesh m,double[][] q,double[] pivot,double[] shift){
   for(Box x:m.b){
    double[] c=x.c;double dx=c[0]-pivot[0],dy=c[1]-pivot[1],dz=c[2]-pivot[2];
    double[] center=v(((q[0][0]*dx+q[0][1]*dy+q[0][2]*dz)+pivot[0])+shift[0],
     ((q[1][0]*dx+q[1][1]*dy+q[1][2]*dz)+pivot[1])+shift[1],
     ((q[2][0]*dx+q[2][1]*dy+q[2][2]*dz)+pivot[2])+shift[2]);
    b.add(new Box(center,x.d,x.q==ID?q:mm(q,x.q),x.color,x.group));
   }
  }
  void ell(double[] c,double[] r,double step,int col,boolean belly){
   b.addAll(TEMPLATES.computeIfAbsent(key(g,c,r,step,col,belly,0),unused->{Mesh template=new Mesh(g);template.buildEll(c,r,step,col,belly);return List.copyOf(template.b);}));
  }
  private void buildEll(double[] c,double[] r,double step,int col,boolean belly){int ny=(int)ceil(r[1]/step),nz=(int)ceil(r[2]/step);for(int j=-ny;j<=ny;j++)for(int k=-nz;k<=nz;k++){double f=1-pow(j*step/r[1],2)-pow(k*step/r[2],2);if(f<0)continue;int nx=(int)(r[0]*sqrt(f)/step);box(add(c,v(0,j*step,k*step)),v((2*nx+1)*step,step,step),belly&&k*step<-.42*r[2]?0x71503e:col);}}
 }
 static void armor(Mesh m,double[] c,double[] r,double step,int n){
  m.b.addAll(TEMPLATES.computeIfAbsent(key(m.g,c,r,step,0,false,n),unused->{Mesh template=new Mesh(m.g);buildArmor(template,c,r,step,n);return List.copyOf(template.b);}));
 }
 private static void buildArmor(Mesh m,double[] c,double[] r,double step,int n){for(int j=0;j<n;j++){double y=-.79*r[1]+1.58*r[1]*j/(n-1),f=sqrt(1-pow(y/r[1],2)),dy=1.58*r[1]/(n-1)*.83;for(int i=0;i<7;i++){double a=-1.38+2.76*i/6;m.box(add(c,v(r[0]*f*sin(a),y,r[2]*f*cos(a))),v(max(step,r[0]*f*.49),dy,step*.85),(j+i)%5==0?L:R,rot('y',a));}if(j==1||j==3)m.box(add(c,v(0,y,r[2]*f+step*.5)),v(step*1.1,dy*.65,step*.48),T);}}
 public static List<Box> pose(int kind,int act,double t){t=max(0,min(1,t));return kind==QUEEN?queen(t,act):small(t,act,kind==FLYING);}
 static List<Box> queen(double t,int act){
  double p=2*PI*t,a=sin(PI*t),hit=max(0,1-abs(t-.64)/.20),fall=act==DEATH?t*t*(3-2*t):0;
  Mesh m=new Mesh(1),root=new Mesh(1),abd=new Mesh(2);double[] ac=v(0,1.77,1.65);
  abd.ell(ac,v(1.21,1.48,1.24),.19,D,true);armor(abd,ac,v(1.27,1.53,1.29),.17,6);
  for(int s:new int[]{-1,1})for(int j=0;j<4;j++){double y=.75+j*.57,f=sqrt(max(.1,1-pow((y-1.77)/1.58,2)));abd.box(v(s*1.19*f,y,1.63),v(.16,.39,.49),S);abd.box(v(s*1.1*f,y,1.24),v(.13,.31,.25),M);}
  root.ell(v(0,.43,1.4),v(.43,.48,.43),.17,B,true);double sweep=act==ABDOMEN?.95*hit-.40*sin(PI*min(1,t/.42)):0,tilt=act==SUMMON||act==AIR_SUMMON?-.28*a:.025*sin(p)*(1-fall);
  root.addMesh(abd,mm(rot('z',sweep),rot('x',tilt)),v(0,.48,1.42),ZERO);
  root.ell(v(0,-.50,1.51),v(.72,.98,.65),.17,D,true);armor(root,v(0,-.50,1.51),v(.74,.96,.67),.14,4);root.ell(v(0,-1.42,1.73),v(.37,.46,.36),.14,M,false);
  Mesh h=new Mesh(0);h.ell(v(0,-2.02,1.94),v(.73,.66,.61),.15,D,false);h.ell(v(0,-2.06,2.12),v(.75,.58,.47),.15,S,false);h.box(v(0,-2.43,2.12),v(.34,.39,.67),M,rot('x',-.20));h.box(v(0,-2.58,1.95),v(.29,.19,.31),R);
  for(int s:new int[]{-1,1}){
   h.box(v(s*.48,-2.37,2.30),v(.48,.46,.19),R,mm(rot('y',s*.28),rot('z',s*.23)));h.box(v(s*.67,-2.08,2.13),v(.22,.53,.37),M,rot('z',s*.19));h.box(v(s*.61,-2.44,1.85),v(.30,.35,.23),S,rot('z',s*.27));
   h.box(v(s*.46,-2.65,2.13),v(.34,.10,.19),B,rot('y',s*.25));h.box(v(s*.46,-2.71,2.13),v(.27,.045,.12),E,rot('y',s*.25));h.box(v(s*.43,-2.74,2.14),v(.10,.035,.074),G,rot('y',s*.25));
   Mesh j=new Mesh(3);double[][] pts={v(s*.43,-2.53,1.83),v(s*.65,-2.77,1.64),v(s*.68,-3.04,1.42),v(s*.53,-3.31,1.29),v(s*.22,-3.49,1.30)};
   double[] w={.30,.28,.23,.14},hh={.34,.29,.24,.14};for(int k=0;k<4;k++)j.beam(pts[k],pts[k+1],w[k],hh[k],k%2==0?T:I);for(int k=1;k<=2;k++)j.beam(pts[k],add(pts[k],v(-s*.22,-.13,.02)),.13,.13,I);
   h.addMesh(j,rot('z',s*(act==BITE?.35*a-.40*hit:.06*sin(p)*(1-fall))),pts[0],ZERO);h.beam(v(s*.22,-2.56,1.76),v(s*.16,-2.77,1.37),.115,.15,S);
   double[][] q={v(s*.41,-2.05,2.40),v(s*.61,-2.10,2.92),v(s*.91,-2.35,3.40),v(s*1.10,-2.73,3.20),v(s*1.26,-3.03,2.83),v(s*1.30,-3.12,2.52)};
   for(int k=1;k<6;k++){q[k][0]+=s*.07*sin(p+s)*(1-fall);if(k>=2)q[k][2]+=.05*sin(p+s)*(1-fall)-.35*fall;}
   int[] col={M,R,D,S,T};for(int k=0;k<5;k++)h.beam(q[k],q[k+1],.17-.017*k,.17-.017*k,col[k]);
  }
  double hp=act==BITE?.10*a-.27*hit:act==LEGS||act==SUMMON||act==AIR_SUMMON||act==PHEROMONE?-.15*a:.02*sin(p)*(1-fall);
  root.addMesh(h,rot('x',hp),v(0,-1.47,1.76),v(0,act==BITE?-.22*hit:0,0));
  double[][] legs={{-1.15,-1.91,-2.53},{-.53,-.63,-.82},{.16,.75,1.78}};
  for(int s:new int[]{-1,1})for(int i=0;i<3;i++){
   root.g=4+(s==1?3:0)+i;double hy=legs[i][0],ky=legs[i][1],fy=legs[i][2],ph=p+(i%2)*PI+(s==1?0:PI),lift=act==WALK?.33*max(0,sin(ph)):0,stride=act==WALK?.48*cos(ph):0,extra=act==LEGS&&i==0?.65*a:0,slam=act==LEGS&&i==0?.42*hit:0;
   double[] hip=v(s*.57,hy,1.52),knee=v(s*(1.38+.12*i+.48*fall),ky+.4*stride,1.94+.20*i+extra-.72*fall),ank=v(s*(2.02+.12*i+.75*fall),fy+stride-.45*slam,.35+lift+extra*.55+.37*fall),foot=add(ank,v(s*.12,-.20-.17*slam,-.29));
   root.box(hip,v(.40,.40,.40),M);root.beam(hip,knee,.29,.35,D);root.beam(add(hip,scale(sub(knee,hip),.39)),knee,.40,.43,S);root.box(knee,v(.36,.37,.36),R);root.beam(knee,ank,.25,.29,D);root.beam(add(knee,scale(sub(ank,knee),.17)),add(knee,scale(sub(ank,knee),.56)),.39,.40,R);root.beam(add(knee,scale(sub(ank,knee),.66)),add(knee,scale(sub(ank,knee),.81)),.30,.32,S);root.beam(ank,foot,.20,.22,T);
  }
  double rp=act==LEGS?-.10*a:act==SUMMON||act==AIR_SUMMON?.07*a:act==HURT?-.12*hit:0;
  m.addMesh(root,rot('x',rp),v(0,0,1.42),v(0,act==HURT?.10*hit:0,act==DEATH?-.37*fall:act==WALK?.045*sin(2*p):.025*sin(p)));return m.b;
 }
 static List<Box> small(double t,int act,boolean fly){
  Mesh m=new Mesh(1),r=new Mesh(1);double p=2*PI*t,a=sin(PI*t),hit=max(0,1-abs(t-.64)/.2);
  r.g=2;r.ell(v(0,.265,.30),v(.19,.225,.17),.058,M,true);armor(r,v(0,.265,.30),v(.20,.23,.18),.04,3);r.g=1;r.ell(v(0,.015,.27),v(.07,.10,.07),.045,D,false);r.ell(v(0,-.10,.29),v(.12,.16,.12),.05,M,false);r.box(v(0,-.10,.39),v(.17,.19,.07),R);
  Mesh h=new Mesh(0);h.ell(v(0,-.29,.33),v(.15,.14,.13),.05,S,false);h.box(v(0,-.345,.38),v(.24,.16,.10),M);
  for(int s:new int[]{-1,1}){
   h.box(v(s*.09,-.425,.345),v(.065,.025,.057),E);h.box(v(s*.09,-.44,.35),v(.026,.012,.03),G);
   Mesh j=new Mesh(3);double[][] q={v(s*.10,-.39,.29),v(s*.147,-.46,.24),v(s*.087,-.54,.21)};j.beam(q[0],q[1],.072,.08,T);j.beam(q[1],q[2],.056,.057,I);h.addMesh(j,rot('z',s*(act==BITE||act==DIVE?.42*a-.5*hit:.08*sin(p))),q[0],ZERO);
   q=new double[][]{v(s*.08,-.31,.43),v(s*.12,-.35,.59),v(s*.19,-.46,.56),v(s*.22,-.52,.47)};
   for(int k=1;k<4;k++)q[k][0]+=.014*sin(p+s);for(int k=0;k<3;k++)h.beam(q[k],q[k+1],.035-k*.004,.038-k*.005,k==1?M:D);
  }
  r.addMesh(h,rot('x',act==BITE||act==DIVE?.16*a-.24*hit:.04*sin(p)),v(0,-.18,.30),ZERO);
  boolean air=fly&&(act==FLIGHT||act==DIVE||act==TAKEOFF||act==LAND||act==HURT);
  double[][] legs={{-.21,-.30,-.40},{-.10,-.12,-.14},{.02,.11,.25}};
  for(int s:new int[]{-1,1})for(int i=0;i<3;i++){r.g=4+(s==1?3:0)+i;double ph=p+(i%2)*PI+(s==1?0:PI),stride=act==WALK?.105*cos(ph):0,lift=act==WALK?.075*max(0,sin(ph)):0;double[] hip=v(s*.10,legs[i][0],.28),knee=v(s*.23,legs[i][1]+stride*.4,air?.20:.34),foot=v(s*(air?.19:.33),legs[i][2]+stride,air?.11:.025+lift);r.beam(hip,knee,.058,.063,M);r.beam(knee,foot,.042,.05,D);r.box(knee,v(.065,.063,.065),S);}
  if(fly)for(int s:new int[]{-1,1})for(int i=0;i<2;i++){
   Mesh w=new Mesh(1);double[] anchor=v(s*.09,-.11+.12*i,.40);double ln=i==0?.39:.28;
   for(int k=0;k<6;k++){double f=(k+.5)/6,x=s*(.045+ln*f),y=-.025+f*.18+.095*i,wid=(i==0?.095:.075)*(1-.55*f);w.box(v(x,y,0),v(ln/6+.009,wid,.012),0xc9d9d5);w.box(v(x,y-wid*.40,.009),v(ln/6+.009,.012,.009),0x789991);}
   double flap=air?.18+1.12*sin(6*p+i*.5):.18;r.addMesh(w,mm(rot('y',-s*flap),rot('z',s*(air?.12:.68))),ZERO,anchor);
  }
  double pitch=act==DIVE?.48*hit:act==TAKEOFF?-.15*a:act==LAND?.12*a:act==HURT?-.22*a:0;
  if(act==DEATH)m.addMesh(r,rot('y',1.55*t),v(0,0,.26),v(0,0,-.04*t));
  else m.addMesh(r,rot('x',pitch),v(0,0,.28),v(0,0,act==EMERGE?-.47*(1-t):0));
  return m.b;
 }
 private AntGeometry(){}
}
