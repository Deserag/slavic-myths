package org.slavicmyths.gorynych;
/** Working 1.9.6 balance, centralized for subsequent rebalance. */
public final class GorynychRules {
 public static final float HP=2800,TRANSFORM_HP=1500,FLOOR=75;
 public static final String BALANCE="GorynychBalance1982";
 public static final int BALANCE_VERSION=1;
 /** Entity and ledger use the same one-time logical-scale migration. State/clean are untouched. */
 public static boolean migrate(net.minecraft.nbt.CompoundTag n,String hpKey,String stateKey){
  if(n.getInt(BALANCE)>=BALANCE_VERSION)return false;
  int state=n.getInt(stateKey);float hp=n.contains(hpKey)?org.slavicmyths.combat.CreatureHealthBalance.proportional(n.getFloat(hpKey),3600,HP):HP;
  if(state==EXHAUSTED||state==CLEANSING)hp=FLOOR;
  else if(state==PURIFIED)hp=HP;
  n.putFloat(hpKey,Math.clamp(hp,FLOOR,HP));n.putInt(BALANCE,BALANCE_VERSION);return true;
 }
 public static final int PATROL=0,GROUND=1,TRANSFORM=2,AIR=3,EXHAUSTED=4,CLEANSING=5,PURIFIED=6;
 public static final int TRANSFORM_TICKS=120,CLEANSE_TICKS=160,GRAB_MAX=60,GRAB_IMMUNITY=240,CLOUD_LIFE=240,CLOUD_USES=3,SHED_INTERVAL=72000;
 public static float remaining(float hp,float damage){return Math.max(FLOOR,hp-Math.max(0,damage));}
 public static int clean(int mask,int head){return head<0||head>2?mask:mask|1<<head;}
 public static boolean canClean(int state,int mask,int head){return (state==EXHAUSTED||state==CLEANSING)&&head>=0&&head<3&&(mask&1<<head)==0;}
 private GorynychRules(){}
}
