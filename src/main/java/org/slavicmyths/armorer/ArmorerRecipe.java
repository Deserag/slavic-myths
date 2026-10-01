package org.slavicmyths.armorer;

import com.google.gson.*;
import java.util.*;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.RegistryObject;

public final class ArmorerRecipe implements IRecipe<CraftingInventory> {
    public static final IRecipeType<ArmorerRecipe> TYPE=IRecipeType.register("slavicmyths:armorer");
    public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,"slavicmyths");
    public static final RegistryObject<Serializer> SHAPED=SERIALIZERS.register("armorer_shaped",()->new Serializer(false));
    public static final RegistryObject<Serializer> SHAPELESS=SERIALIZERS.register("armorer_shapeless",()->new Serializer(true));
    private final ResourceLocation id;public final int width,height;public final boolean shapeless;
    private final NonNullList<Ingredient> ingredients;private final ItemStack output;
    public ArmorerRecipe(ResourceLocation id,int width,int height,boolean shapeless,NonNullList<Ingredient> ingredients,ItemStack output){this.id=id;this.width=width;this.height=height;this.shapeless=shapeless;this.ingredients=ingredients;this.output=output;}
    public boolean matches(CraftingInventory inv,World world) {
        if(shapeless){List<ItemStack> stacks=new ArrayList<>();for(int i=0;i<inv.getContainerSize();i++)if(!inv.getItem(i).isEmpty())stacks.add(inv.getItem(i));return net.minecraftforge.common.util.RecipeMatcher.findMatches(stacks,ingredients)!=null;}
        for(int x=0;x<=inv.getWidth()-width;x++)for(int y=0;y<=inv.getHeight()-height;y++)if(match(inv,x,y,false)||match(inv,x,y,true))return true;return false;
    }
    private boolean match(CraftingInventory inv,int ox,int oy,boolean mirror) {
        for(int y=0;y<inv.getHeight();y++)for(int x=0;x<inv.getWidth();x++) {
            int a=x-ox,b=y-oy;Ingredient ingredient=Ingredient.EMPTY;
            if(a>=0&&b>=0&&a<width&&b<height)ingredient=ingredients.get((mirror?width-a-1:a)+b*width);
            if(!ingredient.test(inv.getItem(x+y*inv.getWidth())))return false;
        }return true;
    }
    public ItemStack assemble(CraftingInventory inv){return output.copy();}
    public ItemStack getResultItem(){return output;}
    public NonNullList<Ingredient> getIngredients(){return ingredients;}
    public ResourceLocation getId(){return id;}
    public IRecipeSerializer<?> getSerializer(){return shapeless?SHAPELESS.get():SHAPED.get();}
    public IRecipeType<?> getType(){return TYPE;}
    @Override public boolean isSpecial(){return true;}
    public boolean canCraftInDimensions(int w,int h){return shapeless?w*h>=ingredients.size():w>=width&&h>=height;}
    public static final class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ArmorerRecipe> {
        private final boolean shapeless;Serializer(boolean shapeless){this.shapeless=shapeless;}
        public ArmorerRecipe fromJson(ResourceLocation id,JsonObject json) {
            NonNullList<Ingredient> list=NonNullList.create();int width=4,height=4;
            if(shapeless) {
                for(JsonElement e:JSONUtils.getAsJsonArray(json,"ingredients")){Ingredient i=Ingredient.fromJson(e);if(i==Ingredient.EMPTY)throw new JsonSyntaxException("Empty ingredient");list.add(i);}
                if(list.isEmpty()||list.size()>16)throw new JsonSyntaxException("Armorer recipe needs 1..16 ingredients");
            } else {
                JsonArray pattern=JSONUtils.getAsJsonArray(json,"pattern");height=pattern.size();
                if(height<1||height>4)throw new JsonSyntaxException("Pattern height must be 1..4");width=pattern.get(0).getAsString().length();
                if(width<1||width>4)throw new JsonSyntaxException("Pattern width must be 1..4");
                Map<Character,Ingredient> key=new HashMap<>();key.put(' ',Ingredient.EMPTY);
                for(Map.Entry<String,JsonElement> e:JSONUtils.getAsJsonObject(json,"key").entrySet()){if(e.getKey().length()!=1||e.getKey().equals(" "))throw new JsonSyntaxException("Invalid pattern key");key.put(e.getKey().charAt(0),Ingredient.fromJson(e.getValue()));}
                Set<Character> used=new HashSet<>();
                for(JsonElement row:pattern){String s=row.getAsString();if(s.length()!=width)throw new JsonSyntaxException("Unequal rows");for(char c:s.toCharArray()){if(!key.containsKey(c))throw new JsonSyntaxException("Undefined key");list.add(key.get(c));if(c!=' ')used.add(c);}}
                if(used.isEmpty()||used.size()!=key.size()-1)throw new JsonSyntaxException("Empty recipe or unused key");
            }
            ItemStack out=ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json,"result"));
            if(out.isEmpty()||out.getCount()<1||out.getCount()>out.getMaxStackSize())throw new JsonSyntaxException("Invalid output");
            return new ArmorerRecipe(id,width,height,shapeless,list,out);
        }
        public ArmorerRecipe fromNetwork(ResourceLocation id,PacketBuffer b){int w=b.readVarInt(),h=b.readVarInt(),n=b.readVarInt();if(w<1||w>4||h<1||h>4||n<1||n>16)throw new IllegalArgumentException("Invalid armorer dimensions");NonNullList<Ingredient> list=NonNullList.create();for(int i=0;i<n;i++)list.add(Ingredient.fromNetwork(b));return new ArmorerRecipe(id,w,h,shapeless,list,b.readItem());}
        public void toNetwork(PacketBuffer b,ArmorerRecipe r){b.writeVarInt(r.width);b.writeVarInt(r.height);b.writeVarInt(r.ingredients.size());for(Ingredient i:r.ingredients)i.toNetwork(b);b.writeItem(r.output);}
    }
}
