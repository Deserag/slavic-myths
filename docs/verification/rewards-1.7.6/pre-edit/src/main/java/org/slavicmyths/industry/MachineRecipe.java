package org.slavicmyths.industry;
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

public record MachineRecipe(boolean refining,Ingredient input,Ingredient flux,ItemStack result,int duration,float experience) implements Recipe<MachineRecipe.Input> {
    public record Input(ItemStack metal,ItemStack flux) implements RecipeInput {public ItemStack getItem(int i){return i==0?metal:flux;}public int size(){return 2;}}
    public static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,"slavicmyths");
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"slavicmyths");
    public static final DeferredHolder<RecipeType<?>,RecipeType<MachineRecipe>> BLOOMERY=TYPES.register("chud_bloomery",()->new RecipeType<>(){}),HEARTH=TYPES.register("refining_hearth",()->new RecipeType<>(){});
    public static final DeferredHolder<RecipeSerializer<?>,Serializer> BLOOMERY_SERIALIZER=SERIALIZERS.register("chud_bloomery",()->new Serializer(false)),HEARTH_SERIALIZER=SERIALIZERS.register("refining_hearth",()->new Serializer(true));
    public MachineRecipe {if(duration<1||duration>72000||experience<0||result.isEmpty()||result.getCount()!=1)throw new IllegalArgumentException("Invalid machine recipe");}
    public boolean matches(Input i,Level level){return input.test(i.metal())&&(!refining||flux.test(i.flux()));}
    public ItemStack assemble(Input i,HolderLookup.Provider p){return result.copy();}
    public ItemStack getResultItem(HolderLookup.Provider p){return result.copy();}
    public boolean canCraftInDimensions(int w,int h){return true;}
    public RecipeSerializer<?> getSerializer(){return refining?HEARTH_SERIALIZER.get():BLOOMERY_SERIALIZER.get();}
    public RecipeType<?> getType(){return refining?HEARTH.get():BLOOMERY.get();}
    public NonNullList<Ingredient> getIngredients(){var list=NonNullList.<Ingredient>create();list.add(input);if(refining)list.add(flux);return list;}
    public boolean isSpecial(){return true;}
    public static final class Serializer implements RecipeSerializer<MachineRecipe> {
        private final MapCodec<MachineRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,MachineRecipe> stream;
        Serializer(boolean refining){
            codec=RecordCodecBuilder.mapCodec(i->i.group(Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MachineRecipe::input),Ingredient.CODEC.optionalFieldOf("flux",Ingredient.EMPTY).forGetter(MachineRecipe::flux),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MachineRecipe::result),Codec.intRange(1,72000).fieldOf("duration").forGetter(MachineRecipe::duration),Codec.FLOAT.optionalFieldOf("experience",0F).forGetter(MachineRecipe::experience)).apply(i,(input,flux,result,time,xp)->new MachineRecipe(refining,input,flux,result,time,xp)));
            stream=new StreamCodec<>(){public MachineRecipe decode(RegistryFriendlyByteBuf b){return new MachineRecipe(refining,Ingredient.CONTENTS_STREAM_CODEC.decode(b),Ingredient.CONTENTS_STREAM_CODEC.decode(b),ItemStack.STREAM_CODEC.decode(b),b.readVarInt(),b.readFloat());}public void encode(RegistryFriendlyByteBuf b,MachineRecipe r){Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.input);Ingredient.CONTENTS_STREAM_CODEC.encode(b,r.flux);ItemStack.STREAM_CODEC.encode(b,r.result);b.writeVarInt(r.duration);b.writeFloat(r.experience);}};
        }
        public MapCodec<MachineRecipe> codec(){return codec;}
        public StreamCodec<RegistryFriendlyByteBuf,MachineRecipe> streamCodec(){return stream;}
    }
}
