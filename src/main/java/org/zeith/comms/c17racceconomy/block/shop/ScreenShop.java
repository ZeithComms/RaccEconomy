package org.zeith.comms.c17racceconomy.block.shop;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.EconomyAPI;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.net.PacketSetShopNotifications;
import org.zeith.comms.c17racceconomy.net.PacketSetShopPrice;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.comms.c17racceconomy.utils.slots.OwnerShopSlot;
import org.zeith.comms.c17racceconomy.utils.slots.ShopSlot;
import org.zeith.hammerlib.client.screen.ScreenWTFMojang;
import org.zeith.hammerlib.client.utils.FXUtils;
import org.zeith.hammerlib.net.Network;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScreenShop
		extends ScreenWTFMojang<ContainerShop>
{
	public static final ResourceLocation STORAGE_GUI = new ResourceLocation("textures/gui/container/generic_54.png");
	public static final ResourceLocation SHOP_GUI = new ResourceLocation(RaccEconomy.MOD_ID, "textures/gui/shop.png");

	final Rectangle box = new Rectangle();

	protected int prevTab0x = 20, prevTab1x = 16;
	protected int tab0x = 20, tab1x = 16;

	public ScreenShop(ContainerShop container, PlayerInventory inv, ITextComponent label)
	{
		super(container, inv, label);
		imageWidth = 176;
		imageHeight = 133;
		this.titleLabelY -= 2;
		this.inventoryLabelY = this.imageHeight - 93;
		this.priceString = Long.toString(container.tile.price.get());
	}

	ImageButton notificationButton;

	@Override
	protected void init()
	{
		super.init();

		if(notificationButton != null)
			buttons.remove(notificationButton);

		if(menu.activeTab == 0 && menu.canModify)
		{
			addButton(notificationButton = new ImageButton(leftPos + 149, topPos + 20, 20, 20, menu.tile.notifications ? 0 : 20, 147, 20, SHOP_GUI, 256, 256, btn ->
			{

				Network.sendToServer(new PacketSetShopNotifications(menu.tile.getBlockPos(), !menu.tile.notifications));
			}
			));
		}
	}

	@Override
	public void tick()
	{
		this.prevTab0x = tab0x;
		this.prevTab1x = tab1x;

		if(this.menu.activeTab == 0) tab0x = Math.min(tab0x + 1, 20);
		else tab0x = Math.max(tab0x - 1, 16);

		if(this.menu.activeTab == 1) tab1x = Math.min(tab1x + 1, 20);
		else tab1x = Math.max(tab1x - 1, 16);

		if(notificationButton != null)
		{
			notificationButton.xTexStart = menu.tile.notifications ? 0 : 20;

			notificationButton.active = notificationButton.visible = menu.activeTab == 0;
		}

		super.tick();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(menu.canModify)
		{
			box.setBounds(leftPos + 77, topPos + 22, 22, 15);
			if(menu.activeTab == 0 && box.contains(mouseX, mouseY))
			{
				if(clickMenuButton(2))
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			}

			int topPos = this.height / 2 - 44;

			box.setBounds(leftPos - tab0x, topPos + 16, tab0x, 22);
			if(box.contains(mouseX, mouseY))
			{
				if(clickMenuButton(0))
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			}

			box.setBounds(leftPos - tab1x, topPos + 48, tab1x, 22);
			if(box.contains(mouseX, mouseY))
			{
				if(clickMenuButton(1))
					minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			}

			if(menu.activeTab == 0 && priceBounds.contains(mouseX, mouseY))
			{
				typingPrice = true;
				priceString = Long.toString(menu.tile.price.get());
			} else if(menu.activeTab == 0 && typingPrice)
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
			if(box.contains(mouseX, mouseY) && menu.activeTab == 0)
			{
				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.mode.toggle"));
			}

			int topPos = this.height / 2 - 44;

			box.setBounds(leftPos - tab0x, topPos + 16, tab0x, 22);
			if(box.contains(mouseX, mouseY))
			{
				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.ui.main"));
			}

			box.setBounds(leftPos - tab1x, topPos + 48, tab1x, 22);
			if(box.contains(mouseX, mouseY))
			{
				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.ui.storage"));
			}

			if(notificationButton != null && notificationButton.isMouseOver(mouseX, mouseY))
			{
				tooltip.add(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.notification2o" + (menu.tile.notifications ? "ff" : "n")));
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
		partialTime = minecraft.getFrameTime();

		if(this.menu.activeTab == 1)
		{
			minecraft.getTextureManager().bind(STORAGE_GUI);
			this.blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		}

		FXUtils.bindTexture(SHOP_GUI);

		if(menu.canModify)
		{
			int topPos = this.height / 2 - 44;

			matrix.pushPose();
			matrix.translate(-MathHelper.lerp(partialTime, prevTab0x, tab0x), 16, 0);
			this.blit(matrix, leftPos, topPos, 176, 0, 19, 22);
			this.blit(matrix, leftPos + 4, topPos + 4, 0, 133, 14, 14);
			matrix.popPose();

			matrix.pushPose();
			matrix.translate(-MathHelper.lerp(partialTime, prevTab1x, tab1x), 48, 0);
			this.blit(matrix, leftPos, topPos, 176, 0, 19, 22);
			this.blit(matrix, leftPos + 4, topPos + 4, 14, 133, 14, 14);
			matrix.popPose();
		}

		if(this.menu.activeTab == 0)
		{
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
		}

		if(this.menu.canModify)
		{
			int topPos = this.height / 2 - 44;
			this.blit(matrix, leftPos - 1, topPos + 16, 194, 0, 4, 22);
			this.blit(matrix, leftPos - 1, topPos + 48, 194, 0, 4, 22);
		}

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

	@Override
	protected boolean clickMenuButton(int button)
	{
		if(this.menu.clickMenuButton(this.minecraft.player, button))
		{
			switch(this.menu.activeTab)
			{
				case 1:
					this.imageHeight = 222;
					this.inventoryLabelY = this.imageHeight - 93;
					break;
				case 0:
				default:
					this.imageHeight = 133;
					this.inventoryLabelY = this.imageHeight - 93;
					break;
			}
			init();
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
			return true;
		}
		return false;
	}
}