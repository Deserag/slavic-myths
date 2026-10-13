package org.slavicmyths.gorynych;
/** Working 1.9.6 balance, centralized for subsequent rebalance. */
public final class GorynychRules {
 public static final float HP=3600,TRANSFORM_HP=1800,FLOOR=180;
 public static final int PATROL=0,GROUND=1,TRANSFORM=2,AIR=3,EXHAUSTED=4,CLEANSING=5,PURIFIED=6;
 public static final int TRANSFORM_TICKS=120,CLEANSE_TICKS=160,GRAB_MAX=60,GRAB_IMMUNITY=240,CLOUD_LIFE=240,CLOUD_USES=3,SHED_INTERVAL=72000;
 public static float remaining(float hp,float damage){return Math.max(FLOOR,hp-Math.max(0,damage));}
 public static int clean(int mask,int head){return head<0||head>2?mask:mask|1<<head;}
 public static boolean canClean(int state,int mask,int head){return (state==EXHAUSTED||state==CLEANSING)&&head>=0&&head<3&&(mask&1<<head)==0;}
 private GorynychRules(){}
}
