package org.slavicmyths.navigation;

/** Display-only arithmetic, independent of server targets, chunks, rendering and persistence. */
public final class SearchCoordinates {
 public static final long WORLD_LIMIT=29_999_984L;
 public static boolean valid(double x,double z,int radius){return Double.isFinite(x)&&Double.isFinite(z)&&Math.abs(x)<=30_001_000&&Math.abs(z)<=30_001_000&&radius>0&&radius<=30_001_000;}
 public static String number(double value){return java.math.BigDecimal.valueOf(value).setScale(1,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('-', '−');}
 public static long lower(double center,int radius){return Math.max(-WORLD_LIMIT,Math.min(WORLD_LIMIT,(long)Math.floor(center-radius)));}
 public static long upper(double center,int radius){return Math.max(-WORLD_LIMIT,Math.min(WORLD_LIMIT,(long)Math.ceil(center+radius)));}
 public static boolean clipped(double x,double z,int r){return x-r< -WORLD_LIMIT||x+r>WORLD_LIMIT||z-r< -WORLD_LIMIT||z+r>WORLD_LIMIT;}
 public static boolean inside(double x,double z,int radius,double px,double pz){return Double.isFinite(px)&&Double.isFinite(pz)&&Math.hypot(px-x,pz-z)<=radius;}
 public static long distance(double x,double z,int radius,double px,double pz){return (long)Math.ceil(Math.max(0,Math.hypot(px-x,pz-z)-radius));}
 public static int visibleRows(int top,int bottom){return Math.max(1,(bottom-top)/12);}
 public static int scroll(int offset,int total,int visible){return Math.max(0,Math.min(offset,Math.max(0,total-visible)));}
 private SearchCoordinates(){}
}
