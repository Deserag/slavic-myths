package org.slavicmyths.rpg;

import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.*;

/** Ordered, counted inputs: component/material/catalyst, plus a non-consumed chisel. */
public record RuneCraftRecipe(List<Counted> inputs, ItemStack result, boolean chisel) implements Recipe<RuneCraftRecipe.Input> {
    public record Counted(Ingredient ingredient,int count,Optional<RuneBase> base) {
        public Counted(Ingredient ingredient,int count){this(ingredient,count,Optional.empty());}
        public boolean test(ItemStack s){return ingredient.test(s)&&base.map(b->b.equals(s.get(org.slavicmyths.item.ItemState.RUNE_BASE.get()))).orElse(true);}
        public ItemStack[] displayItems(){return Arrays.stream(ingredient.getItems()).map(s->{ItemStack copy=s.copyWithCount(count);base.ifPresent(b->copy.set(org.slavicmyths.item.ItemState.RUNE_BASE.get(),b));return copy;}).toArray(ItemStack[]::new);}
        public static final Codec<Counted> CODEC=RecordCodecBuilder.create(i->i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Counted::ingredient),
            Codec.intRange(1,64).fieldOf("count").forGetter(Counted::count),
            RuneBase.CODEC.optionalFieldOf("base").forGetter(Counted::base)).apply(i,Counted::new));
    }
    public record Input(List<ItemStack> items) implements RecipeInput {
        public ItemStack getItem(int n){return items.get(n);}public int size(){return items.size();}
    }
    public RuneCraftRecipe {
        inputs=List.copyOf(inputs);result=result.copy();
        if(inputs.isEmpty()||inputs.size()>3||result.isEmpty()||result.getCount()>result.getMaxStackSize())throw new IllegalArgumentException("Invalid rune recipe");
    }
    public static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,"slavicmyths");
    public static final DeferredHolder<RecipeType<?>,RecipeType<RuneCraftRecipe>> TYPE=TYPES.register("rune_crafting",()->new RecipeType<>(){});
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"slavicmyths");
    public static final DeferredHolder<RecipeSerializer<?>,Serializer> SERIALIZER=SERIALIZERS.register("rune_crafting",Serializer::new);
    public boolean matches(Input in,Level level){
        if(in.size()!=4)return false;
        for(int n=0;n<3;n++)if(n<inputs.size()){
            Counted c=inputs.get(n);if(!c.test(in.getItem(n))||in.getItem(n).getCount()<c.count)return false;
        }else if(!in.getItem(n).isEmpty())return false;
        return !chisel||in.getItem(3).is(RuneFoundation.item("rune_chisel"))&&in.getItem(3).getDamageValue()<in.getItem(3).getMaxDamage();
    }
    public ItemStack assemble(Input in,HolderLookup.Provider lookup){return result.copy();}
    public ItemStack getResultItem(HolderLookup.Provider lookup){return result.copy();}
    public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}public RecipeType<?> getType(){return TYPE.get();}
    public boolean canCraftInDimensions(int w,int h){return w*h>=inputs.size();}public boolean isSpecial(){return true;}
    public NonNullList<Ingredient> getIngredients(){NonNullList<Ingredient> out=NonNullList.create();inputs.forEach(c->out.add(c.ingredient));return out;}
    public static final class Serializer implements RecipeSerializer<RuneCraftRecipe> {
        private static final MapCodec<RuneCraftRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            Counted.CODEC.sizeLimitedListOf(3).fieldOf("inputs").forGetter(RuneCraftRecipe::inputs),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(RuneCraftRecipe::result),
            Codec.BOOL.optionalFieldOf("chisel",false).forGetter(RuneCraftRecipe::chisel)).apply(i,RuneCraftRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,RuneCraftRecipe> STREAM=new StreamCodec<>() {
            public RuneCraftRecipe decode(RegistryFriendlyByteBuf b){int n=b.readVarInt();if(n<1||n>3)throw new IllegalArgumentException("Input count");List<Counted> a=new ArrayList<>();for(int j=0;j<n;j++){Ingredient ing=Ingredient.CONTENTS_STREAM_CODEC.decode(b);int count=b.readVarInt();if(count<1||count>64)throw new IllegalArgumentException("Count");Optional<RuneBase> base=b.readBoolean()?Optional.of(net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(RuneBase.CODEC).decode(b)):Optional.empty();a.add(new Counted(ing,count,base));}return new RuneCraftRecipe(a,ItemStack.STREAM_CODEC.decode(b),b.readBoolean());}
            public void encode(RegistryFriendlyByteBuf b,RuneCraftRecipe r){b.writeVarInt(r.inputs.size());for(Counted c:r.inputs){Ingredient.CONTENTS_STREAM_CODEC.encode(b,c.ingredient);b.writeVarInt(c.count);b.writeBoolean(c.base.isPresent());c.base.ifPresent(base->net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(RuneBase.CODEC).encode(b,base));}ItemStack.STREAM_CODEC.encode(b,r.result);b.writeBoolean(r.chisel);}
        };
        public MapCodec<RuneCraftRecipe> codec(){return CODEC;}public StreamCodec<RegistryFriendlyByteBuf,RuneCraftRecipe> streamCodec(){return STREAM;}
    }
}
