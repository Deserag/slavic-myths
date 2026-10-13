package org.slavicmyths.storage;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.armorer.ArmorerRecipe;
public record DryingRecipe(Ingredient ingredient,ItemStack result,int dryingTime) implements Recipe<SingleRecipeInput>{
 public static final DeferredHolder<RecipeType<?>,RecipeType<DryingRecipe>> TYPE=ArmorerRecipe.TYPES.register("drying",()->new RecipeType<>(){public String toString(){return "slavicmyths:drying";}});
 public static final DeferredHolder<RecipeSerializer<?>,Serializer> SERIALIZER=ArmorerRecipe.SERIALIZERS.register("drying",Serializer::new);
 public DryingRecipe{if(dryingTime<1||result.isEmpty()||result.getCount()!=1)throw new IllegalArgumentException("Invalid drying recipe");result=result.copy();}
 public boolean matches(SingleRecipeInput in,Level l){return ingredient.test(in.item());}public ItemStack assemble(SingleRecipeInput in,HolderLookup.Provider p){return result.copy();}public ItemStack getResultItem(HolderLookup.Provider p){return result.copy();}public boolean canCraftInDimensions(int w,int h){return w*h>=1;}public boolean isSpecial(){return true;}public RecipeType<?>getType(){return TYPE.get();}public RecipeSerializer<?>getSerializer(){return SERIALIZER.get();}
 public static class Serializer implements RecipeSerializer<DryingRecipe>{
  private final MapCodec<DryingRecipe> codec=RecordCodecBuilder.mapCodec(i->i.group(Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DryingRecipe::ingredient),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DryingRecipe::result),Codec.intRange(1,120000).fieldOf("drying_time").forGetter(DryingRecipe::dryingTime)).apply(i,DryingRecipe::new));
  private final StreamCodec<RegistryFriendlyByteBuf,DryingRecipe> stream=new StreamCodec<>(){public DryingRecipe decode(RegistryFriendlyByteBuf b){return new DryingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(b),ItemStack.STREAM_CODEC.decode(b),b.readVarInt());}public void encode(RegistryFriendlyByteBuf b,DryingRecipe r){Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.ingredient);ItemStack.STREAM_CODEC.encode(b,r.result);b.writeVarInt(r.dryingTime);}};
  public MapCodec<DryingRecipe>codec(){return codec;}public StreamCodec<RegistryFriendlyByteBuf,DryingRecipe>streamCodec(){return stream;}
 }
}
