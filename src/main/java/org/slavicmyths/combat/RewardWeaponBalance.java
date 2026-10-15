package org.slavicmyths.combat;

import java.util.Map;
import net.minecraft.world.item.Tier;

/** Approved ordinary raw hits, before crits, enchantments, runes and distinct abilities. */
public final class RewardWeaponBalance {
    public static final int ATAMAN=12, NIGHTINGALE=12, LIKHO=13, TUGARIN=14, VICTOR=15;
    public static final int FROST_BOLT=16, FROST_PULSE=4, SCYTHE=20, KALIN=18, CROSSBOW=16, WHIP=24;
    // Existing server configs store an absolute legacy value; preserve their +/- adjustment.
    public static final int LEGACY_SCYTHE_CONFIG=12;
    public static final Map<String,Integer> RAW=Map.of(
        "ataman_flail",ATAMAN,"nightingale_dagger",NIGHTINGALE,"likho_staff",LIKHO,
        "tugarin_sword",TUGARIN,"victor_spear",VICTOR,"morozko_staff",FROST_BOLT,
        "karachun_scythe",SCYTHE,"trophy_kalin_sabre",KALIN,"three_breath_crossbow",CROSSBOW,"poloz_whip",WHIP);
    public static float swordParameter(Tier tier,int total){return total-1-tier.getAttackDamageBonus();}
    private RewardWeaponBalance(){}
}
