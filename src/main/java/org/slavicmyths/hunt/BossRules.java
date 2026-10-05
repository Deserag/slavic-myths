package org.slavicmyths.hunt;
import java.util.Random;
public final class BossRules {
 public static final long RESPAWN=7L*24000;
 public enum Move {
  IDLE(0,0,0),SWEEP(14,12,50),GRAB(18,26,180),HOLD(20,10,0),GAZE(26,16,200),GROUND(24,18,150),CHASE(80,40,220),LEAP(18,26,160),PUNCH(14,10,24),CHARGE(12,20,120),SPIN(12,28,170),SLAM(20,18,130),STONE(16,14,100),BREATH(22,18,150),RECOVERY(0,10,0),TRANSITION(0,18,0);
  public final int windup,recovery,cooldown;Move(int w,int r,int c){windup=w;recovery=r;cooldown=c;}
 }
 public static int phase(BossKind k,float fraction){return fraction<=(k==BossKind.LIKHO?.30F:.35F)?3:fraction<=(k==BossKind.LIKHO?.65F:.70F)?2:1;}
 public static int cooldown(BossKind k,Move m,int phase){if(m==Move.SPIN&&phase==3)return 150;double factor=k==BossKind.LIKHO&&phase==3?.85:k==BossKind.TUGARIN&&phase>=2?.9:1;return (int)Math.ceil(m.cooldown*factor);}
 public static long seed(long world,int rx,int rz,BossKind kind){return world^rx*341873128712L^rz*132897987541L^(kind==BossKind.LIKHO?0x71C9A32BL:0x7A6A912DL);}
 public static boolean eligibleRegion(long world,int rx,int rz,BossKind kind){return kind==BossKind.TUGARIN||new Random(seed(world,rx,rz,kind)).nextDouble()<.35;}
 public static int[] candidate(long seed,int rx,int rz,int index){Random r=new Random(seed);int shift=r.nextInt(64),odd=2*r.nextInt(32)+1,cell=(index*odd+shift)&63;return new int[]{rx*512+(cell%8)*64+16+r.nextInt(32),rz*512+(cell/8)*64+16+r.nextInt(32)};}
 private BossRules(){}
}
