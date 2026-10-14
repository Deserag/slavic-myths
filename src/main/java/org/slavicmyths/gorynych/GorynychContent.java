package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.sounds.*;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;
public final class GorynychContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static final DeferredHolder<EntityType<?>,EntityType<Gorynych>> BOSS=ModEntities.ENTITIES.register("zmey_gorynych",()->EntityType.Builder.of(Gorynych::new,MobCategory.MONSTER).sized(4.8f,5.5f).eyeHeight(4.8f).fireImmune().clientTrackingRange(20).updateInterval(2).build("slavicmyths:zmey_gorynych"));
 public static final DeferredHolder<EntityType<?>,EntityType<BreathCloud>> CLOUD=ModEntities.ENTITIES.register("gorynych_breath_cloud",()->EntityType.Builder.<BreathCloud>of(BreathCloud::new,MobCategory.MISC).sized(6,1.4f).noSave().clientTrackingRange(12).updateInterval(2).build("slavicmyths:gorynych_breath_cloud"));
 public static final DeferredHolder<EntityType<?>,EntityType<BreathShot>> SHOT=ModEntities.ENTITIES.register("gorynych_breath_shot",()->EntityType.Builder.<BreathShot>of(BreathShot::new,MobCategory.MISC).sized(.6f,.6f).noSave().clientTrackingRange(16).updateInterval(1).build("slavicmyths:gorynych_breath_shot"));
 public static final DeferredHolder<ArmorMaterial,ArmorMaterial> MATERIAL=org.slavicmyths.item.ModGear.ARMOR_MATERIALS.register("dragon",()->new ArmorMaterial(Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,7,ArmorItem.Type.LEGGINGS,6,ArmorItem.Type.BOOTS,3),18,SoundEvents.ARMOR_EQUIP_NETHERITE,()->net.minecraft.world.item.crafting.Ingredient.of(ModItems.PERUNITE.get()),List.of(new ArmorMaterial.Layer(id("dragon"))),2,.1f));
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final Map<EquipmentSlot,DeferredHolder<Item,Item>> ARMOR=new EnumMap<>(EquipmentSlot.class);
 public static final List<String> RUNES=List.of("mor_breath","dragon_flame","storm_strike");
 public static final List<String> SOUNDS_LIST=List.of("ambient_low","step_heavy","roar_ground","head_mor_charge","head_mor_shot","mor_cloud","head_fire_charge","head_fire_breath","head_fireball","head_predator_bite","head_predator_grab","tail_sweep","ground_spikes_warn","ground_spikes_impact","transform_start","wings_unfold","wings_flap","wind_gust","dive","land","exhausted","cleanse_mor_head","cleanse_fire_head","cleanse_predator_head","purified_roar","bottle_collect","crossbow_charge","crossbow_special_fire","crossbow_special_mor","crossbow_special_storm","armor_fullset_proc");
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static{
  for(String s:List.of("gorynych_scale","ancient_serpent_steel","serpent_seal","gorynych_flame_breath_bottle","gorynych_mor_breath_bottle","gorynych_storm_breath_bottle","rune_mor_breath","rune_dragon_flame","rune_storm_strike"))ITEMS.put(s,ModItems.ITEMS.register(s,()->new Item(new Item.Properties().craftRemainder(s.endsWith("_bottle")?Items.GLASS_BOTTLE:null).rarity(s.startsWith("rune_")||s.equals("serpent_seal")?Rarity.EPIC:Rarity.RARE)){
   @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}
  }));
  for(String s:List.of("serpent_crossbow","three_breath_crossbow"))ITEMS.put(s,ModItems.ITEMS.register(s,()->new SerpentCrossbow(s.equals("three_breath_crossbow"))));
  ITEMS.put("zmey_gorynych_spawn_egg",ModItems.ITEMS.register("zmey_gorynych_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS,0x202126,0xA52FDB,new Item.Properties())));
  for(var type:new ArmorItem.Type[]{ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS}){DeferredHolder<Item,Item> h=ModItems.ITEMS.register("dragon_"+type.getName(),()->new ArmorItem(MATERIAL,type,new Item.Properties().durability(type.getDurability(38)).fireResistant().rarity(Rarity.EPIC)){
   @Override public ResourceLocation getArmorTexture(ItemStack s,Entity e,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return id("textures/models/armor/dragon.png");}
   @Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> l,TooltipFlag f){l.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}
  });ARMOR.put(type.getSlot(),h);ITEMS.put("dragon_"+type.getName(),h);}
  for(String s:SOUNDS_LIST)SOUNDS.put(s,ModSounds.SOUNDS.register("gorynych."+s,()->SoundEvent.createFixedRangeEvent(id("gorynych."+s),48)));
 }
 public static Item item(String id){return ITEMS.get(id).get();}
 public static Item bottle(int type){return item(type==0?"gorynych_mor_breath_bottle":type==1?"gorynych_flame_breath_bottle":"gorynych_storm_breath_bottle");}
 public static net.minecraft.network.chat.MutableComponent text(String key,Object...args){return net.minecraft.network.chat.Component.translatable("gorynych196."+key,args);}
 public static void sound(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,String key){w.playSound(null,p,SOUNDS.get(key).get(),SoundSource.HOSTILE,key.equals("ambient_low")?.15f:.65f,1);}
 public static void init(net.neoforged.bus.api.IEventBus bus){GorynychUpgrade.SERIALIZERS.register(bus);bus.addListener(StormVfx::register);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(BOSS.get(),Gorynych.attributes().build()));}
 private GorynychContent(){}
}
