package org.zeith.comms.c17racceconomy.block.admin_shop;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.EconomyAPI;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.net.PacketSetShopPrice;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.comms.c17racceconomy.utils.slots.OwnerShopSlot;
import org.zeith.comms.c17racceconomy.utils.slots.ShopSlot;
import org.zeith.hammerlib.client.screen.ScreenWTFMojang;
import org.zeith.hammerlib.client.utils.FXUtils;
import org.zeith.hammerlib.net.Network;
import vazkii.quark.api.IQuarkButtonIgnored;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScreenAdminShop
		extends ScreenWTFMojang<ContainerAdminShop>
		implements IQuarkButtonIgnored
{
	public static final ResourceLocation SHOP_GUI = new ResourceLocation(RaccEconomy.MOD_ID, "textures/gui/shop.png");

	final Rectangle box = new Rectangle();

	public ScreenAdminShop(ContainerAdminShop container, PlayerInventory inv, ITextComponent label)
	{
		super(container, inv, label);
		imageWidth = 176;
		imageHeight = 133;
		this.titleLabelY -= 2;
		this.inventoryLabelY = this.imageHeight - 93;
		this.priceString = Long.toString(container.tile.price.get());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(menu.canModify)
		{
			box.setBounds(leftPos + 77, topPos + 22, 22, 15);
			if(box.contains(mouseX, mouseY))
			{
				if(clickMenuButton(2))
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			}

			if(priceBounds.contains(mouseX, mouseY))
			{
				typingPrice = true;
				priceString = Long.toString(menu.tile.price.get());
			} else if(typingPrice)
			{
				typingPrice = false;
				try
				{
					long l;
					Network.sendToServer(new PacketSetShopPrice(menu.tile.getBlockPos(), l = Long.parseLong(priceString)));
					menu.tile.price.set(l);
				} catch(NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}
			}
		} else
		{
			box.setBounds(leftPos + 115, topPos + 21, 18, 18);
			if(box.contains(mouseX, mouseY))
			{
				if(clickMenuButton(3))
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY)
	{
		if(this.minecraft.player.inventory.getCarried().isEmpty() && this.hoveredSlot != null && (!this.hoveredSlot.hasItem() && (this.hoveredSlot instanceof ShopSlot || this.hoveredSlot instanceof OwnerShopSlot)))
		{
			ItemStack is = this.hoveredSlot.getItem();
			if(!is.isEmpty()) this.renderTooltip(stack, is, mouseX, mouseY);
		} else super.renderTooltip(stack, mouseX, mouseY);

		List<ITextComponent> tooltip = new ArrayList<>();

		if(menu.canModify)
		{
			box.setBounds(leftPos + 77, topPos + 22, 22, 15);
			if(box.contains(mouseX, mouseY))
			{
				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.mode.toggle"));
			}
		} else
		{
			box.setBounds(leftPos + 115, topPos + 21, 18, 18);
			if(box.contains(mouseX, mouseY) && !menu.tmp.getItem(0).isEmpty())
			{
				ShopMode mode = menu.tile.getMode();

				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.ui.btn." + mode.getSerializedName(), EconomyAPI.getBalanceText(menu.tile.price.get())));

				mouseY -= 16;
			}
		}

		renderWrappedToolTip(stack, tooltip, mouseX, mouseY, font);
	}

	@Override
	protected void renderBackground(MatrixStack matrix, float partialTime, int mouseX, int mouseY)
	{
		FXUtils.bindTexture(SHOP_GUI);

		this.blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		ShopMode mode = menu.tile.getMode();

		if(menu.tile.canPerformTrade(minecraft.player))
		{
			this.blit(matrix, leftPos + 77, topPos + 22, 198, 0, 22, 16);
		}

		int coinX = leftPos + (mode == ShopMode.SELL ? 117 : 45);
		this.blit(matrix, coinX, topPos + 23, 0, 133, 14, 14);

		IFormattableTextComponent txt = EconomyAPI.getBalanceText(menu.tile.price.get());

		if(typingPrice)
		{
			try
			{
				txt = EconomyAPI.getBalanceText(Long.parseLong(priceString));
			} catch(NumberFormatException nfe)
			{
			}
		}

		if(typingPrice && System.currentTimeMillis() % 1000L > 500) txt = txt.append("_");
		int pwidth = this.font.width(txt);
		this.font.drawShadow(matrix, txt, (width - pwidth) / 2F, topPos + 13, 0xFFFFFF);
		priceBounds.setBounds((width - pwidth) / 2, topPos + 13, pwidth, this.font.lineHeight);

		Slot slot = menu.slots.get(0);

		FXUtils.bindTexture(SHOP_GUI);
		this.blit(matrix, leftPos + slot.x - 1, topPos + slot.y - 1, 176, 22, 18, 18);

		IRaccPackData data;
		if(this.minecraft != null && this.minecraft.player != null && (data = CapabilityRaccEconomy.get(this.minecraft.player)) != null)
		{
			ITextComponent balance = data.getBalanceText();
			int x = this.leftPos;
			int y = this.topPos;
			int renderWidth = 14;
			int renderHeight = 14;
			int balWidth = font.width(balance);
			blit(matrix, x + this.imageWidth - renderWidth - balWidth - 8, y + 8 + this.imageHeight, 0, 133, renderWidth, renderHeight);
			font.draw(matrix, balance, x + this.imageWidth - balWidth - 4, y + 10.5F + this.imageHeight, 0xFFFFFF);
		}
	}

	final Rectangle priceBounds = new Rectangle();
	String priceString = "";
	boolean typingPrice = false;

	@Override
	public boolean charTyped(char ch, int key)
	{
		if(typingPrice)
		{
			if(ch >= '0' && ch <= '9')
			{
				String b = priceString;
				priceString += ch;
				try
				{
					Long.parseLong(priceString);
				} catch(NumberFormatException e)
				{
					priceString = b;
				}
			}
			return true;
		}
		return super.charTyped(ch, key);
	}

	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_)
	{
		if(typingPrice)
		{
			if(p_231046_1_ == 259)
			{
				if(!priceString.isEmpty())
				{
					priceString = priceString.substring(0, priceString.length() - 1);
					if(priceString.isEmpty())
						priceString = "0";
				}
				return true;
			}
		}

		return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}
}