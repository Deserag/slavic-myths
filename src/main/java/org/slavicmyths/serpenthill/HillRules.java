package org.slavicmyths.serpenthill;
public final class HillRules {
 public static final int SIZE=128,HEIGHT=144,BUILD_BUDGET=4096,AURA_INTERVAL=30;
 public static final float AURA_DAMAGE=3;
 public static int base(int requested,int min,int max,int fallback){return requested<min||requested+HEIGHT>max?Math.clamp(fallback,min,max-HEIGHT):requested;}
 public static boolean dangerous(int x,int y,int z){return x>=80&&x<=113&&y>=0&&y<=19&&z>=76&&z<=98||x>=60&&x<=84&&y>=81&&y<=91&&z>=30&&z<=52;}
 public static int hillTop(int x,int z,long seed){double dx=(x-63.5)/61.,dz=(z-63.5)/61.,r=Math.sqrt(dx*dx+dz*dz);if(r>1)return -1;double body=88*Math.pow(1-r*r,.7);long hash=(seed+(x/6)*73428767L+(z/6)*912931L);hash=(hash^(hash>>>13))*1274126177L;int rough=Math.floorMod((int)(hash^(hash>>>27)),5)-2;return Math.max(0,((int)body+rough)/3*3);}
 private HillRules(){}
}
