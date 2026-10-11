package org.slavicmyths.earth;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slavicmyths.rpg.Runes;
@EventBusSubscriber(modid="slavicmyths")
public final class EarthRune {
    public static final String ID="earth_heart_iii";
    public static List<Component> tooltip(){return List.of(Component.translatable("earth.slavicmyths.rune_tier"),Component.translatable("earth.slavicmyths.rune_hp"),Component.translatable("earth.slavicmyths.rune_damage"),Component.translatable("earth.slavicmyths.rune_kb"),Component.translatable("earth.slavicmyths.armor_only"));}
    public static int count(LivingEntity e){int n=0;for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){ItemStack s=e.getItemBySlot(slot);if(Runes.armor(s)&&Runes.compatible(s,ID)&&Runes.has(s,ID))n++;}return n;}
    public static void refresh(LivingEntity e){if(e.level().isClientSide)return;int n=count(e);apply(e,Attributes.MAX_HEALTH,"earth_rune_health",n*EarthRules.RUNE_HP);apply(e,Attributes.KNOCKBACK_RESISTANCE,"earth_rune_knockback",n*EarthRules.RUNE_KB);if(e.getHealth()>e.getMaxHealth())e.setHealth(e.getMaxHealth());}
    private static void apply(LivingEntity e,net.minecraft.core.Holder<Attribute> attr,String key,double value){var a=e.getAttribute(attr);if(a==null)return;var id=EarthContent.id(key);a.removeModifier(id);if(value>0)a.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));}
    @SubscribeEvent public static void equip(LivingEquipmentChangeEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof LivingEntity l)refresh(l);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide||e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)||e.getNewDamage()<=0)return;e.setNewDamage((float)(e.getNewDamage()*EarthRules.runeDamage(count(e.getEntity()))));}
    private EarthRune(){}
}
