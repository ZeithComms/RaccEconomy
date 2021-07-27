package org.zeith.comms.c17racceconomy.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zeith.comms.c17racceconomy.block.shop.TileShop;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.java.Cast;

@MainThreaded
public class PacketSetShopNotifications
		implements IPacket
{
	private BlockPos pos;
	private boolean notifications;

	public PacketSetShopNotifications()
	{
	}

	public PacketSetShopNotifications(BlockPos pos, boolean notifications)
	{
		this.pos = pos;
		this.notifications = notifications;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeBoolean(notifications);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		notifications = buf.readBoolean();
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
				Cast.optionally(lvl.getBlockEntity(pos), TileShop.class)
						.filter(shop -> shop.isOwner(sender))
						.ifPresent(shop ->
						{
							shop.notifications = notifications;
							shop.sync();
						});
			}
		}
	}
}