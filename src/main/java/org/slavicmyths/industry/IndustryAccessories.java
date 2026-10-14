package org.slavicmyths.industry;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.Runes;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@EventBusSubscriber(modid="slavicmyths")
public final class IndustryAccessories {
    public static final DeferredHolder<Item,Item> GOGGLES=register("miners_goggles","head"),RING=register("prospectors_ring","ring"),AMULET=register("stone_path_amulet","necklace"),BELT=register("underground_guard_belt","belt");
    private static final TagKey<net.minecraft.world.level.block.Block> ORES=BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c","ores"));
    private static final ThreadLocal<Boolean> OUR_EFFECT=ThreadLocal.withInitial(()->false);
    public static void init(){}
    static DeferredHolder<Item,Item> register(String id,String slot){return ModItems.ITEMS.register(id,()->new Accessory(id,slot));}
    public static boolean wearing(LivingEntity e,Item item){return CuriosApi.getCuriosInventory(e).map(h->h.findCurios(s->s.getItem()==item).stream().anyMatch(r->r.slotContext().identifier().equals(((Accessory)item).slot))).orElse(false);}
    public static boolean ore(BlockState state){return state.is(ORES);}
    @SubscribeEvent public static void speed(PlayerEvent.BreakSpeed e){var p=e.getEntity();var tool=p.getMainHandItem();if(tool.getItem() instanceof PickaxeItem&&ore(e.getState())&&tool.isCorrectToolForDrops(e.getState()))e.setNewSpeed((float)(e.getNewSpeed()*org.slavicmyths.rpg.AdvancedRunes.mining(e,wearing(p,RING.get()))));}
    @SubscribeEvent public static void fall(LivingDamageEvent.Pre e){if(!e.getEntity().level().isClientSide&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)&&wearing(e.getEntity(),AMULET.get()))e.setNewDamage(e.getNewDamage()*.75F);}
    public static int effectiveFortune(net.minecraft.core.Holder<Enchantment> enchant,ItemStack tool,net.minecraft.world.level.storage.loot.LootContext context){
        int base=EnchantmentHelper.getItemEnchantmentLevel(enchant,tool);var state=context.getParamOrNull(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE);
        if(!enchant.is(Enchantments.FORTUNE)||state==null||!ore(state)||!(tool.getItem() instanceof PickaxeItem)||!tool.isCorrectToolForDrops(state)||!Runes.has(tool,"fortune_1"))return base;
        var silk=context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        return MiningRunes.fortune(base,true,EnchantmentHelper.getItemEnchantmentLevel(silk,tool)>0);
    }
    public static CompoundTag elapsed(CompoundTag source,long ticks){
        CompoundTag n=source.copy();int duration=n.getInt("duration");if(duration>=0)n.putInt("duration",(int)Math.max(0,duration-Math.min(Integer.MAX_VALUE,Math.max(0,ticks))));
        if(n.contains("hidden_effect")){var hidden=elapsed(n.getCompound("hidden_effect"),ticks);n.put("hidden_effect",hidden);if(n.getInt("duration")==0){if(n.contains("id"))hidden.put("id",n.get("id"));return hidden;}}return n;
    }
    private static MobEffectInstance external(LivingEntity e){var d=e.getPersistentData();if(!d.contains("IndustryExternalNV"))return null;var n=elapsed(d.getCompound("IndustryExternalNV"),e.level().getGameTime()-d.getLong("IndustryNVAt"));return n.getInt("duration")==0?null:MobEffectInstance.load(n);}
    private static void storeExternal(LivingEntity e,MobEffectInstance effect){var d=e.getPersistentData();if(effect==null)d.remove("IndustryExternalNV");else d.put("IndustryExternalNV",effect.save());d.putLong("IndustryNVAt",e.level().getGameTime());}
    @SubscribeEvent public static void externalAdded(MobEffectEvent.Added e){if(!e.getEntity().level().isClientSide&&!OUR_EFFECT.get()&&e.getEntity().getPersistentData().getBoolean("IndustryGogglesNV")&&e.getEffectInstance().is(MobEffects.NIGHT_VISION)){
        var old=external(e.getEntity());if(old==null)old=new MobEffectInstance(e.getEffectInstance());else old.update(e.getEffectInstance());storeExternal(e.getEntity(),old);
    }}
    @SubscribeEvent public static void externalRemoved(MobEffectEvent.Remove e){if(!e.getEntity().level().isClientSide&&!OUR_EFFECT.get()&&e.getEffect().equals(MobEffects.NIGHT_VISION))storeExternal(e.getEntity(),null);}
    private static void goggles(LivingEntity e,boolean equipped){
        var d=e.getPersistentData();boolean active=d.getBoolean("IndustryGogglesNV");
        if(equipped){if(!active){storeExternal(e,e.getEffect(MobEffects.NIGHT_VISION));d.putBoolean("IndustryGogglesNV",true);}var current=e.getEffect(MobEffects.NIGHT_VISION);
            if(current==null||!current.isInfiniteDuration()&&current.getDuration()<500){OUR_EFFECT.set(true);try{e.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,520,0,true,false,false));}finally{OUR_EFFECT.set(false);}}
        }else if(active){var restore=external(e);d.remove("IndustryGogglesNV");d.remove("IndustryExternalNV");d.remove("IndustryNVAt");OUR_EFFECT.set(true);try{e.removeEffect(MobEffects.NIGHT_VISION);if(restore!=null)e.addEffect(restore);}finally{OUR_EFFECT.set(false);}}
    }
    public static final class Accessory extends IndustryItem implements ICurioItem {
        final String slot;
        Accessory(String id,String slot){super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),id);this.slot=slot;}
        public boolean canEquip(SlotContext c,ItemStack s){return c.identifier().equals(slot);}
        public void onEquip(SlotContext c,ItemStack previous,ItemStack s){if(!c.entity().level().isClientSide){FolkAccessoryItem.award(c.entity(),"first_accessory");refresh(c.entity(),true);}}
        public void onUnequip(SlotContext c,ItemStack next,ItemStack s){if(!c.entity().level().isClientSide)refresh(c.entity(),next.is(this)||CuriosApi.getCuriosInventory(c.entity()).map(h->h.findCurios(s2->s2.is(this)).stream().anyMatch(r->!(r.slotContext().identifier().equals(c.identifier())&&r.slotContext().index()==c.index()))).orElse(false));}
        public void curioTick(SlotContext c,ItemStack s){if(!c.entity().level().isClientSide&&c.identifier().equals(slot)&&c.entity().tickCount%20==0)refresh(c.entity(),true);}
        private void refresh(LivingEntity e,boolean equipped){if(this==GOGGLES.get())goggles(e,equipped);if(this==BELT.get()){modifier(e,Attributes.ARMOR,"underground_guard_armor",equipped?1:0);modifier(e,Attributes.KNOCKBACK_RESISTANCE,"underground_guard_knockback",equipped?.10:0);}}
        private void modifier(LivingEntity e,net.minecraft.core.Holder<Attribute> type,String name,double value){var a=e.getAttribute(type);if(a==null)return;var id=Industry.id(name);a.removeModifier(id);if(value>0)a.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));}
    }
    private IndustryAccessories(){}
}
