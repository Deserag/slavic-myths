package org.slavicmyths.verify;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.slavicmyths.armorer.ArmorerRecipe;
import org.slavicmyths.armorer.ArmorerInput;

/** Target codecs and matching on vanilla registries, without a game/world launch. */
public final class PortRecipesHeadless {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var ops = RegistryOps.create(JsonOps.INSTANCE, access);
        var shaped = new ArmorerRecipe.Serializer(false);
        String json = "{\"pattern\":[\"A   \",\" A  \",\"  A \",\"   A\"],\"key\":{\"A\":{\"item\":\"minecraft:iron_ingot\"}},\"result\":{\"id\":\"minecraft:iron_sword\"}}";
        ArmorerRecipe recipe = shaped.codec().codec().parse(ops, JsonParser.parseString(json)).getOrThrow();
        check(recipe.width == 4 && recipe.height == 4, "4x4 recipe truncated");
        var items = new java.util.ArrayList<ItemStack>();
        for (int y=0;y<4;y++) for(int x=0;x<4;x++) items.add(x==3-y?new ItemStack(Items.IRON_INGOT):ItemStack.EMPTY);
        var input = new ArmorerInput(4,4,items);
        check(recipe.matches(input,null), "Mirrored pattern rejected");
        items.set(1,new ItemStack(Items.STICK));
        check(!recipe.matches(new ArmorerInput(4,4,items),null), "Extra ingredient accepted");
        var encoded = shaped.codec().codec().encodeStart(ops,recipe).getOrThrow();
        check(shaped.codec().codec().parse(ops,encoded).getOrThrow().getIngredients().size()==16,"JSON roundtrip lost grid");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),access);
        try {
            shaped.streamCodec().encode(buffer,recipe);
            var copy = shaped.streamCodec().decode(buffer);
            check(copy.width==4 && copy.getResultItem(access).is(Items.IRON_SWORD),"Network roundtrip lost recipe");
        } finally { buffer.release(); }
        for (String invalid : List.of(
                json.replace("\"A   \",\" A  \",\"  A \",\"   A\"", ""),
                json.replace("\"A   \"", "\"AAAAA\""),
                json.replace("\"   A\"", "\"   B\""),
                json.replace("\"A\":{", "\" \":{"),
                json.replace("\"id\":\"minecraft:iron_sword\"", "\"id\":\"minecraft:iron_sword\",\"count\":2"))) {
            check(shaped.codec().codec().parse(ops,JsonParser.parseString(invalid)).error().isPresent(),"Malformed recipe accepted: "+invalid);
        }
        var shapeless = new ArmorerRecipe.Serializer(true);
        check(shapeless.codec().codec().parse(ops,JsonParser.parseString("{\"ingredients\":[],\"result\":{\"id\":\"minecraft:stick\"}}")).error().isPresent(),"Empty shapeless accepted");
        var mixed = new ArmorerRecipe(4,4,true,List.of(Ingredient.of(Items.IRON_INGOT),Ingredient.of(Items.STICK)),new ItemStack(Items.IRON_SWORD));
        check(mixed.matches(new ArmorerInput(2,1,List.of(new ItemStack(Items.STICK),new ItemStack(Items.IRON_INGOT))),null),"Shapeless order regression");
        System.out.println("PASS: armorer 4x4/mirror/excess input, JSON/network codecs, malformed recipes, shapeless matching");
    }
}
