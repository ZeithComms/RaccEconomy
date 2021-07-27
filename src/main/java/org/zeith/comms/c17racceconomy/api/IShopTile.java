package org.zeith.comms.c17racceconomy.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import org.zeith.comms.c17racceconomy.utils.ShopMode;

public interface IShopTile
{
	void setGoods(ServerPlayerEntity player, ItemStack stack);

	void setPrice(ServerPlayerEntity player, long price);

	boolean canModify(PlayerEntity player);

	ShopMode getMode();

	boolean canPerformTrade(PlayerEntity player);

	boolean performTrade(ServerPlayerEntity player);
}