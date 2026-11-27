package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.combat.*;
import org.slavicmyths.registry.ModItems;
public final class SteppeWeapons {
 public enum Kind {
  POLKAN_SPEAR("polkan_spear","Копьё Полкана",22F,1.35F,1700,"ПКМ: возвратное копьё; Shift+ЛКМ: размах; Shift+ПКМ: три броска."),
  POLKAN_BOW("polkan_bow","Лук Полкана",0F,0F,1200,"Shift+ЛКМ: тройной залп; Shift+ПКМ: руническая стрела."),
  DIV_CLAWS("div_claws","Когти Дива",15F,2.1F,1500,"ПКМ: рывок; Shift+ЛКМ: четыре удара; Shift+ПКМ: пикирование."),
  SKARAPEYA_BLADE("skarapeya_blade","Змеиный клинок Скарапеи",25F,1.25F,2200,"ПКМ: волна или бросок; Shift+ЛКМ: круговой удар или серия; Shift+ПКМ: разделение."),
  SKARAPEYA_GUSLI("skarapeya_gusli","Гусли Скарапеи",8F,1.4F,1400,"ЛКМ: волна; ПКМ: мелодия; Shift+ЛКМ: зона; Shift+ПКМ: яд."),
  JACKAL_DAGGERS("jackal_daggers","Кинжалы степного охотника",10F,2.3F,800,"ПКМ: рывок; Shift+ЛКМ: серия. +5 п.п. к критическому шансу."),
  HYENA_KNUCKLES("hyena_knuckles","Кастеты чёрной гиены",12F,2F,1000,"ПКМ: удар по раненым; Shift+ЛКМ: серия с кровотечением."),
  POLKAN_AXE("polkan_axe","Секира степного князя",23F,0.95F,1400,"ПКМ: рывок; Shift+ЛКМ: размах."),
  DIV_STAFF("div_staff","Посох четырёх проклятий",9F,1.3F,1200,"ПКМ: метка; Shift+ЛКМ: веер; Shift+ПКМ: взрыв четырёх меток."),
  DIV_BOW("div_bow","Лук проклятых перьев",0F,0F,1050,"Shift+ЛКМ: веер; Shift+ПКМ: пробивающее перо."),
  SKARAPEYA_WHIP("skarapeya_whip","Кнут семи змей",18F,1.4F,1600,"ПКМ: хлёсткий удар; Shift+ЛКМ: круговой удар; Shift+ПКМ: яд."),
  SKARAPEYA_NEEDLE("skarapeya_needle","Игла Скарапеи",3F,1F,640,"ЛКМ: выстрел; Shift+ЛКМ: три иглы; Shift+ПКМ: яд. Расходуется прочность, не боеприпасы."),
  STEPPE_SWORD("steppe_sword","Степной меч",12F,1.6F,1100,"ПКМ: специальный удар."),
  STEPPE_AXE("steppe_axe","Степной топор",16F,1F,1100,"ПКМ: специальный удар."),
  STEPPE_MACE("steppe_mace","Степная булава",18F,0.85F,1100,"ПКМ: специальный удар."),
  STEPPE_HALBERD("steppe_halberd","Степная алебарда",14F,1.15F,1100,"ПКМ: специальный удар."),
  STEPPE_SCYTHE("steppe_scythe","Степная коса",13F,1.25F,1100,"ПКМ: специальный удар."),
  STEPPE_SPEAR("steppe_spear","Степное копьё",12F,1.5F,1100,"ПКМ: специальный удар."),
  STEPPE_BOW("steppe_bow","Степной лук",0F,0F,680,"Обычный выстрел."),
  STEPPE_CROSSBOW("steppe_crossbow","Степной арбалет",0F,0F,720,"Обычная зарядка и выстрел.");
  public final String id,name,tip;public final float damage,speed;final int durability;
  Kind(String id,String name,float damage,float speed,int durability,String tip){this.id=id;this.name=name;this.damage=damage;this.speed=speed;this.durability=durability;this.tip=tip;}
  public boolean bow(){return this==POLKAN_BOW||this==DIV_BOW||this==STEPPE_BOW;}
  public boolean standard(){return id.startsWith("steppe_");}
  public boolean serpent(){return this==SKARAPEYA_BLADE||this==SKARAPEYA_GUSLI||this==SKARAPEYA_WHIP||this==SKARAPEYA_NEEDLE;}
  boolean pair(){return this==DIV_CLAWS||this==JACKAL_DAGGERS||this==HYENA_KNUCKLES;}
  float reach(){return switch(this){case POLKAN_SPEAR,STEPPE_HALBERD,SKARAPEYA_WHIP->1.5F;case SKARAPEYA_BLADE,STEPPE_SPEAR,STEPPE_SCYTHE->.75F;default->0;};}
 }
 public static final DeferredHolder<Item,Item> NEEDLE_PROJECTILE=ModItems.ITEMS.register("steppe_needle_projectile",()->new Item(new Item.Properties()));
 public static final Map<Kind,DeferredHolder<Item,Item>> ITEMS=new EnumMap<>(Kind.class);
 public static final Map<String,DeferredHolder<Item,Item>> MATERIALS=new LinkedHashMap<>();
 static {
  material("polkan_seal","Воинская печать Полкана");
  material("div_feather","Проклятое перо");
  material("div_eye","Око Дива");
  material("skarapeya_scale","Чешуя Скарапеи");
  material("skarapeya_gland","Ядовитая железа Скарапеи");
  material("skarapeya_fang","Клык Скарапеи");
  for(Kind k:Kind.values())ITEMS.put(k,ModItems.ITEMS.register(k.id,()->k.bow()?new Bow(k):k==Kind.STEPPE_CROSSBOW?new CrossbowItem(properties(k)){@Override public Component getName(ItemStack s){return label(k,s);}}:new Weapon(k)));
 }
 private static void material(String id,String name){MATERIALS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().rarity(Rarity.RARE)){@Override public Component getName(ItemStack s){return Component.translatableWithFallback(getDescriptionId(),name);}}));}
 public static Item item(Kind k){return ITEMS.get(k).get();}
 public static void init(){}
 static Item.Properties properties(Kind k){return new Item.Properties().durability(k.durability).rarity(k.standard()?Rarity.UNCOMMON:k==Kind.JACKAL_DAGGERS||k==Kind.HYENA_KNUCKLES?Rarity.RARE:Rarity.EPIC);}
 static Component label(Kind k,ItemStack s){return Component.translatableWithFallback(s.getItem().getDescriptionId(),k.name);}
 static ResourceLocation id(String key){return ResourceLocation.fromNamespaceAndPath("slavicmyths",key);}
 public static Kind kind(ItemStack s){if(s.getItem() instanceof Weapon w)return w.kind;if(s.getItem() instanceof Bow b)return b.kind;return ITEMS.get(Kind.STEPPE_CROSSBOW).isBound()&&s.is(item(Kind.STEPPE_CROSSBOW))?Kind.STEPPE_CROSSBOW:null;}
 private static net.minecraft.nbt.CompoundTag state(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 public static boolean split(ItemStack s){return s.getItem() instanceof Weapon w&&w.kind==Kind.SKARAPEYA_BLADE&&state(s).getBoolean("SteppeSplit");}
 public static int poison(ItemStack s){return Math.floorMod(state(s).getInt("SteppePoison"),7);}
 static void mode(ItemStack s,boolean split){var n=state(s);n.putBoolean("SteppeSplit",split);s.set(DataComponents.CUSTOM_DATA,CustomData.of(n));var current=s.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,attrs(Kind.SKARAPEYA_BLADE,!split));var base=attrs(Kind.SKARAPEYA_BLADE,split);for(var e:current.modifiers())if(!e.modifier().id().equals(Item.BASE_ATTACK_DAMAGE_ID)&&!e.modifier().id().equals(Item.BASE_ATTACK_SPEED_ID)&&!e.modifier().id().equals(id("skarapeya_blade_reach")))base=base.withModifierAdded(e.attribute(),e.modifier(),e.slot());s.set(DataComponents.ATTRIBUTE_MODIFIERS,base);}
 static void poison(ItemStack s,int p){var n=state(s);n.putInt("SteppePoison",Math.floorMod(p,7));s.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
 public static boolean visualPair(ItemStack s){Kind k=kind(s);return k!=null&&(k.pair()||split(s));}
 static ItemAttributeModifiers attrs(Kind k,boolean split){var a=MaceItem.attributes(split?17:k.damage,split?2.0F:k.speed);if(k.reach()>0&&!split)a=a.withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,new AttributeModifier(id(k.id+"_reach"),k.reach(),AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);return a;}
 public static boolean isStandard(ItemStack s){var k=kind(s);return k!=null&&k.standard();}
 public static final class Weapon extends Item implements WeaponInputHandler {
  public final Kind kind;
  Weapon(Kind k){super(properties(k).attributes(attrs(k,false)));kind=k;}
  @Override public int getEnchantmentValue(){return 18;}
  @Override public boolean isEnchantable(ItemStack s){return true;}
  @Override public boolean isValidRepairItem(ItemStack s,ItemStack material){return material.is(SteppeResources.ITEMS.get("steppe_ingot").get());}
  @Override public Component getName(ItemStack s){return label(kind,s);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);lines.add(Component.literal(kind.tip));if(kind.serpent())lines.add(Component.literal("Яд: "+SteppeWeaponSkills.poisonName(poison(s))));if(kind==Kind.SKARAPEYA_BLADE)lines.add(Component.literal(split(s)?"Два клинка":"Двусторонний клинок"));}
  @Override public boolean supportsEnchantment(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> e){return !e.is(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE)&&(e.value().isSupportedItem(new ItemStack(Items.IRON_SWORD))||super.supportsEnchantment(stack,e));}
  @Override public boolean isPrimaryItemFor(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> e){return supportsEnchantment(stack,e);}
  @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){if(attacker instanceof net.minecraft.server.level.ServerPlayer p){SteppeWeaponSkills.meleePassive(p,target,stack,kind);stack.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);}return true;}
  @Override public boolean intercepts(Action a){
   if(kind==Kind.SKARAPEYA_GUSLI)return true;
   if(kind==Kind.SKARAPEYA_NEEDLE)return true;
   if(a==Action.RIGHT_CLICK)return true;
   if(a==Action.SHIFT_LEFT_CLICK)return !kind.standard()||kind==Kind.STEPPE_HALBERD;
   return a==Action.SHIFT_RIGHT_CLICK&&(kind==Kind.POLKAN_SPEAR||kind==Kind.DIV_CLAWS||kind==Kind.SKARAPEYA_BLADE||kind==Kind.DIV_STAFF||kind==Kind.SKARAPEYA_WHIP||kind==Kind.STEPPE_HALBERD);
  }
  @Override public void onWeaponAction(net.minecraft.server.level.ServerPlayer p,ItemStack s,Action a){SteppeWeaponSkills.use(p,s,kind,a);}
 }
 public static final class Bow extends BowItem implements WeaponInputHandler{
  final Kind kind;Bow(Kind k){super(properties(k));kind=k;}
  @Override public Component getName(ItemStack s){return label(kind,s);}
  @Override public boolean intercepts(Action a){return kind!=Kind.STEPPE_BOW&&(a==Action.SHIFT_LEFT_CLICK||a==Action.SHIFT_RIGHT_CLICK);}
  @Override public void onWeaponAction(net.minecraft.server.level.ServerPlayer p,ItemStack s,Action a){SteppeWeaponSkills.use(p,s,kind,a);}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);lines.add(Component.literal(kind.tip));}
 }
 private SteppeWeapons(){}
}
