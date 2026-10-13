package org.slavicmyths.finist;
import java.util.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import org.slavicmyths.item.*;
import org.slavicmyths.item.ItemState.QuiverState;
import org.slavicmyths.military.GreyWolfStory;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public final class FinistQuiver extends Item implements ICurioItem {
 private static final Map<LivingEntity,String> SEEN=new WeakHashMap<>();
 public FinistQuiver(Properties p){super(p.component(ItemState.FINIST_QUIVER.get(),new QuiverState(12,0,"")));}
 public boolean canEquip(SlotContext c,ItemStack s){return c.identifier().equals("belt");}
 public static ItemStack equipped(LivingEntity e){var inv=CuriosApi.getCuriosInventory(e).orElse(null);if(inv==null||!inv.isSlotActive("belt",0))return ItemStack.EMPTY;var slots=inv.getStacksHandler("belt").orElse(null);if(slots==null||slots.getSlots()<1)return ItemStack.EMPTY;var s=slots.getStacks().getStackInSlot(0);return s.is(FinistItems.QUIVER.get())?s:ItemStack.EMPTY;}
 public static QuiverState state(ItemStack s){return s.getOrDefault(ItemState.FINIST_QUIVER.get(),new QuiverState(12,0,""));}
 public static int charges(ItemStack s){return s.isEmpty()?0:state(s).charges();}
 public static boolean spend(ItemStack s){var q=state(s);if(q.charges()<=0)return false;s.set(ItemState.FINIST_QUIVER.get(),new QuiverState(q.charges()-1,q.recharge(),q.identity()));return true;}
 @Override public void onEquip(SlotContext c,ItemStack previous,ItemStack s){if(c.entity() instanceof ServerPlayer p){reset(p,s);GreyPackRing.sound(p,"quiver_equip",.4F);}}
 @Override public void onUnequip(SlotContext c,ItemStack next,ItemStack s){SEEN.remove(c.entity());if(c.entity() instanceof ServerPlayer p){var q=state(s);s.set(ItemState.FINIST_QUIVER.get(),new QuiverState(q.charges(),GreyPackRing.now(p),q.identity()));}}
 private static void reset(ServerPlayer p,ItemStack s){var q=state(s);String id=q.identity().isEmpty()?UUID.randomUUID().toString():q.identity();SEEN.put(p,id);s.set(ItemState.FINIST_QUIVER.get(),new QuiverState(q.charges(),GreyPackRing.now(p),id));}
 @Override public void curioTick(SlotContext c,ItemStack s){if(!(c.entity() instanceof ServerPlayer p)||equipped(p)!=s)return;var q=state(s);long now=GreyPackRing.now(p);if(!q.identity().equals(SEEN.get(p))){reset(p,s);return;}if(now<q.recharge()){reset(p,s);return;}long delta=now-q.recharge();if(delta>=160){int next=Math.min(12,q.charges()+(int)Math.min(12,delta/160));s.set(ItemState.FINIST_QUIVER.get(),new QuiverState(next,now-delta%160,q.identity()));}}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(GreyWolfStory.text("quiver.charges",charges(s)));for(String key:List.of("quiver.recharge","quiver.pair","quiver.bonus","quiver.equip"))lines.add(GreyWolfStory.text(key).withStyle(net.minecraft.ChatFormatting.GRAY));}
}
