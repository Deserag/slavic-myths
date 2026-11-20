package org.slavicmyths.marine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Loader-backed proof that all 12 Ocean Rewards recipes actually craft their items. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineCraftGameTests {
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void allTwelveShapedRecipesCraftActualItems(GameTestHelper t) {
        for (String id : List.of(
                "storm_anchor", "abyss_staff", "storm_harpoon",
                "tide_ring", "abyss_necklace", "storm_bracelet",
                "tailwind_feather", "tsar_pike_belt", "zhelva_amulet",
                "marine_compass", "depths_lantern", "breathing_bowl")) {
            var key = ResourceLocation.fromNamespaceAndPath("slavicmyths", id);
            var holder = t.getLevel().getRecipeManager().byKey(key).orElseThrow();
            t.assertTrue(holder.value() instanceof ShapedRecipe,
                "Marine item is not a loaded shaped recipe: " + id);
            var recipe = (ShapedRecipe)holder.value();
            var ingredients = new ArrayList<ItemStack>();
            for (var ingredient : recipe.getIngredients()) {
                t.assertTrue(ingredient.isEmpty() || ingredient.getItems().length > 0,
                    "Unresolvable recipe ingredient: " + id);
                ingredients.add(ingredient.isEmpty()
                    ? ItemStack.EMPTY : ingredient.getItems()[0].copy());
            }
            var input = CraftingInput.of(recipe.getWidth(), recipe.getHeight(), ingredients);
            t.assertTrue(recipe.matches(input, t.getLevel()),
                "Valid marine crafting grid does not match: " + id);
            var result = recipe.assemble(input, t.getLevel().registryAccess());
            t.assertTrue(!result.isEmpty()
                    && result.is(BuiltInRegistries.ITEM.get(key))
                    && result.getCount() == 1,
                "Crafting produces wrong ocean reward: " + id);
        }
        t.succeed();
    }
}
