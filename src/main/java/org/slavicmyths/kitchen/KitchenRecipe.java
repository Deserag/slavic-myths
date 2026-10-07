package org.slavicmyths.kitchen;
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
public record KitchenRecipe(String mode,List<Part> parts,Ingredient tool,int time,ItemStack result,int servings,List<ItemStack> remainders) implements Recipe<KitchenInput> {
 public record Part(Ingredient ingredient,int count){public Part{if(count<1||count>64||ingredient.isEmpty())throw new IllegalArgumentException("Invalid counted ingredient");}static final Codec<Part> CODEC=RecordCodecBuilder.create(i->i.group(Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Part::ingredient),Codec.intRange(1,64).fieldOf("count").forGetter(Part::count)).apply(i,Part::new));}
 public static final DeferredHolder<RecipeType<?>,RecipeType<KitchenRecipe>> TYPE=ArmorerRecipe.TYPES.register("kitchen",()->new RecipeType<>(){public String toString(){return "slavicmyths:kitchen";}});
 public static final DeferredHolder<RecipeSerializer<?>,Serializer> SERIALIZER=ArmorerRecipe.SERIALIZERS.register("kitchen",Serializer::new);
 public KitchenRecipe {
  if(!List.of("ROLLING","POT").contains(mode)||parts.isEmpty()||parts.size()>4||time<1||result.isEmpty()||result.getCount()>result.getMaxStackSize()||servings<0||servings>64||mode.equals("POT")&&servings==0||mode.equals("ROLLING")&&servings!=0||remainders.size()>2)throw new IllegalArgumentException("Invalid kitchen recipe");
  parts=List.copyOf(parts);result=result.copy();remainders=remainders.stream().map(ItemStack::copy).toList();
 }
 public boolean pot(){return mode.equals("POT");}
 /** Allocate counted demands across all four slots, including overlapping tags. Extra unrelated stacks reject. */
 public int[] allocation(KitchenInput input){
  int[] left=new int[input.size()],used=new int[input.size()];List<Ingredient> demands=new ArrayList<>();
  for(Part p:parts)for(int n=0;n<p.count;n++)demands.add(p.ingredient);
  for(int j=0;j<input.size();j++){ItemStack s=input.getItem(j);left[j]=s.getCount();if(!s.isEmpty()&&parts.stream().noneMatch(p->p.ingredient.test(s)))return null;}
  if(java.util.Arrays.stream(left).sum()<demands.size())return null;
  for(Part p:parts){int available=0;for(int j=0;j<input.size();j++)if(p.ingredient.test(input.getItem(j)))available+=left[j];if(available<p.count)return null;}
  demands.sort(Comparator.comparingInt(i->{int n=0;for(ItemStack s:input.stacks())if(i.test(s))n++;return n;}));
  return allocate(demands,0,input,left,used)?used:null;
 }
 private static boolean allocate(List<Ingredient>d,int n,KitchenInput in,int[]left,int[]used){if(n==d.size())return true;for(int j=0;j<left.length;j++)if(left[j]>0&&d.get(n).test(in.getItem(j))){left[j]--;used[j]++;if(allocate(d,n+1,in,left,used))return true;left[j]++;used[j]--;}return false;}
 public boolean matches(KitchenInput in,Level w){return allocation(in)!=null;}
 public ItemStack assemble(KitchenInput in,HolderLookup.Provider p){return result.copy();}
 public ItemStack getResultItem(HolderLookup.Provider p){return result.copy();}
 public NonNullList<Ingredient> getIngredients(){var l=NonNullList.<Ingredient>create();for(Part p:parts)l.add(p.ingredient);return l;}
 public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}public RecipeType<?> getType(){return TYPE.get();}public boolean isSpecial(){return true;}public boolean canCraftInDimensions(int w,int h){return w*h>=parts.size();}
 public static final class Serializer implements RecipeSerializer<KitchenRecipe>{
  private final MapCodec<KitchenRecipe> codec=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.fieldOf("mode").forGetter(KitchenRecipe::mode),Part.CODEC.listOf().fieldOf("ingredients").forGetter(KitchenRecipe::parts),Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(KitchenRecipe::tool),Codec.INT.fieldOf("time").forGetter(KitchenRecipe::time),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(KitchenRecipe::result),Codec.INT.optionalFieldOf("servings",0).forGetter(KitchenRecipe::servings),ItemStack.STRICT_CODEC.listOf().optionalFieldOf("remainders",List.of()).forGetter(KitchenRecipe::remainders)).apply(i,KitchenRecipe::new));
  private final StreamCodec<RegistryFriendlyByteBuf,KitchenRecipe> stream=new StreamCodec<>(){
   public KitchenRecipe decode(RegistryFriendlyByteBuf b){String m=b.readUtf();int n=b.readVarInt();if(n<1||n>4)throw new IllegalArgumentException("Kitchen ingredients");List<Part>p=new ArrayList<>();for(int j=0;j<n;j++)p.add(new Part(Ingredient.CONTENTS_STREAM_CODEC.decode(b),b.readVarInt()));Ingredient t=Ingredient.CONTENTS_STREAM_CODEC.decode(b);int time=b.readVarInt();ItemStack r=ItemStack.STREAM_CODEC.decode(b);int s=b.readVarInt(),nr=b.readVarInt();if(nr<0||nr>2)throw new IllegalArgumentException("Kitchen remainders");List<ItemStack>rem=new ArrayList<>();for(int j=0;j<nr;j++)rem.add(ItemStack.STREAM_CODEC.decode(b));return new KitchenRecipe(m,p,t,time,r,s,rem);}
   public void encode(RegistryFriendlyByteBuf b,KitchenRecipe r){b.writeUtf(r.mode);b.writeVarInt(r.parts.size());for(Part p:r.parts){Ingredient.CONTENTS_STREAM_CODEC.encode(b,p.ingredient);b.writeVarInt(p.count);}Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.tool);b.writeVarInt(r.time);ItemStack.STREAM_CODEC.encode(b,r.result);b.writeVarInt(r.servings);b.writeVarInt(r.remainders.size());for(ItemStack s:r.remainders)ItemStack.STREAM_CODEC.encode(b,s);}
  };
  public MapCodec<KitchenRecipe> codec(){return codec;}public StreamCodec<RegistryFriendlyByteBuf,KitchenRecipe> streamCodec(){return stream;}
 }
}
