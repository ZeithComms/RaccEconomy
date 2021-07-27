package org.zeith.comms.c17racceconomy.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zeith.comms.c17racceconomy.api.IShopTile;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.java.Cast;

@MainThreaded
public class PacketSetShopGoods
		implements IPacket
{
	private BlockPos pos;
	private ItemStack goods;

	public PacketSetShopGoods()
	{
	}

	public PacketSetShopGoods(BlockPos pos, ItemStack goods)
	{
		this.pos = pos;
		this.goods = goods;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeItemStack(goods, false);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		goods = buf.readItem();
	}

	@Override
	public void serverExecute(PacketContext ctx)
	{
		ServerPlayerEntity sender = ctx.getSender();
		if(sender != null)
		{
			World lvl = sender.level;

			if(lvl.isLoaded(pos))
			{
				Cast.optionally(lvl.getBlockEntity(pos), IShopTile.class)
						.ifPresent(shop -> shop.setGoods(sender, goods));
			}
		}
	}
}