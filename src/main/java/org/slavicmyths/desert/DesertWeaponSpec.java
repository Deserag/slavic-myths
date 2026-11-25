package org.slavicmyths.desert;

/** Combat design values (HP and ticks), independent of rendering and registration. */
public enum DesertWeaponSpec {
    SCORPION_DOUBLE("scorpion_double_sting",Rank.COMMON,18,26,0,0),
    STINGER_DAGGER("stinger_dagger",Rank.COMMON,16,24,0,0),
    SAND_SLING("sand_sling",Rank.COMMON,16,28,0,0),
    GLASS_BLADE("desert_glass_blade",Rank.COMMON,20,28,0,0),
    HALA_STAFF("hala_solar_staff",Rank.RARE,24,36,46,0),
    DUNE_MACE("dune_mace",Rank.RARE,28,38,48,0),
    MIRAGE_BOW("mirage_bow",Rank.RARE,26,36,44,0),
    QUICKSAND_WHIP("quicksand_whip",Rank.RARE,22,30,42,0),
    COLONY_SCEPTER("colony_scepter",Rank.LEGENDARY,32,12,60,72),
    FOUR_WINDS("four_winds_fists",Rank.LEGENDARY,34,48,60,72),
    LAMYA_WEAPON("lamya_weapon",Rank.LEGENDARY,34,48,60,72),
    BURIED_HAMMER("buried_hammer",Rank.LEGENDARY,36,48,60,72);
    public enum Rank { COMMON, RARE, LEGENDARY }
    public final String id;
    public final Rank rank;
    public final int baseDamage,rightDamage,shiftRightDamage,shiftLeftDamage;
    DesertWeaponSpec(String id,Rank rank,int base,int right,int shiftRight,int shiftLeft){
        this.id=id;this.rank=rank;baseDamage=base;rightDamage=right;
        shiftRightDamage=shiftRight;shiftLeftDamage=shiftLeft;
    }
    public int cooldownTicks(boolean shiftRight,boolean shiftLeft){
        if(shiftRight&&shiftLeft)return 0;
        if(shiftLeft)return rank==Rank.LEGENDARY?700:0;
        if(shiftRight)return switch(rank){case COMMON->0;case RARE->300;case LEGENDARY->400;};
        return switch(rank){case COMMON->100;case RARE->160;case LEGENDARY->200;};
    }
    public static final int SCORPION_HIT=13,GLASS_DIRECT=22,GLASS_BLEED=6,
        MIRAGE_ARROW=12,WIND_HIT=15,COLONY_WARRIORS=4;
}
