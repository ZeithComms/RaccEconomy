package org.zeith.comms.c17racceconomy.api;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.zeith.comms.c17racceconomy.utils.NFU;

import java.util.Optional;
import java.util.UUID;

public class EconomyAPI
{
	public static long getBalance(MinecraftServer server, UUID player)
	{
		ServerPlayerEntity spe = server.getPlayerList().getPlayer(player);
		if(spe != null) return CapabilityRaccEconomy.get(spe).getBalance();
		return Optional.ofNullable(OfflineRaccEconomyData.read(server, player)).map(IRaccPackData::getBalance).orElse(0L);
	}

	public static boolean setBalance(MinecraftServer server, UUID player, long balance)
	{
		ServerPlayerEntity spe = server.getPlayerList().getPlayer(player);
		if(spe != null)
		{
			CapabilityRaccEconomy.get(spe).setBalance(balance);
			return true;
		} else
		{
			OfflineRaccEconomyData.Reader reader = OfflineRaccEconomyData.read(server, player);
			if(reader != null)
			{
				reader.setBalance(balance);
				return true;
			}
		}
		return false;
	}

	public static IFormattableTextComponent getBalanceText(long balance)
	{
		String str = NFU.balance2String(balance);
		StringTextComponent txt = new StringTextComponent(str);
		txt.setStyle(txt.getStyle().withColor(Color.fromRgb(0xE9B115)).withBold(true));
		return txt;
	}
}