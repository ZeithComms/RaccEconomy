package org.zeith.comms.c17racceconomy.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;

@OnlyIn(Dist.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin
		extends DisplayEffectsScreen<PlayerContainer>
		implements IRecipeShownListener
{
	private static final ResourceLocation RACCPACK_WIDGETS = new ResourceLocation(RaccEconomy.MOD_ID, "textures/gui/shop.png");

	public InventoryScreenMixin(PlayerContainer container, PlayerInventory inv, ITextComponent name)
	{
		super(container, inv, name);
	}

	@Inject(
			method = "renderBg",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/inventory/InventoryScreen;renderEntityInInventory(IIIFFLnet/minecraft/entity/LivingEntity;)V"
			)
	)
	public void renderBalance(MatrixStack matrix, float partialTicks, int mouseX, int mouseY, CallbackInfo ci)
	{
		IRaccPackData data;
		if(this.minecraft != null && this.minecraft.player != null && (data = CapabilityRaccEconomy.get(this.minecraft.player)) != null)
		{
			ITextComponent balance = data.getBalanceText();

			this.minecraft.getTextureManager().bind(RACCPACK_WIDGETS);
			int x = this.leftPos;
			int y = this.topPos;

			int renderWidth = 14;
			int renderHeight = 14;

			int balWidth = font.width(balance);

			blit(matrix, x + this.imageWidth - renderWidth - balWidth - 8, y + 8 + this.imageHeight, 0, 133, renderWidth, renderHeight);
			font.draw(matrix, balance, x + this.imageWidth - balWidth - 4, y + 10.5F + this.imageHeight, 0xFFFFFF);
		}
	}
}