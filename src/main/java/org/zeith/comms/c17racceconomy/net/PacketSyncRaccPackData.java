package org.zeith.comms.c17racceconomy.net;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;

@MainThreaded
public class PacketSyncRaccPackData
		implements IPacket
{
	CompoundNBT nbt;

	public PacketSyncRaccPackData()
	{
	}

	public PacketSyncRaccPackData(IRaccPackData data)
	{
		this.nbt = data.serializeNBT();
	}

	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeNbt(nbt);
	}

	@Override
	public void read(PacketBuffer buf)
	{
		nbt = buf.readNbt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		PlayerEntity clPl = Minecraft.getInstance().player;
		if(clPl != null)
		{
			IRaccPackData data = CapabilityRaccEconomy.get(clPl);
			if(data != null) data.deserializeNBT(nbt);
		}
	}
}