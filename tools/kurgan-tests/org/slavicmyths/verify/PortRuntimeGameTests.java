package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.item.ItemState;

@GameTestHolder("slavicmyths")
@PrefixGameTestTemplate(false)
public final class PortRuntimeGameTests {
    private static Path root(){return Path.of(System.getProperty("slavicmyths.portRoot"));}
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    private static Set<ResourceLocation> files(String directory)throws Exception{
        Path base=root().resolve("src/main/resources/data/slavicmyths/"+directory);Set<ResourceLocation> result=new HashSet<>();
        try(var paths=Files.walk(base)){paths.filter(p->p.toString().endsWith(".json")).forEach(p->result.add(id(base.relativize(p).toString().replace('\\','/').replaceFirst("\\.json$",""))));}return result;
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void data(GameTestHelper test)throws Exception{
        var server=test.getLevel().getServer();
        test.assertTrue(!server.getRecipeManager().hadErrorsLoading(),"Recipe loading reported errors");
        Set<ResourceLocation> recipes=server.getRecipeManager().getRecipeIds().collect(java.util.stream.Collectors.toSet());
        Set<ResourceLocation> loot=new HashSet<>(server.reloadableRegistries().getKeys(Registries.LOOT_TABLE));
        for(var expected:files("recipe"))test.assertTrue(recipes.contains(expected),"Missing loaded recipe "+expected);
        for(var expected:files("loot_table"))test.assertTrue(loot.contains(expected),"Missing loaded loot "+expected);
        for(var expected:files("advancement"))test.assertTrue(server.getAdvancements().get(expected)!=null,"Missing loaded advancement "+expected);
        for(String kind:new String[]{"structure","structure_set","configured_feature","placed_feature"}){
            var registry=switch(kind){case "structure"->Registries.STRUCTURE;case "structure_set"->Registries.STRUCTURE_SET;case "configured_feature"->Registries.CONFIGURED_FEATURE;default->Registries.PLACED_FEATURE;};
            var loaded=test.getLevel().registryAccess().registryOrThrow(registry);
            for(var expected:files("worldgen/"+kind))test.assertTrue(loaded.containsKey(expected),"Missing loaded worldgen "+expected);
        }
        test.assertTrue(test.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).containsKey(id("tailwind")),"Tailwind missing");
        System.out.println("PORT_RUNTIME_DATA_PASS recipes="+files("recipe").size()+" loot="+files("loot_table").size()+" advancements="+files("advancement").size());test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void components(GameTestHelper test){
        var registries=test.getLevel().registryAccess();
        ItemStack pouch=new ItemStack(org.slavicmyths.registry.ModItems.MESHOCHEK_TROFEEV.get());
        var values=net.minecraft.core.NonNullList.withSize(9,ItemStack.EMPTY);values.set(0,new ItemStack(Items.DIAMOND,3));values.set(8,new ItemStack(Items.IRON_SWORD));
        ItemState.runes(values.get(8),2,List.of("thunder"));ItemState.inventory(pouch,false,values);
        var restored=ItemStack.parseOptional(registries,(CompoundTag)pouch.saveOptional(registries));
        test.assertTrue(ItemState.inventory(restored,false,registries).get(0).getCount()==3,"Inventory roundtrip lost count");
        test.assertTrue(ItemState.runes(ItemState.inventory(restored,false,registries).get(8)).runes().equals(List.of("thunder")),"Nested rune component lost");
        ItemStack copy=pouch.copy();var changed=ItemState.inventory(copy,false,registries);changed.get(0).shrink(1);ItemState.inventory(copy,false,changed);
        test.assertTrue(ItemState.inventory(pouch,false,registries).get(0).getCount()==3,"Stack copy aliases inventory");
        CompoundTag old=new CompoundTag(),nested=new CompoundTag();nested.putString("id","minecraft:diamond");nested.putByte("Count",(byte)3);nested.putByte("Slot",(byte)8);ListTag list=new ListTag();list.add(nested);old.put("HuntTrophies",list);old.putString("ForeignKey","preserved");
        ItemStack legacy=new ItemStack(org.slavicmyths.registry.ModItems.MESHOCHEK_TROFEEV.get());legacy.set(DataComponents.CUSTOM_DATA,CustomData.of(old));
        test.assertTrue(ItemState.inventory(legacy,false,registries).get(8).getCount()==3,"Legacy nested DFU failed");
        test.assertTrue(legacy.get(DataComponents.CUSTOM_DATA).copyTag().getString("ForeignKey").equals("preserved"),"Foreign custom data erased");
        test.assertTrue(!legacy.get(DataComponents.CUSTOM_DATA).copyTag().contains("HuntTrophies"),"Migrated key retained");
        ItemStack cargo=new ItemStack(org.slavicmyths.registry.ModItems.FLYING_BROOM.get());ItemState.inventory(cargo,true,values);
        var cargoCopy=ItemStack.parseOptional(registries,(CompoundTag)cargo.saveOptional(registries));test.assertTrue(ItemState.inventory(cargoCopy,true,registries).get(0).getCount()==3,"Cargo roundtrip lost contents");
        CompoundTag invalid=old.copy();invalid.getList("HuntTrophies",10).getCompound(0).putByte("Slot",(byte)9);legacy.set(DataComponents.CUSTOM_DATA,CustomData.of(invalid));legacy.remove(ItemState.TROPHIES.get());
        boolean rejected=false;try{ItemState.inventory(legacy,false,registries);}catch(IllegalStateException expected){rejected=true;}
        test.assertTrue(rejected&&legacy.get(DataComponents.CUSTOM_DATA).copyTag().equals(invalid),"Malformed migration was partially committed");
        System.out.println("PORT_RUNTIME_COMPONENTS_PASS");test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void existingChecks(GameTestHelper test)throws Exception{
        PortRecipesHeadless.main(new String[0]);PortDamageHeadless.main(new String[]{root().resolve("src/main/resources/data/slavicmyths/damage_type").toString()});
        YagaHeadless.main(new String[0]);YagaPlacementHeadless.main(new String[0]);
        System.out.println("PORT_RUNTIME_EXISTING_CHECKS_PASS");test.succeed();
    }
    @GameTest(template="port_empty",timeoutTicks=100)
    public static void registries(GameTestHelper test)throws Exception{
        var baseline=com.google.gson.JsonParser.parseString(Files.readString(root().resolve("docs/port/registry-parity-0.9.4.json"))).getAsJsonObject();
        int checked=0;
        for(var raw:baseline.getAsJsonArray("entries")){
            var row=raw.getAsJsonObject();String kind=row.get("kind").getAsString();
            var key=switch(kind){case "item"->Registries.ITEM;case "block"->Registries.BLOCK;case "entity_type"->Registries.ENTITY_TYPE;case "sound_event"->Registries.SOUND_EVENT;case "mob_effect"->Registries.MOB_EFFECT;case "block_entity_type"->Registries.BLOCK_ENTITY_TYPE;case "menu"->Registries.MENU;case "feature"->Registries.FEATURE;case "structure"->Registries.STRUCTURE;case "structure_type"->Registries.STRUCTURE_TYPE;case "structure_piece"->Registries.STRUCTURE_PIECE;case "recipe_type"->Registries.RECIPE_TYPE;case "recipe_serializer"->Registries.RECIPE_SERIALIZER;case "enchantment"->Registries.ENCHANTMENT;default->null;};
            if(key==null)continue;var id=ResourceLocation.parse(row.get("target_id").getAsString());
            test.assertTrue(test.getLevel().registryAccess().registryOrThrow(key).containsKey(id),"Missing baseline registry "+kind+" "+id);checked++;
        }
        int entities=0;
        for(var id:BuiltInRegistries.ENTITY_TYPE.keySet())if(id.getNamespace().equals("slavicmyths")){
            var entity=BuiltInRegistries.ENTITY_TYPE.get(id).create(test.getLevel());test.assertTrue(entity!=null,"Entity construction failed "+id);
            if(entity instanceof net.minecraft.world.entity.LivingEntity living)test.assertTrue(living.getMaxHealth()>0,"Attributes missing "+id);entities++;
        }
        var slots=top.theillusivec4.curios.api.CuriosApi.getEntitySlots(net.minecraft.world.entity.EntityType.PLAYER,test.getLevel());
        for(String slot:List.of("head","necklace","ring","belt","charm"))test.assertTrue(slots.containsKey(slot)&&slots.get(slot).getSize()==(slot.equals("ring")?2:1),"Curios slot drift "+slot);
        test.assertTrue(entities==47,"Entity registration count "+entities);System.out.println("PORT_RUNTIME_REGISTRIES_PASS checked="+checked+" entities="+entities);test.succeed();
    }
}
