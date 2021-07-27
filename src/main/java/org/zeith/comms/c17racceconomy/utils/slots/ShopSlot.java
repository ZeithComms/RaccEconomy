package org.zeith.comms.c17racceconomy.utils.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.zeith.comms.c17racceconomy.api.IShopTile;

public class ShopSlot
		extends VisibleSlot
{
	public final IShopTile shop;
	public final PlayerEntity player;

	public ShopSlot(PlayerEntity player, IShopTile shop, IInventory inventory, int slot, int x, int y, ResourceLocation group, boolean visibleInitially)
	{
		super(inventory, slot, x, y, group, visibleInitially);
		this.player = player;
		this.shop = shop;
	}

	@Override
	public boolean hasItem()
	{
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean mayPickup(PlayerEntity player)
	{
		return false;
	}

	@Override
	public ItemStack onTake(PlayerEntity player, ItemStack stack)
	{
		return super.onTake(player, stack);
	}
}