package org.zeith.comms.c17racceconomy.utils.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.zeith.comms.c17racceconomy.api.IShopTile;

public class OwnerShopSlot
		extends VisibleSlot
{
	public final IShopTile shop;
	public final PlayerEntity player;

	protected Runnable markDirty;

	public OwnerShopSlot(Runnable markDirty, PlayerEntity player, IShopTile shop, IInventory inventory, int slot, int x, int y, ResourceLocation group, boolean visibleInitially)
	{
		super(inventory, slot, x, y, group, visibleInitially);
		this.markDirty = markDirty;
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
		if(player instanceof ServerPlayerEntity && this.x >= 0 && this.y >= 0)
		{
			shop.setGoods((ServerPlayerEntity) player, stack.copy());
			set(stack.copy());
			markDirty.run();
		}
		return false;
	}

	@Override
	public boolean mayPickup(PlayerEntity player)
	{
		if(player instanceof ServerPlayerEntity && !getItem().isEmpty() && this.x >= 0 && this.y >= 0)
		{
			shop.setGoods((ServerPlayerEntity) player, ItemStack.EMPTY);
			set(ItemStack.EMPTY);
			markDirty.run();
		}
		return false;
	}
}