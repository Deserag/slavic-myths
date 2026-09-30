package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.slavicmyths.rpg.*;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class RpgClient {
 public static CompoundNBT data=new CompoundNBT();public static int xp;
 private static final KeyBinding ACTIVATE=new KeyBinding("key.slavicmyths.path_ability",71,"key.categories.slavicmyths");
 public static void setup(){ClientRegistry.registerKeyBinding(ACTIVATE);ScreenManager.register(RpgMenu.TYPE.get(),RpgScreen::new);}
 public static void sync(CompoundNBT d,int levels){data=d;xp=levels;if(Minecraft.getInstance().player!=null){CompoundNBT root=Minecraft.getInstance().player.getPersistentData();if(!root.contains(net.minecraft.entity.player.PlayerEntity.PERSISTED_NBT_TAG))root.put(net.minecraft.entity.player.PlayerEntity.PERSISTED_NBT_TAG,new CompoundNBT());root.getCompound(net.minecraft.entity.player.PlayerEntity.PERSISTED_NBT_TAG).put(PathData.KEY,d.copy());}if(Minecraft.getInstance().screen instanceof RpgScreen)((RpgScreen)Minecraft.getInstance().screen).refresh();}
 @SubscribeEvent public static void key(InputEvent.KeyInputEvent e){if(Minecraft.getInstance().player!=null&&Minecraft.getInstance().screen==null&&ACTIVATE.consumeClick())RpgNetwork.activate();}
 @SubscribeEvent public static void movement(InputUpdateEvent e){if(e.getMovementInput().shiftKeyDown&&PathData.has(e.getPlayer(),"light_step")){e.getMovementInput().forwardImpulse*=1.12F;e.getMovementInput().leftImpulse*=1.12F;}}
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
  if(Runes.max(e.getItemStack())>0){e.getToolTip().add(new TranslationTextComponent("rpg.slavicmyths.sockets",Runes.slots(e.getItemStack()),Runes.max(e.getItemStack())));for(String id:Runes.list(e.getItemStack()))e.getToolTip().add(new TranslationTextComponent("item.slavicmyths.rune_"+id));e.getToolTip().add(new TranslationTextComponent("rpg.slavicmyths.free_slots",Runes.slots(e.getItemStack())-Runes.list(e.getItemStack()).size()));}
  if(e.getPlayer()!=null&&PathData.has(e.getPlayer(),"sign_sense")&&e.getItemStack().getItem().getRegistryName()!=null){String id=e.getItemStack().getItem().getRegistryName().getPath();if(id.startsWith("rune_"))e.getToolTip().add(new TranslationTextComponent("rpg.slavicmyths."+id));else if(id.equals("ancient_sign")||id.equals("ritual_charcoal")||id.equals("thunder_stone"))e.getToolTip().add(new TranslationTextComponent("rpg.slavicmyths.ritual_hint"));}
 }
}
