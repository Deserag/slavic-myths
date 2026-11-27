package org.slavicmyths.savanna;
public final class SkarapeyaRules {
 public static final int HP=2400,IDLE=0,BITE=1,GRAB=2,COIL=3,TAIL=4,CHARGE=5,RING=6,SPIT=7,TRANSFORM=8,SLASH=9,SPIN=10,BLADE_WAVE=11,DUAL=12,THROW=13,HOROVOD=14,DOUBLE=15,PULSE=16,GROUND=17,RAIN=18,GAZE=19,BLOOD=20,ULTIMATE=21;
 public static final int[] LENGTH={1,34,65,112,38,48,70,55,60,44,64,52,54,76,200,132,80,100,170,58,66,600};
 public static final int[] CD={0,60,360,500,180,300,500,400,0,100,220,260,160,320,480,440,360,500,500,560,600,1300};
 public static final int[] COLOR={0x79B52E,0x9470B4,0xD8B63E,0x71845B,0x497FBA,0xA0A9A8,0xA52E38};
 public static int phase(float hp){return hp<=600?3:hp<=1440?2:1;}
 public static boolean blade(int a){return a>=SLASH&&a<=THROW;}
 public static boolean split(int a){return a==DUAL||a==THROW;}
 public static boolean compatible(int a,int b){return a!=b&&!(a==2&&b==5||a==5&&b==2||a==4&&b==5||a==5&&b==4);}
 private SkarapeyaRules(){}
}
