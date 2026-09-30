package org.slavicmyths.kitchen;
import net.minecraft.item.*;
import org.slavicmyths.registry.ModItems;
public final class KitchenRecipes {
 public static final int COUNT=11;
 public static Item output(int i){Item[] a={ModItems.PANCAKES.get(),ModItems.RASPBERRY_PANCAKES.get(),ModItems.BLUEBERRY_PANCAKES.get(),ModItems.MEAT_PANCAKES.get(),ModItems.KARAVAI.get(),ModItems.BERRY_PIE.get(),ModItems.MUSHROOM_STEW.get(),ModItems.BEEF_STEW.get(),ModItems.PORK_STEW.get(),ModItems.VENISON_STEW.get(),ModItems.BEAR_STEW.get()};return a[i];}
 public static Item[] ingredients(int i){
  if(i<6){Item filling=i==1?ModItems.RASPBERRY.get():i==2?ModItems.BLUEBERRY.get():i==3?Items.COOKED_BEEF:i==5?ModItems.BLUEBERRY.get():Items.SUGAR;return new Item[]{ModItems.FLOUR.get(),i==4?Items.WATER_BUCKET:Items.EGG,filling};}
  Item main=i==6?Items.BROWN_MUSHROOM:i==7?Items.COOKED_BEEF:i==8?Items.COOKED_PORKCHOP:i==9?ModItems.COOKED_VENISON.get():ModItems.COOKED_BEAR_MEAT.get();return new Item[]{main,Items.POTATO,Items.BOWL};
 }
 public static Item tool(int i){return i<6?ModItems.ROLLING_PIN.get():ModItems.METAL_POT.get();}
 public static int count(int i,int slot){return slot==0&&i==4?3:1;}
 public static int amount(int i){return i<4?3:1;}
 private KitchenRecipes(){}
}
