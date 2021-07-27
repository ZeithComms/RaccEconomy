package org.zeith.comms.c17racceconomy.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.zeith.hammerlib.api.tiles.ISyncableTile;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.packets.SyncTileEntityPacket;
import org.zeith.hammerlib.util.java.Cast;

public class PacketRequestSync
		implements IPacket
{
	BlockPos pos;

	public PacketRequestSync()
	{
	}

	public PacketRequestSync(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
	}

	@Override
	public void serverExecute(PacketContext ctx)
	{
		ServerPlayerEntity sender = ctx.getSender();
		if(sender != null && sender.level.isLoaded(pos))
		{
			Cast.optionally(sender.level.getBlockEntity(pos), ISyncableTile.class)
					.ifPresent(st ->
							ctx.withReply(new SyncTileEntityPacket((TileEntity) st)));
		}
	}
}