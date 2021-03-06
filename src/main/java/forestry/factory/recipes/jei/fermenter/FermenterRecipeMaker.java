package forestry.factory.recipes.jei.fermenter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IVariableFermentable;
import forestry.api.recipes.RecipeManagers;
import forestry.core.recipes.jei.JEIUtils;

public class FermenterRecipeMaker {

	private FermenterRecipeMaker() {
	}
	
	public static List<FermenterRecipeWrapper> getFermenterRecipes() {
		List<FermenterRecipeWrapper> recipes = new ArrayList<>();
		for (IFermenterRecipe recipe : RecipeManagers.fermenterManager.recipes()) {
			if (recipe.getResource() != null && recipe.getResource().getItem() instanceof IVariableFermentable) {
				for (ItemStack stack : JEIUtils.getItemVariations(recipe.getResource())) {
					recipes.add(new FermenterRecipeWrapper(recipe, stack));
				}
			} else {
				recipes.add(new FermenterRecipeWrapper(recipe, recipe.getResource()));
			}
		}
		return recipes;
	}
	
}
