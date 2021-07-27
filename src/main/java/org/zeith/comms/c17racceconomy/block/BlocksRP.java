package org.zeith.comms.c17racceconomy.block;

import org.zeith.comms.c17racceconomy.block.admin_shop.BlockAdminShop;
import org.zeith.comms.c17racceconomy.block.shop.BlockShop;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public class BlocksRP
{
	@RegistryName("shop")
	public static final BlockShop SHOP = new BlockShop();

	@RegistryName("admin_shop")
	public static final BlockAdminShop ADMIN_SHOP = new BlockAdminShop();
}