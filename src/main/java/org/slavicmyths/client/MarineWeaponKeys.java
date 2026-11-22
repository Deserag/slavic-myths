package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.marine.*;

/** Existing marine cooldown text; weapon input is owned by WeaponSkillInput. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MarineWeaponKeys {
 @SubscribeEvent public static void hud(RenderGuiEvent.Post event){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui)return;
  var stack=mc.player.getMainHandItem();if(!MarineWeaponRules.weapon(stack))return;
  var tag=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
  String[] keys=stack.is(MarineWeapons.ANCHOR.get())?new String[]{"anchor","slam"}:stack.is(MarineWeapons.STAFF.get())?new String[]{"staffShot","vortex","bubble"}:stack.is(PharaohkaTrident.ITEM.get())?new String[]{"wave"}:new String[]{};
  var g=event.getGuiGraphics();int y=g.guiHeight()-100;
  for(String key:keys){long left=Math.max(0,tag.getLong("MarineWeapon_"+key)-mc.level.getGameTime());g.drawString(mc.font,net.minecraft.network.chat.Component.translatable("marine.hud."+key).append(": ").append(left>0?((left+19)/20)+" s":"✓"),8,y,left>0?0xFFBB66:0x55FFFF,true);y-=12;}
  if(stack.is(MarineWeapons.HARPOON.get()))g.drawString(mc.font,"Гарпун: "+tag.getInt("MarineHarpoonHits")+"/4 · Shift + ПКМ: приковывание · "+Math.max(0,(tag.getLong("MarineHarpoonReady")-mc.level.getGameTime()+19)/20)+" s",8,y,0x55FFFF,true);
 }
}