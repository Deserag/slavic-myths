package org.slavicmyths.village;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/** Only contributes listings. Vanilla selects two per level and owns all offer persistence/restock. */
@EventBusSubscriber(modid="slavicmyths")
public final class VillageTrades {
    @SubscribeEvent public static void trades(VillagerTradesEvent event){
        var id=BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        if(!id.getNamespace().equals("slavicmyths") || !VillageRoles.ROLES.containsKey(id.getPath()))return;
        if(id.getPath().equals("druzhinnik"))return;
        String path="/data/slavicmyths/village_trades/"+id.getPath()+".json";
        try(var stream=VillageTrades.class.getResourceAsStream(path)){
            if(stream==null)throw new IllegalStateException("Missing trade pool "+path);
            JsonObject levels=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            for(int level=1;level<=5;level++){
                for(JsonElement value:levels.getAsJsonArray(Integer.toString(level))){
                    JsonObject row=value.getAsJsonObject();
                    Item input=item(row.get("input").getAsString()),output=item(row.get("output").getAsString());
                    int cost=row.get("cost").getAsInt(),count=row.get("count").getAsInt(),uses=row.get("max_uses").getAsInt(),xp=row.get("xp").getAsInt();
                    float multiplier=row.get("price_multiplier").getAsFloat();
                    if(cost<1||cost>input.getDefaultMaxStackSize()||count<1||count>output.getDefaultMaxStackSize()||uses<1||xp<1)
                        throw new IllegalStateException("Invalid trade "+path+":"+row);
                    event.getTrades().get(level).add((entity,random)->new MerchantOffer(new ItemCost(input,cost),new ItemStack(output,count),uses,xp,multiplier));
                }
            }
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static Item item(String id){var key=ResourceLocation.parse(id);if(!BuiltInRegistries.ITEM.containsKey(key)||BuiltInRegistries.ITEM.get(key)==Items.AIR)throw new IllegalStateException("Unknown trade item "+id);return BuiltInRegistries.ITEM.get(key);}
    private VillageTrades(){}
}
