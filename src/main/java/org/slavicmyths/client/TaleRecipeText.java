package org.slavicmyths.client;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Human-readable recipe from the real codec, including counted custom inputs. */
final class TaleRecipeText {
    static Component display(String encoded){try{JsonObject recipe=JsonParser.parseString(encoded).getAsJsonObject();var text=Component.empty();for(var field:recipe.entrySet()){if(Set.of("group","category","show_notification").contains(field.getKey()))continue;text.append(label(field.getKey())).append(": ").append(value(field.getValue())).append("\n\n");}return text;}catch(RuntimeException failure){return Component.translatable("tales.recipe.unavailable");}}
    private static Component label(String field){if(field.length()==1)return Component.literal(field);String key="tales.recipe."+field;return Component.translatable(net.minecraft.locale.Language.getInstance().has(key)?key:"tales.recipe.condition");}
    private static Component value(JsonElement element){
        if(element.isJsonArray()){var out=Component.empty();int i=0;for(var value:element.getAsJsonArray()){if(i++>0)out.append("; ");out.append(value(value));}return out;}
        if(element.isJsonObject()){var n=element.getAsJsonObject();var out=Component.empty();if(n.has("count"))out.append(n.get("count").getAsString()+" × ");if(n.has("id"))out.append(item(n.get("id").getAsString()));else if(n.has("item"))out.append(item(n.get("item").getAsString()));else if(n.has("tag"))out.append(tag(n.get("tag").getAsString()));else{for(var field:n.entrySet()){if(field.getKey().equals("count"))continue;if(!out.getString().isEmpty())out.append("; ");out.append(label(field.getKey())).append(": ").append(value(field.getValue()));}}if(n.has("components"))out.append(" ").append(value(n.get("components")));return out;}
        if(element.isJsonNull())return Component.empty();String s=element.getAsString();ResourceLocation id=ResourceLocation.tryParse(s);if(id!=null&&s.contains(":")){if(BuiltInRegistries.ITEM.containsKey(id))return item(s);String key="tales.recipe.type."+id.getPath();return net.minecraft.locale.Language.getInstance().has(key)?Component.translatable(key):Component.literal(id.getPath().replace('_',' '));}if(s.equals("true"))return Component.translatable("tales.recipe.yes");if(s.equals("false"))return Component.translatable("tales.recipe.no");if(s.equals("POT")||s.equals("ROLLING"))return Component.translatable("tales.recipe.mode."+s);return Component.literal(s);
    }
    private static Component item(String id){ResourceLocation key=ResourceLocation.tryParse(id);return key!=null&&BuiltInRegistries.ITEM.containsKey(key)?new ItemStack(BuiltInRegistries.ITEM.get(key)).getHoverName():Component.literal(id);}
    private static Component tag(String id){ResourceLocation key=ResourceLocation.tryParse(id);String translated=key==null?"":"tales.recipe.tag."+key.getNamespace()+"."+key.getPath().replace('/','.');return net.minecraft.locale.Language.getInstance().has(translated)?Component.translatable(translated):Component.translatable("tales.recipe.group",id);}
    private TaleRecipeText(){}
}
