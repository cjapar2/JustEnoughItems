package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeCategoryTab extends RecipeGuiTab {
	private final IRecipeGuiLogic logic;
	private final IRecipeCategory<?> category;
	private final IIngredientManager ingredientManager;

	public RecipeCategoryTab(IRecipeGuiLogic logic, IRecipeCategory<?> category, Textures textures, int x, int y, IIngredientManager ingredientManager) {
		super(textures, x, y);
		this.logic = logic;
		this.category = category;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		if (input.is(keyBindings.getLeftClick())) {
			if (!input.isSimulate()) {
				logic.setRecipeCategory(category);
				SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
				soundHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public void draw(boolean selected, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.draw(selected, guiGraphics, mouseX, mouseY);

		int iconX = x + 4;
		int iconY = y + 4;

		IDrawable icon = category.getIcon();
		//noinspection ConstantConditions
		if (icon != null) {
			iconX += (16 - icon.getWidth()) / 2;
			iconY += (16 - icon.getHeight()) / 2;
			icon.draw(guiGraphics, iconX, iconY);
		} else {
			Optional<ITypedIngredient<?>> firstCatalyst = logic.getRecipeCatalysts(category)
				.findFirst();
			if (firstCatalyst.isPresent()) {
				ITypedIngredient<?> ingredient = firstCatalyst.get();
				renderIngredient(guiGraphics, iconX, iconY, ingredient, ingredientManager);
			} else {
				String text = category.getTitle().getString().substring(0, 2);
				Minecraft minecraft = Minecraft.getInstance();
				Font fontRenderer = minecraft.font;
				int textCenterX = x + (TAB_WIDTH / 2);
				int textCenterY = y + (TAB_HEIGHT / 2) - 3;
				int color = isMouseOver(mouseX, mouseY) ? 0xFFFFA0 : 0xE0E0E0;
				int stringCenter = fontRenderer.width(text) / 2;
				guiGraphics.drawString(fontRenderer, text, textCenterX - stringCenter, textCenterY, color);
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
		}
	}

	private static <T> void renderIngredient(GuiGraphics guiGraphics, int iconX, int iconY, ITypedIngredient<T> ingredient, IIngredientManager ingredientManager) {
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient.getType());
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(iconX, iconY, 0);
			RenderSystem.enableDepthTest();
			ingredientRenderer.render(guiGraphics, ingredient.getIngredient());
			RenderSystem.disableDepthTest();
		}
		poseStack.popPose();
	}

	@Override
	public boolean isSelected(IRecipeCategory<?> selectedCategory) {
		ResourceLocation categoryUid = category.getRecipeType().getUid();
		ResourceLocation selectedCategoryUid = selectedCategory.getRecipeType().getUid();
		return categoryUid.equals(selectedCategoryUid);
	}

	@Override
	public List<Component> getTooltip(IModIdHelper modIdHelper) {
		List<Component> tooltip = new ArrayList<>();
		Component title = category.getTitle();
		//noinspection ConstantConditions
		if (title != null) {
			tooltip.add(title);
		}

		ResourceLocation uid = category.getRecipeType().getUid();
		String modId = uid.getNamespace();
		if (modIdHelper.isDisplayingModNameEnabled()) {
			String modName = modIdHelper.getFormattedModNameForModId(modId);
			tooltip.add(Component.literal(modName));
		}
		return tooltip;
	}
}
