package org.slavicmyths.nethersmithing;
import java.util.*;import net.minecraft.world.item.ArmorItem;
/** Owner HP/rune rules; first-set protection/durability and jump remain documented engineering baselines. */
public final class NetherArmorRules {
 public static final List<String> IDS=List.of("zhara_helmet","zhara_chestplate","zhara_leggings","zhara_boots","infernal_crown_helmet","infernal_king_cuirass","infernal_king_leggings","infernal_king_boots");
 public static String id(boolean king,ArmorItem.Type type){return switch(type){case HELMET->king?IDS.get(4):IDS.get(0);case CHESTPLATE->king?IDS.get(5):IDS.get(1);case LEGGINGS->king?IDS.get(6):IDS.get(2);case BOOTS->king?IDS.get(7):IDS.get(3);default->throw new IllegalArgumentException("Not a player armor slot");};}
 public static int sockets(boolean king,ArmorItem.Type type){return king||type==ArmorItem.Type.CHESTPLATE||type==ArmorItem.Type.LEGGINGS?3:2;}
 public static double hp(boolean king,ArmorItem.Type type){return switch(type){case HELMET->king?3:1;case CHESTPLATE->king?6:2;case LEGGINGS->king?4:1;case BOOTS->king?2:1;default->0;};}
 public static int durability(boolean king,ArmorItem.Type type){int base=switch(type){case HELMET->363;case CHESTPLATE->528;case LEGGINGS->495;case BOOTS->429;default->0;};return (int)Math.ceil(base*(king?1.5:1.15));}
 public static double speed(boolean leggings,boolean full){return leggings?.15+(full?.05:0):0;}
 private NetherArmorRules(){}
}
