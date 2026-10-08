package org.slavicmyths.armorer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Existing 4x4 recipes: IDs belong to RecipeHolder in 1.21.1. */
public final class ArmorerRecipe implements Recipe<ArmorerInput> {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "slavicmyths");
    public static final DeferredHolder<RecipeType<?>, RecipeType<ArmorerRecipe>> TYPE = TYPES.register("armorer", () -> new RecipeType<>() {
        @Override public String toString() { return "slavicmyths:armorer"; }
    });
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "slavicmyths");
    public static final DeferredHolder<RecipeSerializer<?>, Serializer> SHAPED = SERIALIZERS.register("armorer_shaped", () -> new Serializer(false));
    public static final DeferredHolder<RecipeSerializer<?>, Serializer> SHAPELESS = SERIALIZERS.register("armorer_shapeless", () -> new Serializer(true));
    public final int width, height;
    public final boolean shapeless;
    public final boolean copyComponents;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;

    public ArmorerRecipe(int width, int height, boolean shapeless, List<Ingredient> ingredients, ItemStack output) {
        this(width,height,shapeless,ingredients,output,false);
    }
    public ArmorerRecipe(int width, int height, boolean shapeless, List<Ingredient> ingredients, ItemStack output,boolean copyComponents) {
        if (width < 1 || width > 4 || height < 1 || height > 4 || ingredients.isEmpty() || ingredients.size() > 16
                || (!shapeless && ingredients.size() != width * height)
                || ingredients.stream().allMatch(Ingredient::isEmpty)
                || (shapeless && ingredients.stream().anyMatch(Ingredient::isEmpty))
                || output.isEmpty() || output.getCount() > output.getMaxStackSize()) {
            throw new IllegalArgumentException("Invalid armorer recipe");
        }
        this.width = width; this.height = height; this.shapeless = shapeless;
        if(copyComponents&&ingredients.getFirst().isEmpty())throw new IllegalArgumentException("Component source must be the first nonempty ingredient");
        this.copyComponents=copyComponents;
        this.ingredients = NonNullList.create(); this.ingredients.addAll(ingredients);
        this.output = output.copy();
    }
    public boolean matches(ArmorerInput inv,Level world) {
        if(shapeless){List<ItemStack> stacks=new ArrayList<>();for(int i=0;i<inv.size();i++)if(!inv.getItem(i).isEmpty())stacks.add(inv.getItem(i));return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(stacks,ingredients)!=null;}
        for(int x=0;x<=inv.width()-width;x++)for(int y=0;y<=inv.height()-height;y++)if(match(inv,x,y,false)||match(inv,x,y,true))return true;return false;
    }
    private boolean match(ArmorerInput inv,int ox,int oy,boolean mirror) {
        for(int y=0;y<inv.height();y++)for(int x=0;x<inv.width();x++) {
            int a=x-ox,b=y-oy;Ingredient ingredient=Ingredient.EMPTY;
            if(a>=0&&b>=0&&a<width&&b<height)ingredient=ingredients.get((mirror?width-a-1:a)+b*width);
            if(!ingredient.test(inv.getItem(x+y*inv.width())))return false;
        }return true;
    }
    @Override public ItemStack assemble(ArmorerInput input, HolderLookup.Provider registries) {
        if(copyComponents)for(ItemStack stack:input.items())if(ingredients.getFirst().test(stack)) {
            ItemStack result=stack.transmuteCopy(output.getItem(),output.getCount());
            result.applyComponents(output.getComponentsPatch());return result;
        }
        return output.copy();
    }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return output.copy(); }
    @Override public NonNullList<ItemStack> getRemainingItems(ArmorerInput input) {
        NonNullList<ItemStack> remaining=Recipe.super.getRemainingItems(input);
        // Two non-stackable blades: the primary output and the second blade in its input slot.
        // Each blade inherits only its own donor's data, never duplicates the other blade's runes.
        if(output.is(org.slavicmyths.combat.RareWeapons.TUGARIN.get())) {
            boolean first=true;
            for(int i=0;i<input.size();i++)if(ingredients.getFirst().test(input.getItem(i))) {
                if(first){first=false;continue;}
                remaining.set(i,input.getItem(i).transmuteCopy(output.getItem(),1));break;
            }
        }
        return remaining;
    }
    @Override public NonNullList<Ingredient> getIngredients() { NonNullList<Ingredient> copy = NonNullList.create(); copy.addAll(ingredients); return copy; }
    @Override public RecipeSerializer<?> getSerializer() { return shapeless ? SHAPELESS.get() : SHAPED.get(); }
    @Override public RecipeType<?> getType() { return TYPE.get(); }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean canCraftInDimensions(int w, int h) { return shapeless ? w * h >= ingredients.size() : w >= width && h >= height; }

    private record Pattern(List<String> rows, Map<String, Ingredient> key) {
        static final MapCodec<Pattern> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("pattern").forGetter(Pattern::rows),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter(Pattern::key)
        ).apply(instance, Pattern::new));
        List<Ingredient> ingredients() {
            if (rows.isEmpty() || rows.size() > 4 || rows.getFirst().isEmpty() || rows.getFirst().length() > 4)
                throw new IllegalArgumentException("Pattern dimensions must be 1..4");
            for (String symbol : key.keySet()) if (symbol.length() != 1 || symbol.equals(" "))
                throw new IllegalArgumentException("Invalid pattern key");
            List<Ingredient> result = new ArrayList<>(); Set<String> used = new HashSet<>();
            for (String row : rows) {
                if (row.length() != rows.getFirst().length()) throw new IllegalArgumentException("Unequal rows");
                for (char c : row.toCharArray()) {
                    String symbol = String.valueOf(c);
                    if (c == ' ') result.add(Ingredient.EMPTY);
                    else { if (!key.containsKey(symbol)) throw new IllegalArgumentException("Undefined key"); result.add(key.get(symbol)); used.add(symbol); }
                }
            }
            if (used.isEmpty() || !used.equals(key.keySet())) throw new IllegalArgumentException("Empty recipe or unused key");
            return result;
        }
        static Pattern fromRecipe(ArmorerRecipe recipe) {
            Map<String, Ingredient> key = new LinkedHashMap<>(); List<String> rows = new ArrayList<>();
            for (int y = 0; y < recipe.height; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < recipe.width; x++) {
                    Ingredient ingredient = recipe.ingredients.get(x + y * recipe.width);
                    if (ingredient.isEmpty()) row.append(' ');
                    else { String symbol = String.valueOf((char) ('A' + key.size())); key.put(symbol, ingredient); row.append(symbol); }
                }
                rows.add(row.toString());
            }
            return new Pattern(rows, key);
        }
    }
    private record ShapedData(Pattern pattern, ItemStack result,boolean copyComponents) {
        ArmorerRecipe recipe() {
            List<Ingredient> ingredients = pattern.ingredients();
            return new ArmorerRecipe(pattern.rows.getFirst().length(), pattern.rows.size(), false, ingredients, result,copyComponents);
        }
    }
    private record ShapelessData(List<Ingredient> ingredients, ItemStack result,boolean copyComponents) { }
    public static final class Serializer implements RecipeSerializer<ArmorerRecipe> {
        private final MapCodec<ArmorerRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, ArmorerRecipe> streamCodec;
        public Serializer(boolean shapeless) {
            if (shapeless) {
                MapCodec<ShapelessData> data = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.sizeLimitedListOf(16).fieldOf("ingredients").forGetter(ShapelessData::ingredients),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ShapelessData::result),
                        Codec.BOOL.optionalFieldOf("copy_components",false).forGetter(ShapelessData::copyComponents)
                ).apply(instance, ShapelessData::new));
                codec = data.flatXmap(value -> checked(() -> new ArmorerRecipe(4, 4, true, value.ingredients, value.result,value.copyComponents)),
                        recipe -> DataResult.success(new ShapelessData(recipe.ingredients, recipe.output,recipe.copyComponents)));
            } else {
                MapCodec<ShapedData> data = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Pattern.CODEC.forGetter(ShapedData::pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ShapedData::result),
                        Codec.BOOL.optionalFieldOf("copy_components",false).forGetter(ShapedData::copyComponents)
                ).apply(instance, ShapedData::new));
                codec = data.flatXmap(value -> checked(value::recipe),
                        recipe -> DataResult.success(new ShapedData(Pattern.fromRecipe(recipe), recipe.output,recipe.copyComponents)));
            }
            streamCodec = new StreamCodec<>() {
                @Override public ArmorerRecipe decode(RegistryFriendlyByteBuf buffer) {
                    int w = buffer.readVarInt(), h = buffer.readVarInt(), n = buffer.readVarInt();
                    if (w < 1 || w > 4 || h < 1 || h > 4 || n < 1 || n > 16 || (!shapeless && n != w * h))
                        throw new IllegalArgumentException("Invalid armorer dimensions");
                    List<Ingredient> list = new ArrayList<>();
                    for (int i = 0; i < n; i++) list.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                    ItemStack result=ItemStack.STREAM_CODEC.decode(buffer);return new ArmorerRecipe(w,h,shapeless,list,result,buffer.readBoolean());
                }
                @Override public void encode(RegistryFriendlyByteBuf buffer, ArmorerRecipe recipe) {
                    buffer.writeVarInt(recipe.width); buffer.writeVarInt(recipe.height); buffer.writeVarInt(recipe.ingredients.size());
                    for (Ingredient ingredient : recipe.ingredients) Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
                    buffer.writeBoolean(recipe.copyComponents);
                }
            };
        }
        private static <T> DataResult<T> checked(java.util.function.Supplier<T> action) {
            try { return DataResult.success(action.get()); }
            catch (IllegalArgumentException error) { return DataResult.error(error::getMessage); }
        }
        @Override public MapCodec<ArmorerRecipe> codec() { return codec; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ArmorerRecipe> streamCodec() { return streamCodec; }
    }
}
