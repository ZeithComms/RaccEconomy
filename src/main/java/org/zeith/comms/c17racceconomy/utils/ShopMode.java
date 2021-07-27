package org.zeith.comms.c17racceconomy.utils;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum ShopMode
		implements IStringSerializable
{
	BUY,
	SELL;

	public static final EnumProperty<ShopMode> MODE = EnumProperty.create("mode", ShopMode.class);

	@Override
	public String getSerializedName()
	{
		return name().toLowerCase(Locale.ROOT);
	}
}