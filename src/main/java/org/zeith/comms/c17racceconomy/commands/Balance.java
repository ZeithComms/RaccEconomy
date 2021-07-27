package org.zeith.comms.c17racceconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;

public class Balance
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
				Commands.literal("balance")
						.executes(src ->
						{
							ServerPlayerEntity player = src.getSource().getPlayerOrException();
							IRaccPackData data = CapabilityRaccEconomy.get(player);
							src.getSource().sendSuccess(new TranslationTextComponent("command.balance.self", data.getBalanceText()), true);
							return 1;
						})
						.then(Commands.argument("player", EntityArgument.player())
								.requires(s -> s.hasPermission(2))
								.executes(src ->
								{
									ServerPlayerEntity player = EntityArgument.getPlayer(src, "player");
									IRaccPackData data = CapabilityRaccEconomy.get(player);
									src.getSource().sendSuccess(new TranslationTextComponent("command.balance", player.getName(), data.getBalanceText()), true);
									return 1;
								})
						)
						/*.then(Commands.literal("/sync")
								.executes(src ->
								{
									ServerPlayerEntity player = src.getSource().getPlayerOrException();
									IRaccPackData data = CapabilityRaccPack.get(player);
									data.sync();
									return 1;
								})
						)*/
		);
	}
}