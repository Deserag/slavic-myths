package org.slavicmyths.brewing;
import org.slavicmyths.kitchen.KitchenInput;
import org.slavicmyths.kitchen.KitchenRecipe;
import org.slavicmyths.kitchen.KitchenRecipe.Part;
import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.armorer.ArmorerRecipe;
public record VatRecipe(String mode,List<Part> parts,Ingredient catalyst,int time,ItemStack result,boolean retainCatalyst,List<ItemStack> remainders) implements Recipe<KitchenInput> {
 public static final DeferredHolder<RecipeType<?>,RecipeType<VatRecipe>> TYPE=ArmorerRecipe.TYPES.register("vat",()->new RecipeType<>(){public String toString(){return "slavicmyths:vat";}});
 public static final DeferredHolder<RecipeSerializer<?>,Serializer> SERIALIZER=ArmorerRecipe.SERIALIZERS.register("vat",Serializer::new);
 public VatRecipe {
  if(!List.of("MALTING","INFUSION","WORT","MUST").contains(mode)||parts.isEmpty()||parts.size()>4||time<1||result.isEmpty()||result.getCount()>result.getMaxStackSize()||remainders.size()>2)throw new IllegalArgumentException("Invalid vat recipe");
  parts=List.copyOf(parts);result=result.copy();remainders=remainders.stream().map(ItemStack::copy).toList();
 }
 public int[] allocation(KitchenInput input){return KitchenRecipe.allocation(parts,input);}
 public boolean matches(KitchenInput in,Level w){return allocation(in)!=null;}
 public ItemStack assemble(KitchenInput in,HolderLookup.Provider p){return result.copy();}
 public ItemStack getResultItem(HolderLookup.Provider p){return result.copy();}
 public NonNullList<Ingredient> getIngredients(){var l=NonNullList.<Ingredient>create();for(Part p:parts)l.add(p.ingredient());return l;}
 public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}public RecipeType<?> getType(){return TYPE.get();}public boolean isSpecial(){return true;}public boolean canCraftInDimensions(int w,int h){return w*h>=parts.size();}
 public static final class Serializer implements RecipeSerializer<VatRecipe>{
  private final MapCodec<VatRecipe> codec=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.fieldOf("mode").forGetter(VatRecipe::mode),Part.CODEC.listOf().fieldOf("ingredients").forGetter(VatRecipe::parts),Ingredient.CODEC.optionalFieldOf("catalyst",Ingredient.EMPTY).forGetter(VatRecipe::catalyst),Codec.INT.fieldOf("time").forGetter(VatRecipe::time),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(VatRecipe::result),Codec.BOOL.optionalFieldOf("retain_catalyst",false).forGetter(VatRecipe::retainCatalyst),ItemStack.STRICT_CODEC.listOf().optionalFieldOf("remainders",List.of()).forGetter(VatRecipe::remainders)).apply(i,VatRecipe::new));
  private final StreamCodec<RegistryFriendlyByteBuf,VatRecipe> stream=new StreamCodec<>(){
   public VatRecipe decode(RegistryFriendlyByteBuf b){String m=b.readUtf();int n=b.readVarInt();if(n<1||n>4)throw new IllegalArgumentException("Kitchen ingredients");List<Part>p=new ArrayList<>();for(int j=0;j<n;j++)p.add(new Part(Ingredient.CONTENTS_STREAM_CODEC.decode(b),b.readVarInt()));Ingredient t=Ingredient.CONTENTS_STREAM_CODEC.decode(b);int time=b.readVarInt();ItemStack r=ItemStack.STREAM_CODEC.decode(b);boolean s=b.readBoolean();int nr=b.readVarInt();if(nr<0||nr>2)throw new IllegalArgumentException("Kitchen remainders");List<ItemStack>rem=new ArrayList<>();for(int j=0;j<nr;j++)rem.add(ItemStack.STREAM_CODEC.decode(b));return new VatRecipe(m,p,t,time,r,s,rem);}
   public void encode(RegistryFriendlyByteBuf b,VatRecipe r){b.writeUtf(r.mode);b.writeVarInt(r.parts.size());for(Part p:r.parts){Ingredient.CONTENTS_STREAM_CODEC.encode(b,p.ingredient());b.writeVarInt(p.count());}Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.catalyst);b.writeVarInt(r.time);ItemStack.STREAM_CODEC.encode(b,r.result);b.writeBoolean(r.retainCatalyst);b.writeVarInt(r.remainders.size());for(ItemStack s:r.remainders)ItemStack.STREAM_CODEC.encode(b,s);}
  };
  public MapCodec<VatRecipe> codec(){return codec;}public StreamCodec<RegistryFriendlyByteBuf,VatRecipe> streamCodec(){return stream;}
 }
}
