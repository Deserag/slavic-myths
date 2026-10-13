package org.slavicmyths.wood;
import java.util.*;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WoodlandFuel {
 private static final Map<Item,Integer> TIMES=new HashMap<>();
 public static void setup(){for(Woodlands.Set s:Woodlands.SETS.values())for(String k:s.blocks.keySet()){
  if(k.equals("leaves")||k.equals("wall_sign"))continue;
  TIMES.put(s.get(k).asItem(),k.equals("slab")?150:k.equals("button")||k.equals("sapling")?100:k.equals("door")?200:k.equals("sign")?200:300);
 }}
 @SubscribeEvent public static void fuel(FurnaceFuelBurnTimeEvent e){Integer time=TIMES.get(e.getItemStack().getItem());if(time!=null)e.setBurnTime(time);}
}
