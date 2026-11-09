package org.slavicmyths.sisters;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One ordinary crafting-grid recipe: original relic + matching shard + ingot. */
public final class SisterArtifactRecipe extends CustomRecipe {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"slavicmyths");
    public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<SisterArtifactRecipe>> SERIALIZER=
        SERIALIZERS.register("sisters_artifact_awaken",()->new SimpleCraftingRecipeSerializer<>(SisterArtifactRecipe::new));
    public SisterArtifactRecipe(CraftingBookCategory category){super(category);}
    private static String power(CraftingInput input){
        if(input.ingredientCount()!=3)return null;
        ItemStack artifact=ItemStack.EMPTY,shard=ItemStack.EMPTY,ingot=ItemStack.EMPTY;
        for(int i=0;i<input.size();i++){
            ItemStack stack=input.getItem(i);
            if(stack.isEmpty())continue;
            if(SisterArtifacts.isArtifact(stack)){if(!artifact.isEmpty())return null;artifact=stack;continue;}
            boolean matchShard=false,matchIngot=false;
            for(int j=0;j<6;j++){
                if(stack.is(shard(j)))matchShard=true;
                if(stack.is(ingot(j)))matchIngot=true;
            }
            if(matchShard){if(!shard.isEmpty())return null;shard=stack;}
            else if(matchIngot){if(!ingot.isEmpty())return null;ingot=stack;}
            else return null;
        }
        if(artifact.isEmpty()||shard.isEmpty()||ingot.isEmpty())return null;
        for(int j=0;j<6;j++)if(shard.is(shard(j))&&ingot.is(ingot(j))){
            String id=SisterArtifacts.IDS.get(j);
            return SisterArtifacts.powers(artifact).size()<3&&!SisterArtifacts.powers(artifact).contains(id)?id:null;
        }
        return null;
    }
    private static net.minecraft.world.item.Item shard(int id){
        return switch(id){
            case 0->SistersContent.TRYASEYA_SHARD.get();case 1->SistersContent.GNETEYA_SHARD.get();
            case 2->SistersContent.LEDEYA_SHARD.get();case 3->SistersContent.AGNEYA_SHARD.get();
            case 4->SistersContent.NEVEYA_SHARD.get();default->SistersContent.LOMEYA_SHARD.get();
        };
    }
    private static net.minecraft.world.item.Item ingot(int id){
        return switch(id){
            case 0->SistersContent.TRYASEYA_INGOT.get();case 1->SistersContent.GNETEYA_INGOT.get();
            case 2->SistersContent.LEDEYA_INGOT.get();case 3->SistersContent.AGNEYA_INGOT.get();
            case 4->SistersContent.NEVEYA_INGOT.get();default->SistersContent.LOMEYA_INGOT.get();
        };
    }
    @Override public boolean matches(CraftingInput input,Level level){return power(input)!=null;}
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries){
        String id=power(input);if(id==null)return ItemStack.EMPTY;
        for(int i=0;i<input.size();i++)if(SisterArtifacts.isArtifact(input.getItem(i)))
            return SisterArtifacts.addPower(input.getItem(i),id);
        return ItemStack.EMPTY;
    }
    @Override public boolean canCraftInDimensions(int width,int height){return width*height>=3;}
    @Override public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
}
