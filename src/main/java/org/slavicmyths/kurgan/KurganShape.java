package org.slavicmyths.kurgan;
/** Smooth compact mound profile, independently testable without a world. */
public final class KurganShape {
 public static final int[] RADIUS={14,24,40},HEIGHT={10,18,30};
 public static int height(int kind,int x,int z){double r=RADIUS[kind],q=(x*x+z*z)/(r*r);return q>=1?0:(int)Math.round(HEIGHT[kind]*(1-q)*(1-q));}
 public static int kind(long seed,int x,int z){long h=seed^((long)x*341873128712L)^((long)z*132897987541L);h=(h^(h>>>33))*0xff51afd7ed558ccdL;int roll=(int)Math.floorMod(h^(h>>>33),100);return roll<60?0:roll<90?1:2;}
 private KurganShape(){}
}
