package org.slavicmyths.accessories;

/** Small shared, testable policies; ticks use the persistent overworld clock. */
public final class AccessoryRules {
    public static final int BATH_CD=1200, HARVEST_CD=100, PODVEY_CD=1200, LESHY_CD=1200, SPARK_CD=160;
    public static boolean ready(long now,long until){return now>=until;}
    public static int bathDuration(int ticks,boolean ordinary,boolean protectedEffect){
        return ordinary&&!protectedEffect&&ticks>0?Math.max(1,(int)Math.floor(ticks*.8)):ticks;
    }
    public static float fall(float damage,float distance,boolean ready){
        return damage>0&&distance>=12&&ready?damage*.3F:damage;
    }
    public static boolean forest(float finalDamage,boolean forest,boolean hostile){return finalDamage>=6&&forest&&hostile;}
    public static boolean harvest(boolean manual,boolean mature,boolean edible,boolean rune,float random){
        return manual&&mature&&edible&&!rune&&random<.1F;
    }
    public static boolean spark(boolean melee,boolean hostile,boolean immune,float random){return melee&&hostile&&!immune&&random<.25F;}
    private AccessoryRules(){}
}
