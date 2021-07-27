package org.zeith.comms.c17racceconomy.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.net.PacketSyncRaccPackData;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.net.Network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RaccEconomyDataImpl
		implements IRaccPackData, ICapabilityProvider
{
	@NBTSerializable
	private long balance;

	@Override
	public long getBalance()
	{
		return balance;
	}

	@Override
	public long setBalance(long newBal)
	{
		newBal = Math.max(newBal, 0L);
		long obalance = getBalance();
		if(newBal != obalance)
		{
			balance = newBal;
			sync();
		}
		return obalance;
	}

	@Override
	public long addBalance(long amount)
	{
		long bal = getBalance();
		if(amount > 0) setBalance(bal + amount);
		return bal;
	}

	@Override
	public boolean tryTakeBalance(long amount)
	{
		long balance = getBalance();
		if(balance >= amount)
		{
			setBalance(balance - amount);
			return true;
		}
		return false;
	}

	@Override
	public long takeBalance(long amount)
	{
		long balance = getBalance();
		amount = Math.min(amount, balance);
		if(amount > 0L) setBalance(balance - amount);
		return amount;
	}

	@Override
	public void sync()
	{
		// non-syncable
	}

	public static RaccEconomyDataImpl newInstance(PlayerEntity owner)
	{
		if(owner instanceof ServerPlayerEntity)
			return new ServerImpl((ServerPlayerEntity) owner);
		return new RaccEconomyDataImpl();
	}

	final LazyOptional<IRaccPackData> selfContainer = LazyOptional.of(() -> this);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		return CapabilityRaccEconomy.RP_DATA.orEmpty(cap, selfContainer);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
	{
		return CapabilityRaccEconomy.RP_DATA.orEmpty(cap, selfContainer);
	}

	private static class ServerImpl
			extends RaccEconomyDataImpl
	{
		ServerPlayerEntity owner;

		public ServerImpl(ServerPlayerEntity owner)
		{
			this.owner = owner;
		}

		@Override
		public void sync()
		{
			Network.sendTo(new PacketSyncRaccPackData(this), owner);
		}
	}
}