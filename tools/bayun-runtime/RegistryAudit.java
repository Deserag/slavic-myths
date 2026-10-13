package org.slavicmyths.verify;

import com.google.gson.*;
import java.nio.file.*;
import net.minecraft.core.registries.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths_bayun")
@PrefixGameTestTemplate(false)
public final class RegistryAudit {
    @GameTest(template="empty",timeoutTicks=20)
    public static void registered(GameTestHelper helper)throws Exception {
        JsonObject result=new JsonObject();
        JsonArray items=new JsonArray();
        for(var id:BuiltInRegistries.ITEM.keySet())if(id.getNamespace().equals("slavicmyths")){
            var item=BuiltInRegistries.ITEM.get(id);JsonObject value=new JsonObject();
            value.addProperty("id",id.toString());value.addProperty("class",item.getClass().getName());
            value.addProperty("food",item.getDefaultInstance().has(DataComponents.FOOD));items.add(value);
        }
        result.add("items",items);
        JsonObject entityFacts=new JsonObject();for(var id:BuiltInRegistries.ENTITY_TYPE.keySet())if(id.getNamespace().equals("slavicmyths")){var entity=BuiltInRegistries.ENTITY_TYPE.get(id).create(helper.getLevel());if(entity instanceof net.minecraft.world.entity.LivingEntity living){JsonObject fact=new JsonObject();fact.addProperty("class",living.getClass().getName());fact.addProperty("health",living.getMaxHealth());fact.addProperty("armor",living.getArmorValue());fact.addProperty("speed",living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));entityFacts.add(id.toString(),fact);}}result.add("livingFacts",entityFacts);
        for(var key:java.util.List.of(Registries.ENTITY_TYPE,Registries.BLOCK,Registries.FEATURE)){
            var registry=BuiltInRegistries.REGISTRY.get(key.location());JsonArray ids=new JsonArray();
            for(var id:registry.keySet())if(id.getNamespace().equals("slavicmyths"))ids.add(id.toString());
            result.add(key.location().getPath(),ids);
        }
        for(var key:java.util.List.of(Registries.STRUCTURE,Registries.CONFIGURED_FEATURE,Registries.PLACED_FEATURE)){
            JsonArray ids=new JsonArray();for(var id:helper.getLevel().registryAccess().registryOrThrow(key).keySet())if(id.getNamespace().equals("slavicmyths"))ids.add(id.toString());result.add(key.location().getPath(),ids);
        }
        JsonArray recipes=new JsonArray();for(var recipe:helper.getLevel().getRecipeManager().getRecipes())if(recipe.id().getNamespace().equals("slavicmyths"))recipes.add(recipe.id().toString());result.add("recipes",recipes);
        Path out=Path.of(System.getProperty("slavicmyths.audit.path"));Files.createDirectories(out.getParent());Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(result));
        System.out.println("BAYUN REAL REGISTRY AUDIT PASS items="+items.size());helper.succeed();
    }
}
