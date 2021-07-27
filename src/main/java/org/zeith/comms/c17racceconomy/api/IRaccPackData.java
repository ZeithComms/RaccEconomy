package org.zeith.comms.c17racceconomy.api;

import net.minecraft.util.text.ITextComponent;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;

public interface IRaccPackData
		extends IAutoNBTSerializable
{
	default ITextComponent getBalanceText()
	{
		return EconomyAPI.getBalanceText(getBalance());
	}

	long getBalance();

	long setBalance(long newBal);

	long addBalance(long amount);

	boolean tryTakeBalance(long amount);

	long takeBalance(long amount);

	void sync();
}