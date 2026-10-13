package org.slavicmyths.rewards;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.rpg.Runes;
/** The existing data component and socket costs remain authoritative. */
public final class RewardRunes {
 public static boolean supports(ItemStack s,String id){return switch(id){case "stomp"->s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.BOOTS;case "dispelling"->s.getItem() instanceof ArmorItem a&&(a.getType()==ArmorItem.Type.HELMET||a.getType()==ArmorItem.Type.CHESTPLATE);case "battle_line"->Runes.armor(s);case "pursuit","suppression"->java.util.Set.of("sword","dagger","spear","heavy").contains(Runes.category(s));default->false;};}
 public static boolean active(LivingEntity e,String id){if(id.equals("pursuit")||id.equals("suppression"))return supports(e.getMainHandItem(),id)&&Runes.has(e.getMainHandItem(),id);for(var s:e.getArmorSlots())if(supports(s,id)&&Runes.has(s,id))return true;return false;}
 private RewardRunes(){}
}
