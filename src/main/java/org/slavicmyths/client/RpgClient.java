package org.slavicmyths.client;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.rpg.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RpgClient {
 public static CompoundTag data=new CompoundTag();public static int xp;
 private static final KeyMapping ACTIVATE=new KeyMapping("key.slavicmyths.path_ability",71,"key.categories.slavicmyths");
 public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(ACTIVATE);}
 public static void sync(CompoundTag d,int levels){data=d;xp=levels;if(Minecraft.getInstance().player!=null){CompoundTag root=Minecraft.getInstance().player.getPersistentData();if(!root.contains(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG))root.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,new CompoundTag());root.getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).put(PathData.KEY,d.copy());}if(Minecraft.getInstance().screen instanceof RpgScreen)((RpgScreen)Minecraft.getInstance().screen).refresh();}
 @SubscribeEvent public static void key(InputEvent.Key e){if(Minecraft.getInstance().player!=null&&Minecraft.getInstance().screen==null&&ACTIVATE.consumeClick())RpgNetwork.activate();}
 @SubscribeEvent public static void movement(MovementInputUpdateEvent e){if(e.getInput().shiftKeyDown&&PathData.has(e.getEntity(),"light_step")){e.getInput().forwardImpulse*=1.12F;e.getInput().leftImpulse*=1.12F;}}
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
  if(Runes.max(e.getItemStack())>0){e.getToolTip().add(Component.translatable("rpg.slavicmyths.sockets",Runes.slots(e.getItemStack()),Runes.max(e.getItemStack())));for(String id:Runes.list(e.getItemStack()))e.getToolTip().add(Component.translatable("item.slavicmyths.rune_"+id));e.getToolTip().add(Component.translatable("rpg.slavicmyths.free_slots",Runes.slots(e.getItemStack())-Runes.list(e.getItemStack()).size()));}
  if(e.getEntity()!=null&&PathData.has(e.getEntity(),"sign_sense")&&net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem())!=null){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem()).getPath();if(id.startsWith("rune_"))e.getToolTip().add(Component.translatable("rpg.slavicmyths."+id));else if(id.equals("ancient_sign")||id.equals("ritual_charcoal")||id.equals("thunder_stone"))e.getToolTip().add(Component.translatable("rpg.slavicmyths.ritual_hint"));}
 }
}
