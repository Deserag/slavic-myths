package org.slavicmyths.client;

/** Pure local lash deformation; authored grip, UV and server hit remain unchanged. */
public final class WhipMotion {
 public record Point(double x,double y,double z){}
 private static final double[] Y={5.0,6.855165123780746,8.765838102031957,10.755846432588008,12.74585476314406,14.656527741395273,16.41169286517602,17.94137723974499,19.18459717628632,20.091789419137477,20.626787076386652,21.526787076386654,23.526787076386654};
 private static final double[] X={8.0,7.041148922791594,6.450108509468915,6.250441676175259,6.450108509468916,7.041148922791596,8.000000000000002,9.288435374475384,10.85508919373035,12.637503913853221,14.564620284687606,16.564620284687606,14.564620284687606};
 private static double ease(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
 private static double angle(double phase,double along){
  double local=Math.clamp((phase-.08*along)/(1-.08*along),0,1);
  if(local<.20)return -.85*ease(local/.20);
  if(local<.45)return -.85+2.35*ease((local-.20)/.25);
  if(local<.72)return 1.50-.50*ease((local-.45)/.27);
  return 1.0-ease((local-.72)/.28);
 }
 private static double referenceX(double y){
  for(int i=1;i<Y.length;i++)if(y<=Y[i])return X[i-1]+(X[i]-X[i-1])*Math.clamp((y-Y[i-1])/(Y[i]-Y[i-1]),0,1);
  return X[X.length-1];
 }
 public static Point point(double x,double y,double z,float phase,float sway){
  if(y<=5)return new Point(x,y,z);
  phase=Math.clamp(phase,0,1);
  double along=Math.clamp((y-5)/19.526787076386654,0,1),yy=5,zz=8;
  double extension=phase>0&&phase<1?Math.pow(Math.sin(Math.PI*phase),2):0;
  double step=(18+102*extension)*along/16;
  for(int i=0;i<16;i++){
   double u=along*(i+.5)/16;
   double a=angle(phase,u)+restBend(u)*(1-extension)+sway*.09*u;
   yy-=Math.cos(a)*step;zz-=Math.sin(a)*step;
  }
  double xx=8+(x-referenceX(y))*.22+.65*Math.sin(along*Math.PI)*sway;
  // Rotate thickness with the tangent as well. Flipping only Y would turn
  // every lash cube inside out and invert its visible winding at rest.
  double tangent=angle(phase,along)+restBend(along)*(1-extension)+sway*.09*along;
  return new Point(xx,yy+(z-8)*.22*Math.sin(tangent),zz-(z-8)*.22*Math.cos(tangent));
 }
 /** The free end first clears the top collar, then hangs away from the wooden grip. */
 private static double restBend(double along){return .22+1.65*Math.exp(-12*along);}
 private WhipMotion(){}
}
