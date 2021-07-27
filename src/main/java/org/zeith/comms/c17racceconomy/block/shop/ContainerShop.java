package org.zeith.comms.c17racceconomy.block.shop;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.comms.c17racceconomy.utils.slots.OwnerShopSlot;
import org.zeith.comms.c17racceconomy.utils.slots.ShopSlot;
import org.zeith.comms.c17racceconomy.utils.slots.VisibleSlot;
import org.zeith.hammerlib.api.forge.ContainerAPI;
import org.zeith.hammerlib.api.inv.IScreenContainer;
import org.zeith.hammerlib.api.inv.SimpleInventory;

import java.util.stream.Collectors;

public class ContainerShop
		extends Container
		implements IScreenContainer
{
	public static final ResourceLocation INVENTORY_GROUP = new ResourceLocation("inventory");
	public static final ResourceLocation SHOP_GROUP = new ResourceLocation(RaccEconomy.MOD_ID, "shop");
	public static final ResourceLocation STORAGE_GROUP = new ResourceLocation(RaccEconomy.MOD_ID, "storage");

	public final TileShop tile;
	public final boolean canModify;

	protected int activeTab;

	public final SimpleInventory tmp = new SimpleInventory(1);

	public ContainerShop(PlayerEntity player, int windowId, TileShop shop, boolean canModify)
	{
		super(ContainerAPI.TILE_CONTAINER, windowId);
		this.tile = shop;
		this.canModify = canModify;

		int x;

		ShopMode mode = tile.getMode();

		tmp.setItem(0, tile.goods.get().copy());

		if(shop.isOwner(player))
			addSlot(new OwnerShopSlot(this::broadcastChanges, player, shop, tmp, 0, mode == ShopMode.BUY ? 116 : 44, 22, SHOP_GROUP, true));
		else
			addSlot(new ShopSlot(player, shop, tmp, 0, mode == ShopMode.BUY ? 116 : 44, 22, SHOP_GROUP, true));

		{
			IInventory i = canModify ? shop.storage : tmp;

			for(x = 0; x < 6; ++x)
				for(int y = 0; y < 9; ++y)
					this.addSlot(new VisibleSlot(i, canModify ? y + x * 9 : 0, (8 + y * 18), (18 + x * 18), STORAGE_GROUP, false));
		}

		for(x = 0; x < 3; ++x)
			for(int y = 0; y < 9; ++y)
				this.addSlot(new VisibleSlot(player.inventory, y + x * 9 + 9, 8 + y * 18, 51 + x * 18, INVENTORY_GROUP, true));

		for(x = 0; x < 9; ++x)
			this.addSlot(new VisibleSlot(player.inventory, x, 8 + x * 18, 109, INVENTORY_GROUP, true));
	}

	@Override
	public boolean clickMenuButton(PlayerEntity player, int button)
	{
		if(button == 0)
		{
			slots.stream()
					.filter(VisibleSlot.filterVisibleSlotsInGroup(SHOP_GROUP))
					.forEach(VisibleSlot.setVisibilityForAll(true));

			slots.stream()
					.filter(VisibleSlot.filterVisibleSlotsInGroup(STORAGE_GROUP))
					.forEach(VisibleSlot.setVisibilityForAll(false));

			VisibleSlot.setRelativeCoords(8, 51, slots.stream().filter(VisibleSlot.filterVisibleSlotsInGroup(INVENTORY_GROUP)).collect(Collectors.toList()));
			VisibleSlot.setRelativeCoords(tile.getMode() == ShopMode.BUY ? 116 : 44, 22, slots.stream().filter(VisibleSlot.filterVisibleSlotsInGroup(SHOP_GROUP)).collect(Collectors.toList()));

			activeTab = 0;

			return true;
		}

		if(button == 1 && tile.canModify(player))
		{
			slots.stream()
					.filter(VisibleSlot.filterVisibleSlotsInGroup(SHOP_GROUP))
					.forEach(VisibleSlot.setVisibilityForAll(false));

			slots.stream()
					.filter(VisibleSlot.filterVisibleSlotsInGroup(STORAGE_GROUP))
					.forEach(VisibleSlot.setVisibilityForAll(true));

			VisibleSlot.setRelativeCoords(8, 140, slots.stream().filter(VisibleSlot.filterVisibleSlotsInGroup(INVENTORY_GROUP)).collect(Collectors.toList()));

			activeTab = 1;

			return true;
		}

		if(button == 2 && tile.canModify(player))
		{
			ShopMode mode = tile.getMode();
			tile.setMode(mode = (mode == ShopMode.SELL ? ShopMode.BUY : ShopMode.SELL));

			VisibleSlot.setRelativeCoords(mode == ShopMode.BUY ? 116 : 44, 22, slots.stream().filter(VisibleSlot.filterVisibleSlotsInGroup(SHOP_GROUP)).collect(Collectors.toList()));

			return true;
		}

		if(button == 3)
		{
			if(tile.isOnClient())
				return !tmp.getItem(0).isEmpty();
			if(tile.isOnServer() && player instanceof ServerPlayerEntity)
			{
				tile.performTrade((ServerPlayerEntity) player);
				return !tmp.getItem(0).isEmpty();
			}
		}

		return false;
	}

	@Override
	public boolean stillValid(PlayerEntity player)
	{
		return !tile.isRemoved() && tile.getBlockPos().distSqr(player.position(), false) < 64D;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int clickSlotIdx)
	{
		ItemStack stack = ItemStack.EMPTY;

		if(canModify && activeTab == 1)
		{
			Slot slot = this.slots.get(clickSlotIdx);
			if(slot != null && slot.hasItem())
			{
				ItemStack itemstack1 = slot.getItem();
				stack = itemstack1.copy();
				if(clickSlotIdx < 6 * 9)
				{
					if(!this.moveItemStackTo(itemstack1, 6 * 9 + 1, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else if(!this.moveItemStackTo(itemstack1, 1, 6 * 9 + 1, false))
					return ItemStack.EMPTY;

				if(itemstack1.isEmpty())
					slot.set(ItemStack.EMPTY);
				else
					slot.setChanged();
			}
		}

		return stack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Screen openScreen(PlayerInventory inv, ITextComponent label)
	{
		return new ScreenShop(this, inv, label);
	}
}